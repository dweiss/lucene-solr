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

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHits;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.component.HighlightComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin for dynamic, unsupervised grouping of search results based on
 * the content of their text fields.
 *
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
   * Engine name in component configuration parameters.
   */
  public static final String CONF_PARAM_ENGINE_NAME = "name";

  /**
   * Default engine name.
   */
  public static final String DEFAULT_ENGINE_NAME = "default";

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

  /**
   * Convert a DocList to a SolrDocumentList
   * <p>
   * The optional param "ids" is populated with the lucene document id
   * for each SolrDocument.
   *
   * @param docs     The {@link org.apache.solr.search.DocList} to convert
   * @param searcher The {@link org.apache.solr.search.SolrIndexSearcher} to use to load the docs from the Lucene index
   * @param fields   The names of the Fields to load
   * @param ids      A map to store the ids of the docs
   * @return The new {@link SolrDocumentList} containing all the loaded docs
   * @throws IOException if there was a problem loading the docs
   * @since solr 1.4
   */
  public static SolrDocumentList docListToSolrDocumentList(
      DocList docs,
      SolrIndexSearcher searcher,
      Set<String> fields,
      Map<SolrDocument, Integer> ids) throws IOException {
    IndexSchema schema = searcher.getSchema();

    SolrDocumentList list = new SolrDocumentList();
    list.setNumFound(docs.matches());
    list.setMaxScore(docs.maxScore());
    list.setStart(docs.offset());

    DocIterator dit = docs.iterator();

    while (dit.hasNext()) {
      int docid = dit.nextDoc();

      Document luceneDoc = searcher.doc(docid, fields);
      SolrDocument doc = new SolrDocument();

      for (IndexableField field : luceneDoc) {
        if (null == fields || fields.contains(field.name())) {
          SchemaField sf = schema.getField(field.name());
          doc.addField(field.name(), sf.getType().toObject(field));
        }
      }
      if (docs.hasScores() && (null == fields || fields.contains("score"))) {
        doc.addField("score", dit.score());
      }

      list.add(doc);

      if (ids != null) {
        ids.put(doc, docid);
      }
    }
    return list;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void init(NamedList args) {
    this.initParams = args;
    super.init(args);
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
      SolrResourceLoader loader = core.getResourceLoader();

      for (Map.Entry<String, Object> entry : initParams) {
        if ("engine".equals(entry.getKey())) {
          NamedList<Object> engineInitParams = (NamedList<Object>) entry.getValue();
          Boolean optional = engineInitParams.getBooleanArg("optional");
          optional = (optional == null ? Boolean.FALSE : optional);

          String engineClassName = StringUtils.defaultIfBlank(
              (String) engineInitParams.get("classname"),
              CarrotClusteringEngine.class.getName());

          EngineConfiguration defaultParams = new EngineConfiguration();
          defaultParams.setDocIdField(docIdField);
          defaultParams.extractFrom(engineInitParams.toSolrParams());

          // Set up engine name.
          final String engineName = StringUtils.defaultIfBlank(
              (String) engineInitParams.get(CONF_PARAM_ENGINE_NAME), "");

          // Instantiate the clustering engine and split to appropriate map.
          ClusteringEngine engine = new CarrotClusteringEngine(engineName, defaultParams);
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

  private Set<String> getFieldsToLoad(EngineConfiguration configuration) {
    Set<String> fields = new LinkedHashSet<>(configuration.fields());
    fields.add(configuration.docIdField());

    String languageField = configuration.languageField();
    if (StringUtils.isNotBlank(languageField)) {
      fields.add(languageField);
    }
    return fields;
  }

  @Override
  public void prepare(ResponseBuilder rb) {
    SolrParams params = rb.req.getParams();
    if (!params.getBool(COMPONENT_NAME, false)) {
      return;
    }
  }

  @Override
  public void process(ResponseBuilder rb) throws IOException {
    SolrParams params = rb.req.getParams();
    if (!params.getBool(COMPONENT_NAME, false)) {
      return;
    }

    final String name = getClusteringEngineName(rb);
    ClusteringEngine engine = clusteringEngines.get(name);
    if (engine != null) {
      EngineConfiguration requestConfiguration = engine.defaultConfiguration().clone().extractFrom(rb.req.getParams());

      checkAvailable(name, engine);
      DocListAndSet results = rb.getResults();
      Map<SolrDocument, Integer> docIds = new HashMap<>(results.docList.size());
      Set<String> fieldsToLoad = getFieldsToLoad(requestConfiguration);
      SolrDocumentList solrDocList = docListToSolrDocumentList(
          results.docList, rb.req.getSearcher(), fieldsToLoad, docIds);

      List<InputDocument> inputs = getDocuments(requestConfiguration, solrDocList, docIds, rb.getQuery(), rb.req);

      Object clusters = engine.cluster(requestConfiguration, rb.getQuery(), inputs, rb.req);
      rb.rsp.add("clusters", clusters);
    } else {
      log.warn("No engine named: {}", name);
    }
  }

  /**
   * Prepares Carrot2 documents for clustering.
   */
  private List<InputDocument> getDocuments(EngineConfiguration requestParameters,
                                           SolrDocumentList solrDocList, Map<SolrDocument, Integer> docIds,
                                           Query query, final SolrQueryRequest sreq) throws IOException {
    SolrParams solrParams = sreq.getParams();
    SolrCore core = sreq.getCore();
    String[] fieldsArray = requestParameters.fields().toArray(String[]::new);

    Function<SolrDocument, String> assignLanguage;
    String languageField = requestParameters.languageField();
    if (languageField != null) {
      assignLanguage = (doc) -> {
        Object fieldValue = doc.getFieldValue(languageField);
        return Objects.requireNonNullElse(fieldValue, requestParameters.language()).toString();
      };
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
        args.put(HighlightParams.FIELDS, fieldsArray);
        args.put(HighlightParams.HIGHLIGHT, "true");
        // We don't want any highlight marks.
        args.put(HighlightParams.SIMPLE_PRE, "");
        args.put(HighlightParams.SIMPLE_POST, "");
        args.put(HighlightParams.FRAGSIZE, solrParams.getInt(EngineConfiguration.SUMMARY_FRAGSIZE, solrParams.getInt(HighlightParams.FRAGSIZE, 100)));
        args.put(HighlightParams.SNIPPETS, solrParams.getInt(EngineConfiguration.SUMMARY_SNIPPETS, solrParams.getInt(HighlightParams.SNIPPETS, 1)));
        req = new LocalSolrQueryRequest(core, query.toString(), "", 0, 1, args) {
          @Override
          public SolrIndexSearcher getSearcher() {
            return sreq.getSearcher();
          }
        };
      } else {
        log.warn("No highlighter configured, cannot produce summary");
        produceSummary = false;
      }
    }

    Iterator<SolrDocument> docsIter = solrDocList.iterator();
    List<InputDocument> result = new ArrayList<>(solrDocList.size());

    while (docsIter.hasNext()) {
      SolrDocument sdoc = docsIter.next();

      // Store Solr id of the document, we need it to map document instances
      // found in clusters back to identifiers.
      InputDocument inputDocument = new InputDocument(
          assignLanguage.apply(sdoc),
          sdoc.getFieldValue(requestParameters.docIdField()));
      result.add(inputDocument);

      Function<String, Collection<?>> snippetProvider = (field) -> null;
      if (produceSummary && docIds != null) {
        DocList docAsList = new DocSlice(0, 1,
            new int[]{docIds.get(sdoc)}, new float[]{1.0f}, 1, 1.0f, TotalHits.Relation.EQUAL_TO);
        NamedList<Object> highlights = highlighter.doHighlighting(docAsList, query, req, fieldsArray);
        if (highlights != null && highlights.size() == 1) {
          @SuppressWarnings("unchecked")
          NamedList<String[]> tmp = (NamedList<String[]>) highlights.getVal(0);
          snippetProvider = (field) -> {
            String[] values = tmp.get(field);
            if (values == null) {
              return Collections.emptyList();
            } else {
              return Arrays.asList(values);
            }
          };
        }
      }

      Function<String, Collection<Object>> fullValueProvider = sdoc::getFieldValues;

      StringBuilder sb = new StringBuilder();
      for (String field : fieldsArray) {
        Collection<?> values = snippetProvider.apply(field);
        if (values == null || values.isEmpty()) {
          values = fullValueProvider.apply(field);
        }

        if (values != null && !values.isEmpty()) {
          sb.setLength(0);
          for (Object ob : values) {
            // Join multiple values with a period so that Carrot2 does not pick up
            // phrases that cross multiple field value boundaries (in most cases it would
            // create useless phrases).
            if (sb.length() > 0) {
              sb.append(" . ");
            }
            sb.append(Objects.toString(ob, ""));
          }
          inputDocument.addClusteredField(field, sb.toString());
        }
      }
    }

    return result;
  }

  private void checkAvailable(String name, ClusteringEngine engine) {
    if (!engine.isAvailable()) {
      throw new SolrException(ErrorCode.SERVER_ERROR,
          "Clustering engine declared, but not available, check the logs: " + name);
    }
  }

  private String getClusteringEngineName(ResponseBuilder rb) {
    return rb.req.getParams().get(REQ_PARAM_ENGINE, DEFAULT_ENGINE_NAME);
  }

  @Override
  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) {
    SolrParams params = rb.req.getParams();
    if (!params.getBool(COMPONENT_NAME, false)) {
      return;
    }

    sreq.params.remove(COMPONENT_NAME);
    if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) != 0) {
      String fl = sreq.params.get(CommonParams.FL, "*");
      // if fl=* then we don't need to check.
      if (fl.indexOf('*') >= 0) {
        return;
      }

      String name = getClusteringEngineName(rb);
      ClusteringEngine engine = clusteringEngines.get(name);
      if (engine != null) {
        checkAvailable(name, engine);

        EngineConfiguration requestConfiguration = engine.defaultConfiguration().clone().extractFrom(rb.req.getParams());
        Set<String> fields = getFieldsToLoad(requestConfiguration);
        if (fields == null || fields.size() == 0) {
          return;
        }

        StringBuilder sb = new StringBuilder();
        String[] flparams = fl.split("[,\\s]+");
        Set<String> flParamSet = new HashSet<>(flparams.length);
        for (String flparam : flparams) {
          // no need trim() because of split() by \s+
          flParamSet.add(flparam);
        }
        for (String aFieldToLoad : fields) {
          if (!flParamSet.contains(aFieldToLoad)) {
            sb.append(',').append(aFieldToLoad);
          }
        }
        if (sb.length() > 0) {
          sreq.params.set(CommonParams.FL, fl + sb.toString());
        }
      } else {
        log.warn("No engine named: {}", name);
      }
    }
  }

  @Override
  public void finishStage(ResponseBuilder rb) {
    SolrParams params = rb.req.getParams();
    if (!params.getBool(COMPONENT_NAME, false)) {
      return;
    }

    if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
      String name = getClusteringEngineName(rb);
      ClusteringEngine engine = clusteringEngines.get(name);
      if (engine != null) {
        EngineConfiguration requestConfiguration = engine.defaultConfiguration().clone().extractFrom(rb.req.getParams());
        checkAvailable(name, engine);
        SolrDocumentList solrDocList = (SolrDocumentList) rb.rsp.getResponse();
        // TODO: Currently, docIds is set to null in distributed environment.
        // This causes CarrotParams.PRODUCE_SUMMARY doesn't work.
        // To work CarrotParams.PRODUCE_SUMMARY under distributed mode, we can choose either one of:
        // (a) In each shard, ClusteringComponent produces summary and finishStage()
        //     merges these summaries.
        // (b) Adding doHighlighting(SolrDocumentList, ...) method to SolrHighlighter and
        //     making SolrHighlighter uses "external text" rather than stored values to produce snippets.
        Map<SolrDocument, Integer> docIds = null;
        try {
          List<InputDocument> inputs = getDocuments(requestConfiguration, solrDocList, docIds, rb.getQuery(), rb.req);
          Object clusters = engine.cluster(requestConfiguration, rb.getQuery(), inputs, rb.req);
          rb.rsp.add("clusters", clusters);
        } catch (IOException e) {
          throw new SolrException(ErrorCode.SERVER_ERROR, e);
        }
      } else {
        log.warn("No engine named: {}", name);
      }
    }
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
}
