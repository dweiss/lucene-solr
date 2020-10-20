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

import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Search results clustering engine API.
 *
 * @lucene.experimental
 */
public abstract class ClusteringEngine {
  private final String name;

  protected ClusteringEngine(String name) {
    this.name = name;
  }

  /**
   * Initialize the engine, parse default configuration.
   */
  public void init(NamedList<?> config, SolrCore core) {
    // Do nothing.
  }

  /**
   * @return Return this engine's name.
   */
  public final String getName() {
    return name;
  }

  /**
   * Do the clustering, return a clusters structure to be appended to
   * {@link SolrQueryResponse}.
   */
  public abstract List<NamedList<Object>> cluster(Query query, SolrDocumentList solrDocumentList,
                                                  Map<SolrDocument, Integer> docIds, SolrQueryRequest sreq);

  /**
   * @return The set of field names to load for each document.
   */
  public abstract Set<String> getFieldsToLoad(SolrQueryRequest sreq);

  /**
   * @return Returns {@code true} if the engine is available for processing requests.
   */
  public abstract boolean isAvailable();
}
