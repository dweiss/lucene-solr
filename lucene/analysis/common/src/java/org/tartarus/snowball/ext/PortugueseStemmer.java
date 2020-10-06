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
public class PortugueseStemmer extends org.tartarus.snowball.SnowballStemmer {

  private static final long serialVersionUID = 1L;
  private static final java.lang.invoke.MethodHandles.Lookup methodObject =
      java.lang.invoke.MethodHandles.lookup();

  private static final Among a_0[] = {
    new Among("", -1, 3), new Among("\u00E3", 0, 1), new Among("\u00F5", 0, 2)
  };

  private static final Among a_1[] = {
    new Among("", -1, 3), new Among("a~", 0, 1), new Among("o~", 0, 2)
  };

  private static final Among a_2[] = {
    new Among("ic", -1, -1),
    new Among("ad", -1, -1),
    new Among("os", -1, -1),
    new Among("iv", -1, 1)
  };

  private static final Among a_3[] = {
    new Among("ante", -1, 1), new Among("avel", -1, 1), new Among("\u00EDvel", -1, 1)
  };

  private static final Among a_4[] = {
    new Among("ic", -1, 1), new Among("abil", -1, 1), new Among("iv", -1, 1)
  };

  private static final Among a_5[] = {
    new Among("ica", -1, 1),
    new Among("\u00E2ncia", -1, 1),
    new Among("\u00EAncia", -1, 4),
    new Among("logia", -1, 2),
    new Among("ira", -1, 9),
    new Among("adora", -1, 1),
    new Among("osa", -1, 1),
    new Among("ista", -1, 1),
    new Among("iva", -1, 8),
    new Among("eza", -1, 1),
    new Among("idade", -1, 7),
    new Among("ante", -1, 1),
    new Among("mente", -1, 6),
    new Among("amente", 12, 5),
    new Among("\u00E1vel", -1, 1),
    new Among("\u00EDvel", -1, 1),
    new Among("ico", -1, 1),
    new Among("ismo", -1, 1),
    new Among("oso", -1, 1),
    new Among("amento", -1, 1),
    new Among("imento", -1, 1),
    new Among("ivo", -1, 8),
    new Among("a\u00E7a~o", -1, 1),
    new Among("u\u00E7a~o", -1, 3),
    new Among("ador", -1, 1),
    new Among("icas", -1, 1),
    new Among("\u00EAncias", -1, 4),
    new Among("logias", -1, 2),
    new Among("iras", -1, 9),
    new Among("adoras", -1, 1),
    new Among("osas", -1, 1),
    new Among("istas", -1, 1),
    new Among("ivas", -1, 8),
    new Among("ezas", -1, 1),
    new Among("idades", -1, 7),
    new Among("adores", -1, 1),
    new Among("antes", -1, 1),
    new Among("a\u00E7o~es", -1, 1),
    new Among("u\u00E7o~es", -1, 3),
    new Among("icos", -1, 1),
    new Among("ismos", -1, 1),
    new Among("osos", -1, 1),
    new Among("amentos", -1, 1),
    new Among("imentos", -1, 1),
    new Among("ivos", -1, 8)
  };

