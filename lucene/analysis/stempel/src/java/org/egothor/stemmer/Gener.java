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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * The Gener object helps in the discarding of nodes which break the reduction effort and defend the
 * structure against large reductions.
 */
public class Gener extends Reduce {
  /** Constructor for the Gener object. */
  public Gener() {}

  /**
   * Return a Trie with infrequent values occurring in the given Trie removed.
   *
   * @param orig the Trie to optimize
   * @return a new optimized Trie
   */
  @Override
  public Trie optimize(Trie orig) {
    List<CharSequence> cmds = orig.cmds;
    List<Row> rows = new ArrayList<>();
    List<Row> orows = orig.rows;
    int remap[] = new int[orows.size()];

    Arrays.fill(remap, 1);
    for (int j = orows.size() - 1; j >= 0; j--) {
      if (eat(orows.get(j), remap)) {
        remap[j] = 0;
      }
    }

    Arrays.fill(remap, -1);
    rows = removeGaps(orig.root, orows, new ArrayList<Row>(), remap);

    return new Trie(orig.forward, remap[orig.root], cmds, rows);
  }

  /**
   * Test whether the given Row of Cells in a Trie should be included in an optimized Trie.
   *
   * @param in the Row to test
   * @param remap Description of the Parameter
   * @return <code>true</code> if the Row should remain, <code>false
   *      </code> otherwise
   */
  public boolean eat(Row in, int remap[]) {
    int sum = 0;
    for (Iterator<Cell> i = in.cells.values().iterator(); i.hasNext(); ) {
      Cell c = i.next();
      sum += c.cnt;
      if (c.ref >= 0) {
        if (remap[c.ref] == 0) {
          c.ref = -1;
        }
      }
    }
    int frame = sum / 10;
    boolean live = false;
    for (Iterator<Cell> i = in.cells.values().iterator(); i.hasNext(); ) {
      Cell c = i.next();
      if (c.cnt < frame && c.cmd >= 0) {
        c.cnt = 0;
        c.cmd = -1;
      }
      if (c.cmd >= 0 || c.ref >= 0) {
        live |= true;
      }
    }
    return !live;
  }
}
