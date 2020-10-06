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

/**
 * The Optimizer class is a Trie that will be reduced (have empty rows removed).
 *
 * <p>This is the result of allowing a joining of rows when there is no collision between non-<code>
 * null</code> values in the rows. Information loss, resulting in the stemmer not being able to
 * recognize words (as in Optimizer), is curtailed, allowing the stemmer to recognize words for
 * which the original trie was built. Use of this class allows the stemmer to be self-teaching.
 */
public class Optimizer2 extends Optimizer {
  /** Constructor for the Optimizer2 object. */
  public Optimizer2() {}

  /**
   * Merge the given Cells and return the resulting Cell.
   *
   * @param m the master Cell
   * @param e the existing Cell
   * @return the resulting Cell, or <code>null</code> if the operation cannot be realized
   */
  @Override
  public Cell merge(Cell m, Cell e) {
    if (m.cmd == e.cmd && m.ref == e.ref && m.skip == e.skip) {
      Cell c = new Cell(m);
      c.cnt += e.cnt;
      return c;
    } else {
      return null;
    }
  }
}
