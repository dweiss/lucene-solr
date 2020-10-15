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
  private static final String PARAM_PREFIX = "engine.";

  public static final String PARAM_ALGORITHM = PARAM_PREFIX + "algorithm";

  public static final String PARAM_MAX_LABELS = PARAM_PREFIX + "maxLabels";
  public static final String PARAM_INCLUDE_SUBCLUSTERS = PARAM_PREFIX + "includeSubclusters";
  public static final String PARAM_INCLUDE_OTHER_TOPICS = PARAM_PREFIX + "includeOtherTopics";

  public static String TITLE_FIELD_NAME = PARAM_PREFIX + "title";
  public static String URL_FIELD_NAME = PARAM_PREFIX + "url";
  public static String SNIPPET_FIELD_NAME = PARAM_PREFIX + "snippet";
  public static String LANGUAGE_FIELD_NAME = PARAM_PREFIX + "lang";
  public static String CUSTOM_FIELD_NAME = PARAM_PREFIX + "custom";

  public static String PRODUCE_SUMMARY = PARAM_PREFIX + "produceSummary";
  public static String SUMMARY_FRAGSIZE = PARAM_PREFIX + "fragSize";
  public static String SUMMARY_SNIPPETS = PARAM_PREFIX + "summarySnippets";

  public static String LANGUAGE_CODE_MAP = PARAM_PREFIX + "lcmap";

  /**
   * Points to Carrot<sup>2</sup> resources
   */
  public static String RESOURCES_DIR = PARAM_PREFIX + "resourcesDir";

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
   * @return The name of the clustering algorithm to use (Carrot2 service extension provider name).
   */
  public String algorithmName() {
    return algorithmName;
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
