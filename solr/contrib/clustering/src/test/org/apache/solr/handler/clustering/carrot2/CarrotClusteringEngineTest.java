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

import com.carrotsearch.randomizedtesting.RandomizedContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.clustering.AbstractClusteringTestCase;
import org.apache.solr.handler.clustering.ClusteringComponent;
import org.apache.solr.handler.clustering.ClusteringEngine;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.search.DocList;
import org.carrot2.clustering.Cluster;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests {@link CarrotClusteringEngine}.
 */
public class CarrotClusteringEngineTest extends AbstractClusteringTestCase {
  @Test
  public void testLingoAlgorithm() throws Exception {
    compareToExpected(clusters(getClusteringEngine("lingo"), new MatchAllDocsQuery()));
  }

  @Test
  public void testStcAlgorithm() throws Exception {
    compareToExpected(clusters(getClusteringEngine("stc"), new MatchAllDocsQuery()));
  }

  @Test
  public void testKMeansAlgorithm() throws Exception {
    compareToExpected(clusters(getClusteringEngine("kmeans"), new MatchAllDocsQuery()));
  }

  @Test
  public void testParamSubClusters() throws Exception {
    compareToExpected("off", clusters(getClusteringEngine("mock"), new MatchAllDocsQuery(), params -> {
      params.set(EngineConfiguration.PARAM_INCLUDE_SUBCLUSTERS, false);
    }));
    compareToExpected("on", clusters(getClusteringEngine("mock"), new MatchAllDocsQuery(), params -> {
      params.set(EngineConfiguration.PARAM_INCLUDE_SUBCLUSTERS, true);
    }));
  }

  @Test
  public void testParamOtherTopics() throws Exception {
    compareToExpected(clusters(getClusteringEngine("mock"), new MatchAllDocsQuery(), params -> {
      params.set(EngineConfiguration.PARAM_INCLUDE_OTHER_TOPICS, false);
    }));
  }

  /**
   * We'll make two queries, one with- and another one without summary
   * and assert that documents are shorter when highlighter is in use.
   */
  @Test
  public void testClusteringOnHighlights() throws Exception {
    Query query = new TermQuery(new Term("snippet", "mine"));

    Consumer<ModifiableSolrParams> common = params -> {
      params.add(EngineConfiguration.SNIPPET_FIELD_NAME, "snippet");
      params.add(EngineConfiguration.SUMMARY_FRAGSIZE, Integer.toString(80));
      params.add(EngineConfiguration.SUMMARY_SNIPPETS, Integer.toString(1));
    };

    List<Cluster<SolrDocument>> highlighted = clusters(getClusteringEngine("echo"), query,
        common.andThen(params -> {
          params.add(EngineConfiguration.PRODUCE_SUMMARY, "true");
        }));

    List<Cluster<SolrDocument>> full = clusters(getClusteringEngine("echo"), query,
        common.andThen(params -> {
          params.add(EngineConfiguration.PRODUCE_SUMMARY, "false");
        }));

    // Echo clustering algorithm just returns document fields as cluster labels
    // so highlighted snippets should never be longer than full field content.
    Assert.assertEquals(highlighted.size(), full.size());
    for (int i = 0; i < highlighted.size(); i++) {
      List<String> labels1 = highlighted.get(i).getLabels();
      List<String> labels2 = full.get(i).getLabels();
      assertEquals(labels1.size(), labels2.size());
      for (int j = 0; j < labels1.size(); j++) {
        assertThat("Summary shorter than original document?",
            labels1.get(j).length(),
            Matchers.lessThanOrEqualTo(labels2.get(j).length()));
      }
    }
  }

