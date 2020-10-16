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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.apache.lucene.search.TotalHits;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.clustering.ClusteringEngine;
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
import org.carrot2.language.LanguageComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.stream.Collectors;

/**
 * Search results clustering engine based on Carrot2 clustering algorithms.
 *
 * @lucene.experimental
 * @see "https://project.carrot2.org"
 */
public class CarrotClusteringEngine extends ClusteringEngine {
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
   * Set to {@code true} if the default algorithm is available.
   */
  private boolean engineAvailable;

  /**
   * Solr core we're bound to.
   */
  private SolrCore core;

  /**
   * All resources required for the clustering engine.
   */
  private EngineContext engineContext;

  /**
   * Default configuration parameters.
   */
  private EngineConfiguration defaultParams;

  public CarrotClusteringEngine(String name) {
    super(name);
  }

  @Override
  public boolean isAvailable() {
    return engineAvailable;
  }

  @Override
  public void init(NamedList<?> config, final SolrCore core) {
    this.core = core;

    final SolrParams initParams = config.toSolrParams();

    this.defaultParams = new EngineConfiguration();
    this.defaultParams.extractFrom(initParams);
    this.engineContext = new EngineContext(defaultParams.resources(), core);

    // TODO: core.getResourceLoader()?
    {
      ClusteringAlgorithm defaultAlgorithm = engineContext.getAlgorithm(defaultParams.algorithmName());
      LanguageComponents defaultLanguage = engineContext.getLanguage(defaultParams.language());

      if (defaultAlgorithm == null) {
        log.warn("The default clustering algorithm for engine {} is not available: {}",
            getName(), defaultParams.algorithmName());
      }

      if (defaultLanguage == null) {
        log.warn("The default language for engine {} is not available: {}",
            getName(), defaultParams.language());
      }

      engineAvailable = (defaultAlgorithm != null && defaultLanguage != null);
    }

    SchemaField uniqueField = core.getLatestSchema().getUniqueKeyField();
    if (uniqueField == null) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
          CarrotClusteringEngine.class.getSimpleName() + " requires the schema to have a uniqueKeyField");
    }
    this.idFieldName = uniqueField.getName();
  }

  @Override
  public List<NamedList<Object>> cluster(Query query, SolrDocumentList solrDocList,
                                         Map<SolrDocument, Integer> docIds, SolrQueryRequest sreq) {
    try {
      // Layer any parameters from the request over the defaults.
      EngineConfiguration requestParameters = this.defaultParams.clone();
      requestParameters.extractFrom(sreq.getParams());

      ClusteringAlgorithm algorithm = engineContext.getAlgorithm(requestParameters.algorithmName());
      if (algorithm == null) {
        throw new RuntimeException(String.format(Locale.ROOT,
            "Algorithm '%s' not found in clustering component '%s'.",
            requestParameters.algorithmName(),
            getName()));
      }

      String defaultLanguage = requestParameters.language();
      LanguageComponents languageComponents = engineContext.getLanguage(defaultLanguage);
      if (languageComponents == null) {
        throw new RuntimeException(String.format(Locale.ROOT,
            "Language '%s' is not supported in clustering component '%s'.",
            defaultLanguage,
            getName()));
      }

      if (!algorithm.supports(languageComponents)) {
        throw new RuntimeException(String.format(Locale.ROOT,
            "Language '%s' is not supported by algorithm '%s' in clustering component '%s'.",
            defaultLanguage,
            requestParameters.algorithmName(),
            getName()));
      }

      LinkedHashMap<String, String> attrs = requestParameters.otherParameters();
      // Set the optional query hint. We extract just the terms
      if (!attrs.containsKey("queryHint")) {
        Set<String> termSet = new LinkedHashSet<>();
        query.visit(new QueryVisitor() {
          @Override
          public void consumeTerms(Query query, Term... terms) {
            for (Term t : terms) {
              termSet.add(t.text());
            }
          }
        });
        attrs.put("queryHint", String.join(" ", termSet));
      }
      algorithm.accept(new AttrVisitorFromFlattenedKeys(attrs));

      // Collect logical input "documents".
      List<InputDocument> documents = getDocuments(requestParameters, solrDocList, docIds, query, sreq);

      // Split documents into language groups.
      Map<String, List<InputDocument>> documentsByLanguage =
          documents.stream()
              .collect(
                  Collectors.groupingBy(
                      doc -> {
                        String lang = doc.language();
                        return lang == null ? defaultLanguage : lang;
                      }));

      // Cluster documents within each language group.
      HashSet<String> warnOnce = new HashSet<>();
      LinkedHashMap<String, List<Cluster<InputDocument>>> clustersByLanguage =
          new LinkedHashMap<>();
      for (Map.Entry<String, List<InputDocument>> e : documentsByLanguage.entrySet()) {
        String lang = e.getKey();
        if (!engineContext.isLanguageSupported(lang)) {
          if (warnOnce.add(lang)) {
            log.warn(
                "Language '{}' is not supported, documents in this "
                    + "language will not be clustered.", lang);
          }
        } else {
          LanguageComponents langComponents = engineContext.getLanguage(lang);
          if (!algorithm.supports(langComponents)) {
            if (warnOnce.add(lang)) {
              log.warn(
                  "Language '{}' is not supported by algorithm '{}', documents in this "
                      + "language will not be clustered.", lang,
                    requestParameters.algorithmName());
            }
          } else {
            clustersByLanguage.put(
                lang, algorithm.cluster(e.getValue().stream(), langComponents));
          }
        }
      }

      List<Cluster<InputDocument>> clusters;
      if (clustersByLanguage.size() == 1) {
        clusters = clustersByLanguage.values().iterator().next();
      } else {
        clusters = clustersByLanguage.entrySet().stream()
            .map(e -> {
              Cluster<InputDocument> cluster = new Cluster<>();
              cluster.addLabel(e.getKey());
              e.getValue().forEach(cluster::addCluster);
              return cluster;
            })
            .collect(Collectors.toList());
      }

      return clustersToNamedList(documents, clusters, requestParameters);
    } catch (Exception e) {
      log.error("Carrot2 clustering failed", e);
      throw new SolrException(ErrorCode.SERVER_ERROR, "Carrot2 clustering failed", e);
    }
  }

  @Override
  public Set<String> getFieldsToLoad(SolrQueryRequest sreq) {
    HashSet<String> fields = new HashSet<>(getFieldsForClustering(sreq));
    fields.add(idFieldName);

    String languageField = defaultParams.languageField();
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

    String titleFieldSpec = solrParams.get(EngineConfiguration.TITLE_FIELD_NAME, "title");
    String snippetFieldSpec = solrParams.get(EngineConfiguration.SNIPPET_FIELD_NAME, titleFieldSpec);
    if (StringUtils.isBlank(snippetFieldSpec)) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, EngineConfiguration.SNIPPET_FIELD_NAME
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
  private List<InputDocument> getDocuments(EngineConfiguration requestParameters,
                                           SolrDocumentList solrDocList, Map<SolrDocument, Integer> docIds,
                                           Query query, final SolrQueryRequest sreq) throws IOException {
    SolrHighlighter highlighter = null;
    SolrParams solrParams = sreq.getParams();
    SolrCore core = sreq.getCore();

    String titleFieldSpec = solrParams.get(EngineConfiguration.TITLE_FIELD_NAME, "title");
    String snippetFieldSpec = solrParams.get(EngineConfiguration.SNIPPET_FIELD_NAME, titleFieldSpec);

    // Get the documents
    boolean produceSummary = solrParams.getBool(EngineConfiguration.PRODUCE_SUMMARY, false);

    SolrQueryRequest req = null;
    String[] snippetFieldAry = null;
    if (produceSummary) {
      highlighter = ((HighlightComponent) core.getSearchComponents().get(HighlightComponent.COMPONENT_NAME)).getHighlighter();
      if (highlighter != null) {
        Map<String, Object> args = new HashMap<>();
        snippetFieldAry = snippetFieldSpec.split("[, ]");
        args.put(HighlightParams.FIELDS, snippetFieldAry);
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

    float[] scores = {1.0f};
    int[] docsHolder = new int[1];
    Query theQuery = query;

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
      InputDocument inputDocument = new InputDocument(assignLanguage.apply(sdoc), sdoc.getFieldValue(idFieldName));
      inputDocument.addClusteredField("title", getConcatenated(sdoc, titleFieldSpec));
      inputDocument.addClusteredField("snippet", snippet);

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

      result.add(inputDocument);
    }

    return result;
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

  private List<NamedList<Object>> clustersToNamedList(List<InputDocument> documents,
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
        cluster.add("docs", unclustered.stream().map(InputDocument::getSolrDocumentId)
            .collect(Collectors.toList()));
      }
    }

    return result;
  }

  private void clustersToNamedListRecursive(
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

      converted.add("docs", docs.stream().map(InputDocument::getSolrDocumentId)
          .collect(Collectors.toList()));

      if (params.includeSubclusters() && !cluster.getClusters().isEmpty()) {
        List<NamedList<Object>> subclusters = new ArrayList<>();
        converted.add("clusters", subclusters);
        clustersToNamedListRecursive(cluster.getClusters(), subclusters, params);
      }
    }
  }

  private LinkedHashSet<InputDocument> collectUniqueDocuments(Cluster<InputDocument> cluster, LinkedHashSet<InputDocument> unique) {
    unique.addAll(cluster.getDocuments());
    for (Cluster<InputDocument> sub : cluster.getClusters()) {
      collectUniqueDocuments(sub, unique);
    }
    return unique;
  }
}
