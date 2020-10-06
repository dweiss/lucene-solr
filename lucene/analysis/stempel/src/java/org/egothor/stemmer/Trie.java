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
 * A Trie is used to store a dictionary of words and their stems.
 *
 * <p>Actually, what is stored are words with their respective patch commands. A trie can be termed
 * forward (keys read from left to right) or backward (keys read from right to left). This property
 * will vary depending on the language for which a Trie is constructed.
 */
public class Trie {
  List<Row> rows = new ArrayList<>();
  List<CharSequence> cmds = new ArrayList<>();
  int root;

  boolean forward = false;

  /**
   * Constructor for the Trie object.
   *
   * @param is the input stream
   * @exception IOException if an I/O error occurs
   */
  public Trie(DataInput is) throws IOException {
    forward = is.readBoolean();
    root = is.readInt();
    for (int i = is.readInt(); i > 0; i--) {
      cmds.add(is.readUTF());
    }
    for (int i = is.readInt(); i > 0; i--) {
      rows.add(new Row(is));
    }
  }

  /**
   * Constructor for the Trie object.
   *
   * @param forward set to <code>true</code>
   */
  public Trie(boolean forward) {
    rows.add(new Row());
    root = 0;
    this.forward = forward;
  }

  /**
   * Constructor for the Trie object.
   *
   * @param forward <code>true</code> if read left to right, <code>false</code> if read right to
   *     left
   * @param root index of the row that is the root node
   * @param cmds the patch commands to store
   * @param rows a Vector of Vectors. Each inner Vector is a node of this Trie
   */
  public Trie(boolean forward, int root, List<CharSequence> cmds, List<Row> rows) {
    this.rows = rows;
    this.cmds = cmds;
    this.root = root;
    this.forward = forward;
  }

  /**
   * Gets the all attribute of the Trie object
   *
   * @param key Description of the Parameter
   * @return The all value
   */
  public CharSequence[] getAll(CharSequence key) {
    int res[] = new int[key.length()];
    int resc = 0;
    Row now = getRow(root);
    int w;
    StrEnum e = new StrEnum(key, forward);
    boolean br = false;

    for (int i = 0; i < key.length() - 1; i++) {
      Character ch = e.next();
      w = now.getCmd(ch);
      if (w >= 0) {
        int n = w;
        for (int j = 0; j < resc; j++) {
          if (n == res[j]) {
            n = -1;
            break;
          }
        }
        if (n >= 0) {
          res[resc++] = n;
        }
      }
      w = now.getRef(ch);
      if (w >= 0) {
        now = getRow(w);
      } else {
        br = true;
        break;
      }
    }
    if (br == false) {
      w = now.getCmd(e.next());
      if (w >= 0) {
        int n = w;
        for (int j = 0; j < resc; j++) {
          if (n == res[j]) {
            n = -1;
            break;
          }
        }
        if (n >= 0) {
          res[resc++] = n;
        }
      }
    }

    if (resc < 1) {
      return null;
    }
    CharSequence R[] = new CharSequence[resc];
    for (int j = 0; j < resc; j++) {
      R[j] = cmds.get(res[j]);
    }
    return R;
  }

  /**
   * Return the number of cells in this Trie object.
   *
   * @return the number of cells
   */
  public int getCells() {
    int size = 0;
    for (Row row : rows) size += row.getCells();
    return size;
  }

  /**
   * Gets the cellsPnt attribute of the Trie object
   *
   * @return The cellsPnt value
   */
  public int getCellsPnt() {
    int size = 0;
    for (Row row : rows) size += row.getCellsPnt();
    return size;
  }

  /**
   * Gets the cellsVal attribute of the Trie object
   *
   * @return The cellsVal value
   */
  public int getCellsVal() {
    int size = 0;
    for (Row row : rows) size += row.getCellsVal();
    return size;
  }