  /**
   * We'll make two queries, one short summaries and another one with longer
   * summaries and will check that the results differ.
   */
  @Test
  public void testSummaryFragSize() throws Exception {
    Query query = new TermQuery(new Term("snippet", "mine"));

    Consumer<ModifiableSolrParams> common = params -> {
      params.add(EngineConfiguration.PRODUCE_SUMMARY, "true");
      params.add(EngineConfiguration.SNIPPET_FIELD_NAME, "snippet");
      params.add(EngineConfiguration.SUMMARY_SNIPPETS, Integer.toString(1));
    };

    List<Cluster<SolrDocument>> shortSummaries = clusters(getClusteringEngine("echo"), query,
        common.andThen(params -> {
          params.add(EngineConfiguration.SUMMARY_FRAGSIZE, Integer.toString(30));
        }));

    List<Cluster<SolrDocument>> longSummaries = clusters(getClusteringEngine("echo"), query,
        common.andThen(params -> {
          params.add(EngineConfiguration.SUMMARY_FRAGSIZE, Integer.toString(80));
        }));

    Assert.assertEquals(shortSummaries.size(), longSummaries.size());
    for (int i = 0; i < shortSummaries.size(); i++) {
      List<String> shortLabels = shortSummaries.get(i).getLabels();
      List<String> longLabels = longSummaries.get(i).getLabels();
      assertEquals(shortLabels.size(), longLabels.size());
      for (int j = 0; j < shortLabels.size(); j++) {
        assertThat("Shorter summary is longer than longer summary?",
            shortLabels.get(j).length(),
            Matchers.lessThanOrEqualTo(longLabels.get(j).length()));
      }
    }
  }

  /**
   * Test passing algorithm parameters via SolrParams.
   */
  @Test
  public void testPassingAttributes() throws Exception {
    compareToExpected(clusters(getClusteringEngine("mock"), new MatchAllDocsQuery(), params -> {
      params.set("maxClusters", 2);
      params.set("hierarchyDepth", 1);
      params.add(EngineConfiguration.PARAM_INCLUDE_OTHER_TOPICS, "false");
    }));
  }

  /**
   * Test passing algorithm parameters via Solr configuration file.
   */
  @Test
  public void testPassingAttributesViaSolrConfig() throws Exception {
    compareToExpected(clusters(getClusteringEngine("mock-solrconfig-attrs"), new MatchAllDocsQuery()));
  }

  /**
   * Test maximum label truncation.
   */
  @Test
  public void testParamMaxLabels() throws Exception {
    List<Cluster<SolrDocument>> clusters = clusters(getClusteringEngine("mock"), new MatchAllDocsQuery(), params -> {
      params.set("labelsPerCluster", "5");
      params.set(EngineConfiguration.PARAM_INCLUDE_OTHER_TOPICS, "false");
      params.set(EngineConfiguration.PARAM_MAX_LABELS, "3");
    });

    clusters.forEach(c -> {
      assertThat(c.getLabels(), Matchers.hasSize(3));
    });
  }

  /**
   * Verify the engine ordering is respected.
   */
  @Test
  public void testDefaultEngineOrder() throws IOException {
    ClusteringComponent comp = (ClusteringComponent) h.getCore().getSearchComponent("testDefaultEngineOrder");
    Map<String, ClusteringEngine> engines = getSearchClusteringEngines(comp);
    assertEquals(
        Arrays.asList("stc", "default", "mock"),
        new ArrayList<>(engines.keySet()));

    compareToExpected(clusters(engines.get(
        ClusteringComponent.DEFAULT_ENGINE_NAME), new MatchAllDocsQuery()));
  }

  @Test
  public void testCustomLanguageResources() throws Exception {
    compareToExpected(clusters(
        getClusteringEngine("testCustomLanguageResources"),
        new MatchAllDocsQuery()));
  }

