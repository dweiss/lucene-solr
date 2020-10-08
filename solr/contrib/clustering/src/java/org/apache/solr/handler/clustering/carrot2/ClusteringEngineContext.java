package org.apache.solr.handler.clustering.carrot2;

import org.carrot2.clustering.ClusteringAlgorithm;
import org.carrot2.clustering.ClusteringAlgorithmProvider;
import org.carrot2.clustering.kmeans.BisectingKMeansClusteringAlgorithm;
import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.clustering.stc.STCClusteringAlgorithm;
import org.carrot2.language.LanguageComponents;
import org.carrot2.language.LanguageComponentsLoader;
import org.carrot2.language.LoadedLanguages;
import org.carrot2.util.ChainedResourceLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class ClusteringEngineContext {
  private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final LinkedHashMap<String, LanguageComponents> languages;
  private final Map<String, ClusteringAlgorithmProvider> algorithmProviders;

  private final static Map<String, String> aliasedNames;

  static {
    aliasedNames = new HashMap<>();
    aliasedNames.put(LingoClusteringAlgorithm.class.getName(), LingoClusteringAlgorithm.NAME);
    aliasedNames.put(STCClusteringAlgorithm.class.getName(), STCClusteringAlgorithm.NAME);
    aliasedNames.put(BisectingKMeansClusteringAlgorithm.class.getName(), BisectingKMeansClusteringAlgorithm.NAME);
  }

  ClusteringEngineContext() {
    LanguageComponentsLoader loader = LanguageComponents.loader();

    List<Path> resourceLocations = Collections.emptyList();
    if (!resourceLocations.isEmpty()) {
      logger.info(
          "Clustering algorithm resources first looked up relative to: {}", resourceLocations);

      loader.withResourceLookup(
          (provider) ->
              new ChainedResourceLookup(
                  Arrays.asList(
                      new PathResourceLookup(resourceLocations),
                      provider.defaultResourceLookup())));
    } else {
      logger.info("Resources read from defaults (JARs).");
    }

    ClassLoader classLoader = getClass().getClassLoader();
    algorithmProviders =
        ServiceLoader.load(ClusteringAlgorithmProvider.class, classLoader)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toMap(ClusteringAlgorithmProvider::name, e -> e));

    // Only load the resources of algorithms we're interested in.
    loader.limitToAlgorithms(
        algorithmProviders.values().stream()
            .map(Supplier::get)
            .toArray(ClusteringAlgorithm[]::new));

    languages = new LinkedHashMap<>();
    try {
      LoadedLanguages loadedLanguages = loader.load();
      for (String lang : loadedLanguages.languages()) {
        languages.put(lang, loadedLanguages.language(lang));
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }

    // Debug info about loaded languages.
    if (logger.isDebugEnabled()) {
      for (String lang : languages.keySet()) {
        logger.trace(
            "Loaded language '"
                + lang
                + "' with components: "
                + "\n  - "
                + languages.get(lang).components().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining("\n  - ")));
      }
    }

    // Remove algorithms for which there are no languages that are supported.
    algorithmProviders
        .entrySet()
        .removeIf(e -> !isAlgorithmAvailable(e.getValue(), languages.values()));

    algorithmProviders.forEach(
        (name, prov) -> {
          String supportedLanguages =
              languages.values().stream()
                  .filter(lc -> prov.get().supports(lc))
                  .map(LanguageComponents::language)
                  .collect(Collectors.joining(", "));

          logger.info(
              "Clustering algorithm {} loaded with support for the following languages: {}",
              name,
              supportedLanguages);
        });

    logger.info("Available clustering algorithms: " +
        algorithmProviders.keySet().stream().collect(Collectors.joining(", ")));
    logger.info("Available languages: " +
        languages.keySet().stream().collect(Collectors.joining(", ")));
  }

  public ClusteringAlgorithm getAlgorithm(String algorithmName) {
    if (!algorithmProviders.containsKey(algorithmName)
        && aliasedNames.containsKey(algorithmName)) {
      algorithmName = aliasedNames.get(algorithmName);
    }

    ClusteringAlgorithmProvider provider = algorithmProviders.get(algorithmName);
    return provider == null ? null : provider.get();
  }

  public LanguageComponents getLanguage(String language) {
    if (!languages.containsKey(language)) {
      throw new RuntimeException("Language not available: " + language);
    }
    return languages.get(language);
  }

  private boolean isAlgorithmAvailable(
      ClusteringAlgorithmProvider provider, Collection<LanguageComponents> languages) {
    ClusteringAlgorithm algorithm = provider.get();
    Optional<LanguageComponents> first = languages.stream().filter(algorithm::supports).findFirst();
    if (first.isEmpty()) {
      logger.warn("Algorithm does not support any of the available languages: {}", provider.name());
      return false;
    } else {
      return true;
    }
  }
}