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

package org.tartarus.snowball.ext;

import org.tartarus.snowball.Among;

/**
 * This class implements the stemming algorithm defined by a snowball script.
 *
 * <p>Generated by Snowball 2.0.0 - https://snowballstem.org/
 */
@SuppressWarnings("unused")
public class SwedishStemmer extends org.tartarus.snowball.SnowballStemmer {

  private static final long serialVersionUID = 1L;
  private static final java.lang.invoke.MethodHandles.Lookup methodObject =
      java.lang.invoke.MethodHandles.lookup();

  private static final Among a_0[] = {
    new Among("a", -1, 1),
    new Among("arna", 0, 1),
    new Among("erna", 0, 1),
    new Among("heterna", 2, 1),
    new Among("orna", 0, 1),
    new Among("ad", -1, 1),
    new Among("e", -1, 1),
    new Among("ade", 6, 1),
    new Among("ande", 6, 1),
    new Among("arne", 6, 1),
    new Among("are", 6, 1),
    new Among("aste", 6, 1),
    new Among("en", -1, 1),
    new Among("anden", 12, 1),
    new Among("aren", 12, 1),
    new Among("heten", 12, 1),
    new Among("ern", -1, 1),
    new Among("ar", -1, 1),
    new Among("er", -1, 1),
    new Among("heter", 18, 1),
    new Among("or", -1, 1),
    new Among("s", -1, 2),
    new Among("as", 21, 1),
    new Among("arnas", 22, 1),
    new Among("ernas", 22, 1),
    new Among("ornas", 22, 1),
    new Among("es", 21, 1),
    new Among("ades", 26, 1),
    new Among("andes", 26, 1),
    new Among("ens", 21, 1),
    new Among("arens", 29, 1),
    new Among("hetens", 29, 1),
    new Among("erns", 21, 1),
    new Among("at", -1, 1),
    new Among("andet", -1, 1),
    new Among("het", -1, 1),
    new Among("ast", -1, 1)
  };

  private static final Among a_1[] = {
    new Among("dd", -1, -1),
    new Among("gd", -1, -1),
    new Among("nn", -1, -1),
    new Among("dt", -1, -1),
    new Among("gt", -1, -1),
    new Among("kt", -1, -1),
    new Among("tt", -1, -1)
  };

  private static final Among a_2[] = {
    new Among("ig", -1, 1),
    new Among("lig", 0, 1),
    new Among("els", -1, 1),
    new Among("fullt", -1, 3),
    new Among("l\u00F6st", -1, 2)
  };

  private static final char g_v[] = {17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 24, 0, 32};

  private static final char g_s_ending[] = {119, 127, 149};

  private int I_x;
  private int I_p1;

  private boolean r_mark_regions() {
    I_p1 = limit;
    int v_1 = cursor;
    {
      int c = cursor + 3;
      if (0 > c || c > limit) {
        return false;
      }
      cursor = c;
    }
    I_x = cursor;
    cursor = v_1;
    golab0:
    while (true) {
      int v_2 = cursor;
      lab1:
      {
        if (!(in_grouping(g_v, 97, 246))) {
          break lab1;
        }
        cursor = v_2;
        break golab0;
      }
      cursor = v_2;
      if (cursor >= limit) {
        return false;
      }
      cursor++;
    }
    golab2:
    while (true) {
      lab3:
      {
        if (!(out_grouping(g_v, 97, 246))) {
          break lab3;
        }
        break golab2;
      }
      if (cursor >= limit) {
        return false;
      }
      cursor++;
    }
    I_p1 = cursor;
    lab4:
    {
      if (!(I_p1 < I_x)) {
        break lab4;
      }
      I_p1 = I_x;
    }
    return true;
  }

  private boolean r_main_suffix() {
    int among_var;
    if (cursor < I_p1) {
      return false;
    }
    int v_2 = limit_backward;
    limit_backward = I_p1;
    ket = cursor;
    among_var = find_among_b(a_0);
    if (among_var == 0) {
      limit_backward = v_2;
      return false;
    }
    bra = cursor;
    limit_backward = v_2;
    switch (among_var) {
      case 1:
        slice_del();
        break;
      case 2:
        if (!(in_grouping_b(g_s_ending, 98, 121))) {
          return false;
        }
        slice_del();
        break;
    }
    return true;
  }

  private boolean r_consonant_pair() {
    if (cursor < I_p1) {
      return false;
    }
    int v_2 = limit_backward;
    limit_backward = I_p1;
    int v_3 = limit - cursor;
    if (find_among_b(a_1) == 0) {
      limit_backward = v_2;
      return false;
    }
    cursor = limit - v_3;
    ket = cursor;
    if (cursor <= limit_backward) {
      limit_backward = v_2;
      return false;
    }
    cursor--;
    bra = cursor;
    slice_del();
    limit_backward = v_2;
    return true;
  }

  private boolean r_other_suffix() {
    int among_var;
    if (cursor < I_p1) {
      return false;
    }
    int v_2 = limit_backward;
    limit_backward = I_p1;
    ket = cursor;
    among_var = find_among_b(a_2);
    if (among_var == 0) {
      limit_backward = v_2;
      return false;
    }
    bra = cursor;
    switch (among_var) {
      case 1:
        slice_del();
        break;
      case 2:
        slice_from("l\u00F6s");
        break;
      case 3:
        slice_from("full");
        break;
    }
    limit_backward = v_2;
    return true;
  }

  public boolean stem() {
    int v_1 = cursor;
    r_mark_regions();
    cursor = v_1;
    limit_backward = cursor;
    cursor = limit;
    int v_2 = limit - cursor;
    r_main_suffix();
    cursor = limit - v_2;
    int v_3 = limit - cursor;
    r_consonant_pair();
    cursor = limit - v_3;
    int v_4 = limit - cursor;
    r_other_suffix();
    cursor = limit - v_4;
    cursor = limit_backward;
    return true;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof SwedishStemmer;
  }

  @Override
  public int hashCode() {
    return SwedishStemmer.class.getName().hashCode();
  }
}
