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
package org.apache.solr.client.solrj.embedded;

import org.apache.solr.client.solrj.LargeVolumeTestBase;
import org.junit.BeforeClass;

/**
 * @see org.apache.solr.client.solrj.impl.BinaryRequestWriter
 * @see org.apache.solr.client.solrj.request.JavaBinUpdateRequestCodec
 */
// commented out on: 24-Dec-2018
// @LuceneTestCase.BadApple(bugUrl="https://issues.apache.org/jira/browse/SOLR-12028") //
// 14-Oct-2018
public class LargeVolumeBinaryJettyTest extends LargeVolumeTestBase {
  @BeforeClass
  public static void beforeTest() throws Exception {
    initCore();
  }
}
