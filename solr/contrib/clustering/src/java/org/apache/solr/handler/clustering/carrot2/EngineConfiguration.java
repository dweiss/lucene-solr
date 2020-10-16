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

import org.apache.solr.common.params.SolrParams;

import java.util.LinkedHashMap;

/**
 * Clustering engine configuration parameters (and other parameters that
 * may tweak the algorithm on a per-request basis).
 *
 * @lucene.experimental
 */
public final class EngineConfiguration implements Cloneable {
  /**
   * Common prefix for configuration of engine settings.
   */
  private static final String PARAM_PREFIX = "engine.";

  /**
   * @see #algorithmName()
   */
  public static final String PARAM_ALGORITHM = PARAM_PREFIX + "algorithm";

  /**
   * @see #maxLabels()
   */
  public static final String PARAM_MAX_LABELS = PARAM_PREFIX + "maxLabels";

  /**
   * @see #includeSubclusters()
   */
  public static final String PARAM_INCLUDE_SUBCLUSTERS = PARAM_PREFIX + "includeSubclusters";

  /**
   * @see #includeOtherTopics()
   */
  public static final String PARAM_INCLUDE_OTHER_TOPICS = PARAM_PREFIX + "includeOtherTopics";

  /**
   * @see #language()
   */
  public static final String PARAM_LANGUAGE = PARAM_PREFIX + "language";

  /**
   * @see #resources()
   */
  public static final String PARAM_RESOURCES = PARAM_PREFIX + "resources";

  /**
   * @see #languageField()
   */
  public static final String PARAM_LANGUAGE_FIELD = PARAM_PREFIX + "languageField";


  public static String TITLE_FIELD_NAME = PARAM_PREFIX + "title";
  public static String SNIPPET_FIELD_NAME = PARAM_PREFIX + "snippet";

  public static String PRODUCE_SUMMARY = PARAM_PREFIX + "produceSummary";
  public static String SUMMARY_FRAGSIZE = PARAM_PREFIX + "fragSize";
  public static String SUMMARY_SNIPPETS = PARAM_PREFIX + "summarySnippets";

  public static String LANGUAGE_CODE_MAP = PARAM_PREFIX + "lcmap";

  /**
   * @see #PARAM_MAX_LABELS
   */
  private int maxLabels = Integer.MAX_VALUE;

  /**
   * @see #PARAM_INCLUDE_SUBCLUSTERS
   */
  private boolean includeSubclusters = true;

  /**
   * @see #PARAM_INCLUDE_OTHER_TOPICS
   */
  private boolean includeOtherTopics = true;

  /**
   * @see #PARAM_ALGORITHM
   */
  private String algorithmName;

  /**
   * @see #PARAM_RESOURCES
   */
  private String resources;

  /**
   * @see #PARAM_LANGUAGE
   */
  private String language = "English";

  /**
   * @see #PARAM_LANGUAGE_FIELD
   */
  private String languageField;

  /**
   * Non-engine configuration parameters (algorithm parameters).
   */
  private LinkedHashMap<String, String> otherParameters = new LinkedHashMap<>();


  EngineConfiguration() {
  }

  /**
   * Extract parameter values from the given {@link SolrParams}.
   */
  void extractFrom(SolrParams params) {
    params.stream().forEachOrdered(e -> {
      switch (e.getKey()) {
        case PARAM_MAX_LABELS:
          maxLabels = params.getInt(PARAM_MAX_LABELS);
          break;
        case PARAM_INCLUDE_SUBCLUSTERS:
          includeSubclusters = params.getBool(PARAM_INCLUDE_SUBCLUSTERS);
          break;
        case PARAM_INCLUDE_OTHER_TOPICS:
          includeOtherTopics = params.getBool(PARAM_INCLUDE_OTHER_TOPICS);
          break;
        case PARAM_ALGORITHM:
          algorithmName = params.get(PARAM_ALGORITHM);
          break;
        case PARAM_RESOURCES:
          resources = params.get(PARAM_RESOURCES);
          break;
        case PARAM_LANGUAGE:
          language = params.get(PARAM_LANGUAGE);
          break;
        case PARAM_LANGUAGE_FIELD:
          languageField = params.get(PARAM_LANGUAGE_FIELD);
          break;
        default:
          // Unrecognized parameter. Preserve it.
          String[] value = e.getValue();
          if (value != null) {
            if (value.length == 1) {
              otherParameters.put(e.getKey(), value[0]);
            } else {
              otherParameters.put(e.getKey(), String.join(", ", value));
            }
          }
          break;
      }
    });
  }

  /**
   * @return Maximum number of returned cluster labels (even if the algorithm
   * returns more).
   */
  public int maxLabels() {
    return maxLabels;
  }

  /**
   * @return If {@code true}, include subclusters in response (if the algorithm
   * produces hierarchical clustering).
   */
  public boolean includeSubclusters() {
    return includeSubclusters;
  }

  /**
   * @return If {@code true}, include a synthetic cluster called "Other Topics" that
   * consists of all documents not assigned to any other cluster.
   */
  public boolean includeOtherTopics() {
    return includeOtherTopics;
  }

  /**
   * @return Name of the clustering algorithm to use (as loaded via the service
   *    * extension point {@link org.carrot2.clustering.ClusteringAlgorithm}).
   */
  public String algorithmName() {
    return algorithmName;
  }

  /**
   * @return Return Solr component-configuration relative language resources path.
   */
  public String resources() {
    return resources;
  }

  /**
   * @return Name of the default language to use for clustering. The corresponding
   * {@link org.carrot2.language.LanguageComponents} must be available (loaded via
   * service provider extension).
   */
  public String language() {
    return language;
  }

  /**
   * @return Name of the field that carries each document's language. {@code null} value
   * means all documents will be clustered according to the default {@link #language()}.
   * If not {@code null} and the document's field has a missing value, it will be clustered
   * using the default {@link #language()} as well.
   */
  public String languageField() {
    return languageField;
  }

  LinkedHashMap<String, String> otherParameters() {
    return otherParameters;
  }

  @Override
  public EngineConfiguration clone() {
    try {
      EngineConfiguration clone = (EngineConfiguration) super.clone();
      clone.otherParameters = new LinkedHashMap<>(this.otherParameters);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}