  /**
   * Return the element that is stored in a cell associated with the given key.
   *
   * @param key the key
   * @return the associated element
   */
  public CharSequence getFully(CharSequence key) {
    Row now = getRow(root);
    int w;
    Cell c;
    int cmd = -1;
    StrEnum e = new StrEnum(key, forward);
    Character ch = null;
    Character aux = null;

    for (int i = 0; i < key.length(); ) {
      ch = e.next();
      i++;

      c = now.at(ch);
      if (c == null) {
        return null;
      }

      cmd = c.cmd;

      for (int skip = c.skip; skip > 0; skip--) {
        if (i < key.length()) {
          aux = e.next();
        } else {
          return null;
        }
        i++;
      }

      w = now.getRef(ch);
      if (w >= 0) {
        now = getRow(w);
      } else if (i < key.length()) {
        return null;
      }
    }
    return (cmd == -1) ? null : cmds.get(cmd);
  }

  /**
   * Return the element that is stored as last on a path associated with the given key.
   *
   * @param key the key associated with the desired element
   * @return the last on path element
   */
  public CharSequence getLastOnPath(CharSequence key) {
    Row now = getRow(root);
    int w;
    CharSequence last = null;
    StrEnum e = new StrEnum(key, forward);

    for (int i = 0; i < key.length() - 1; i++) {
      Character ch = e.next();
      w = now.getCmd(ch);
      if (w >= 0) {
        last = cmds.get(w);
      }
      w = now.getRef(ch);
      if (w >= 0) {
        now = getRow(w);
      } else {
        return last;
      }
    }
    w = now.getCmd(e.next());
    return (w >= 0) ? cmds.get(w) : last;
  }

  /**
   * Return the Row at the given index.
   *
   * @param index the index containing the desired Row
   * @return the Row
   */
  private Row getRow(int index) {
    if (index < 0 || index >= rows.size()) {
      return null;
    }
    return rows.get(index);
  }

  /**
   * Write this Trie to the given output stream.
   *
   * @param os the output stream
   * @exception IOException if an I/O error occurs
   */
  public void store(DataOutput os) throws IOException {
    os.writeBoolean(forward);
    os.writeInt(root);
    os.writeInt(cmds.size());
    for (CharSequence cmd : cmds) os.writeUTF(cmd.toString());

    os.writeInt(rows.size());
    for (Row row : rows) row.store(os);
  }

  /**
   * Add the given key associated with the given patch command. If either parameter is null this
   * method will return without executing.
   *
   * @param key the key
   * @param cmd the patch command
   */
  void add(CharSequence key, CharSequence cmd) {
    if (key == null || cmd == null) {
      return;
    }
    if (cmd.length() == 0) {
      return;
    }
    int id_cmd = cmds.indexOf(cmd);
    if (id_cmd == -1) {
      id_cmd = cmds.size();
      cmds.add(cmd);
    }

    int node = root;
    Row r = getRow(node);

    StrEnum e = new StrEnum(key, forward);

    for (int i = 0; i < e.length() - 1; i++) {
      Character ch = e.next();
      node = r.getRef(ch);
      if (node >= 0) {
        r = getRow(node);
      } else {
        node = rows.size();
        Row n;
        rows.add(n = new Row());
        r.setRef(ch, node);
        r = n;
      }
    }
    r.setCmd(e.next(), id_cmd);
  }

  /**
   * Remove empty rows from the given Trie and return the newly reduced Trie.
   *
   * @param by the Trie to reduce
   * @return the newly reduced Trie
   */
  public Trie reduce(Reduce by) {
    return by.optimize(this);
  }

  /** writes debugging info to the printstream */
  public void printInfo(PrintStream out, CharSequence prefix) {
    out.println(
        prefix
            + "nds "
            + rows.size()
            + " cmds "
            + cmds.size()
            + " cells "
            + getCells()
            + " valcells "
            + getCellsVal()
            + " pntcells "
            + getCellsPnt());
  }

  /** This class is part of the Egothor Project */
  class StrEnum {
    CharSequence s;
    int from;
    int by;

    /**
     * Constructor for the StrEnum object
     *
     * @param s Description of the Parameter
     * @param up Description of the Parameter
     */
    StrEnum(CharSequence s, boolean up) {
      this.s = s;
      if (up) {
        from = 0;
        by = 1;
      } else {
        from = s.length() - 1;
        by = -1;
      }
    }

    int length() {
      return s.length();
    }

    char next() {
      char ch = s.charAt(from);
      from += by;
      return ch;
    }
  }
}
