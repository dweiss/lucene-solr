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

import org.carrot2.attrs.AttrComposite;
import org.carrot2.clustering.Cluster;
import org.carrot2.clustering.ClusteringAlgorithm;
import org.carrot2.clustering.Document;
import org.carrot2.language.LanguageComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MockClusteringAlgorithm extends AttrComposite implements ClusteringAlgorithm {
  // @IntRange(min = 1, max = 5)
  public int depth = 2;

  // @IntRange(min = 1, max = 5)
  public int labels = 1;

  //@IntRange(min = 0)
  public int maxClusters = 0;

  @Override
  public boolean supports(LanguageComponents languageComponents) {
    return true;
  }

  @Override
  public Set<Class<?>> requiredLanguageComponents() {
    return Collections.emptySet();
  }

  @Override
  public <T extends Document> List<Cluster<T>> cluster(Stream<? extends T> documentStream, LanguageComponents languageComponents) {
    List<T> documents = documentStream.collect(Collectors.toList());
    List<Cluster<T>> clusters = new ArrayList<>();

    if (maxClusters > 0) {
      documents = documents.subList(0, maxClusters);
    }

    int documentIndex = 1;
    for (T document : documents) {
      StringBuilder label = new StringBuilder("Cluster " + documentIndex);
      Cluster<T> cluster = createCluster(label.toString(), documentIndex, document);
      clusters.add(cluster);
      for (int i = 1; i <= depth; i++) {
        label.append(".");
        label.append(i);
        Cluster<T> newCluster = createCluster(label.toString(), documentIndex, document);
        cluster.addCluster(newCluster);
        cluster = newCluster;
      }
      documentIndex++;
    }

    return clusters;
  }

  private <T extends Document> Cluster<T> createCluster(String labelBase, int documentIndex, T document) {
    Cluster<T> cluster = new Cluster<T>();
    cluster.setScore(documentIndex * 0.25);

    for (int i = 0; i < labels; i++) {
      cluster.addLabel(labelBase + "#" + (i + 1));
    }

    cluster.addDocument(document);

    return cluster;
  }
}
