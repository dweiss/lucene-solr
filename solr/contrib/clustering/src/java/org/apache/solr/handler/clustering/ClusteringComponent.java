/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.solr.handler.clustering;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHits;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.component.HighlightComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.carrot2.clustering.Cluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A plugin for dynamic, unsupervised grouping of search results based on
 * the content of their text fields.
 * <p>
 * The default implementation is based on clustering algorithms from the
 * <a href="https://project.carrot2.org">Carrot<sup>2</sup> project</a>.
 *
 * @lucene.experimental
 */
public class ClusteringComponent extends SearchComponent implements SolrCoreAware {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Component registration name for {@link org.apache.solr.handler.component.SearchHandler}.
   */
  public static final String COMPONENT_NAME = "clustering";

  /**
   * Request parameter selecting a named {@link ClusteringEngine} to use for performing
   * clustering.
   */
  public static final String REQ_PARAM_ENGINE = COMPONENT_NAME + ".engine";

  /**
   * Internal parameter for shard requests when we only collect clustering documents.
   */
  private static final String REQ_PARAM_COLLECT_INPUTS = COMPONENT_NAME + ".collectInputs";

  /**
   * Engine name in component configuration parameters.
   */
  public static final String CONF_PARAM_ENGINE_NAME = "name";

  /**
   * Default engine name.
   */
  public static final String DEFAULT_ENGINE_NAME = "default";

  private static final String RESPONSE_SECTION_INPUT_DOCUMENTS = "clustering-inputs";
  public static final String RESPONSE_SECTION_CLUSTERS = "clusters";

  /**
   * Declaration-order list of search clustering engines.
   */
  private final LinkedHashMap<String, ClusteringEngine> clusteringEngines = new LinkedHashMap<>();

  /**
   * Initialization parameters temporarily saved here, the component
   * is initialized in {@link #inform(SolrCore)} because we need to know
   * the core's {@link SolrResourceLoader}.
   *
   * @see #init(NamedList)
   */
  private NamedList<Object> initParams;

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void init(NamedList args) {
    super.init(args);
    this.initParams = args;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void inform(SolrCore core) {
    SchemaField uniqueField = core.getLatestSchema().getUniqueKeyField();
    if (uniqueField == null) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
          ClusteringComponent.class.getSimpleName() + " requires the declaration of uniqueKeyField in the schema.");
    }
    String docIdField = uniqueField.getName();

