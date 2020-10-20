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

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchComponent;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test {@link ClusteringComponent}.
 */
@Ignore
public class ClusteringComponentTest extends AbstractClusteringTestCase {
  @Before
  public void doBefore() {
    clearIndex();
  }

  @Test
  public void testComponentExists() {
    SolrCore core = h.getCore();

    SearchComponent sc = core.getSearchComponent("clustering");
    assertTrue("sc is null and it shouldn't be", sc != null);
    ModifiableSolrParams params = new ModifiableSolrParams();

    params.add(ClusteringComponent.COMPONENT_NAME, "true");
    params.add(CommonParams.Q, "*:*");

    SolrRequestHandler handler = core.getRequestHandler("/select");
    SolrQueryResponse rsp;
    rsp = new SolrQueryResponse();
    rsp.addResponseHeader(new SimpleOrderedMap<>());
    try (SolrQueryRequest req = new LocalSolrQueryRequest(core, params)) {
      handler.handleRequest(req, rsp);
      NamedList<?> values = rsp.getValues();
      Object clusters = values.get("clusters");
      assertTrue("The returned clusters must not be null.", clusters != null);
    }
  }
}
