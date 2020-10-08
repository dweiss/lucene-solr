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
package org.apache.solr.handler.clustering.carrot2;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHits;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.SuppressForbidden;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.clustering.SearchClusteringEngine;
import org.apache.solr.handler.component.HighlightComponent;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocSlice;
import org.apache.solr.search.SolrIndexSearcher;
import org.carrot2.clustering.Cluster;
import org.carrot2.clustering.ClusteringAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Search results clustering engine based on Carrot2 clustering algorithms.
 *
 * @lucene.experimental
 * @see "http://project.carrot2.org"
 */
public class CarrotClusteringEngine extends SearchClusteringEngine {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  /**
   * The subdirectory in Solr config dir to read customized Carrot2 resources from.
   */
  static final String CARROT_RESOURCES_PREFIX = "clustering/carrot2";

  /**
   * Name of Solr document's field containing the document's identifier. To avoid
   * repeating the content of documents in clusters on output, each cluster contains
   * identifiers of documents it contains.
   */
  private String idFieldName;

  /**
   * A {@link ClusteringAlgorithm} name (provider name) used for actual clustering.
   */
  private String clusteringAlgorithmName;

  /**
   * Set to {@code true} if {@link #clusteringAlgorithmName} is available.
   */
  private boolean algorithmAvailable;

  /**
   * Solr core we're bound to.
   */
  private SolrCore core;

  /**
   * All resources required for the clustering engine.
   */
  private ClusteringEngineContext engineContext;

  @Override
  public boolean isAvailable() {
    return algorithmAvailable;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public String init(NamedList config, final SolrCore core) {
    this.core = core;

    String result = super.init(config, core);
    final SolrParams initParams = config.toSolrParams();

    this.engineContext = new ClusteringEngineContext();

    // Make sure the requested Carrot2 clustering algorithm class is available
    clusteringAlgorithmName = initParams.get(CarrotParams.ALGORITHM);

    // TODO: core.getResourceLoader()?
    algorithmAvailable = (engineContext.getAlgorithm(clusteringAlgorithmName) != null);
    if (!isAvailable()) {
      log.info("Clustering algorithm is not available: " + clusteringAlgorithmName);
    }

    SchemaField uniqueField = core.getLatestSchema().getUniqueKeyField();
    if (uniqueField == null) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
          CarrotClusteringEngine.class.getSimpleName() + " requires the schema to have a uniqueKeyField");
    }
    this.idFieldName = uniqueField.getName();