  private static final Among a_6[] = {
    new Among("ada", -1, 1),
    new Among("ida", -1, 1),
    new Among("ia", -1, 1),
    new Among("aria", 2, 1),
    new Among("eria", 2, 1),
    new Among("iria", 2, 1),
    new Among("ara", -1, 1),
    new Among("era", -1, 1),
    new Among("ira", -1, 1),
    new Among("ava", -1, 1),
    new Among("asse", -1, 1),
    new Among("esse", -1, 1),
    new Among("isse", -1, 1),
    new Among("aste", -1, 1),
    new Among("este", -1, 1),
    new Among("iste", -1, 1),
    new Among("ei", -1, 1),
    new Among("arei", 16, 1),
    new Among("erei", 16, 1),
    new Among("irei", 16, 1),
    new Among("am", -1, 1),
    new Among("iam", 20, 1),
    new Among("ariam", 21, 1),
    new Among("eriam", 21, 1),
    new Among("iriam", 21, 1),
    new Among("aram", 20, 1),
    new Among("eram", 20, 1),
    new Among("iram", 20, 1),
    new Among("avam", 20, 1),
    new Among("em", -1, 1),
    new Among("arem", 29, 1),
    new Among("erem", 29, 1),
    new Among("irem", 29, 1),
    new Among("assem", 29, 1),
    new Among("essem", 29, 1),
    new Among("issem", 29, 1),
    new Among("ado", -1, 1),
    new Among("ido", -1, 1),
    new Among("ando", -1, 1),
    new Among("endo", -1, 1),
    new Among("indo", -1, 1),
    new Among("ara~o", -1, 1),
    new Among("era~o", -1, 1),
    new Among("ira~o", -1, 1),
    new Among("ar", -1, 1),
    new Among("er", -1, 1),
    new Among("ir", -1, 1),
    new Among("as", -1, 1),
    new Among("adas", 47, 1),
    new Among("idas", 47, 1),
    new Among("ias", 47, 1),
    new Among("arias", 50, 1),
    new Among("erias", 50, 1),
    new Among("irias", 50, 1),
    new Among("aras", 47, 1),
    new Among("eras", 47, 1),
    new Among("iras", 47, 1),
    new Among("avas", 47, 1),
    new Among("es", -1, 1),
    new Among("ardes", 58, 1),
    new Among("erdes", 58, 1),
    new Among("irdes", 58, 1),
    new Among("ares", 58, 1),
    new Among("eres", 58, 1),
    new Among("ires", 58, 1),
    new Among("asses", 58, 1),
    new Among("esses", 58, 1),
    new Among("isses", 58, 1),
    new Among("astes", 58, 1),
    new Among("estes", 58, 1),
    new Among("istes", 58, 1),
    new Among("is", -1, 1),
    new Among("ais", 71, 1),
    new Among("eis", 71, 1),
    new Among("areis", 73, 1),
    new Among("ereis", 73, 1),
    new Among("ireis", 73, 1),
    new Among("\u00E1reis", 73, 1),
    new Among("\u00E9reis", 73, 1),
    new Among("\u00EDreis", 73, 1),
    new Among("\u00E1sseis", 73, 1),
    new Among("\u00E9sseis", 73, 1),
    new Among("\u00EDsseis", 73, 1),
    new Among("\u00E1veis", 73, 1),
    new Among("\u00EDeis", 73, 1),
    new Among("ar\u00EDeis", 84, 1),
    new Among("er\u00EDeis", 84, 1),
    new Among("ir\u00EDeis", 84, 1),
    new Among("ados", -1, 1),
    new Among("idos", -1, 1),
    new Among("amos", -1, 1),
    new Among("\u00E1ramos", 90, 1),
    new Among("\u00E9ramos", 90, 1),
    new Among("\u00EDramos", 90, 1),
    new Among("\u00E1vamos", 90, 1),
    new Among("\u00EDamos", 90, 1),
    new Among("ar\u00EDamos", 95, 1),
    new Among("er\u00EDamos", 95, 1),
    new Among("ir\u00EDamos", 95, 1),
    new Among("emos", -1, 1),
    new Among("aremos", 99, 1),
    new Among("eremos", 99, 1),
    new Among("iremos", 99, 1),
    new Among("\u00E1ssemos", 99, 1),
    new Among("\u00EAssemos", 99, 1),
    new Among("\u00EDssemos", 99, 1),
    new Among("imos", -1, 1),
    new Among("armos", -1, 1),
    new Among("ermos", -1, 1),
    new Among("irmos", -1, 1),
    new Among("\u00E1mos", -1, 1),
    new Among("ar\u00E1s", -1, 1),
    new Among("er\u00E1s", -1, 1),
    new Among("ir\u00E1s", -1, 1),
    new Among("eu", -1, 1),
    new Among("iu", -1, 1),
    new Among("ou", -1, 1),
    new Among("ar\u00E1", -1, 1),
    new Among("er\u00E1", -1, 1),
    new Among("ir\u00E1", -1, 1)
  };