  @Test
  public void testParamDefaultLanguage() throws Exception {
    compareToExpected(clusters(
        getClusteringEngine("testParamDefaultLanguage"),
        new MatchAllDocsQuery()));
  }

/*
  @Test
  public void testSolrStopWordsUsedInCarrot2Clustering() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("merge-resources", false);
    params.set(AttributeUtils.getKey(
        LexicalResourcesCheckClusteringAlgorithm.class, "wordsToCheck"),
    "online,solrownstopword");

    // "solrownstopword" is in stopwords.txt, so we're expecting
    // only one cluster with label "online".
    final List<NamedList<Object>> clusters = checkEngine(
        getClusteringEngine("lexical-resource-check"), 1, params);
    assertEquals(getLabels(clusters.get(0)), Collections.singletonList("online"));
  }

  @Test
  public void testSolrStopWordsNotDefinedOnAFieldForClustering() throws Exception {
    ModifiableSolrParams params = new ModifiableSolrParams();
    // Force string fields to be used for clustering. Does not make sense
    // in a real word, but does the job in the test.
    params.set(CarrotParams.TITLE_FIELD_NAME, "url");
    params.set(CarrotParams.SNIPPET_FIELD_NAME, "url");
    params.set("merge-resources", false);
    params.set(AttributeUtils.getKey(
        LexicalResourcesCheckClusteringAlgorithm.class, "wordsToCheck"),
    "online,solrownstopword");

    final List<NamedList<Object>> clusters = checkEngine(
        getClusteringEngine("lexical-resource-check"), 2, params);
    assertEquals(Collections.singletonList("online"), getLabels(clusters.get(0)));
    assertEquals(Collections.singletonList("solrownstopword"), getLabels(clusters.get(1)));
  }
  
  @Test
  public void testHighlightingOfMultiValueField() throws Exception {
    final String snippetWithoutSummary = getLabels(clusterWithHighlighting(
        false, 30, 3, "multi", 1).get(0)).get(1);
    assertTrue("Snippet contains first value", snippetWithoutSummary.contains("First"));
    assertTrue("Snippet contains second value", snippetWithoutSummary.contains("Second"));
    assertTrue("Snippet contains third value", snippetWithoutSummary.contains("Third"));

    final String snippetWithSummary = getLabels(clusterWithHighlighting(
        true, 30, 3, "multi", 1).get(0)).get(1);
    assertTrue("Snippet with summary shorter than full snippet",
        snippetWithoutSummary.length() > snippetWithSummary.length());
    assertTrue("Summary covers first value", snippetWithSummary.contains("First"));
    assertTrue("Summary covers second value", snippetWithSummary.contains("Second"));
    assertTrue("Summary covers third value", snippetWithSummary.contains("Third"));
  }
  
  @Test
  public void testConcatenatingMultipleFields() throws Exception {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.TITLE_FIELD_NAME, "title,heading");
    params.add(CarrotParams.SNIPPET_FIELD_NAME, "snippet,body");

    final List<String> labels = getLabels(checkEngine(
        getClusteringEngine("echo"), 1, 1, new TermQuery(new Term("body",
            "snippet")), params).get(0));
    assertTrue("Snippet contains third value", labels.get(0).contains("Title field"));
    assertTrue("Snippet contains third value", labels.get(0).contains("Heading field"));
    assertTrue("Snippet contains third value", labels.get(1).contains("Snippet field"));
    assertTrue("Snippet contains third value", labels.get(1).contains("Body field"));
  }

  @Test
  public void testHighlightingMultipleFields() throws Exception {
    final TermQuery query = new TermQuery(new Term("snippet", "content"));

    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.TITLE_FIELD_NAME, "title,heading");
    params.add(CarrotParams.SNIPPET_FIELD_NAME, "snippet,body");
    params.add(CarrotParams.PRODUCE_SUMMARY, Boolean.toString(false));
    
    final String snippetWithoutSummary = getLabels(checkEngine(
        getClusteringEngine("echo"), 1, 1, query, params).get(0)).get(1);
    assertTrue("Snippet covers snippet field", snippetWithoutSummary.contains("snippet field"));
    assertTrue("Snippet covers body field", snippetWithoutSummary.contains("body field"));

    params.set(CarrotParams.PRODUCE_SUMMARY, Boolean.toString(true));
    params.add(CarrotParams.SUMMARY_FRAGSIZE, Integer.toString(30));
    params.add(CarrotParams.SUMMARY_SNIPPETS, Integer.toString(2));
    final String snippetWithSummary = getLabels(checkEngine(
        getClusteringEngine("echo"), 1, 1, query, params).get(0)).get(1);    
    assertTrue("Snippet with summary shorter than full snippet",
        snippetWithoutSummary.length() > snippetWithSummary.length());
    assertTrue("Snippet covers snippet field", snippetWithSummary.contains("snippet field"));
    assertTrue("Snippet covers body field", snippetWithSummary.contains("body field"));

  }

  @Test
  public void testOneCarrot2SupportedLanguage() throws Exception {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.LANGUAGE_FIELD_NAME, "lang");

    final List<String> labels = getLabels(checkEngine(
        getClusteringEngine("echo"), 1, 1, new TermQuery(new Term("url",
            "one_supported_language")), params).get(0));
    assertEquals(3, labels.size());
    assertEquals("Correct Carrot2 language", LanguageCode.CHINESE_SIMPLIFIED.name(), labels.get(2));
  }
  
  @Test
  public void testOneCarrot2SupportedLanguageOfMany() throws Exception {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.LANGUAGE_FIELD_NAME, "lang");
    
    final List<String> labels = getLabels(checkEngine(
        getClusteringEngine("echo"), 1, 1, new TermQuery(new Term("url",
            "one_supported_language_of_many")), params).get(0));
    assertEquals(3, labels.size());
    assertEquals("Correct Carrot2 language", LanguageCode.GERMAN.name(), labels.get(2));
  }
  
  @Test
  public void testLanguageCodeMapping() throws Exception {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.LANGUAGE_FIELD_NAME, "lang");
    params.add(CarrotParams.LANGUAGE_CODE_MAP, "POLISH:pl");
    
    final List<String> labels = getLabels(checkEngine(
        getClusteringEngine("echo"), 1, 1, new TermQuery(new Term("url",
            "one_supported_language_of_many")), params).get(0));
    assertEquals(3, labels.size());
    assertEquals("Correct Carrot2 language", LanguageCode.POLISH.name(), labels.get(2));
  }
  
  @Test
  public void testPassingOfCustomFields() throws Exception {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.CUSTOM_FIELD_NAME, "intfield_i:intfield");
    params.add(CarrotParams.CUSTOM_FIELD_NAME, "floatfield_f:floatfield");
    params.add(CarrotParams.CUSTOM_FIELD_NAME, "heading:multi");
    
    // Let the echo mock clustering algorithm know which custom field to echo
    params.add("custom-fields", "intfield,floatfield,multi");
    
    final List<String> labels = getLabels(checkEngine(
        getClusteringEngine("echo"), 1, 1, new TermQuery(new Term("url",
            "custom_fields")), params).get(0));
    assertEquals(5, labels.size());
    assertEquals("Integer field", "10", labels.get(2));
    assertEquals("Float field", "10.5", labels.get(3));
    assertEquals("List field", "[first, second]", labels.get(4));
  }

  @Test
  public void testCustomTokenizer() throws Exception {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.TITLE_FIELD_NAME, "title");
    params.add(CarrotParams.SNIPPET_FIELD_NAME, "snippet");

    final List<String> labels = getLabels(checkEngine(
        getClusteringEngine("custom-duplicating-tokenizer"), 1, 15, new TermQuery(new Term("title",
            "field")), params).get(0));
    
    // The custom test tokenizer duplicates each token's text
    assertTrue("First token", labels.get(0).contains("TitleTitle"));
  }
  
  @Test
  public void testCustomStemmer() throws Exception {
    final ModifiableSolrParams params = new ModifiableSolrParams();
    params.add(CarrotParams.TITLE_FIELD_NAME, "title");
    params.add(CarrotParams.SNIPPET_FIELD_NAME, "snippet");
    
    final List<String> labels = getLabels(checkEngine(
        getClusteringEngine("custom-duplicating-stemmer"), 1, 12, new TermQuery(new Term("title",
            "field")), params).get(0));
    
    // The custom test stemmer duplicates and lowercases each token's text
    assertTrue("First token", labels.get(0).contains("titletitle"));
  }
*/


