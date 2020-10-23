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

import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryVisitor;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.carrot2.clustering.Cluster;
import org.carrot2.clustering.ClusteringAlgorithm;
import org.carrot2.language.LanguageComponents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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
   * Set to {@code true} if the default algorithm is available.
   */
  private boolean engineAvailable;

  /**
   * All resources required for the clustering engine.
   */
  private EngineContext engineContext;

  public CarrotClusteringEngine(String name, EngineConfiguration defaultParams) {
    super(name, defaultParams);
  }

  @Override
  public boolean isAvailable() {
    return engineAvailable;
  }

  @Override
  public void init(final SolrCore core) {
    EngineConfiguration defaultParams = defaultConfiguration();
    this.engineContext = new EngineContext(defaultParams.resources(), core);

    // TODO: core.getResourceLoader()?
    {
      ClusteringAlgorithm defaultAlgorithm = engineContext.getAlgorithm(defaultParams.algorithmName());
      LanguageComponents defaultLanguage = engineContext.getLanguage(defaultParams.language());

      if (defaultAlgorithm == null) {
        log.warn("The default clustering algorithm for engine '{}' is not available: {}",
            getName(), defaultParams.algorithmName());
      }

      if (defaultLanguage == null) {
        log.warn("The default language for engine {} is not available: {}",
            getName(), defaultParams.language());
      }

      engineAvailable = (defaultAlgorithm != null && defaultLanguage != null);
    }
  }

  @Override
  public List<NamedList<Object>> cluster(EngineConfiguration requestParameters,
                                         Query query,
                                         List<InputDocument> documents,
                                         SolrQueryRequest sreq) {
    try {
      checkParameters(requestParameters);

      ClusteringAlgorithm algorithm = engineContext.getAlgorithm(requestParameters.algorithmName());
      populateAlgorithmParameters(query, requestParameters, algorithm);


      // Split documents into language groups.
      String defaultLanguage = requestParameters.language();
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
                      + "language will not be clustered.", lang, requestParameters.algorithmName());
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
      log.error("Clustering request failed.", e);
      throw new SolrException(ErrorCode.SERVER_ERROR, "Carrot2 clustering failed", e);
    }
  }

  private void populateAlgorithmParameters(Query query, EngineConfiguration requestParameters, ClusteringAlgorithm algorithm) {
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
  }

  private void checkParameters(EngineConfiguration requestParameters) {
    ClusteringAlgorithm algorithm = engineContext.getAlgorithm(requestParameters.algorithmName());
    if (algorithm == null) {
      throw new SolrException(ErrorCode.BAD_REQUEST, String.format(Locale.ROOT,
          "Algorithm '%s' not found in clustering component '%s'.",
          requestParameters.algorithmName(),
          getName()));
    }

    String defaultLanguage = requestParameters.language();
    LanguageComponents languageComponents = engineContext.getLanguage(defaultLanguage);
    if (languageComponents == null) {
      throw new SolrException(ErrorCode.BAD_REQUEST, String.format(Locale.ROOT,
          "Language '%s' is not supported in clustering component '%s'.",
          defaultLanguage,
          getName()));
    }

    if (!algorithm.supports(languageComponents)) {
      throw new SolrException(ErrorCode.BAD_REQUEST, String.format(Locale.ROOT,
          "Language '%s' is not supported by algorithm '%s' in clustering component '%s'.",
          defaultLanguage,
          requestParameters.algorithmName(),
          getName()));
    }

    if (requestParameters.fields().isEmpty()) {
      throw new SolrException(ErrorCode.BAD_REQUEST, String.format(Locale.ROOT,
          "At least one field name specifying content for clustering is required in parameter '%s'.",
          EngineConfiguration.PARAM_FIELDS));
    }
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
