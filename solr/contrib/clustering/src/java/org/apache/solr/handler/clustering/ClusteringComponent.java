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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrResourceLoader;
import org.apache.solr.handler.clustering.carrot2.CarrotClusteringEngine;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.handler.component.ShardRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A plugin for clustering search results. The default implementation
 * includes clustering algorithms from the
 * <a href="https://project.carrot2.org">Carrot<sup>2</sup> project</a>.
 *
 * @lucene.experimental
 */
public class ClusteringComponent extends SearchComponent implements SolrCoreAware {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * Base name for all component parameters. This name is also used to
   * register this component with SearchHandler.
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
    if (initParams != null) {
      log.info("Initializing Clustering Engines");

      // Our target list of engines, split into search-results and document clustering.
      SolrResourceLoader loader = core.getResourceLoader();

      for (Map.Entry<String, Object> entry : initParams) {
        if ("engine".equals(entry.getKey())) {
          NamedList<Object> engineInitParams = (NamedList<Object>) entry.getValue();
          Boolean optional = engineInitParams.getBooleanArg("optional");
          optional = (optional == null ? Boolean.FALSE : optional);

          String engineClassName = StringUtils.defaultIfBlank(
              (String) engineInitParams.get("classname"),
              CarrotClusteringEngine.class.getName());

          // Set up engine name.
          final String engineName = StringUtils.defaultIfBlank(
              (String) engineInitParams.get(CONF_PARAM_ENGINE_NAME), "");

          // Instantiate the clustering engine and split to appropriate map. 
          final ClusteringEngine engine = loader.newInstance(
              engineClassName,
              ClusteringEngine.class,
              new String[0],
              new Class<?>[]{String.class},
              new Object[]{engineName});

          engine.init(engineInitParams, core);

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

      log.info("Finished Initializing Clustering Engines");
    }
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
      checkAvailable(name, engine);
      DocListAndSet results = rb.getResults();
      Map<SolrDocument, Integer> docIds = new HashMap<>(results.docList.size());
      Set<String> fieldsToLoad = engine.getFieldsToLoad(rb.req);
      SolrDocumentList solrDocList = docListToSolrDocumentList(
          results.docList, rb.req.getSearcher(), fieldsToLoad, docIds);
      Object clusters = engine.cluster(rb.getQuery(), solrDocList, docIds, rb.req);
      rb.rsp.add("clusters", clusters);
    } else {
      log.warn("No engine named: {}", name);
    }
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
        Set<String> fields = engine.getFieldsToLoad(rb.req);
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
        Object clusters = engine.cluster(rb.getQuery(), solrDocList, docIds, rb.req);
        rb.rsp.add("clusters", clusters);
      } else {
        log.warn("No engine named: {}", name);
      }
    }
  }

  /**
   * @return A map of initialized clustering engines.
   */
  Map<String, ClusteringEngine> getClusteringEngines() {
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
