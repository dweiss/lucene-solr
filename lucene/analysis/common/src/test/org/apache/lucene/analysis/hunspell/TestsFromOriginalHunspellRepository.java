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
package org.apache.lucene.analysis.hunspell;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Same as {@link SpellCheckerTest}, but checks all Hunspell's test data. The path to the checked
 * out Hunspell repository should be in {@code -Dhunspell.repo.path=...} system property.
 */
@RunWith(Parameterized.class)
public class TestsFromOriginalHunspellRepository {
  private final Path pathPrefix;

  public TestsFromOriginalHunspellRepository(String testName, Path pathPrefix) {
    this.pathPrefix = pathPrefix;
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() throws IOException {
    String hunspellRepo = System.getProperty("hunspell.repo.path");
    if (hunspellRepo == null) {
      throw new AssumptionViolatedException("hunspell.repo.path property not specified.");
    }

    Set<String> names = new TreeSet<>();
    Path tests = Path.of(hunspellRepo).resolve("tests");
    try (DirectoryStream<Path> files = Files.newDirectoryStream(tests)) {
      for (Path file : files) {
        String name = file.getFileName().toString();
        if (name.endsWith(".aff")) {
          names.add(name.substring(0, name.length() - 4));
        }
      }
    }

    return names.stream().map(s -> new Object[] {s, tests.resolve(s)}).collect(Collectors.toList());
  }

  @Test
  public void test() throws IOException, ParseException {
    SpellCheckerTest.checkSpellCheckerExpectations(pathPrefix);
  }
}