  private static final Among a_7[] = {
    new Among("a", -1, 1),
    new Among("i", -1, 1),
    new Among("o", -1, 1),
    new Among("os", -1, 1),
    new Among("\u00E1", -1, 1),
    new Among("\u00ED", -1, 1),
    new Among("\u00F3", -1, 1)
  };

  private static final Among a_8[] = {
    new Among("e", -1, 1),
    new Among("\u00E7", -1, 2),
    new Among("\u00E9", -1, 1),
    new Among("\u00EA", -1, 1)
  };

  private static final char g_v[] = {
    17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 19, 12, 2
  };

  private int I_p2;
  private int I_p1;
  private int I_pV;

  private boolean r_prelude() {
    int among_var;
    while (true) {
      int v_1 = cursor;
      lab0:
      {
        bra = cursor;
        among_var = find_among(a_0);
        if (among_var == 0) {
          break lab0;
        }
        ket = cursor;
        switch (among_var) {
          case 1:
            slice_from("a~");
            break;
          case 2:
            slice_from("o~");
            break;
          case 3:
            if (cursor >= limit) {
              break lab0;
            }
            cursor++;
            break;
        }
        continue;
      }
      cursor = v_1;
      break;
    }
    return true;
  }

  private boolean r_mark_regions() {
    I_pV = limit;
    I_p1 = limit;
    I_p2 = limit;
    int v_1 = cursor;
    lab0:
    {
      lab1:
      {
        int v_2 = cursor;
        lab2:
        {
          if (!(in_grouping(g_v, 97, 250))) {
            break lab2;
          }
          lab3:
          {
            int v_3 = cursor;
            lab4:
            {
              if (!(out_grouping(g_v, 97, 250))) {
                break lab4;
              }
              golab5:
              while (true) {
                lab6:
                {
                  if (!(in_grouping(g_v, 97, 250))) {
                    break lab6;
                  }
                  break golab5;
                }
                if (cursor >= limit) {
                  break lab4;
                }
                cursor++;
              }
              break lab3;
            }
            cursor = v_3;
            if (!(in_grouping(g_v, 97, 250))) {
              break lab2;
            }
            golab7:
            while (true) {
              lab8:
              {
                if (!(out_grouping(g_v, 97, 250))) {
                  break lab8;
                }
                break golab7;
              }
              if (cursor >= limit) {
                break lab2;
              }
              cursor++;
            }
          }
          break lab1;
        }
        cursor = v_2;
        if (!(out_grouping(g_v, 97, 250))) {
          break lab0;
        }
        lab9:
        {
          int v_6 = cursor;
          lab10:
          {
            if (!(out_grouping(g_v, 97, 250))) {
              break lab10;
            }
            golab11:
            while (true) {
              lab12:
              {
                if (!(in_grouping(g_v, 97, 250))) {
                  break lab12;
                }
                break golab11;
              }
              if (cursor >= limit) {
                break lab10;
              }
              cursor++;
            }
            break lab9;
          }
          cursor = v_6;
          if (!(in_grouping(g_v, 97, 250))) {
            break lab0;
          }
          if (cursor >= limit) {
            break lab0;
          }
          cursor++;
        }
      }
      I_pV = cursor;
    }
    cursor = v_1;
    int v_8 = cursor;
    lab13:
    {
      golab14:
      while (true) {
        lab15:
        {
          if (!(in_grouping(g_v, 97, 250))) {
            break lab15;
          }
          break golab14;
        }
        if (cursor >= limit) {
          break lab13;
        }
        cursor++;
      }
      golab16:
      while (true) {
        lab17:
        {
          if (!(out_grouping(g_v, 97, 250))) {
            break lab17;
          }
          break golab16;
        }
        if (cursor >= limit) {
          break lab13;
        }
        cursor++;
      }
      I_p1 = cursor;
      golab18:
      while (true) {
        lab19:
        {
          if (!(in_grouping(g_v, 97, 250))) {
            break lab19;
          }
          break golab18;
        }
        if (cursor >= limit) {
          break lab13;
        }
        cursor++;
      }
      golab20:
      while (true) {
        lab21:
        {
          if (!(out_grouping(g_v, 97, 250))) {
            break lab21;
          }
          break golab20;
        }
        if (cursor >= limit) {
          break lab13;
        }
        cursor++;
      }
      I_p2 = cursor;
    }
    cursor = v_8;
    return true;
  }