    if (initParams != null) {
      for (Map.Entry<String, Object> entry : initParams) {
        if ("engine".equals(entry.getKey())) {
          NamedList<Object> engineInitParams = (NamedList<Object>) entry.getValue();
          Boolean optional = engineInitParams.getBooleanArg("optional");
          optional = (optional == null ? Boolean.FALSE : optional);

          EngineConfiguration defaultParams = new EngineConfiguration();
          defaultParams.setDocIdField(docIdField);
          defaultParams.extractFrom(engineInitParams.toSolrParams());

          // Set up engine name.
          final String engineName = StringUtils.defaultIfBlank(
              (String) engineInitParams.get(CONF_PARAM_ENGINE_NAME), "");

          // Instantiate the clustering engine and split to appropriate map.
          ClusteringEngine engine = new ClusteringEngineImpl(engineName, defaultParams);
          engine.init(core);

          if (!engine.isAvailable()) {
            if (optional) {
              if (log.isInfoEnabled()) {
                log.info("Optional clustering engine reports it is not available: {}", engine.getName());
              }
            } else {
              throw new SolrException(ErrorCode.SERVER_ERROR,
                  "A required clustering engine failed to initialize, check the logs: " + engine.getName());
            }
          }

          if (clusteringEngines.put(engine.getName(), engine) != null) {
            throw new SolrException(ErrorCode.SERVER_ERROR,
                String.format(Locale.ROOT,
                    "Duplicate clustering engine named '%s'.", engine.getName()));
          }
        }
      }

      setupDefaultEngine(clusteringEngines);
    }
  }

  @Override
  public void prepare(ResponseBuilder rb) {
    // Do nothing.
  }

  private void logEntry(String msg, Object... args) {
  }

  /**
   * Entry point for clustering in local server mode (non-distributed).
   *
   * @param rb The {@link ResponseBuilder}.
   * @throws IOException Propagated if an I/O exception occurs.
   */
  @Override
  public void process(ResponseBuilder rb) throws IOException {
    boolean enabled = rb.req.getParams().getBool(COMPONENT_NAME, false);
    logEntry("process(enabled={}, isShard={})", enabled, rb.req.getParams().getBool(ShardParams.IS_SHARD));
    if (!enabled) {
      return;
    }

    String name = getClusteringEngineName(rb);
    ClusteringEngine engine = getEngine(name);
    EngineConfiguration requestConfiguration = engine.defaultConfiguration().clone().extractFrom(rb.req.getParams());

    List<InputDocument> inputs = getDocuments(rb, requestConfiguration);

    if (rb.req.getParams().getBool(ShardParams.IS_SHARD, false) &&
        rb.req.getParams().getBool(REQ_PARAM_COLLECT_INPUTS, false)) {
      rb.rsp.add(RESPONSE_SECTION_INPUT_DOCUMENTS, documentsToNamedList(inputs));
    } else {
      doCluster(rb, inputs, engine, requestConfiguration);
    }
  }

  private void doCluster(ResponseBuilder rb, List<InputDocument> inputs, ClusteringEngine engine, EngineConfiguration requestConfiguration) {
    List<Cluster<InputDocument>> clusters = engine.cluster(requestConfiguration, rb.getQuery(), inputs);
    rb.rsp.add(RESPONSE_SECTION_CLUSTERS, clustersToNamedList(inputs, clusters, requestConfiguration));
  }

  @Override
  public int distributedProcess(ResponseBuilder rb) throws IOException {
    logEntry("distributedProcess()");
    return super.distributedProcess(rb);
  }

  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
    boolean enabled = rb.req.getParams().getBool(COMPONENT_NAME, false);
    logEntry("handleResponses(purpose={})", sreq.purpose);
    if (!enabled) {
      return;
    }

    super.handleResponses(rb, sreq);
  }

  @Override
  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) {
    boolean enabled = rb.req.getParams().getBool(COMPONENT_NAME, false);
    logEntry("modifyRequest(enabled={}, purpose={})", enabled, sreq.purpose);
    if (!enabled) {
      return;
    }

    assert sreq.params.getBool(COMPONENT_NAME, false) :
        "Shard request should propagate clustering component enabled state?";

    // Piggyback collecting inputs for clustering on top of get fields request.
    if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) != 0) {
      sreq.params.set(REQ_PARAM_COLLECT_INPUTS, true);
    }
  }

  @Override
  public void finishStage(ResponseBuilder rb) {
    boolean enabled = rb.req.getParams().getBool(COMPONENT_NAME, false);
    logEntry("finishStage(enabled={}, stage={})", enabled, rb.stage);
    if (!enabled) {
      return;
    }

    if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
      List<InputDocument> inputs = new ArrayList<>();
      rb.finished.stream()
          .filter(shardRequest -> (shardRequest.purpose & ShardRequest.PURPOSE_GET_FIELDS) != 0)
          .flatMap(shardRequest -> shardRequest.responses.stream())
          .filter(rsp -> rsp.getException() == null)
          .forEach(rsp -> {
            NamedList<Object> response = rsp.getSolrResponse().getResponse();
            @SuppressWarnings("unchecked")
            List<NamedList<Object>> partialInputs = (List<NamedList<Object>>) response.get(RESPONSE_SECTION_INPUT_DOCUMENTS);
            if (partialInputs != null) {
              inputs.addAll(documentsFromNamedList(partialInputs));
            }
          });

      String name = getClusteringEngineName(rb);
      ClusteringEngine engine = getEngine(name);
      EngineConfiguration requestConfiguration = engine.defaultConfiguration().clone().extractFrom(rb.req.getParams());

      doCluster(rb, inputs, engine, requestConfiguration);
    }
  }

  /**
   * Prepares input documents for clustering.
   */
  private List<InputDocument> getDocuments(ResponseBuilder responseBuilder,
                                           EngineConfiguration requestParameters) throws IOException {
    SolrQueryRequest solrRequest = responseBuilder.req;
    DocList solrDocList = responseBuilder.getResults().docList;
    Query query = responseBuilder.getQuery();
    SolrIndexSearcher indexSearcher = responseBuilder.req.getSearcher();
    SolrParams solrParams = solrRequest.getParams();
    SolrCore core = solrRequest.getCore();
    String[] fieldsToCluster = requestParameters.fields().toArray(String[]::new);

    Function<Map<String, String>, String> assignLanguage;
    String languageField = requestParameters.languageField();
    if (languageField != null) {
      assignLanguage = (doc) -> doc.getOrDefault(languageField, requestParameters.language());
    } else {
      assignLanguage = (doc) -> requestParameters.language();
    }

    boolean produceSummary = solrParams.getBool(EngineConfiguration.PRODUCE_SUMMARY, false);

    SolrQueryRequest req = null;
    SolrHighlighter highlighter = null;
    if (produceSummary) {
      highlighter = ((HighlightComponent) core.getSearchComponents().get(HighlightComponent.COMPONENT_NAME)).getHighlighter();
      if (highlighter != null) {
        Map<String, Object> args = new HashMap<>();
        args.put(HighlightParams.FIELDS, fieldsToCluster);
        args.put(HighlightParams.HIGHLIGHT, "true");
        // We don't want any highlight marks.
        args.put(HighlightParams.SIMPLE_PRE, "");
        args.put(HighlightParams.SIMPLE_POST, "");
        args.put(HighlightParams.FRAGSIZE, solrParams.getInt(EngineConfiguration.SUMMARY_FRAGSIZE, solrParams.getInt(HighlightParams.FRAGSIZE, 100)));
        args.put(HighlightParams.SNIPPETS, solrParams.getInt(EngineConfiguration.SUMMARY_SNIPPETS, solrParams.getInt(HighlightParams.SNIPPETS, 1)));
        req = new LocalSolrQueryRequest(core, query.toString(), "", 0, 1, args) {
          @Override
          public SolrIndexSearcher getSearcher() {
            return indexSearcher;
          }
        };
      } else {
        log.warn("No highlighter configured, cannot produce summary");
        produceSummary = false;
      }
    }

    IndexSchema schema = indexSearcher.getSchema();
    Map<String, Function<IndexableField, String>> fieldsToLoad = new LinkedHashMap<>();
    for (String fld : requestParameters.getFieldsToLoad()) {
      FieldType type = schema.getField(fld).getType();
      fieldsToLoad.put(fld, (fieldValue) -> type.toObject(fieldValue).toString());
    }

    List<InputDocument> result = new ArrayList<>(solrDocList.size());
    DocIterator dit = solrDocList.iterator();
    while (dit.hasNext()) {
      int internalId = dit.nextDoc();

      Map<String, String> docFieldValues = new LinkedHashMap<>();
      for (IndexableField indexableField : indexSearcher.doc(internalId, fieldsToLoad.keySet())) {
        String fieldName = indexableField.name();
        Function<IndexableField, String> toString = fieldsToLoad.get(fieldName);
        if (toString != null) {
          String value = toString.apply(indexableField);
          docFieldValues.compute(fieldName, (k, v) -> {
            if (v == null) {
              return value;
            } else {
              return v + " . " + value;
            }
          });
        }
      }

      InputDocument inputDocument = new InputDocument(
          docFieldValues.get(requestParameters.docIdField()),
          assignLanguage.apply(docFieldValues));
      result.add(inputDocument);

      Function<String, String> snippetProvider = (field) -> null;
      if (produceSummary) {
        DocList docAsList = new DocSlice(0, 1,
            new int[]{internalId},
            new float[]{1.0f},
            1,
            1.0f,
            TotalHits.Relation.EQUAL_TO);

        NamedList<Object> highlights = highlighter.doHighlighting(docAsList, query, req, fieldsToCluster);
        if (highlights != null && highlights.size() == 1) {
          @SuppressWarnings("unchecked")
          NamedList<String[]> tmp = (NamedList<String[]>) highlights.getVal(0);
          snippetProvider = (field) -> {
            String[] values = tmp.get(field);
            if (values == null) {
              return null;
            } else {
              return String.join(" . ", Arrays.asList(values));
            }
          };
        }
      }

      Function<String, String> fullValueProvider = docFieldValues::get;

      for (String field : fieldsToCluster) {
        String values = snippetProvider.apply(field);
        if (values == null) {
          values = fullValueProvider.apply(field);
        }
        if (values != null) {
          inputDocument.addClusteredField(field, values);
        }
      }
    }

    return result;
  }

  private ClusteringEngine getEngine(String name) {
    ClusteringEngine engine = clusteringEngines.get(name);
    if (engine == null) {
      throw new SolrException(ErrorCode.SERVER_ERROR,
          "Clustering engine not known: " + name);
    }
    if (!engine.isAvailable()) {
      throw new SolrException(ErrorCode.SERVER_ERROR,
          "Clustering engine is not available: " + name);
    }
    return engine;
  }

  private String getClusteringEngineName(ResponseBuilder rb) {
    return rb.req.getParams().get(REQ_PARAM_ENGINE, DEFAULT_ENGINE_NAME);
  }

  /**
   * @return A map of initialized clustering engines.
   */
  public Map<String, ClusteringEngine> getClusteringEngines() {
    return clusteringEngines;
  }

  @Override
  public String getDescription() {
    return "Search results clustering component";
  }

  /**
   * Setup the default clustering engine.
   */
  private static <T extends ClusteringEngine> void setupDefaultEngine(LinkedHashMap<String, T> map) {
    // If there's already a default algorithm, leave it as is.
    String engineName = DEFAULT_ENGINE_NAME;
    T defaultEngine = map.get(engineName);

    if (defaultEngine == null ||
        !defaultEngine.isAvailable()) {
      // If there's no default algorithm, and there are any algorithms available, 
      // the first definition becomes the default algorithm.
      for (Map.Entry<String, T> e : map.entrySet()) {
        if (e.getValue().isAvailable()) {
          engineName = e.getKey();
          defaultEngine = e.getValue();
          map.put(DEFAULT_ENGINE_NAME, defaultEngine);
          break;
        }
      }
    }

    if (defaultEngine != null) {
      if (log.isInfoEnabled()) {
        log.info("Default clustering engine: {} [{}]", engineName, defaultEngine.getClass().getSimpleName());
      }
    } else {
      log.warn("No default clustering engine.");
    }
  }

  private static List<InputDocument> documentsFromNamedList(List<NamedList<Object>> docList) {
    return docList.stream()
        .map(docProps -> {
          InputDocument doc = new InputDocument(
              docProps.get("id"),
              (String) docProps.get("language"));

          docProps.forEach((fieldName, value) -> {
            doc.addClusteredField(fieldName, (String) value);
          });
          doc.visitFields(docProps::add);
          return doc;
        })
        .collect(Collectors.toList());
  }

  private static List<NamedList<Object>> documentsToNamedList(List<InputDocument> documents) {
    return documents.stream()
        .map(doc -> {
          NamedList<Object> docProps = new SimpleOrderedMap<>();
          docProps.add("id", doc.getId());
          docProps.add("language", doc.language());
          doc.visitFields(docProps::add);
          return docProps;
        })
        .collect(Collectors.toList());
  }

  private static List<NamedList<Object>> clustersToNamedList(List<InputDocument> documents,
                                                             List<Cluster<InputDocument>> clusters,
                                                             EngineConfiguration params) {
    List<NamedList<Object>> result = new ArrayList<>();
    clustersToNamedListRecursive(clusters, result, params);

    if (params.includeOtherTopics()) {
      LinkedHashSet<InputDocument> clustered = new LinkedHashSet<>();
      clusters.forEach(cluster -> collectUniqueDocuments(cluster, clustered));
      List<InputDocument> unclustered = documents.stream()
          .filter(doc -> !clustered.contains(doc))
          .collect(Collectors.toList());

      if (!unclustered.isEmpty()) {
        NamedList<Object> cluster = new SimpleOrderedMap<>();
        result.add(cluster);
        cluster.add("other-topics", true);
        cluster.add("labels", Collections.singletonList("Other topics"));
        cluster.add("score", 0);
        cluster.add("docs", unclustered.stream().map(InputDocument::getId)
            .collect(Collectors.toList()));
      }
    }

    return result;
  }

  private static void clustersToNamedListRecursive(
      List<Cluster<InputDocument>> outputClusters,
      List<NamedList<Object>> parent, EngineConfiguration params) {
    for (Cluster<InputDocument> cluster : outputClusters) {
      NamedList<Object> converted = new SimpleOrderedMap<>();
      parent.add(converted);

      // Add labels
      List<String> labels = cluster.getLabels();
      if (labels.size() > params.maxLabels()) {
        labels = labels.subList(0, params.maxLabels());
      }
      converted.add("labels", labels);

      // Add cluster score
      final Double score = cluster.getScore();
      if (score != null) {
        converted.add("score", score);
      }

      List<InputDocument> docs;
      if (params.includeSubclusters()) {
        docs = cluster.getDocuments();
      } else {
        docs = new ArrayList<>(collectUniqueDocuments(cluster, new LinkedHashSet<>()));
      }

      converted.add("docs", docs.stream().map(InputDocument::getId)
          .collect(Collectors.toList()));

      if (params.includeSubclusters() && !cluster.getClusters().isEmpty()) {
        List<NamedList<Object>> subclusters = new ArrayList<>();
        converted.add("clusters", subclusters);
        clustersToNamedListRecursive(cluster.getClusters(), subclusters, params);
      }
    }
  }

  private static LinkedHashSet<InputDocument> collectUniqueDocuments(Cluster<InputDocument> cluster, LinkedHashSet<InputDocument> unique) {
    unique.addAll(cluster.getDocuments());
    for (Cluster<InputDocument> sub : cluster.getClusters()) {
      collectUniqueDocuments(sub, unique);
    }
    return unique;
  }
}
