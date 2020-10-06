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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * The MultiTrie is a Trie of Tries. It stores words and their associated patch commands. The
 * MultiTrie handles patch commands individually (each command by itself).
 */
public class MultiTrie extends Trie {
  final char EOM = '*';
  final String EOM_NODE = "" + EOM;

  List<Trie> tries = new ArrayList<>();

  int BY = 1;

  /**
   * Constructor for the MultiTrie object.
   *
   * @param is the input stream
   * @exception IOException if an I/O error occurs
   */
  public MultiTrie(DataInput is) throws IOException {
    super(false);
    forward = is.readBoolean();
    BY = is.readInt();
    for (int i = is.readInt(); i > 0; i--) {
      tries.add(new Trie(is));
    }
  }

  /**
   * Constructor for the MultiTrie object
   *
   * @param forward set to <code>true</code> if the elements should be read left to right
   */
  public MultiTrie(boolean forward) {
    super(forward);
  }

  /**
   * Return the element that is stored in a cell associated with the given key.
   *
   * @param key the key to the cell holding the desired element
   * @return the element
   */
  @Override
  public CharSequence getFully(CharSequence key) {
    StringBuilder result = new StringBuilder(tries.size() * 2);
    for (int i = 0; i < tries.size(); i++) {
      CharSequence r = tries.get(i).getFully(key);
      if (r == null || (r.length() == 1 && r.charAt(0) == EOM)) {
        return result;
      }
      result.append(r);
    }
    return result;
  }

  /**
   * Return the element that is stored as last on a path belonging to the given key.
   *
   * @param key the key associated with the desired element
   * @return the element that is stored as last on a path
   */
  @Override
  public CharSequence getLastOnPath(CharSequence key) {
    StringBuilder result = new StringBuilder(tries.size() * 2);
    for (int i = 0; i < tries.size(); i++) {
      CharSequence r = tries.get(i).getLastOnPath(key);
      if (r == null || (r.length() == 1 && r.charAt(0) == EOM)) {
        return result;
      }
      result.append(r);
    }
    return result;
  }

  /**
   * Write this data structure to the given output stream.
   *
   * @param os the output stream
   * @exception IOException if an I/O error occurs
   */
  @Override
  public void store(DataOutput os) throws IOException {
    os.writeBoolean(forward);
    os.writeInt(BY);
    os.writeInt(tries.size());
    for (Trie trie : tries) trie.store(os);
  }

  /**
   * Add an element to this structure consisting of the given key and patch command.
   *
   * <p>This method will return without executing if the <code>cmd</code> parameter's length is 0.
   *
   * @param key the key
   * @param cmd the patch command
   */
  @Override
  public void add(CharSequence key, CharSequence cmd) {
    if (cmd.length() == 0) {
      return;
    }
    int levels = cmd.length() / BY;
    while (levels >= tries.size()) {
      tries.add(new Trie(forward));
    }
    for (int i = 0; i < levels; i++) {
      tries.get(i).add(key, cmd.subSequence(BY * i, BY * i + BY));
    }
    tries.get(levels).add(key, EOM_NODE);
  }

  /**
   * Remove empty rows from the given Trie and return the newly reduced Trie.
   *
   * @param by the Trie to reduce
   * @return the newly reduced Trie
   */
  @Override
  public Trie reduce(Reduce by) {
    List<Trie> h = new ArrayList<>();
    for (Trie trie : tries) h.add(trie.reduce(by));

    MultiTrie m = new MultiTrie(forward);
    m.tries = h;
    return m;
  }

  /**
   * Print the given prefix and the position(s) in the Trie where it appears.
   *
   * @param prefix the desired prefix
   */
  @Override
  public void printInfo(PrintStream out, CharSequence prefix) {
    int c = 0;
    for (Trie trie : tries) trie.printInfo(out, prefix + "[" + (++c) + "] ");
  }
}