  private boolean r_postlude() {
    int among_var;
    while (true) {
      int v_1 = cursor;
      lab0:
      {
        bra = cursor;
        among_var = find_among(a_1);
        if (among_var == 0) {
          break lab0;
        }
        ket = cursor;
        switch (among_var) {
          case 1:
            slice_from("\u00E3");
            break;
          case 2:
            slice_from("\u00F5");
            break;
          case 3:
            if (cursor >= limit) {
              break lab0;
            }
            cursor++;
            break;
        }
        continue;
      }
      cursor = v_1;
      break;
    }
    return true;
  }

  private boolean r_RV() {
    if (!(I_pV <= cursor)) {
      return false;
    }
    return true;
  }

  private boolean r_R1() {
    if (!(I_p1 <= cursor)) {
      return false;
    }
    return true;
  }

  private boolean r_R2() {
    if (!(I_p2 <= cursor)) {
      return false;
    }
    return true;
  }

  private boolean r_standard_suffix() {
    int among_var;
    ket = cursor;
    among_var = find_among_b(a_5);
    if (among_var == 0) {
      return false;
    }
    bra = cursor;
    switch (among_var) {
      case 1:
        if (!r_R2()) {
          return false;
        }
        slice_del();
        break;
      case 2:
        if (!r_R2()) {
          return false;
        }
        slice_from("log");
        break;
      case 3:
        if (!r_R2()) {
          return false;
        }
        slice_from("u");
        break;
      case 4:
        if (!r_R2()) {
          return false;
        }
        slice_from("ente");
        break;
      case 5:
        if (!r_R1()) {
          return false;
        }
        slice_del();
        int v_1 = limit - cursor;
        lab0:
        {
          ket = cursor;
          among_var = find_among_b(a_2);
          if (among_var == 0) {
            cursor = limit - v_1;
            break lab0;
          }
          bra = cursor;
          if (!r_R2()) {
            cursor = limit - v_1;
            break lab0;
          }
          slice_del();
          switch (among_var) {
            case 1:
              ket = cursor;
              if (!(eq_s_b("at"))) {
                cursor = limit - v_1;
                break lab0;
              }
              bra = cursor;
              if (!r_R2()) {
                cursor = limit - v_1;
                break lab0;
              }
              slice_del();
              break;
          }
        }
        break;
      case 6:
        if (!r_R2()) {
          return false;
        }
        slice_del();
        int v_2 = limit - cursor;
        lab1:
        {
          ket = cursor;
          if (find_among_b(a_3) == 0) {
            cursor = limit - v_2;
            break lab1;
          }
          bra = cursor;
          if (!r_R2()) {
            cursor = limit - v_2;
            break lab1;
          }
          slice_del();
        }
        break;
      case 7:
        if (!r_R2()) {
          return false;
        }
        slice_del();
        int v_3 = limit - cursor;
        lab2:
        {
          ket = cursor;
          if (find_among_b(a_4) == 0) {
            cursor = limit - v_3;
            break lab2;
          }
          bra = cursor;
          if (!r_R2()) {
            cursor = limit - v_3;
            break lab2;
          }
          slice_del();
        }
        break;
      case 8:
        if (!r_R2()) {
          return false;
        }
        slice_del();
        int v_4 = limit - cursor;
        lab3:
        {
          ket = cursor;
          if (!(eq_s_b("at"))) {
            cursor = limit - v_4;
            break lab3;
          }
          bra = cursor;
          if (!r_R2()) {
            cursor = limit - v_4;
            break lab3;
          }
          slice_del();
        }
        break;
      case 9:
        if (!r_RV()) {
          return false;
        }
        if (!(eq_s_b("e"))) {
          return false;
        }
        slice_from("ir");
        break;
    }
    return true;
  }

