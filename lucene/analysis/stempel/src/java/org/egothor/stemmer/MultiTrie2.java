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
import java.util.ArrayList;
import java.util.List;

/**
 * The MultiTrie is a Trie of Tries.
 *
 * <p>It stores words and their associated patch commands. The MultiTrie handles patch commands
 * broken into their constituent parts, as a MultiTrie does, but the commands are delimited by the
 * skip command.
 */
public class MultiTrie2 extends MultiTrie {
  /**
   * Constructor for the MultiTrie object.
   *
   * @param is the input stream
   * @exception IOException if an I/O error occurs
   */
  public MultiTrie2(DataInput is) throws IOException {
    super(is);
  }

  /**
   * Constructor for the MultiTrie2 object
   *
   * @param forward set to <code>true</code> if the elements should be read left to right
   */
  public MultiTrie2(boolean forward) {
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
    try {
      CharSequence lastkey = key;
      CharSequence p[] = new CharSequence[tries.size()];
      char lastch = ' ';
      for (int i = 0; i < tries.size(); i++) {
        CharSequence r = tries.get(i).getFully(lastkey);
        if (r == null || (r.length() == 1 && r.charAt(0) == EOM)) {
          return result;
        }
        if (cannotFollow(lastch, r.charAt(0))) {
          return result;
        } else {
          lastch = r.charAt(r.length() - 2);
        }
        // key=key.substring(lengthPP(r));
        p[i] = r;
        if (p[i].charAt(0) == '-') {
          if (i > 0) {
            key = skip(key, lengthPP(p[i - 1]));
          }
          key = skip(key, lengthPP(p[i]));
        }
        // key = skip(key, lengthPP(r));
        result.append(r);
        if (key.length() != 0) {
          lastkey = key;
        }
      }
    } catch (IndexOutOfBoundsException x) {
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
    try {
      CharSequence lastkey = key;
      CharSequence p[] = new CharSequence[tries.size()];
      char lastch = ' ';
      for (int i = 0; i < tries.size(); i++) {
        CharSequence r = tries.get(i).getLastOnPath(lastkey);
        if (r == null || (r.length() == 1 && r.charAt(0) == EOM)) {
          return result;
        }
        // System.err.println("LP:"+key+" last:"+lastch+" new:"+r);
        if (cannotFollow(lastch, r.charAt(0))) {
          return result;
        } else {
          lastch = r.charAt(r.length() - 2);
        }
        // key=key.substring(lengthPP(r));
        p[i] = r;
        if (p[i].charAt(0) == '-') {
          if (i > 0) {
            key = skip(key, lengthPP(p[i - 1]));
          }
          key = skip(key, lengthPP(p[i]));
        }
        // key = skip(key, lengthPP(r));
        result.append(r);
        if (key.length() != 0) {
          lastkey = key;
        }
      }
    } catch (IndexOutOfBoundsException x) {
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
    super.store(os);
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
    // System.err.println( cmd );
    CharSequence p[] = decompose(cmd);
    int levels = p.length;
    // System.err.println("levels "+key+" cmd "+cmd+"|"+levels);
    while (levels >= tries.size()) {
      tries.add(new Trie(forward));
    }
    CharSequence lastkey = key;
    for (int i = 0; i < levels; i++) {
      if (key.length() > 0) {
        tries.get(i).add(key, p[i]);
        lastkey = key;
      } else {
        tries.get(i).add(lastkey, p[i]);
      }
      // System.err.println("-"+key+" "+p[i]+"|"+key.length());
      /*
       * key=key.substring(lengthPP(p[i]));
       */
      if (p[i].length() > 0 && p[i].charAt(0) == '-') {
        if (i > 0) {
          key = skip(key, lengthPP(p[i - 1]));
        }
        key = skip(key, lengthPP(p[i]));
      }
      // System.err.println("--->"+key);
    }
    if (key.length() > 0) {
      tries.get(levels).add(key, EOM_NODE);
    } else {
      tries.get(levels).add(lastkey, EOM_NODE);
    }
  }

  /**
   * Break the given patch command into its constituent pieces. The pieces are delimited by NOOP
   * commands.
   *
   * @param cmd the patch command
   * @return an array containing the pieces of the command
   */
  public CharSequence[] decompose(CharSequence cmd) {
    int parts = 0;

    for (int i = 0; 0 <= i && i < cmd.length(); ) {
      int next = dashEven(cmd, i);
      if (i == next) {
        parts++;
        i = next + 2;
      } else {
        parts++;
        i = next;
      }
    }

    CharSequence part[] = new CharSequence[parts];
    int x = 0;

    for (int i = 0; 0 <= i && i < cmd.length(); ) {
      int next = dashEven(cmd, i);
      if (i == next) {
        part[x++] = cmd.subSequence(i, i + 2);
        i = next + 2;
      } else {
        part[x++] = (next < 0) ? cmd.subSequence(i, cmd.length()) : cmd.subSequence(i, next);
        i = next;
      }
    }
    return part;
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

    MultiTrie2 m = new MultiTrie2(forward);
    m.tries = h;
    return m;
  }

  private boolean cannotFollow(char after, char goes) {
    switch (after) {
      case '-':
      case 'D':
        return after == goes;
    }
    return false;
  }

  private CharSequence skip(CharSequence in, int count) {
    if (forward) {
      return in.subSequence(count, in.length());
    } else {
      return in.subSequence(0, in.length() - count);
    }
  }

  private int dashEven(CharSequence in, int from) {
    while (from < in.length()) {
      if (in.charAt(from) == '-') {
        return from;
      } else {
        from += 2;
      }
    }
    return -1;
  }

  @SuppressWarnings("fallthrough")
  private int lengthPP(CharSequence cmd) {
    int len = 0;
    for (int i = 0; i < cmd.length(); i++) {
      switch (cmd.charAt(i++)) {
        case '-':
        case 'D':
          len += cmd.charAt(i) - 'a' + 1;
          break;
        case 'R':
          len++; /* intentional fallthrough */
        case 'I':
          break;
      }
    }
    return len;
  }
}
