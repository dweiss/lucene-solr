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
package org.apache.solr.parser;

import org.apache.lucene.queryparser.charstream.CharStream;

/** Token Manager. */
public class QueryParserTokenManager implements QueryParserConstants {
  int commentNestingDepth;

  /** Debug output. */
  // (debugStream omitted).
  /** Set debug output. */
  // (setDebugStream omitted).
  private final int jjStopStringLiteralDfa_3(int pos, long active0) {
    switch (pos) {
      case 0:
        if ((active0 & 0x200L) != 0L) return 31;
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 25;
          return 63;
        }
        if ((active0 & 0x80000000L) != 0L) return 37;
        if ((active0 & 0x400000L) != 0L) return 64;
        if ((active0 & 0x30000L) != 0L) return 15;
        return -1;
      case 1:
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 25;
          jjmatchedPos = 1;
          return 63;
        }
        return -1;
      case 2:
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 25;
          jjmatchedPos = 2;
          return 63;
        }
        return -1;
      case 3:
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 25;
          jjmatchedPos = 3;
          return 63;
        }
        return -1;
      case 4:
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 25;
          jjmatchedPos = 4;
          return 63;
        }
        return -1;
      case 5:
        if ((active0 & 0x200000000L) != 0L) {
          jjmatchedKind = 25;
          jjmatchedPos = 5;
          return 63;
        }
        return -1;
      default:
        return -1;
    }
  }

  private final int jjStartNfa_3(int pos, long active0) {
    return jjMoveNfa_3(jjStopStringLiteralDfa_3(pos, active0), pos + 1);
  }

  private int jjStopAtPos(int pos, int kind) {
    jjmatchedKind = kind;
    jjmatchedPos = pos;
    return pos + 1;
  }

  private int jjMoveStringLiteralDfa0_3() {
    switch (curChar) {
      case 40:
        return jjStopAtPos(0, 19);
      case 41:
        return jjStopAtPos(0, 20);
      case 42:
        return jjStartNfaWithStates_3(0, 22, 64);
      case 43:
        return jjStartNfaWithStates_3(0, 16, 15);
      case 45:
        return jjStartNfaWithStates_3(0, 17, 15);
      case 47:
        return jjMoveStringLiteralDfa1_3(0x200L);
      case 58:
        return jjStopAtPos(0, 21);
      case 91:
        return jjStopAtPos(0, 30);
      case 94:
        return jjStopAtPos(0, 23);
      case 102:
        return jjMoveStringLiteralDfa1_3(0x200000000L);
      case 123:
        return jjStartNfaWithStates_3(0, 31, 37);
      default:
        return jjMoveNfa_3(0, 0);
    }
  }

  private int jjMoveStringLiteralDfa1_3(long active0) {
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_3(0, active0);
      return 1;
    }
    switch (curChar) {
      case 42:
        if ((active0 & 0x200L) != 0L) return jjStopAtPos(1, 9);
        break;
      case 105:
        return jjMoveStringLiteralDfa2_3(active0, 0x200000000L);
      default:
        break;
    }
    return jjStartNfa_3(0, active0);
  }

  private int jjMoveStringLiteralDfa2_3(long old0, long active0) {
    if (((active0 &= old0)) == 0L) return jjStartNfa_3(0, old0);
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_3(1, active0);
      return 2;
    }
    switch (curChar) {
      case 108:
        return jjMoveStringLiteralDfa3_3(active0, 0x200000000L);
      default:
        break;
    }
    return jjStartNfa_3(1, active0);
  }

  private int jjMoveStringLiteralDfa3_3(long old0, long active0) {
    if (((active0 &= old0)) == 0L) return jjStartNfa_3(1, old0);
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_3(2, active0);
      return 3;
    }
    switch (curChar) {
      case 116:
        return jjMoveStringLiteralDfa4_3(active0, 0x200000000L);
      default:
        break;
    }
    return jjStartNfa_3(2, active0);
  }

  private int jjMoveStringLiteralDfa4_3(long old0, long active0) {
    if (((active0 &= old0)) == 0L) return jjStartNfa_3(2, old0);
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_3(3, active0);
      return 4;
    }
    switch (curChar) {
      case 101:
        return jjMoveStringLiteralDfa5_3(active0, 0x200000000L);
      default:
        break;
    }
    return jjStartNfa_3(3, active0);
  }

  private int jjMoveStringLiteralDfa5_3(long old0, long active0) {
    if (((active0 &= old0)) == 0L) return jjStartNfa_3(3, old0);
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_3(4, active0);
      return 5;
    }
    switch (curChar) {
      case 114:
        return jjMoveStringLiteralDfa6_3(active0, 0x200000000L);
      default:
        break;
    }
    return jjStartNfa_3(4, active0);
  }

  private int jjMoveStringLiteralDfa6_3(long old0, long active0) {
    if (((active0 &= old0)) == 0L) return jjStartNfa_3(4, old0);
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_3(5, active0);
      return 6;
    }
    switch (curChar) {
      case 40:
        if ((active0 & 0x200000000L) != 0L) return jjStopAtPos(6, 33);
        break;
      default:
        break;
    }
    return jjStartNfa_3(5, active0);
  }

  private int jjStartNfaWithStates_3(int pos, int kind, int state) {
    jjmatchedKind = kind;
    jjmatchedPos = pos;
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      return pos + 1;
    }
    return jjMoveNfa_3(state, pos + 1);
  }

  static final long[] jjbitVec0 = {0x1L, 0x0L, 0x0L, 0x0L};
  static final long[] jjbitVec1 = {
    0xfffffffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
  };
  static final long[] jjbitVec3 = {0x0L, 0x0L, 0xffffffffffffffffL, 0xffffffffffffffffL};
  static final long[] jjbitVec4 = {
    0xfffefffffffffffeL, 0xffffffffffffffffL, 0xffffffffffffffffL, 0xffffffffffffffffL
  };

  private int jjMoveNfa_3(int startState, int curPos) {
    int startsAt = 0;
    jjnewStateCnt = 63;
    int i = 1;
    jjstateSet[0] = startState;
    int kind = 0x7fffffff;
    for (; ; ) {
      if (++jjround == 0x7fffffff) ReInitRounds();
      if (curChar < 64) {
        long l = 1L << curChar;
        do {
          switch (jjstateSet[--i]) {
            case 64:
            case 27:
              if ((0xfbfffcfaffffd9ffL & l) == 0L) break;
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 31:
              if ((0xffff7bffffffffffL & l) != 0L) {
                jjCheckNAddStates(0, 2);
              }
              break;
            case 0:
              if ((0xfbff54f8ffffd9ffL & l) != 0L) {
                if (kind > 28) kind = 28;
                {
                  jjCheckNAddTwoStates(27, 28);
                }
              } else if ((0x100002600L & l) != 0L) {
                if (kind > 8) kind = 8;
              } else if ((0x280200000000L & l) != 0L) jjstateSet[jjnewStateCnt++] = 15;
              else if (curChar == 47) {
                jjAddStates(3, 4);
              } else if (curChar == 34) {
                jjCheckNAddStates(5, 7);
              }
              if ((0x7bff50f8ffffd9ffL & l) != 0L) {
                if (kind > 25) kind = 25;
                {
                  jjCheckNAddStates(8, 12);
                }
              } else if (curChar == 42) {
                if (kind > 27) kind = 27;
              } else if (curChar == 33) {
                if (kind > 15) kind = 15;
              }
              if (curChar == 38) jjstateSet[jjnewStateCnt++] = 4;
              break;
            case 63:
              if ((0xfbfffcfaffffd9ffL & l) != 0L) {
                if (kind > 28) kind = 28;
                {
                  jjCheckNAddTwoStates(27, 28);
                }
              }
              if ((0x7bfff8faffffd9ffL & l) != 0L) {
                jjCheckNAddStates(13, 15);
              } else if (curChar == 42) {
                if (kind > 27) kind = 27;
              }
              if ((0x7bfff8faffffd9ffL & l) != 0L) {
                if (kind > 25) kind = 25;
                {
                  jjCheckNAddTwoStates(56, 57);
                }
              }
              break;
            case 4:
              if (curChar == 38 && kind > 13) kind = 13;
              break;
            case 5:
              if (curChar == 38) jjstateSet[jjnewStateCnt++] = 4;
              break;
            case 13:
              if (curChar == 33 && kind > 15) kind = 15;
              break;
            case 14:
              if ((0x280200000000L & l) != 0L) jjstateSet[jjnewStateCnt++] = 15;
              break;
            case 15:
              if ((0x100002600L & l) != 0L && kind > 18) kind = 18;
              break;
            case 16:
              if (curChar == 34) {
                jjCheckNAddStates(5, 7);
              }
              break;
            case 17:
              if ((0xfffffffbffffffffL & l) != 0L) {
                jjCheckNAddStates(5, 7);
              }
              break;
            case 19:
              {
                jjCheckNAddStates(5, 7);
              }
              break;
            case 20:
              if (curChar == 34 && kind > 24) kind = 24;
              break;
            case 22:
              if ((0x3ff000000000000L & l) == 0L) break;
              if (kind > 26) kind = 26;
              {
                jjAddStates(16, 17);
              }
              break;
            case 23:
              if (curChar == 46) {
                jjCheckNAdd(24);
              }
              break;
            case 24:
              if ((0x3ff000000000000L & l) == 0L) break;
              if (kind > 26) kind = 26;
              {
                jjCheckNAdd(24);
              }
              break;
            case 25:
              if (curChar == 42 && kind > 27) kind = 27;
              break;
            case 26:
              if ((0xfbff54f8ffffd9ffL & l) == 0L) break;
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 29:
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 30:
              if (curChar == 47) {
                jjAddStates(3, 4);
              }
              break;
            case 32:
              if ((0xffff7fffffffffffL & l) != 0L) {
                jjCheckNAddStates(0, 2);
              }
              break;
            case 33:
              if (curChar == 47) {
                jjCheckNAddStates(0, 2);
              }
              break;
            case 35:
              if (curChar == 47 && kind > 29) kind = 29;
              break;
            case 37:
              if (curChar == 33) {
                jjCheckNAddStates(18, 20);
              }
              break;
            case 38:
              if ((0x100002600L & l) != 0L) {
                jjCheckNAddTwoStates(38, 39);
              }
              break;
            case 39:
              if ((0xdfffffffffffffffL & l) != 0L) {
                jjCheckNAddStates(21, 24);
              }
              break;
            case 40:
              if (curChar == 61) {
                jjCheckNAddStates(25, 30);
              }
              break;
            case 41:
              if (curChar == 34) {
                jjCheckNAddStates(31, 33);
              }
              break;
            case 42:
              if ((0xfffffffbffffffffL & l) != 0L) {
                jjCheckNAddStates(31, 33);
              }
              break;
            case 44:
              {
                jjCheckNAddStates(31, 33);
              }
              break;
            case 45:
              if (curChar == 34) {
                jjCheckNAddStates(18, 20);
              }
              break;
            case 48:
              if ((0xfffffdfefffff9ffL & l) == 0L) break;
              if (kind > 32) kind = 32;
              jjstateSet[jjnewStateCnt++] = 48;
              break;
            case 49:
              if (curChar == 39) {
                jjCheckNAddStates(34, 36);
              }
              break;
            case 50:
              if ((0xffffff7fffffffffL & l) != 0L) {
                jjCheckNAddStates(34, 36);
              }
              break;
            case 52:
              {
                jjCheckNAddStates(34, 36);
              }
              break;
            case 53:
              if (curChar == 39) {
                jjCheckNAddStates(18, 20);
              }
              break;
            case 54:
              if ((0xfffffffeffffffffL & l) != 0L) {
                jjCheckNAddStates(37, 40);
              }
              break;
            case 55:
              if ((0x7bff50f8ffffd9ffL & l) == 0L) break;
              if (kind > 25) kind = 25;
              {
                jjCheckNAddStates(8, 12);
              }
              break;
            case 56:
              if ((0x7bfff8faffffd9ffL & l) == 0L) break;
              if (kind > 25) kind = 25;
              {
                jjCheckNAddTwoStates(56, 57);
              }
              break;
            case 58:
              if (kind > 25) kind = 25;
              {
                jjCheckNAddTwoStates(56, 57);
              }
              break;
            case 59:
              if ((0x7bfff8faffffd9ffL & l) != 0L) {
                jjCheckNAddStates(13, 15);
              }
              break;
            case 61:
              {
                jjCheckNAddStates(13, 15);
              }
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        long l = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            case 64:
              if ((0x97ffffff87ffffffL & l) != 0L) {
                if (kind > 28) kind = 28;
                {
                  jjCheckNAddTwoStates(27, 28);
                }
              } else if (curChar == 92) {
                jjCheckNAddTwoStates(29, 29);
              }
              break;
            case 31:
              {
                jjCheckNAddStates(0, 2);
              }
              if (curChar == 92) {
                jjCheckNAdd(33);
              }
              break;
            case 0:
              if ((0x97ffffff87ffffffL & l) != 0L) {
                if (kind > 25) kind = 25;
                {
                  jjCheckNAddStates(8, 12);
                }
              } else if (curChar == 92) {
                jjCheckNAddStates(41, 43);
              } else if (curChar == 123) jjstateSet[jjnewStateCnt++] = 37;
              else if (curChar == 126) {
                if (kind > 26) kind = 26;
                jjstateSet[jjnewStateCnt++] = 22;
              }
              if ((0x97ffffff87ffffffL & l) != 0L) {
                if (kind > 28) kind = 28;
                {
                  jjCheckNAddTwoStates(27, 28);
                }
              }
              if (curChar == 78) jjstateSet[jjnewStateCnt++] = 11;
              else if (curChar == 124) jjstateSet[jjnewStateCnt++] = 8;
              else if (curChar == 79) jjstateSet[jjnewStateCnt++] = 6;
              else if (curChar == 65) jjstateSet[jjnewStateCnt++] = 2;
              break;
            case 63:
              if ((0x97ffffff87ffffffL & l) != 0L) {
                if (kind > 28) kind = 28;
                {
                  jjCheckNAddTwoStates(27, 28);
                }
              } else if (curChar == 92) {
                jjCheckNAddTwoStates(58, 58);
              }
              if ((0x97ffffff87ffffffL & l) != 0L) {
                jjCheckNAddStates(13, 15);
              } else if (curChar == 92) {
                jjCheckNAddTwoStates(61, 61);
              }
              if ((0x97ffffff87ffffffL & l) != 0L) {
                if (kind > 25) kind = 25;
                {
                  jjCheckNAddTwoStates(56, 57);
                }
              } else if (curChar == 92) {
                jjCheckNAddTwoStates(29, 29);
              }
              break;
            case 1:
              if (curChar == 68 && kind > 13) kind = 13;
              break;
            case 2:
              if (curChar == 78) jjstateSet[jjnewStateCnt++] = 1;
              break;
            case 3:
              if (curChar == 65) jjstateSet[jjnewStateCnt++] = 2;
              break;
            case 6:
              if (curChar == 82 && kind > 14) kind = 14;
              break;
            case 7:
              if (curChar == 79) jjstateSet[jjnewStateCnt++] = 6;
              break;
            case 8:
              if (curChar == 124 && kind > 14) kind = 14;
              break;
            case 9:
              if (curChar == 124) jjstateSet[jjnewStateCnt++] = 8;
              break;
            case 10:
              if (curChar == 84 && kind > 15) kind = 15;
              break;
            case 11:
              if (curChar == 79) jjstateSet[jjnewStateCnt++] = 10;
              break;
            case 12:
              if (curChar == 78) jjstateSet[jjnewStateCnt++] = 11;
              break;
            case 17:
              if ((0xffffffffefffffffL & l) != 0L) {
                jjCheckNAddStates(5, 7);
              }
              break;
            case 18:
              if (curChar == 92) jjstateSet[jjnewStateCnt++] = 19;
              break;
            case 19:
              {
                jjCheckNAddStates(5, 7);
              }
              break;
            case 21:
              if (curChar != 126) break;
              if (kind > 26) kind = 26;
              jjstateSet[jjnewStateCnt++] = 22;
              break;
            case 26:
              if ((0x97ffffff87ffffffL & l) == 0L) break;
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 27:
              if ((0x97ffffff87ffffffL & l) == 0L) break;
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 28:
              if (curChar == 92) {
                jjCheckNAddTwoStates(29, 29);
              }
              break;
            case 29:
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 32:
              {
                jjCheckNAddStates(0, 2);
              }
              break;
            case 34:
              if (curChar == 92) {
                jjCheckNAdd(33);
              }
              break;
            case 36:
              if (curChar == 92) {
                jjCheckNAdd(33);
              }
              break;
            case 39:
              if ((0xdfffffffffffffffL & l) != 0L) {
                jjCheckNAddStates(21, 24);
              }
              break;
            case 42:
              if ((0xffffffffefffffffL & l) != 0L) {
                jjCheckNAddStates(31, 33);
              }
              break;
            case 43:
              if (curChar == 92) jjstateSet[jjnewStateCnt++] = 44;
              break;
            case 44:
              {
                jjCheckNAddStates(31, 33);
              }
              break;
            case 46:
              if (curChar != 125) break;
              if (kind > 32) kind = 32;
              {
                jjCheckNAddTwoStates(47, 48);
              }
              break;
            case 47:
              if (curChar == 123) jjstateSet[jjnewStateCnt++] = 37;
              break;
            case 48:
              if ((0xf7ffffffbfffffffL & l) == 0L) break;
              if (kind > 32) kind = 32;
              {
                jjCheckNAdd(48);
              }
              break;
            case 50:
              if ((0xffffffffefffffffL & l) != 0L) {
                jjCheckNAddStates(34, 36);
              }
              break;
            case 51:
              if (curChar == 92) jjstateSet[jjnewStateCnt++] = 52;
              break;
            case 52:
              {
                jjCheckNAddStates(34, 36);
              }
              break;
            case 54:
              if ((0xdfffffffffffffffL & l) != 0L) {
                jjCheckNAddStates(37, 40);
              }
              break;
            case 55:
              if ((0x97ffffff87ffffffL & l) == 0L) break;
              if (kind > 25) kind = 25;
              {
                jjCheckNAddStates(8, 12);
              }
              break;
            case 56:
              if ((0x97ffffff87ffffffL & l) == 0L) break;
              if (kind > 25) kind = 25;
              {
                jjCheckNAddTwoStates(56, 57);
              }
              break;
            case 57:
              if (curChar == 92) {
                jjCheckNAddTwoStates(58, 58);
              }
              break;
            case 58:
              if (kind > 25) kind = 25;
              {
                jjCheckNAddTwoStates(56, 57);
              }
              break;
            case 59:
              if ((0x97ffffff87ffffffL & l) != 0L) {
                jjCheckNAddStates(13, 15);
              }
              break;
            case 60:
              if (curChar == 92) {
                jjCheckNAddTwoStates(61, 61);
              }
              break;
            case 61:
              {
                jjCheckNAddStates(13, 15);
              }
              break;
            case 62:
              if (curChar == 92) {
                jjCheckNAddStates(41, 43);
              }
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else {
        int hiByte = (curChar >> 8);
        int i1 = hiByte >> 6;
        long l1 = 1L << (hiByte & 077);
        int i2 = (curChar & 0xff) >> 6;
        long l2 = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            case 64:
            case 27:
              if (!jjCanMove_2(hiByte, i1, i2, l1, l2)) break;
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 31:
            case 32:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(0, 2);
              }
              break;
            case 0:
              if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                if (kind > 8) kind = 8;
              }
              if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
                if (kind > 28) kind = 28;
                {
                  jjCheckNAddTwoStates(27, 28);
                }
              }
              if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
                if (kind > 25) kind = 25;
                {
                  jjCheckNAddStates(8, 12);
                }
              }
              break;
            case 63:
              if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
                if (kind > 25) kind = 25;
                {
                  jjCheckNAddTwoStates(56, 57);
                }
              }
              if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(13, 15);
              }
              if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
                if (kind > 28) kind = 28;
                {
                  jjCheckNAddTwoStates(27, 28);
                }
              }
              break;
            case 15:
              if (jjCanMove_0(hiByte, i1, i2, l1, l2) && kind > 18) kind = 18;
              break;
            case 17:
            case 19:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(5, 7);
              }
              break;
            case 26:
              if (!jjCanMove_2(hiByte, i1, i2, l1, l2)) break;
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 29:
              if (!jjCanMove_1(hiByte, i1, i2, l1, l2)) break;
              if (kind > 28) kind = 28;
              {
                jjCheckNAddTwoStates(27, 28);
              }
              break;
            case 38:
              if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddTwoStates(38, 39);
              }
              break;
            case 39:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(21, 24);
              }
              break;
            case 42:
            case 44:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(31, 33);
              }
              break;
            case 48:
              if (!jjCanMove_1(hiByte, i1, i2, l1, l2)) break;
              if (kind > 32) kind = 32;
              jjstateSet[jjnewStateCnt++] = 48;
              break;
            case 50:
            case 52:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(34, 36);
              }
              break;
            case 54:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(37, 40);
              }
              break;
            case 55:
              if (!jjCanMove_2(hiByte, i1, i2, l1, l2)) break;
              if (kind > 25) kind = 25;
              {
                jjCheckNAddStates(8, 12);
              }
              break;
            case 56:
              if (!jjCanMove_2(hiByte, i1, i2, l1, l2)) break;
              if (kind > 25) kind = 25;
              {
                jjCheckNAddTwoStates(56, 57);
              }
              break;
            case 58:
              if (!jjCanMove_1(hiByte, i1, i2, l1, l2)) break;
              if (kind > 25) kind = 25;
              {
                jjCheckNAddTwoStates(56, 57);
              }
              break;
            case 59:
              if (jjCanMove_2(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(13, 15);
              }
              break;
            case 61:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjCheckNAddStates(13, 15);
              }
              break;
            default:
              if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) break;
              else break;
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        jjmatchedKind = kind;
        jjmatchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 63 - (jjnewStateCnt = startsAt))) return curPos;
      try {
        curChar = input_stream.readChar();
      } catch (java.io.IOException e) {
        return curPos;
      }
    }
  }

  private final int jjStopStringLiteralDfa_2(int pos, long active0) {
    switch (pos) {
      default:
        return -1;
    }
  }

  private final int jjStartNfa_2(int pos, long active0) {
    return jjMoveNfa_2(jjStopStringLiteralDfa_2(pos, active0), pos + 1);
  }

  private int jjMoveStringLiteralDfa0_2() {
    switch (curChar) {
      case 42:
        return jjMoveStringLiteralDfa1_2(0x400L);
      case 47:
        return jjMoveStringLiteralDfa1_2(0x200L);
      default:
        return jjMoveNfa_2(0, 0);
    }
  }

  private int jjMoveStringLiteralDfa1_2(long active0) {
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_2(0, active0);
      return 1;
    }
    switch (curChar) {
      case 42:
        if ((active0 & 0x200L) != 0L) return jjStopAtPos(1, 9);
        break;
      case 47:
        if ((active0 & 0x400L) != 0L) return jjStopAtPos(1, 10);
        break;
      default:
        break;
    }
    return jjStartNfa_2(0, active0);
  }

  private int jjMoveNfa_2(int startState, int curPos) {
    int startsAt = 0;
    jjnewStateCnt = 1;
    int i = 1;
    jjstateSet[0] = startState;
    int kind = 0x7fffffff;
    for (; ; ) {
      if (++jjround == 0x7fffffff) ReInitRounds();
      if (curChar < 64) {
        long l = 1L << curChar;
        do {
          switch (jjstateSet[--i]) {
            case 0:
              if ((0x100002600L & l) != 0L) kind = 8;
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        long l = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            default:
              break;
          }
        } while (i != startsAt);
      } else {
        int hiByte = (curChar >> 8);
        int i1 = hiByte >> 6;
        long l1 = 1L << (hiByte & 077);
        int i2 = (curChar & 0xff) >> 6;
        long l2 = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            case 0:
              if (jjCanMove_0(hiByte, i1, i2, l1, l2) && kind > 8) kind = 8;
              break;
            default:
              if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) break;
              else break;
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        jjmatchedKind = kind;
        jjmatchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 1 - (jjnewStateCnt = startsAt))) return curPos;
      try {
        curChar = input_stream.readChar();
      } catch (java.io.IOException e) {
        return curPos;
      }
    }
  }

  private int jjMoveStringLiteralDfa0_0() {
    return jjMoveNfa_0(0, 0);
  }

  private int jjMoveNfa_0(int startState, int curPos) {
    int startsAt = 0;
    jjnewStateCnt = 5;
    int i = 1;
    jjstateSet[0] = startState;
    int kind = 0x7fffffff;
    for (; ; ) {
      if (++jjround == 0x7fffffff) ReInitRounds();
      if (curChar < 64) {
        long l = 1L << curChar;
        do {
          switch (jjstateSet[--i]) {
            case 0:
              if ((0x3ff000000000000L & l) != 0L) {
                if (kind > 34) kind = 34;
                {
                  jjCheckNAddTwoStates(2, 3);
                }
              } else if (curChar == 45) {
                jjCheckNAdd(2);
              } else if (curChar == 61) {
                jjCheckNAddTwoStates(1, 2);
              }
              break;
            case 1:
              if (curChar == 45) {
                jjCheckNAdd(2);
              }
              break;
            case 2:
              if ((0x3ff000000000000L & l) == 0L) break;
              if (kind > 34) kind = 34;
              {
                jjCheckNAddTwoStates(2, 3);
              }
              break;
            case 3:
              if (curChar == 46) {
                jjCheckNAdd(4);
              }
              break;
            case 4:
              if ((0x3ff000000000000L & l) == 0L) break;
              if (kind > 34) kind = 34;
              {
                jjCheckNAdd(4);
              }
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        long l = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            default:
              break;
          }
        } while (i != startsAt);
      } else {
        int hiByte = (curChar >> 8);
        int i1 = hiByte >> 6;
        long l1 = 1L << (hiByte & 077);
        int i2 = (curChar & 0xff) >> 6;
        long l2 = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            default:
              if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) break;
              else break;
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        jjmatchedKind = kind;
        jjmatchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 5 - (jjnewStateCnt = startsAt))) return curPos;
      try {
        curChar = input_stream.readChar();
      } catch (java.io.IOException e) {
        return curPos;
      }
    }
  }

  private final int jjStopStringLiteralDfa_1(int pos, long active0) {
    switch (pos) {
      case 0:
        if ((active0 & 0x800000000L) != 0L) {
          jjmatchedKind = 39;
          return 6;
        }
        return -1;
      default:
        return -1;
    }
  }

  private final int jjStartNfa_1(int pos, long active0) {
    return jjMoveNfa_1(jjStopStringLiteralDfa_1(pos, active0), pos + 1);
  }

  private int jjMoveStringLiteralDfa0_1() {
    switch (curChar) {
      case 84:
        return jjMoveStringLiteralDfa1_1(0x800000000L);
      case 93:
        return jjStopAtPos(0, 36);
      case 125:
        return jjStopAtPos(0, 37);
      default:
        return jjMoveNfa_1(0, 0);
    }
  }

  private int jjMoveStringLiteralDfa1_1(long active0) {
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      jjStopStringLiteralDfa_1(0, active0);
      return 1;
    }
    switch (curChar) {
      case 79:
        if ((active0 & 0x800000000L) != 0L) return jjStartNfaWithStates_1(1, 35, 6);
        break;
      default:
        break;
    }
    return jjStartNfa_1(0, active0);
  }

  private int jjStartNfaWithStates_1(int pos, int kind, int state) {
    jjmatchedKind = kind;
    jjmatchedPos = pos;
    try {
      curChar = input_stream.readChar();
    } catch (java.io.IOException e) {
      return pos + 1;
    }
    return jjMoveNfa_1(state, pos + 1);
  }

  private int jjMoveNfa_1(int startState, int curPos) {
    int startsAt = 0;
    jjnewStateCnt = 7;
    int i = 1;
    jjstateSet[0] = startState;
    int kind = 0x7fffffff;
    for (; ; ) {
      if (++jjround == 0x7fffffff) ReInitRounds();
      if (curChar < 64) {
        long l = 1L << curChar;
        do {
          switch (jjstateSet[--i]) {
            case 0:
              if ((0xfffffffeffffffffL & l) != 0L) {
                if (kind > 39) kind = 39;
                {
                  jjCheckNAdd(6);
                }
              }
              if ((0x100002600L & l) != 0L) {
                if (kind > 12) kind = 12;
              } else if (curChar == 34) {
                jjCheckNAddTwoStates(2, 4);
              }
              break;
            case 1:
              if (curChar == 34) {
                jjCheckNAddTwoStates(2, 4);
              }
              break;
            case 2:
              if ((0xfffffffbffffffffL & l) != 0L) {
                jjCheckNAddStates(44, 46);
              }
              break;
            case 3:
              if (curChar == 34) {
                jjCheckNAddStates(44, 46);
              }
              break;
            case 5:
              if (curChar == 34 && kind > 38) kind = 38;
              break;
            case 6:
              if ((0xfffffffeffffffffL & l) == 0L) break;
              if (kind > 39) kind = 39;
              {
                jjCheckNAdd(6);
              }
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else if (curChar < 128) {
        long l = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            case 0:
            case 6:
              if ((0xdfffffffdfffffffL & l) == 0L) break;
              if (kind > 39) kind = 39;
              {
                jjCheckNAdd(6);
              }
              break;
            case 2:
              {
                jjAddStates(44, 46);
              }
              break;
            case 4:
              if (curChar == 92) jjstateSet[jjnewStateCnt++] = 3;
              break;
            default:
              break;
          }
        } while (i != startsAt);
      } else {
        int hiByte = (curChar >> 8);
        int i1 = hiByte >> 6;
        long l1 = 1L << (hiByte & 077);
        int i2 = (curChar & 0xff) >> 6;
        long l2 = 1L << (curChar & 077);
        do {
          switch (jjstateSet[--i]) {
            case 0:
              if (jjCanMove_0(hiByte, i1, i2, l1, l2)) {
                if (kind > 12) kind = 12;
              }
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                if (kind > 39) kind = 39;
                {
                  jjCheckNAdd(6);
                }
              }
              break;
            case 2:
              if (jjCanMove_1(hiByte, i1, i2, l1, l2)) {
                jjAddStates(44, 46);
              }
              break;
            case 6:
              if (!jjCanMove_1(hiByte, i1, i2, l1, l2)) break;
              if (kind > 39) kind = 39;
              {
                jjCheckNAdd(6);
              }
              break;
            default:
              if (i1 == 0 || l1 == 0 || i2 == 0 || l2 == 0) break;
              else break;
          }
        } while (i != startsAt);
      }
      if (kind != 0x7fffffff) {
        jjmatchedKind = kind;
        jjmatchedPos = curPos;
        kind = 0x7fffffff;
      }
      ++curPos;
      if ((i = jjnewStateCnt) == (startsAt = 7 - (jjnewStateCnt = startsAt))) return curPos;
      try {
        curChar = input_stream.readChar();
      } catch (java.io.IOException e) {
        return curPos;
      }
    }
  }

  /** Token literal values. */
  public static final String[] jjstrLiteralImages = {
    "",
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    "\53",
    "\55",
    null,
    "\50",
    "\51",
    "\72",
    "\52",
    "\136",
    null,
    null,
    null,
    null,
    null,
    null,
    "\133",
    "\173",
    null,
    "\146\151\154\164\145\162\50",
    null,
    "\124\117",
    "\135",
    "\175",
    null,
    null,
  };

  protected Token jjFillToken() {
    final Token t;
    final String curTokenImage;
    final int beginLine;
    final int endLine;
    final int beginColumn;
    final int endColumn;
    String im = jjstrLiteralImages[jjmatchedKind];
    curTokenImage = (im == null) ? input_stream.GetImage() : im;
    beginLine = input_stream.getBeginLine();
    beginColumn = input_stream.getBeginColumn();
    endLine = input_stream.getEndLine();
    endColumn = input_stream.getEndColumn();
    t = Token.newToken(jjmatchedKind);
    t.kind = jjmatchedKind;
    t.image = curTokenImage;

    t.beginLine = beginLine;
    t.endLine = endLine;
    t.beginColumn = beginColumn;
    t.endColumn = endColumn;

    return t;
  }

  static final int[] jjnextStates = {
    32, 34, 35, 31, 36, 17, 18, 20, 56, 59, 25, 60, 57, 59, 25, 60,
    22, 23, 38, 39, 46, 38, 39, 40, 46, 38, 39, 41, 49, 54, 46, 42,
    43, 45, 50, 51, 53, 38, 39, 54, 46, 58, 61, 29, 2, 4, 5,
  };

  private static final boolean jjCanMove_0(int hiByte, int i1, int i2, long l1, long l2) {
    switch (hiByte) {
      case 48:
        return ((jjbitVec0[i2] & l2) != 0L);
      default:
        return false;
    }
  }

  private static final boolean jjCanMove_1(int hiByte, int i1, int i2, long l1, long l2) {
    switch (hiByte) {
      case 0:
        return ((jjbitVec3[i2] & l2) != 0L);
      default:
        if ((jjbitVec1[i1] & l1) != 0L) return true;
        return false;
    }
  }

  private static final boolean jjCanMove_2(int hiByte, int i1, int i2, long l1, long l2) {
    switch (hiByte) {
      case 0:
        return ((jjbitVec3[i2] & l2) != 0L);
      case 48:
        return ((jjbitVec1[i2] & l2) != 0L);
      default:
        if ((jjbitVec4[i1] & l1) != 0L) return true;
        return false;
    }
  }

  int curLexState = 3;
  int defaultLexState = 3;
  int jjnewStateCnt;
  int jjround;
  int jjmatchedPos;
  int jjmatchedKind;

  /** Get the next Token. */
  public Token getNextToken() {
    Token matchedToken;
    int curPos = 0;

    EOFLoop:
    for (; ; ) {
      try {
        curChar = input_stream.BeginToken();
      } catch (Exception e) {
        jjmatchedKind = 0;
        jjmatchedPos = -1;
        matchedToken = jjFillToken();
        return matchedToken;
      }
      image = jjimage;
      image.setLength(0);
      jjimageLen = 0;

      switch (curLexState) {
        case 0:
          jjmatchedKind = 0x7fffffff;
          jjmatchedPos = 0;
          curPos = jjMoveStringLiteralDfa0_0();
          break;
        case 1:
          jjmatchedKind = 0x7fffffff;
          jjmatchedPos = 0;
          curPos = jjMoveStringLiteralDfa0_1();
          break;
        case 2:
          jjmatchedKind = 0x7fffffff;
          jjmatchedPos = 0;
          curPos = jjMoveStringLiteralDfa0_2();
          if (jjmatchedPos == 0 && jjmatchedKind > 11) {
            jjmatchedKind = 11;
          }
          break;
        case 3:
          jjmatchedKind = 0x7fffffff;
          jjmatchedPos = 0;
          curPos = jjMoveStringLiteralDfa0_3();
          break;
      }
      if (jjmatchedKind != 0x7fffffff) {
        if (jjmatchedPos + 1 < curPos) input_stream.backup(curPos - jjmatchedPos - 1);
        if ((jjtoToken[jjmatchedKind >> 6] & (1L << (jjmatchedKind & 077))) != 0L) {
          matchedToken = jjFillToken();
          if (jjnewLexState[jjmatchedKind] != -1) curLexState = jjnewLexState[jjmatchedKind];
          return matchedToken;
        } else {
          SkipLexicalActions(null);
          if (jjnewLexState[jjmatchedKind] != -1) curLexState = jjnewLexState[jjmatchedKind];
          continue EOFLoop;
        }
      }
      int error_line = input_stream.getEndLine();
      int error_column = input_stream.getEndColumn();
      String error_after = null;
      boolean EOFSeen = false;
      try {
        input_stream.readChar();
        input_stream.backup(1);
      } catch (java.io.IOException e1) {
        EOFSeen = true;
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
        if (curChar == '\n' || curChar == '\r') {
          error_line++;
          error_column = 0;
        } else error_column++;
      }
      if (!EOFSeen) {
        input_stream.backup(1);
        error_after = curPos <= 1 ? "" : input_stream.GetImage();
      }
      throw new TokenMgrError(
          EOFSeen,
          curLexState,
          error_line,
          error_column,
          error_after,
          curChar,
          TokenMgrError.LEXICAL_ERROR);
    }
  }

  void SkipLexicalActions(Token matchedToken) {
    switch (jjmatchedKind) {
      case 9:
        image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
        commentNestingDepth++;
        break;
      case 10:
        image.append(input_stream.GetSuffix(jjimageLen + (lengthOfMatch = jjmatchedPos + 1)));
        commentNestingDepth -= 1;
        SwitchTo(commentNestingDepth == 0 ? DEFAULT : COMMENT);
        break;
      default:
        break;
    }
  }

  void MoreLexicalActions() {
    jjimageLen += (lengthOfMatch = jjmatchedPos + 1);
    switch (jjmatchedKind) {
      default:
        break;
    }
  }

  void TokenLexicalActions(Token matchedToken) {
    switch (jjmatchedKind) {
      default:
        break;
    }
  }

  private void jjCheckNAdd(int state) {
    if (jjrounds[state] != jjround) {
      jjstateSet[jjnewStateCnt++] = state;
      jjrounds[state] = jjround;
    }
  }

  private void jjAddStates(int start, int end) {
    do {
      jjstateSet[jjnewStateCnt++] = jjnextStates[start];
    } while (start++ != end);
  }

  private void jjCheckNAddTwoStates(int state1, int state2) {
    jjCheckNAdd(state1);
    jjCheckNAdd(state2);
  }

  private void jjCheckNAddStates(int start, int end) {
    do {
      jjCheckNAdd(jjnextStates[start]);
    } while (start++ != end);
  }

  /** Constructor. */
  public QueryParserTokenManager(CharStream stream) {

    input_stream = stream;
  }

  /** Constructor. */
  public QueryParserTokenManager(CharStream stream, int lexState) {
    ReInit(stream);
    SwitchTo(lexState);
  }

  /** Reinitialise parser. */
  public void ReInit(CharStream stream) {

    jjmatchedPos = jjnewStateCnt = 0;
    curLexState = defaultLexState;
    input_stream = stream;
    ReInitRounds();
  }

  private void ReInitRounds() {
    int i;
    jjround = 0x80000001;
    for (i = 63; i-- > 0; ) jjrounds[i] = 0x80000000;
  }

  /** Reinitialise parser. */
  public void ReInit(CharStream stream, int lexState) {

    ReInit(stream);
    SwitchTo(lexState);
  }

  /** Switch to specified lex state. */
  public void SwitchTo(int lexState) {
    if (lexState >= 4 || lexState < 0)
      throw new TokenMgrError(
          "Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.",
          TokenMgrError.INVALID_LEXICAL_STATE);
    else curLexState = lexState;
  }

  /** Lexer state names. */
  public static final String[] lexStateNames = {
    "Boost", "Range", "COMMENT", "DEFAULT",
  };

  /** Lex State array. */
  public static final int[] jjnewLexState = {
    -1, -1, -1, -1, -1, -1, -1, -1, -1, 2, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0,
    -1, -1, -1, -1, -1, -1, 1, 1, -1, -1, 3, -1, 3, 3, -1, -1,
  };

  static final long[] jjtoToken = {
    0xffffffe001L,
  };
  static final long[] jjtoSkip = {
    0x1f00L,
  };
  static final long[] jjtoSpecial = {
    0x0L,
  };
  static final long[] jjtoMore = {
    0x0L,
  };
  protected CharStream input_stream;

  private final int[] jjrounds = new int[63];
  private final int[] jjstateSet = new int[2 * 63];
  private final StringBuilder jjimage = new StringBuilder();
  private StringBuilder image = jjimage;
  private int jjimageLen;
  private int lengthOfMatch;
  protected int curChar;
}
