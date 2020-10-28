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

import org.apache.solr.BaseDistributedSearchTestCase;
import org.apache.solr.SolrTestCaseJ4.SuppressSSL;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CommonParams;
import org.junit.Assert;
import org.junit.Test;

@SuppressSSL
public class ClusteringComponentDistributedTest extends BaseDistributedSearchTestCase {
  @Override
  public String getSolrHome() {
    return getFile("clustering/solr/collection1").getParent();
  }

  @Test
  @ShardsFixed(num = 2)
  public void testDistributedRequest() throws Exception {
    del("*:*");
    int numberOfDocs = 0;
    for (String[] doc : AbstractClusteringTestCase.DOCUMENTS) {
      index(id, Integer.toString(numberOfDocs++), "title", doc[0], "snippet", doc[1]);
    }
    commit();

    handle.clear();
    handle.put("responseHeader", SKIP);
    handle.put("response", SKIP);

    QueryResponse response = query(
        ClusteringComponent.COMPONENT_NAME, "true",
        ClusteringComponent.REQUEST_PARAM_ENGINE, "lingo",
        CommonParams.Q, "*:*",
        CommonParams.SORT, id + " desc");

    Object clusters = response.getResponse().get(ClusteringComponent.RESPONSE_SECTION_CLUSTERS);
    Assert.assertNotNull(clusters);
    Assert.assertTrue(response.getClusteringResponse().getClusters().size() > 0);
  }
}
