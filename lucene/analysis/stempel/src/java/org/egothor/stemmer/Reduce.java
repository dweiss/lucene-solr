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

/** The Reduce object is used to remove gaps in a Trie which stores a dictionary. */
public class Reduce {

  /** Constructor for the Reduce object. */
  public Reduce() {}

  /**
   * Optimize (remove holes in the rows) the given Trie and return the restructured Trie.
   *
   * @param orig the Trie to optimize
   * @return the restructured Trie
   */
  public Trie optimize(Trie orig) {
    List<CharSequence> cmds = orig.cmds;
    List<Row> rows = new ArrayList<>();
    List<Row> orows = orig.rows;
    int remap[] = new int[orows.size()];

    Arrays.fill(remap, -1);
    rows = removeGaps(orig.root, rows, new ArrayList<Row>(), remap);

    return new Trie(orig.forward, remap[orig.root], cmds, rows);
  }

  List<Row> removeGaps(int ind, List<Row> old, List<Row> to, int remap[]) {
    remap[ind] = to.size();

    Row now = old.get(ind);
    to.add(now);
    Iterator<Cell> i = now.cells.values().iterator();
    for (; i.hasNext(); ) {
      Cell c = i.next();
      if (c.ref >= 0 && remap[c.ref] < 0) {
        removeGaps(c.ref, old, to, remap);
      }
    }
    to.set(remap[ind], new Remap(now, remap));
    return to;
  }

  /** This class is part of the Egothor Project */
  class Remap extends Row {
    /**
     * Constructor for the Remap object
     *
     * @param old Description of the Parameter
     * @param remap Description of the Parameter
     */
    public Remap(Row old, int remap[]) {
      super();
      Iterator<Character> i = old.cells.keySet().iterator();
      for (; i.hasNext(); ) {
        Character ch = i.next();
        Cell c = old.at(ch);
        Cell nc;
        if (c.ref >= 0) {
          nc = new Cell(c);
          nc.ref = remap[nc.ref];
        } else {
          nc = new Cell(c);
        }
        cells.put(ch, nc);
      }
    }
  }
}