  private CarrotClusteringEngine getClusteringEngine(String engineName) {
    ClusteringComponent comp = (ClusteringComponent) h.getCore()
        .getSearchComponent("clustering");
    assertNotNull("Clustering component should not be null", comp);
    CarrotClusteringEngine engine =
        (CarrotClusteringEngine) getSearchClusteringEngines(comp).get(engineName);
    assertNotNull("Clustering engine for name: " + engineName
        + " should not be null", engine);
    return engine;
  }

  private void compareToExpected(List<Cluster<SolrDocument>> clusters) throws IOException {
    compareToExpected("", clusters);
  }

  private void compareToExpected(String expectedResourceSuffix,
                                 List<Cluster<SolrDocument>> clusters) throws IOException {
    RandomizedContext ctx = RandomizedContext.current();
    String resourceName = String.format(Locale.ROOT,
        "%s-%s%s.txt",
        ctx.getTargetClass().getSimpleName(),
        ctx.getTargetMethod().getName(),
        expectedResourceSuffix.isEmpty() ? "" : "-" + expectedResourceSuffix);

    try (InputStream is = getClass().getResourceAsStream(resourceName)) {
      if (is == null) {
        throw new AssertionError("Test resource not found: " + resourceName + " (class-relative to " +
            getClass().getName() + ")");
      }

      String expected = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      String actual = toString(clusters);

      Function<String, String> normalize = v -> {
        return v.replaceAll("\r", "").replaceAll("[ \t]+", " ").trim();
      };

      if (!normalize.apply(expected).equals(normalize.apply(actual))) {
        throw new AssertionError(String.format(Locale.ROOT,
            "The actual clusters structure differs from the expected one. Expected:\n%s\n\nActual:\n%s",
            expected,
            actual));
      }
    }
  }

