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

import org.carrot2.clustering.Document;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Representation of a single logical "document" for clustering.
 */
class CarrotDocument implements Document {
  private final Object solrDocumentId;
  private final Map<String, String> clusteredFields = new LinkedHashMap<>();

  CarrotDocument(Object solrDocumentId) {
    this.solrDocumentId = Objects.requireNonNull(solrDocumentId);
  }

  @Override
  public void visitFields(BiConsumer<String, String> fieldConsumer) {
    clusteredFields.forEach(fieldConsumer);
  }

  public Object getSolrDocumentId() {
    return solrDocumentId;
  }

  public void addClusteredField(String fieldName, String fieldValue) {
    assert !clusteredFields.containsKey(fieldName);
    clusteredFields.put(fieldName, fieldValue);
  }
}