  private boolean r_verb_suffix() {
    if (cursor < I_pV) {
      return false;
    }
    int v_2 = limit_backward;
    limit_backward = I_pV;
    ket = cursor;
    if (find_among_b(a_6) == 0) {
      limit_backward = v_2;
      return false;
    }
    bra = cursor;
    slice_del();
    limit_backward = v_2;
    return true;
  }

  private boolean r_residual_suffix() {
    ket = cursor;
    if (find_among_b(a_7) == 0) {
      return false;
    }
    bra = cursor;
    if (!r_RV()) {
      return false;
    }
    slice_del();
    return true;
  }

  private boolean r_residual_form() {
    int among_var;
    ket = cursor;
    among_var = find_among_b(a_8);
    if (among_var == 0) {
      return false;
    }
    bra = cursor;
    switch (among_var) {
      case 1:
        if (!r_RV()) {
          return false;
        }
        slice_del();
        ket = cursor;
        lab0:
        {
          int v_1 = limit - cursor;
          lab1:
          {
            if (!(eq_s_b("u"))) {
              break lab1;
            }
            bra = cursor;
            int v_2 = limit - cursor;
            if (!(eq_s_b("g"))) {
              break lab1;
            }
            cursor = limit - v_2;
            break lab0;
          }
          cursor = limit - v_1;
          if (!(eq_s_b("i"))) {
            return false;
          }
          bra = cursor;
          int v_3 = limit - cursor;
          if (!(eq_s_b("c"))) {
            return false;
          }
          cursor = limit - v_3;
        }
        if (!r_RV()) {
          return false;
        }
        slice_del();
        break;
      case 2:
        slice_from("c");
        break;
    }
    return true;
  }

  public boolean stem() {
    int v_1 = cursor;
    r_prelude();
    cursor = v_1;
    r_mark_regions();
    limit_backward = cursor;
    cursor = limit;
    int v_3 = limit - cursor;
    lab0:
    {
      lab1:
      {
        int v_4 = limit - cursor;
        lab2:
        {
          int v_5 = limit - cursor;
          lab3:
          {
            int v_6 = limit - cursor;
            lab4:
            {
              if (!r_standard_suffix()) {
                break lab4;
              }
              break lab3;
            }
            cursor = limit - v_6;
            if (!r_verb_suffix()) {
              break lab2;
            }
          }
          cursor = limit - v_5;
          int v_7 = limit - cursor;
          lab5:
          {
            ket = cursor;
            if (!(eq_s_b("i"))) {
              break lab5;
            }
            bra = cursor;
            int v_8 = limit - cursor;
            if (!(eq_s_b("c"))) {
              break lab5;
            }
            cursor = limit - v_8;
            if (!r_RV()) {
              break lab5;
            }
            slice_del();
          }
          cursor = limit - v_7;
          break lab1;
        }
        cursor = limit - v_4;
        if (!r_residual_suffix()) {
          break lab0;
        }
      }
    }
    cursor = limit - v_3;
    int v_9 = limit - cursor;
    r_residual_form();
    cursor = limit - v_9;
    cursor = limit_backward;
    int v_10 = cursor;
    r_postlude();
    cursor = v_10;
    return true;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof PortugueseStemmer;
  }

  @Override
  public int hashCode() {
    return PortugueseStemmer.class.getName().hashCode();
  }
}