  private String toString(List<Cluster<SolrDocument>> clusters) {
    return toString(clusters, "", new StringBuilder()).toString();
  }

  private StringBuilder toString(List<Cluster<SolrDocument>> clusters, String indent, StringBuilder sb) {
    clusters.forEach(c -> {
      sb.append(indent);
      sb.append("- " + c.getLabels().stream().collect(Collectors.joining("; ")));
      if (!c.getDocuments().isEmpty()) {
        sb.append(" [" + c.getDocuments().size() + "]");
      }
      sb.append("\n");

      if (!c.getClusters().isEmpty()) {
        toString(c.getClusters(), indent + "  ", sb);
      }
    });
    return sb;
  }

  private List<Cluster<SolrDocument>> clusters(ClusteringEngine engine, Query query) throws IOException {
    return clusters(engine, query, params -> {
    });
  }

  private List<Cluster<SolrDocument>> clusters(ClusteringEngine engine, Query query, Consumer<ModifiableSolrParams> params)
      throws IOException {
    return h.getCore().withSearcher(searcher -> {
      DocList docList = searcher.getDocList(query, (Query) null, new Sort(), 0,
          numberOfTestDocs);

      ModifiableSolrParams solrParams = new ModifiableSolrParams();
      params.accept(solrParams);

      try (LocalSolrQueryRequest req = new LocalSolrQueryRequest(h.getCore(), solrParams)) {
        Map<SolrDocument, Integer> docToId = new HashMap<>();
        SolrDocumentList solrDocList =
            ClusteringComponent.docListToSolrDocumentList(docList, searcher, engine.getFieldsToLoad(req), docToId);

        List<NamedList<Object>> results = engine.cluster(query, solrDocList, docToId, req);

        Map<String, SolrDocument> idToDoc = new HashMap<>();
        docToId.forEach((doc, id) -> idToDoc.put(id.toString(), doc));

        return results.stream().map(c -> toCluster(c, idToDoc)).collect(Collectors.toList());
      }
    });
  }

  @SuppressWarnings("unchecked")
  private Cluster<SolrDocument> toCluster(NamedList<Object> v, Map<String, SolrDocument> idToDoc) {
    Cluster<SolrDocument> c = new Cluster<>();
    v.forEach((key, value) -> {
      switch (key) {
        case "docs":
          ((List<String>) value).forEach(docId -> c.addDocument(idToDoc.get(docId)));
          break;
        case "labels":
          ((List<String>) value).forEach(c::addLabel);
          break;
        case "score":
          c.setScore(((Number) value).doubleValue());
          break;
        case "clusters":
          ((List<NamedList<Object>>) value).forEach(sub -> {
            c.addCluster(toCluster(sub, idToDoc));
          });
          break;
        case "other-topics":
          // Just ignore the attribute.
          break;
        default:
          throw new RuntimeException("Unknown output property " + key + " in cluster: " + v.jsonStr());
      }
    });
    return c;
  }
}