    return result;
  }

  @Override
  public Object cluster(Query query, SolrDocumentList solrDocList,
                        Map<SolrDocument, Integer> docIds, SolrQueryRequest sreq) {
    try {
      ClusteringAlgorithm algorithm = engineContext.getAlgorithm(clusteringAlgorithmName);

      /*
      // Prepare attributes for Carrot2 clustering call
      Map<String, Object> attributes = new HashMap<>();
      List<Document> documents = getDocuments(solrDocList, docIds, query, sreq);
      attributes.put(AttributeNames.DOCUMENTS, documents);
      attributes.put(AttributeNames.QUERY, query.toString());

      // Pass the fields on which clustering runs.
      attributes.put("solrFieldNames", getFieldsForClustering(sreq));

      // Pass extra overriding attributes from the request, if any
      extractCarrotAttributes(sreq.getParams(), attributes);
       */

      // Perform clustering and convert to an output structure of clusters.
      List<CarrotDocument> documents = getDocuments(solrDocList, docIds, query, sreq);
      List<Cluster<CarrotDocument>> clusters = algorithm.cluster(documents.stream(),
          engineContext.getLanguage("English"));
      return clustersToNamedList(documents, clusters, sreq.getParams());
    } catch (Exception e) {
      log.error("Carrot2 clustering failed", e);
      throw new SolrException(ErrorCode.SERVER_ERROR, "Carrot2 clustering failed", e);
    }
  }

  @Override
  protected Set<String> getFieldsToLoad(SolrQueryRequest sreq) {
    SolrParams solrParams = sreq.getParams();

    HashSet<String> fields = new HashSet<>(getFieldsForClustering(sreq));
    fields.add(idFieldName);
    fields.add(solrParams.get(CarrotParams.URL_FIELD_NAME, "url"));
    fields.addAll(getCustomFieldsMap(solrParams).keySet());

    String languageField = solrParams.get(CarrotParams.LANGUAGE_FIELD_NAME);
    if (StringUtils.isNotBlank(languageField)) {
      fields.add(languageField);
    }
    return fields;
  }

  /**
   * Returns the names of fields that will be delivering the actual
   * content for clustering. Currently, there are two such fields: document
   * title and document content.
   */
  private Set<String> getFieldsForClustering(SolrQueryRequest sreq) {
    SolrParams solrParams = sreq.getParams();

    String titleFieldSpec = solrParams.get(CarrotParams.TITLE_FIELD_NAME, "title");
    String snippetFieldSpec = solrParams.get(CarrotParams.SNIPPET_FIELD_NAME, titleFieldSpec);
    if (StringUtils.isBlank(snippetFieldSpec)) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, CarrotParams.SNIPPET_FIELD_NAME
          + " must not be blank.");
    }

    final Set<String> fields = new HashSet<>();
    fields.addAll(Arrays.asList(titleFieldSpec.split("[, ]")));
    fields.addAll(Arrays.asList(snippetFieldSpec.split("[, ]")));
    return fields;
  }

  /**
   * Prepares Carrot2 documents for clustering.
   */
  private List<CarrotDocument> getDocuments(SolrDocumentList solrDocList, Map<SolrDocument, Integer> docIds,
                                            Query query, final SolrQueryRequest sreq) throws IOException {
    SolrHighlighter highlighter = null;
    SolrParams solrParams = sreq.getParams();
    SolrCore core = sreq.getCore();

    String urlField = solrParams.get(CarrotParams.URL_FIELD_NAME, "url");
    String titleFieldSpec = solrParams.get(CarrotParams.TITLE_FIELD_NAME, "title");
    String snippetFieldSpec = solrParams.get(CarrotParams.SNIPPET_FIELD_NAME, titleFieldSpec);
    // TODO: language handling.
    //String languageField = solrParams.get(CarrotParams.LANGUAGE_FIELD_NAME, null);

    // Maps Solr field names to Carrot2 custom field names
    Map<String, String> customFields = getCustomFieldsMap(solrParams);

    // Parse language code map string into a map
    /*
    Map<String, String> languageCodeMap = new HashMap<>();
    if (StringUtils.isNotBlank(languageField)) {
      for (String pair : solrParams.get(CarrotParams.LANGUAGE_CODE_MAP, "").split("[, ]")) {
        final String[] split = pair.split(":");
        if (split.length == 2 && StringUtils.isNotBlank(split[0]) && StringUtils.isNotBlank(split[1])) {
          languageCodeMap.put(split[0], split[1]);
        } else {
          log.warn("Unsupported format for {}: '{}'. Skipping this mapping."
              , CarrotParams.LANGUAGE_CODE_MAP, pair);
        }
      }
    }
     */

    // Get the documents
    boolean produceSummary = solrParams.getBool(CarrotParams.PRODUCE_SUMMARY, false);

    SolrQueryRequest req = null;
    String[] snippetFieldAry = null;
    if (produceSummary) {
      highlighter = HighlightComponent.getHighlighter(core);
      if (highlighter != null) {
        Map<String, Object> args = new HashMap<>();
        snippetFieldAry = snippetFieldSpec.split("[, ]");
        args.put(HighlightParams.FIELDS, snippetFieldAry);
        args.put(HighlightParams.HIGHLIGHT, "true");
        args.put(HighlightParams.SIMPLE_PRE, ""); //we don't care about actually highlighting the area
        args.put(HighlightParams.SIMPLE_POST, "");
        args.put(HighlightParams.FRAGSIZE, solrParams.getInt(CarrotParams.SUMMARY_FRAGSIZE, solrParams.getInt(HighlightParams.FRAGSIZE, 100)));
        args.put(HighlightParams.SNIPPETS, solrParams.getInt(CarrotParams.SUMMARY_SNIPPETS, solrParams.getInt(HighlightParams.SNIPPETS, 1)));
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
    List<CarrotDocument> result = new ArrayList<>(solrDocList.size());

    float[] scores = {1.0f};
    int[] docsHolder = new int[1];
    Query theQuery = query;

    while (docsIter.hasNext()) {
      SolrDocument sdoc = docsIter.next();
      String snippet = null;

      // TODO: docIds will be null when running distributed search.
      // See comment in ClusteringComponent#finishStage().
      if (produceSummary && docIds != null) {
        docsHolder[0] = docIds.get(sdoc);
        DocList docAsList = new DocSlice(0, 1, docsHolder, scores, 1, 1.0f, TotalHits.Relation.EQUAL_TO);
        NamedList<Object> highlights = highlighter.doHighlighting(docAsList, theQuery, req, snippetFieldAry);
        if (highlights != null && highlights.size() == 1) {
          // should only be one value given our setup
          // should only be one document
          @SuppressWarnings("unchecked")
          NamedList<String[]> tmp = (NamedList<String[]>) highlights.getVal(0);

          final StringBuilder sb = new StringBuilder();
          for (int j = 0; j < snippetFieldAry.length; j++) {
            // Join fragments with a period, so that Carrot2 does not create
            // cross-fragment phrases, such phrases rarely make sense.
            String[] highlt = tmp.get(snippetFieldAry[j]);
            if (highlt != null && highlt.length > 0) {
              for (int i = 0; i < highlt.length; i++) {
                sb.append(highlt[i]);
                sb.append(" . ");
              }
            }
          }
          snippet = sb.toString();
        }
      }

      // If summaries not enabled or summary generation failed, use full content.
      if (snippet == null) {
        snippet = getConcatenated(sdoc, snippetFieldSpec);
      }

      // Store Solr id of the document, we need it to map document instances
      // found in clusters back to identifiers.
      CarrotDocument carrotDocument = new CarrotDocument(sdoc.getFieldValue(idFieldName));
      carrotDocument.addClusteredField("title", getConcatenated(sdoc, titleFieldSpec));
      carrotDocument.addClusteredField("snippet", snippet);
      /*
        // TODO: url field is no more
        //Objects.toString(sdoc.getFieldValue(urlField), "");
      };
       */

      // Set language
      /*
      if (StringUtils.isNotBlank(languageField)) {
        Collection<Object> languages = sdoc.getFieldValues(languageField);
        if (languages != null) {

          // Use the first Carrot2-supported language
          for (Object l : languages) {
            String lang = Objects.toString(l, "");

            if (languageCodeMap.containsKey(lang)) {
              lang = languageCodeMap.get(lang);
            }

            // Language detection Library for Java uses dashes to separate
            // language variants, such as 'zh-cn', but Carrot2 uses underscores.
            if (lang.indexOf('-') > 0) {
              lang = lang.replace('-', '_');
            }

            // If the language is supported by Carrot2, we'll get a non-null value
            final LanguageCode carrot2Language = LanguageCode.forISOCode(lang);
            if (carrot2Language != null) {
              carrotDocument.setLanguage(carrot2Language);
              break;
            }
          }
        }
      }
       */

      /*
      // TODO: custom fields?
      // Add custom fields
      if (customFields != null) {
        for (Entry<String, String> entry : customFields.entrySet()) {
          carrotDocument.setField(entry.getValue(), sdoc.getFieldValue(entry.getKey()));
        }
      }
       */

      result.add(carrotDocument);
    }

    return result;
  }

  /**
   * Prepares a map of Solr field names (keys) to the corresponding Carrot2
   * custom field names.
   */
  private Map<String, String> getCustomFieldsMap(SolrParams solrParams) {
    Map<String, String> customFields = new HashMap<>();
    String[] customFieldsSpec = solrParams.getParams(CarrotParams.CUSTOM_FIELD_NAME);
    if (customFieldsSpec != null) {
      customFields = new HashMap<>();
      for (String customFieldSpec : customFieldsSpec) {
        String[] split = customFieldSpec.split(":");
        if (split.length == 2 && StringUtils.isNotBlank(split[0]) && StringUtils.isNotBlank(split[1])) {
          customFields.put(split[0], split[1]);
        } else {
          log.warn("Unsupported format for {}: '{}'. Skipping this field definition."
              , CarrotParams.CUSTOM_FIELD_NAME, customFieldSpec);
        }
      }
    }
    return customFields;
  }

  private String getConcatenated(SolrDocument sdoc, String fieldsSpec) {
    StringBuilder result = new StringBuilder();
    for (String field : fieldsSpec.split("[, ]")) {
      Collection<Object> vals = sdoc.getFieldValues(field);
      if (vals == null) continue;
      Iterator<Object> ite = vals.iterator();
      while (ite.hasNext()) {
        // Join multiple values with a period so that Carrot2 does not pick up
        // phrases that cross field value boundaries (in most cases it would
        // create useless phrases).
        result.append(Objects.toString(ite.next(), "")).append(" . ");
      }
    }
    return result.toString().trim();
  }

  private List<NamedList<Object>> clustersToNamedList(List<CarrotDocument> documents,
                                                      List<Cluster<CarrotDocument>> clusters,
                                                      SolrParams solrParams) {
    List<NamedList<Object>> result = new ArrayList<>();
    clustersToNamedList(clusters, result, solrParams.getBool(
        CarrotParams.OUTPUT_SUB_CLUSTERS, true), solrParams.getInt(
        CarrotParams.NUM_DESCRIPTIONS, Integer.MAX_VALUE));

    LinkedHashSet<CarrotDocument> clustered = new LinkedHashSet<>();
    clusters.forEach(cluster -> collectUniqueDocuments(cluster, clustered));
    List<CarrotDocument> unclustered = documents.stream()
        .filter(doc -> !clustered.contains(doc))
        .collect(Collectors.toList());

    if (!unclustered.isEmpty()) {
      NamedList<Object> cluster = new SimpleOrderedMap<>();
      result.add(cluster);
      cluster.add("other-topics", true);
      cluster.add("labels", Arrays.asList("Other topics"));
      cluster.add("score", 0);
      cluster.add("docs", unclustered.stream().map(CarrotDocument::getSolrDocumentId)
          .collect(Collectors.toList()));
    }

    return result;
  }

  private void clustersToNamedList(
      List<Cluster<CarrotDocument>> outputClusters,
      List<NamedList<Object>> parent, boolean outputSubClusters, int maxLabels) {
    for (Cluster<CarrotDocument> outCluster : outputClusters) {
      NamedList<Object> cluster = new SimpleOrderedMap<>();
      parent.add(cluster);

      // Add labels
      List<String> labels = outCluster.getLabels();
      if (labels.size() > maxLabels) {
        labels = labels.subList(0, maxLabels);
      }
      cluster.add("labels", labels);

      // Add cluster score
      final Double score = outCluster.getScore();
      if (score != null) {
        cluster.add("score", score);
      }

      // Add documents
      List<CarrotDocument> docs = outputSubClusters ? outCluster.getDocuments() :
          new ArrayList<>(collectUniqueDocuments(outCluster, new LinkedHashSet<>()));

      cluster.add("docs", docs.stream().map(CarrotDocument::getSolrDocumentId)
          .collect(Collectors.toList()));

      // Add subclusters
      if (outputSubClusters && !outCluster.getClusters().isEmpty()) {
        List<NamedList<Object>> subclusters = new ArrayList<>();
        cluster.add("clusters", subclusters);
        clustersToNamedList(outCluster.getClusters(), subclusters,
            outputSubClusters, maxLabels);
      }
    }
  }

  private LinkedHashSet<CarrotDocument> collectUniqueDocuments(Cluster<CarrotDocument> cluster, LinkedHashSet<CarrotDocument> unique) {
    unique.addAll(cluster.getDocuments());
    for (Cluster<CarrotDocument> sub : cluster.getClusters()) {
      collectUniqueDocuments(sub, unique);
    }
    return unique;
  }

  @SuppressForbidden(reason = "Uses context class loader as a workaround to inject correct classloader to 3rd party libs")
  private static <T> T withContextClassLoader(ClassLoader loader, Supplier<T> action) {
    Thread ct = Thread.currentThread();
    ClassLoader prev = ct.getContextClassLoader();
    try {
      ct.setContextClassLoader(loader);
      return action.get();
    } finally {
      ct.setContextClassLoader(prev);
    }
  }
}
