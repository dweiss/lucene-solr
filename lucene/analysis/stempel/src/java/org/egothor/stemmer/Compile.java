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
package org.egothor.stemmer;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.StringTokenizer;
import org.apache.lucene.util.SuppressForbidden;

/** The Compile class is used to compile a stemmer table. */
public class Compile {

  static boolean backward;
  static boolean multi;
  static Trie trie;

  /** no instantiation */
  private Compile() {}

  /**
   * Entry point to the Compile application.
   *
   * <p>This program takes any number of arguments: the first is the name of the desired stemming
   * algorithm to use (a list is available in the package description) , all of the rest should be
   * the path or paths to a file or files containing a stemmer table to compile.
   *
   * @param args the command line arguments
   */
  @SuppressForbidden(reason = "System.out required: command line tool")
  public static void main(java.lang.String[] args) throws Exception {
    if (args.length < 1) {
      return;
    }

    args[0].toUpperCase(Locale.ROOT);

    backward = args[0].charAt(0) == '-';
    int qq = (backward) ? 1 : 0;
    boolean storeorig = false;

    if (args[0].charAt(qq) == '0') {
      storeorig = true;
      qq++;
    }

    multi = args[0].charAt(qq) == 'M';
    if (multi) {
      qq++;
    }

    String charset = System.getProperty("egothor.stemmer.charset", "UTF-8");

    char optimizer[] = new char[args[0].length() - qq];
    for (int i = 0; i < optimizer.length; i++) {
      optimizer[i] = args[0].charAt(qq + i);
    }

    for (int i = 1; i < args.length; i++) {
      // System.out.println("[" + args[i] + "]");
      Diff diff = new Diff();

      allocTrie();

      System.out.println(args[i]);
      try (LineNumberReader in =
          new LineNumberReader(
              Files.newBufferedReader(Paths.get(args[i]), Charset.forName(charset)))) {
        for (String line = in.readLine(); line != null; line = in.readLine()) {
          try {
            line = line.toLowerCase(Locale.ROOT);
            StringTokenizer st = new StringTokenizer(line);
            String stem = st.nextToken();
            if (storeorig) {
              trie.add(stem, "-a");
            }
            while (st.hasMoreTokens()) {
              String token = st.nextToken();
              if (token.equals(stem) == false) {
                trie.add(token, diff.exec(token, stem));
              }
            }
          } catch (java.util.NoSuchElementException x) {
            // no base token (stem) on a line
          }
        }
      }

      Optimizer o = new Optimizer();
      Optimizer2 o2 = new Optimizer2();
      Lift l = new Lift(true);
      Lift e = new Lift(false);
      Gener g = new Gener();

      for (int j = 0; j < optimizer.length; j++) {
        String prefix;
        switch (optimizer[j]) {
          case 'G':
            trie = trie.reduce(g);
            prefix = "G: ";
            break;
          case 'L':
            trie = trie.reduce(l);
            prefix = "L: ";
            break;
          case 'E':
            trie = trie.reduce(e);
            prefix = "E: ";
            break;
          case '2':
            trie = trie.reduce(o2);
            prefix = "2: ";
            break;
          case '1':
            trie = trie.reduce(o);
            prefix = "1: ";
            break;
          default:
            continue;
        }
        trie.printInfo(System.out, prefix + " ");
      }

      try (DataOutputStream os =
          new DataOutputStream(
              new BufferedOutputStream(Files.newOutputStream(Paths.get(args[i] + ".out"))))) {
        os.writeUTF(args[0]);
        trie.store(os);
      }
    }
  }

  static void allocTrie() {
    if (multi) {
      trie = new MultiTrie2(!backward);
    } else {
      trie = new Trie(!backward);
    }
  }
}
