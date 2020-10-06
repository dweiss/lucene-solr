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

package org.apache.lucene.analysis.standard;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * This class implements Word Break rules from the Unicode Text Segmentation algorithm, as specified
 * in <a href="http://unicode.org/reports/tr29/">Unicode Standard Annex #29</a>.
 *
 * <p>Tokens produced are of the following types:
 *
 * <ul>
 *   <li>&lt;ALPHANUM&gt;: A sequence of alphabetic and numeric characters
 *   <li>&lt;NUM&gt;: A number
 *   <li>&lt;SOUTHEAST_ASIAN&gt;: A sequence of characters from South and Southeast Asian languages,
 *       including Thai, Lao, Myanmar, and Khmer
 *   <li>&lt;IDEOGRAPHIC&gt;: A single CJKV ideographic character
 *   <li>&lt;HIRAGANA&gt;: A single hiragana character
 *   <li>&lt;KATAKANA&gt;: A sequence of katakana characters
 *   <li>&lt;HANGUL&gt;: A sequence of Hangul characters
 *   <li>&lt;EMOJI&gt;: A sequence of Emoji characters
 * </ul>
 */
@SuppressWarnings("fallthrough")
public final class StandardTokenizerImpl {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private int ZZ_BUFFERSIZE = 255;

  /** lexical states */
  public static final int YYINITIAL = 0;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l ZZ_LEXSTATE[l+1] is the state in
   * the DFA for the lexical state l at the beginning of a line l is of the form l = 2*k, k a non
   * negative integer
   */
  private static final int ZZ_LEXSTATE[] = {0, 0};

  /** Translates characters to character classes */
  private static final String ZZ_CMAP_PACKED =
      "\42\0\1\32\1\7\3\0\1\31\2\0\1\7\1\0\1\24\1\0"
          + "\1\25\1\0\12\21\1\23\1\24\5\0\32\15\4\0\1\26\1\0"
          + "\32\15\56\0\1\4\1\15\2\0\1\5\1\4\6\0\1\15\1\0"
          + "\1\23\2\0\1\15\5\0\27\15\1\0\37\15\1\0\u01ca\15\4\0"
          + "\14\15\5\0\1\23\10\0\5\15\7\0\1\15\1\0\1\15\21\0"
          + "\160\5\5\15\1\0\2\15\2\0\4\15\1\24\1\15\6\0\1\15"
          + "\1\23\3\15\1\0\1\15\1\0\24\15\1\0\123\15\1\0\213\15"
          + "\1\0\7\5\246\15\1\0\46\15\2\0\1\15\7\0\47\15\1\0"
          + "\1\24\7\0\55\5\1\0\1\5\1\0\2\5\1\0\2\5\1\0"
          + "\1\5\10\0\33\33\5\0\3\33\1\15\1\23\13\0\6\5\6\0"
          + "\2\24\2\0\13\5\1\0\1\5\3\0\53\15\25\5\12\20\1\0"
          + "\1\20\1\24\1\0\2\15\1\5\143\15\1\0\1\15\10\5\1\0"
          + "\6\5\2\15\2\5\1\0\4\5\2\15\12\20\3\15\2\0\1\15"
          + "\17\0\1\5\1\15\1\5\36\15\33\5\2\0\131\15\13\5\1\15"
          + "\16\0\12\20\41\15\11\5\2\15\2\0\1\24\1\0\1\15\5\0"
          + "\26\15\4\5\1\15\11\5\1\15\3\5\1\15\5\5\22\0\31\15"
          + "\3\5\104\0\25\15\1\0\10\15\26\0\60\5\66\15\3\5\1\15"
          + "\22\5\1\15\7\5\12\15\2\5\2\0\12\20\1\0\20\15\3\5"
          + "\1\0\10\15\2\0\2\15\2\0\26\15\1\0\7\15\1\0\1\15"
          + "\3\0\4\15\2\0\1\5\1\15\7\5\2\0\2\5\2\0\3\5"
          + "\1\15\10\0\1\5\4\0\2\15\1\0\3\15\2\5\2\0\12\20"
          + "\2\15\17\0\3\5\1\0\6\15\4\0\2\15\2\0\26\15\1\0"
          + "\7\15\1\0\2\15\1\0\2\15\1\0\2\15\2\0\1\5\1\0"
          + "\5\5\4\0\2\5\2\0\3\5\3\0\1\5\7\0\4\15\1\0"
          + "\1\15\7\0\12\20\2\5\3\15\1\5\13\0\3\5\1\0\11\15"
          + "\1\0\3\15\1\0\26\15\1\0\7\15\1\0\2\15\1\0\5\15"
          + "\2\0\1\5\1\15\10\5\1\0\3\5\1\0\3\5\2\0\1\15"
          + "\17\0\2\15\2\5\2\0\12\20\11\0\1\15\7\0\3\5\1\0"
          + "\10\15\2\0\2\15\2\0\26\15\1\0\7\15\1\0\2\15\1\0"
          + "\5\15\2\0\1\5\1\15\7\5\2\0\2\5\2\0\3\5\10\0"
          + "\2\5\4\0\2\15\1\0\3\15\2\5\2\0\12\20\1\0\1\15"
          + "\20\0\1\5\1\15\1\0\6\15\3\0\3\15\1\0\4\15\3\0"
          + "\2\15\1\0\1\15\1\0\2\15\3\0\2\15\3\0\3\15\3\0"
          + "\14\15\4\0\5\5\3\0\3\5\1\0\4\5\2\0\1\15\6\0"
          + "\1\5\16\0\12\20\20\0\4\5\1\0\10\15\1\0\3\15\1\0"
          + "\27\15\1\0\20\15\3\0\1\15\7\5\1\0\3\5\1\0\4\5"
          + "\7\0\2\5\1\0\3\15\5\0\2\15\2\5\2\0\12\20\20\0"
          + "\1\15\3\5\1\0\10\15\1\0\3\15\1\0\27\15\1\0\12\15"
          + "\1\0\5\15\2\0\1\5\1\15\7\5\1\0\3\5\1\0\4\5"
          + "\7\0\2\5\7\0\1\15\1\0\2\15\2\5\2\0\12\20\1\0"
          + "\2\15\16\0\3\5\1\0\10\15\1\0\3\15\1\0\51\15\2\0"
          + "\1\15\7\5\1\0\3\5\1\0\4\5\1\15\5\0\3\15\1\5"
          + "\7\0\3\15\2\5\2\0\12\20\12\0\6\15\2\0\2\5\1\0"
          + "\22\15\3\0\30\15\1\0\11\15\1\0\1\15\2\0\7\15\3\0"
          + "\1\5\4\0\6\5\1\0\1\5\1\0\10\5\6\0\12\20\2\0"
          + "\2\5\15\0\60\34\1\35\2\34\7\35\5\0\7\34\10\35\1\0"
          + "\12\20\47\0\2\34\1\0\1\34\2\0\2\34\1\0\1\34\2\0"
          + "\1\34\6\0\4\34\1\0\7\34\1\0\3\34\1\0\1\34\1\0"
          + "\1\34\2\0\2\34\1\0\4\34\1\35\2\34\6\35\1\0\2\35"
          + "\1\34\2\0\5\34\1\0\1\34\1\0\6\35\2\0\12\20\2\0"
          + "\4\34\40\0\1\15\27\0\2\5\6\0\12\20\13\0\1\5\1\0"
          + "\1\5\1\0\1\5\4\0\2\5\10\15\1\0\44\15\4\0\24\5"
          + "\1\0\2\5\5\15\13\5\1\0\44\5\11\0\1\5\71\0\53\34"
          + "\24\35\1\34\12\20\6\0\6\34\4\35\4\34\3\35\1\34\3\35"
          + "\2\34\7\35\3\34\4\35\15\34\14\35\1\34\1\35\12\20\4\35"
          + "\2\34\46\15\1\0\1\15\5\0\1\15\2\0\53\15\1\0\4\15"
          + "\u0100\17\111\15\1\0\4\15\2\0\7\15\1\0\1\15\1\0\4\15"
          + "\2\0\51\15\1\0\4\15\2\0\41\15\1\0\4\15\2\0\7\15"
          + "\1\0\1\15\1\0\4\15\2\0\17\15\1\0\71\15\1\0\4\15"
          + "\2\0\103\15\2\0\3\5\40\0\20\15\20\0\126\15\2\0\6\15"
          + "\3\0\u026c\15\2\0\21\15\1\0\32\15\5\0\113\15\3\0\13\15"
          + "\7\0\15\15\1\0\4\15\3\5\13\0\22\15\3\5\13\0\22\15"
          + "\2\5\14\0\15\15\1\0\3\15\1\0\2\5\14\0\64\34\40\35"
          + "\3\0\1\34\4\0\1\34\1\35\2\0\12\20\41\0\4\5\1\0"
          + "\12\20\6\0\130\15\10\0\5\15\2\5\42\15\1\5\1\15\5\0"
          + "\106\15\12\0\37\15\1\0\14\5\4\0\14\5\12\0\12\20\36\34"
          + "\2\0\5\34\13\0\54\34\4\0\32\34\6\0\12\20\1\34\3\0"
          + "\2\34\40\0\27\15\5\5\4\0\65\34\12\35\1\0\35\35\2\0"
          + "\1\5\12\20\6\0\12\20\6\0\16\34\2\0\17\5\101\0\5\5"
          + "\57\15\21\5\7\15\4\0\12\20\21\0\11\5\14\0\3\5\36\15"
          + "\15\5\2\15\12\20\54\15\16\5\14\0\44\15\24\5\10\0\12\20"
          + "\3\0\3\15\12\20\44\15\2\0\11\15\107\0\3\5\1\0\25\5"
          + "\4\15\1\5\4\15\3\5\2\15\1\0\2\5\6\0\300\15\66\5"
          + "\5\0\5\5\u0116\15\2\0\6\15\2\0\46\15\2\0\6\15\2\0"
          + "\10\15\1\0\1\15\1\0\1\15\1\0\1\15\1\0\37\15\2\0"
          + "\65\15\1\0\7\15\1\0\1\15\3\0\3\15\1\0\7\15\3\0"
          + "\4\15\2\0\6\15\4\0\15\15\5\0\3\15\1\0\7\15\17\0"
          + "\1\5\1\12\2\5\10\0\2\25\12\0\1\25\2\0\1\23\2\0"
          + "\5\5\1\26\14\0\1\4\2\0\2\26\3\0\1\24\4\0\1\4"
          + "\12\0\1\26\13\0\5\5\1\0\12\5\1\0\1\15\15\0\1\15"
          + "\20\0\15\15\63\0\23\5\1\10\15\5\21\0\1\15\4\0\1\15"
          + "\2\0\12\15\1\0\1\15\3\0\5\15\4\0\1\4\1\0\1\15"
          + "\1\0\1\15\1\0\1\15\1\0\4\15\1\0\12\15\1\16\2\0"
          + "\4\15\5\0\5\15\4\0\1\15\21\0\51\15\13\0\6\4\17\0"
          + "\2\4\u016f\0\2\4\14\0\1\4\137\0\1\4\106\0\1\4\31\0"
          + "\13\4\4\0\3\4\273\0\14\15\1\16\47\15\300\0\2\4\12\0"
          + "\1\4\11\0\1\4\72\0\4\4\1\0\5\4\1\4\1\0\7\4"
          + "\1\4\2\4\1\4\1\4\1\0\2\4\2\4\1\4\4\4\1\3"
          + "\2\4\1\4\1\4\2\4\2\4\1\4\3\4\1\4\3\4\2\4"
          + "\10\4\3\4\5\4\1\4\1\4\1\4\5\4\14\4\13\4\2\4"
          + "\2\4\1\4\1\4\2\4\1\4\1\4\22\4\1\4\2\4\2\4"
          + "\6\4\12\0\2\4\6\4\1\4\1\4\1\4\2\4\3\4\2\4"
          + "\10\4\2\4\4\4\2\4\13\4\2\4\5\4\2\4\2\4\1\4"
          + "\5\4\2\4\1\4\1\4\1\4\2\4\24\4\2\4\5\4\6\4"
          + "\1\4\2\4\1\3\1\4\2\4\1\4\4\4\1\4\2\4\1\4"
          + "\2\0\2\4\4\3\1\4\1\4\2\4\1\4\1\0\1\4\1\0"
          + "\1\4\6\0\1\4\3\0\1\4\6\0\1\4\12\0\2\4\17\0"
          + "\1\4\2\0\1\4\4\0\1\4\1\0\1\4\4\0\3\4\1\0"
          + "\1\4\13\0\2\4\3\4\55\0\3\4\11\0\1\4\16\0\1\4"
          + "\16\0\1\4\u0174\0\2\4\u01cf\0\3\4\23\0\2\4\63\0\1\4"
          + "\4\0\1\4\252\0\57\15\1\0\57\15\1\0\205\15\6\0\4\15"
          + "\3\5\2\15\14\0\46\15\1\0\1\15\5\0\1\15\2\0\70\15"
          + "\7\0\1\15\17\0\1\5\27\15\11\0\7\15\1\0\7\15\1\0"
          + "\7\15\1\0\7\15\1\0\7\15\1\0\7\15\1\0\7\15\1\0"
          + "\7\15\1\0\40\5\57\0\1\15\120\0\32\27\1\0\131\27\14\0"
          + "\326\27\57\0\1\15\1\0\1\27\31\0\11\27\6\5\1\4\5\22"
          + "\2\0\3\27\1\15\1\15\1\4\3\0\126\30\2\0\2\5\2\22"
          + "\3\30\133\22\1\0\4\22\5\0\51\15\3\0\136\17\21\0\33\15"
          + "\65\0\20\22\227\0\1\4\1\0\1\4\66\0\57\22\1\0\130\22"
          + "\250\0\u19b6\27\112\0\u51d6\27\52\0\u048d\15\103\0\56\15\2\0\u010d\15"
          + "\3\0\20\15\12\20\2\15\24\0\57\15\4\5\1\0\12\5\1\0"
          + "\37\15\2\5\120\15\2\5\45\0\11\15\2\0\147\15\2\0\44\15"
          + "\1\0\10\15\77\0\13\15\1\5\3\15\1\5\4\15\1\5\27\15"
          + "\5\5\30\0\64\15\14\0\2\5\62\15\22\5\12\0\12\20\6\0"
          + "\22\5\6\15\3\0\1\15\1\0\1\15\2\0\12\20\34\15\10\5"
          + "\2\0\27\15\15\5\14\0\35\17\3\0\4\5\57\15\16\5\16\0"
          + "\1\15\12\20\6\0\5\34\1\35\12\34\12\20\5\34\1\0\51\15"
          + "\16\5\11\0\3\15\1\5\10\15\2\5\2\0\12\20\6\0\33\34"
          + "\3\35\62\34\1\35\1\34\3\35\2\34\2\35\5\34\2\35\1\34"
          + "\1\35\1\34\30\0\5\34\13\15\5\5\2\0\3\15\2\5\12\0"
          + "\6\15\2\0\6\15\2\0\6\15\11\0\7\15\1\0\7\15\1\0"
          + "\53\15\1\0\12\15\12\0\163\15\10\5\1\0\2\5\2\0\12\20"
          + "\6\0\u2ba4\17\14\0\27\17\4\0\61\17\u2104\0\u016e\27\2\0\152\27"
          + "\46\0\7\15\14\0\5\15\5\0\1\33\1\5\12\33\1\0\15\33"
          + "\1\0\5\33\1\0\1\33\1\0\2\33\1\0\2\33\1\0\12\33"
          + "\142\15\41\0\u016b\15\22\0\100\15\2\0\66\15\50\0\14\15\4\0"
          + "\16\5\1\6\1\11\1\24\2\0\1\23\1\24\13\0\20\5\3\0"
          + "\2\26\30\0\3\26\1\24\1\0\1\25\1\0\1\24\1\23\32\0"
          + "\5\15\1\0\207\15\2\0\1\5\7\0\1\25\4\0\1\24\1\0"
          + "\1\25\1\0\12\20\1\23\1\24\5\0\32\15\4\0\1\26\1\0"
          + "\32\15\13\0\70\22\2\5\37\17\3\0\6\17\2\0\6\17\2\0"
          + "\6\17\2\0\3\17\34\0\3\5\4\0\14\15\1\0\32\15\1\0"
          + "\23\15\1\0\2\15\1\0\17\15\2\0\16\15\42\0\173\15\105\0"
          + "\65\15\210\0\1\5\202\0\35\15\3\0\61\15\17\0\1\5\37\0"
          + "\40\15\20\0\33\15\5\0\46\15\5\5\5\0\36\15\2\0\44\15"
          + "\4\0\10\15\1\0\5\15\52\0\236\15\2\0\12\20\6\0\44\15"
          + "\4\0\44\15\4\0\50\15\10\0\64\15\234\0\u0137\15\11\0\26\15"
          + "\12\0\10\15\230\0\6\15\2\0\1\15\1\0\54\15\1\0\2\15"
          + "\3\0\1\15\2\0\27\15\12\0\27\15\11\0\37\15\101\0\23\15"
          + "\1\0\2\15\12\0\26\15\12\0\32\15\106\0\70\15\6\0\2\15"
          + "\100\0\1\15\3\5\1\0\2\5\5\0\4\5\4\15\1\0\3\15"
          + "\1\0\33\15\4\0\3\5\4\0\1\5\40\0\35\15\3\0\35\15"
          + "\43\0\10\15\1\0\34\15\2\5\31\0\66\15\12\0\26\15\12\0"
          + "\23\15\15\0\22\15\156\0\111\15\67\0\63\15\15\0\63\15\u030d\0"
          + "\3\5\65\15\17\5\37\0\12\20\17\0\4\5\55\15\13\5\2\0"
          + "\1\5\22\0\31\15\7\0\12\20\6\0\3\5\44\15\16\5\1\0"
          + "\12\20\20\0\43\15\1\5\2\0\1\15\11\0\3\5\60\15\16\5"
          + "\4\15\5\0\3\5\3\0\12\20\1\15\1\0\1\15\43\0\22\15"
          + "\1\0\31\15\14\5\6\0\1\5\101\0\7\15\1\0\1\15\1\0"
          + "\4\15\1\0\17\15\1\0\12\15\7\0\57\15\14\5\5\0\12\20"
          + "\6\0\4\5\1\0\10\15\2\0\2\15\2\0\26\15\1\0\7\15"
          + "\1\0\2\15\1\0\5\15\2\0\1\5\1\15\7\5\2\0\2\5"
          + "\2\0\3\5\2\0\1\15\6\0\1\5\5\0\5\15\2\5\2\0"
          + "\7\5\3\0\5\5\213\0\65\15\22\5\4\15\5\0\12\20\46\0"
          + "\60\15\24\5\2\15\1\0\1\15\10\0\12\20\246\0\57\15\7\5"
          + "\2\0\11\5\27\0\4\15\2\5\42\0\60\15\21\5\3\0\1\15"
          + "\13\0\12\20\46\0\53\15\15\5\10\0\12\20\66\0\32\34\3\0"
          + "\17\35\4\0\12\20\2\34\3\0\1\34\u0160\0\100\15\12\20\25\0"
          + "\1\15\u01c0\0\71\15\u0107\0\11\15\1\0\45\15\10\5\1\0\10\5"
          + "\1\15\17\0\12\20\30\0\36\15\2\0\26\5\1\0\16\5\u0349\0"
          + "\u039a\15\146\0\157\15\21\0\304\15\u0abc\0\u042f\15\u0fd1\0\u0247\15\u21b9\0"
          + "\u0239\15\7\0\37\15\1\0\12\20\146\0\36\15\2\0\5\5\13\0"
          + "\60\15\7\5\11\0\4\15\14\0\12\20\11\0\25\15\5\0\23\15"
          + "\u0370\0\105\15\13\0\1\15\56\5\20\0\4\5\15\15\100\0\1\15"
          + "\u401f\0\1\22\1\30\u0bfe\0\153\15\5\0\15\15\3\0\11\15\7\0"
          + "\12\15\3\0\2\5\1\0\4\5\u14c1\0\5\5\3\0\26\5\2\0"
          + "\7\5\36\0\4\5\224\0\3\5\u01bb\0\125\15\1\0\107\15\1\0"
          + "\2\15\2\0\1\15\2\0\2\15\2\0\4\15\1\0\14\15\1\0"
          + "\1\15\1\0\7\15\1\0\101\15\1\0\4\15\2\0\10\15\1\0"
          + "\7\15\1\0\34\15\1\0\4\15\1\0\5\15\1\0\1\15\3\0"
          + "\7\15\1\0\u0154\15\2\0\31\15\1\0\31\15\1\0\37\15\1\0"
          + "\31\15\1\0\37\15\1\0\31\15\1\0\37\15\1\0\31\15\1\0"
          + "\37\15\1\0\31\15\1\0\10\15\2\0\62\20\u0200\0\67\5\4\0"
          + "\62\5\10\0\1\5\16\0\1\5\26\0\5\5\1\0\17\5\u0550\0"
          + "\7\5\1\0\21\5\2\0\7\5\1\0\2\5\1\0\5\5\u07d5\0"
          + "\305\15\13\0\7\5\51\0\104\15\7\5\5\0\12\20\u04a6\0\4\15"
          + "\1\0\33\15\1\0\2\15\1\0\1\15\2\0\1\15\1\0\12\15"
          + "\1\0\4\15\1\0\1\15\1\0\1\15\6\0\1\15\4\0\1\15"
          + "\1\0\1\15\1\0\1\15\1\0\3\15\1\0\2\15\1\0\1\15"
          + "\2\0\1\15\1\0\1\15\1\0\1\15\1\0\1\15\1\0\1\15"
          + "\1\0\2\15\1\0\1\15\2\0\4\15\1\0\7\15\1\0\4\15"
          + "\1\0\4\15\1\0\1\15\1\0\12\15\1\0\21\15\5\0\3\15"
          + "\1\0\5\15\1\0\21\15\u0144\0\4\4\1\4\312\4\1\4\60\4"
          + "\15\0\3\4\37\0\1\4\32\15\6\0\32\15\2\0\4\4\2\16"
          + "\14\15\2\16\12\15\4\0\1\4\2\0\12\4\22\0\71\4\32\1"
          + "\1\30\2\4\15\4\12\0\1\4\24\0\1\4\2\0\11\4\1\0"
          + "\4\4\11\0\7\4\2\4\256\4\42\4\2\4\141\4\1\3\16\4"
          + "\2\4\2\4\1\4\3\4\2\4\44\4\3\3\2\4\1\3\2\4"
          + "\3\3\44\4\2\4\3\4\1\4\4\4\5\2\102\4\2\3\2\4"
          + "\13\3\25\4\4\3\4\4\1\3\1\4\11\3\3\4\1\3\4\4"
          + "\3\3\1\4\3\3\42\4\1\3\123\4\1\4\77\4\10\0\3\4"
          + "\6\4\1\4\30\4\7\4\2\4\2\4\1\4\2\3\4\4\1\3"
          + "\14\4\1\4\2\4\4\4\2\4\1\3\4\4\2\3\15\4\2\4"
          + "\2\4\1\4\10\4\2\4\11\4\1\4\5\4\3\4\14\4\3\4"
          + "\10\4\3\4\2\4\1\4\1\4\1\4\4\4\1\4\6\4\1\4"
          + "\3\4\1\4\6\4\113\4\3\3\3\4\5\3\60\0\43\4\1\3"
          + "\20\4\3\3\11\4\1\3\5\4\5\4\1\4\1\3\6\4\15\4"
          + "\6\4\3\4\1\4\1\4\2\4\3\4\1\4\2\4\7\4\6\4"
          + "\164\0\14\4\125\0\53\4\14\0\4\4\70\0\10\4\12\0\6\4"
          + "\50\0\10\4\36\0\122\4\14\0\4\4\10\4\5\3\1\4\2\3"
          + "\6\4\1\3\11\4\12\3\1\4\1\0\1\4\2\3\1\4\6\4"
          + "\1\0\52\4\2\4\4\4\3\4\1\4\1\4\47\4\15\4\5\4"
          + "\2\3\1\4\2\3\6\4\3\4\15\4\1\4\15\3\42\4\u05fe\4"
          + "\2\0\ua6d7\27\51\0\u1035\27\13\0\336\27\2\0\u1682\27\u295e\0\u021e\27"
          + "\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\uffff\0\u05ee\0"
          + "\1\5\36\0\137\13\1\14\200\0\360\5\uffff\0\uffff\0\ufe12\0";

  /** Translates characters to character classes */
  private static final char[] ZZ_CMAP = zzUnpackCMap(ZZ_CMAP_PACKED);

  /** Translates DFA states to action switch labels. */
  private static final int[] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
      "\1\0\2\1\3\2\2\1\1\3\1\2\1\4\2\5"
          + "\1\6\1\1\1\7\1\10\1\3\1\11\1\2\1\0"
          + "\4\2\1\0\1\2\2\0\1\3\1\0\1\3\2\2"
          + "\1\0\1\5\1\2\1\5\1\0\2\3\1\0\2\2"
          + "\2\0\1\2\1\0\2\3\5\2\1\0\1\2\1\3"
          + "\3\2";

  private static int[] zzUnpackAction() {
    int[] result = new int[61];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value;
      while (--count > 0);
    }
    return j;
  }

  /** Translates a state to a row index in the transition table */
  private static final int[] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
      "\0\0\0\36\0\74\0\132\0\170\0\226\0\264\0\322"
          + "\0\360\0\u010e\0\u012c\0\u014a\0\u0168\0\u0186\0\u01a4\0\u01c2"
          + "\0\u01e0\0\u01fe\0\u021c\0\u023a\0\74\0\u0258\0\u0276\0\u0294"
          + "\0\u02b2\0\264\0\u02d0\0\u02ee\0\322\0\u030c\0\u032a\0\u0348"
          + "\0\u0366\0\u0384\0\u03a2\0\u03c0\0\u03de\0\u03fc\0\u01a4\0\u041a"
          + "\0\u0438\0\u0456\0\u0474\0\u0492\0\u04b0\0\u04ce\0\u04ec\0\u050a"
          + "\0\u0528\0\u0546\0\u0564\0\u0582\0\u05a0\0\u05be\0\u05dc\0\u05fa"
          + "\0\36\0\u0618\0\360\0\u0636\0\u0654";

  private static int[] zzUnpackRowMap() {
    int[] result = new int[61];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** The transition table of the DFA */
  private static final int[] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
      "\1\2\1\3\1\4\1\5\1\6\2\2\1\7\2\2"
          + "\1\10\2\2\1\11\1\12\1\13\1\14\1\15\1\16"
          + "\3\2\1\17\1\20\1\21\2\2\1\22\2\23\37\0"
          + "\1\24\3\0\2\25\1\0\5\25\20\0\1\25\5\0"
          + "\1\4\2\0\1\4\1\0\1\26\2\4\20\0\1\4"
          + "\2\0\1\4\2\0\1\5\2\0\1\5\1\27\1\30"
          + "\2\5\20\0\1\5\5\0\1\6\2\0\1\6\1\27"
          + "\1\31\2\6\20\0\1\6\5\0\1\32\2\0\1\33"
          + "\1\34\3\32\20\0\1\32\3\0\1\5\1\6\5\0"
          + "\1\35\3\0\1\6\24\0\2\11\1\0\10\11\2\36"
          + "\1\0\1\37\1\0\1\37\1\40\2\0\1\37\1\0"
          + "\1\22\1\0\1\11\5\0\1\12\1\11\1\0\1\12"
          + "\1\41\1\42\2\12\3\11\2\36\1\0\1\37\1\0"
          + "\1\37\1\40\2\0\1\37\1\0\1\22\1\0\1\12"
          + "\5\0\2\13\1\0\5\13\2\11\1\13\2\36\1\0"
          + "\1\37\1\0\1\37\1\40\2\0\1\37\1\0\1\22"
          + "\1\0\1\13\5\0\2\14\1\0\5\14\3\11\2\14"
          + "\2\0\2\43\1\44\2\0\1\43\1\0\1\22\1\0"
          + "\1\14\5\0\1\15\1\14\1\0\1\45\1\46\3\15"
          + "\3\11\2\14\2\0\2\43\1\44\2\0\1\43\1\0"
          + "\1\22\1\0\1\15\5\0\2\16\1\0\5\16\5\0"
          + "\1\16\3\0\1\40\6\0\1\16\5\0\2\47\1\0"
          + "\5\47\3\11\2\14\1\50\3\0\1\47\4\0\1\22"
          + "\1\0\1\47\5\0\2\20\1\0\5\20\20\0\1\20"
          + "\5\0\2\21\1\0\5\21\20\0\1\21\5\0\2\22"
          + "\1\0\5\22\3\11\2\36\1\0\1\37\1\0\1\37"
          + "\1\40\2\0\1\51\1\52\1\22\1\0\1\22\5\0"
          + "\2\23\1\0\5\23\17\0\2\23\5\0\2\24\1\0"
          + "\5\24\20\0\1\24\2\0\1\4\1\53\1\54\1\4"
          + "\2\0\1\4\1\0\1\26\2\4\1\0\1\54\16\0"
          + "\1\4\12\0\1\55\1\56\24\0\1\4\1\53\1\54"
          + "\1\5\2\0\1\5\1\27\1\30\2\5\1\0\1\54"
          + "\16\0\1\5\2\0\1\4\1\53\1\54\1\6\2\0"
          + "\1\6\1\27\1\31\2\6\1\0\1\54\16\0\1\6"
          + "\5\0\1\33\2\0\1\33\1\34\3\33\20\0\1\33"
          + "\10\0\1\57\32\0\2\36\1\0\5\36\3\11\2\36"
          + "\2\0\2\60\1\40\2\0\1\60\1\0\1\22\1\0"
          + "\1\36\5\0\2\37\1\0\5\37\3\11\13\0\1\11"
          + "\1\0\1\37\5\0\2\40\1\0\5\40\3\11\2\36"
          + "\1\50\3\0\1\40\4\0\1\22\1\0\1\40\5\0"
          + "\2\11\1\0\2\11\1\61\1\62\4\11\2\36\1\0"
          + "\1\37\1\0\1\37\1\40\2\0\1\37\1\0\1\22"
          + "\1\0\1\11\2\0\1\4\1\53\1\54\1\12\1\11"
          + "\1\0\1\12\1\41\1\42\2\12\1\11\1\63\1\11"
          + "\2\36\1\0\1\37\1\0\1\37\1\40\2\0\1\37"
          + "\1\0\1\22\1\0\1\12\5\0\2\43\1\0\5\43"
          + "\3\0\2\14\13\0\1\43\5\0\2\44\1\0\5\44"
          + "\3\11\2\14\1\50\3\0\1\44\4\0\1\22\1\0"
          + "\1\44\5\0\1\45\1\14\1\0\1\45\1\46\3\45"
          + "\3\11\2\14\2\0\2\43\1\44\2\0\1\43\1\0"
          + "\1\22\1\0\1\45\5\0\2\14\1\0\1\64\4\14"
          + "\3\11\2\14\2\0\2\43\1\44\2\0\1\43\1\0"
          + "\1\22\1\0\1\14\5\0\2\50\1\0\5\50\5\0"
          + "\1\50\3\0\1\40\6\0\1\50\5\0\2\51\1\0"
          + "\5\51\3\11\2\36\4\0\1\40\4\0\1\22\1\0"
          + "\1\51\5\0\2\52\1\0\5\52\16\0\1\51\1\0"
          + "\1\52\2\0\1\4\2\0\1\53\2\0\1\53\1\65"
          + "\1\66\2\53\20\0\1\53\5\0\1\54\2\0\1\54"
          + "\1\65\1\67\2\54\20\0\1\54\2\0\1\4\1\53"
          + "\1\54\5\0\1\70\3\0\1\54\32\0\1\56\1\71"
          + "\26\0\1\57\2\0\1\57\1\0\3\57\20\0\1\57"
          + "\5\0\2\60\1\0\5\60\3\0\2\36\13\0\1\60"
          + "\2\0\1\4\1\53\1\54\2\11\1\0\2\11\1\72"
          + "\3\11\1\63\1\11\2\36\1\0\1\37\1\0\1\37"
          + "\1\40\2\0\1\37\1\0\1\22\1\0\1\11\5\0"
          + "\2\11\1\0\3\11\1\62\1\73\3\11\2\36\1\0"
          + "\1\37\1\0\1\37\1\40\2\0\1\37\1\0\1\22"
          + "\1\0\1\11\5\0\1\63\1\11\1\0\1\63\1\74"
          + "\1\75\2\63\3\11\2\36\1\0\1\37\1\0\1\37"
          + "\1\40\2\0\1\37\1\0\1\22\1\0\1\63\5\0"
          + "\1\64\1\14\1\0\1\64\1\14\3\64\3\11\2\14"
          + "\2\0\2\43\1\44\2\0\1\43\1\0\1\22\1\0"
          + "\1\64\12\0\1\55\25\0\1\4\1\53\1\54\1\53"
          + "\2\0\1\53\1\65\1\66\2\53\1\0\1\54\16\0"
          + "\1\53\2\0\1\4\1\53\2\54\2\0\1\54\1\65"
          + "\1\67\2\54\1\0\1\54\16\0\1\54\3\0\1\53"
          + "\1\54\5\0\1\70\3\0\1\54\22\0\1\53\1\54"
          + "\2\11\1\0\2\11\1\72\3\11\1\63\1\11\2\36"
          + "\1\0\1\37\1\0\1\37\1\40\2\0\1\37\1\0"
          + "\1\22\1\0\1\11\5\0\2\11\1\0\2\11\1\61"
          + "\5\11\2\36\1\0\1\37\1\0\1\37\1\40\2\0"
          + "\1\37\1\0\1\22\1\0\1\11\2\0\1\4\1\53"
          + "\1\54\1\63\1\11\1\0\1\63\1\74\1\75\2\63"
          + "\1\11\1\63\1\11\2\36\1\0\1\37\1\0\1\37"
          + "\1\40\2\0\1\37\1\0\1\22\1\0\1\63";

  private static int[] zzUnpackTrans() {
    int[] result = new int[1650];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value;
      while (--count > 0);
    }
    return j;
  }

  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String ZZ_ERROR_MSG[] = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /** ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code> */
  private static final int[] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
      "\1\0\1\11\22\1\1\0\4\1\1\0\1\1\2\0"
          + "\1\1\1\0\3\1\1\0\3\1\1\0\2\1\1\0"
          + "\2\1\2\0\1\1\1\0\7\1\1\0\1\11\4\1";

  private static int[] zzUnpackAttribute() {
    int[] result = new int[61];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int[] result) {
    int i = 0; /* index in packed string  */
    int j = offset; /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value;
      while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /**
   * this buffer contains the current text to be matched and is the source of the yytext() string
   */
  private char zzBuffer[] = new char[ZZ_BUFFERSIZE];

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read from input */
  private int zzEndRead;

  /** number of newlines encountered up to the start of the matched text */
  private int yyline;

  /** the number of characters up to the start of the matched text */
  private int yychar;

  /** the number of characters from the last newline up to the start of the matched text */
  private int yycolumn;

  /** zzAtBOL == true iff the scanner is currently at the beginning of a line */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true iff the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /**
   * The number of occupied positions in zzBuffer beyond zzEndRead. When a lead/high surrogate has
   * been read from the input stream into the final zzBuffer position, this will have a value of 1;
   * otherwise, it will have a value of 0.
   */
  private int zzFinalHighSurrogate = 0;

  /* user code: */
  /** Alphanumeric sequences */
  public static final int WORD_TYPE = StandardTokenizer.ALPHANUM;

  /** Numbers */
  public static final int NUMERIC_TYPE = StandardTokenizer.NUM;

  /**
   * Chars in class \p{Line_Break = Complex_Context} are from South East Asian scripts (Thai, Lao,
   * Myanmar, Khmer, etc.). Sequences of these are kept together as as a single token rather than
   * broken up, because the logic required to break them at word boundaries is too complex for
   * UAX#29.
   *
   * <p>See Unicode Line Breaking Algorithm: http://www.unicode.org/reports/tr14/#SA
   */
  public static final int SOUTH_EAST_ASIAN_TYPE = StandardTokenizer.SOUTHEAST_ASIAN;

  /** Ideographic token type */
  public static final int IDEOGRAPHIC_TYPE = StandardTokenizer.IDEOGRAPHIC;

  /** Hiragana token type */
  public static final int HIRAGANA_TYPE = StandardTokenizer.HIRAGANA;

  /** Katakana token type */
  public static final int KATAKANA_TYPE = StandardTokenizer.KATAKANA;

  /** Hangul token type */
  public static final int HANGUL_TYPE = StandardTokenizer.HANGUL;

  /** Emoji token type */
  public static final int EMOJI_TYPE = StandardTokenizer.EMOJI;

  /** Character count processed so far */
  public final int yychar() {
    return yychar;
  }

  /** Fills CharTermAttribute with the current token text. */
  public final void getText(CharTermAttribute t) {
    t.copyBuffer(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
  }

  /** Sets the scanner buffer size in chars */
  public final void setBufferSize(int numChars) {
    ZZ_BUFFERSIZE = numChars;
    char[] newZzBuffer = new char[ZZ_BUFFERSIZE];
    System.arraycopy(zzBuffer, 0, newZzBuffer, 0, Math.min(zzBuffer.length, ZZ_BUFFERSIZE));
    zzBuffer = newZzBuffer;
  }

  /**
   * Creates a new scanner
   *
   * @param in the java.io.Reader to read input from.
   */
  public StandardTokenizerImpl(java.io.Reader in) {
    this.zzReader = in;
  }

  /**
   * Unpacks the compressed character translation table.
   *
   * @param packed the packed character translation table
   * @return the unpacked character translation table
   */
  private static char[] zzUnpackCMap(String packed) {
    char[] map = new char[0x110000];
    int i = 0; /* index in packed string  */
    int j = 0; /* index in unpacked array */
    while (i < 4122) {
      int count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value;
      while (--count > 0);
    }
    return map;
  }

  /* -------------------------------------------------------------------------------- */
  /* Begin Lucene-specific disable-buffer-expansion modifications to skeleton.default */

  /**
   * Refills the input buffer.
   *
   * @return <code>false</code>, iff there was new input.
   * @exception java.io.IOException if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {

    /* first: make room (if you can) */
    if (zzStartRead > 0) {
      zzEndRead += zzFinalHighSurrogate;
      zzFinalHighSurrogate = 0;
      System.arraycopy(zzBuffer, zzStartRead, zzBuffer, 0, zzEndRead - zzStartRead);

      /* translate stored positions */
      zzEndRead -= zzStartRead;
      zzCurrentPos -= zzStartRead;
      zzMarkedPos -= zzStartRead;
      zzStartRead = 0;
    }

    /* fill the buffer with new input */
    int requested = zzBuffer.length - zzEndRead - zzFinalHighSurrogate;
    if (requested == 0) {
      return true;
    }
    int numRead = zzReader.read(zzBuffer, zzEndRead, requested);

    /* not supposed to occur according to specification of java.io.Reader */
    if (numRead == 0) {
      throw new java.io.IOException(
          "Reader returned 0 characters. See JFlex examples for workaround.");
    }
    if (numRead > 0) {
      zzEndRead += numRead;
      if (Character.isHighSurrogate(zzBuffer[zzEndRead - 1])) {
        if (numRead
            == requested) { // We might have requested too few chars to encode a full Unicode
          // character.
          --zzEndRead;
          zzFinalHighSurrogate = 1;
          if (numRead == 1) {
            return true;
          }
        } else { // There is room in the buffer for at least one more char
          int c = zzReader.read(); // Expecting to read a low surrogate char
          if (c == -1) {
            return true;
          } else {
            zzBuffer[zzEndRead++] = (char) c;
            return false;
          }
        }
      }
      /* potentially more input available */
      return false;
    }

    /* numRead < 0 ==> end of stream */
    return true;
  }

  /* End Lucene-specific disable-buffer-expansion modifications to skeleton.default */
  /* ------------------------------------------------------------------------------ */

  /** Closes the input stream. */
  public final void yyclose() throws java.io.IOException {
    zzAtEOF = true; /* indicate end of file */
    zzEndRead = zzStartRead; /* invalidate buffer    */

    if (zzReader != null) zzReader.close();
  }

  /**
   * Resets the scanner to read from a new input stream. Does not close the old reader.
   *
   * <p>All internal variables are reset, the old input stream <b>cannot</b> be reused (internal
   * buffer is discarded and lost). Lexical state is set to <code>ZZ_INITIAL</code>.
   *
   * <p>Internal scan buffer is resized down to its initial length, if it has grown.
   *
   * @param reader the new input stream
   */
  public final void yyreset(java.io.Reader reader) {
    zzReader = reader;
    zzAtBOL = true;
    zzAtEOF = false;
    zzEOFDone = false;
    zzEndRead = zzStartRead = 0;
    zzCurrentPos = zzMarkedPos = 0;
    zzFinalHighSurrogate = 0;
    yyline = yychar = yycolumn = 0;
    zzLexicalState = YYINITIAL;
    if (zzBuffer.length > ZZ_BUFFERSIZE) zzBuffer = new char[ZZ_BUFFERSIZE];
  }

  /** Returns the current lexical state. */
  public final int yystate() {
    return zzLexicalState;
  }

  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }

  /** Returns the text matched by the current regular expression. */
  public final String yytext() {
    return new String(zzBuffer, zzStartRead, zzMarkedPos - zzStartRead);
  }

  /**
   * Returns the character at position <code>pos</code> from the matched text.
   *
   * <p>It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch. A value from 0 to yylength()-1.
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer[zzStartRead + pos];
  }

  /** Returns the length of the matched text region. */
  public final int yylength() {
    return zzMarkedPos - zzStartRead;
  }

  /**
   * Reports an error that occured while scanning.
   *
   * <p>In a wellformed scanner (no or only correct usage of yypushback(int) and a match-all
   * fallback rule) this method will only be called with things that "Can't Possibly Happen". If
   * this method is called, something is seriously wrong (e.g. a JFlex bug producing a faulty
   * scanner etc.).
   *
   * <p>Usual syntax/scanner level error handling should be done in error fallback rules.
   *
   * @param errorCode the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    } catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }

  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * <p>They will be read again by then next call of the scanning method
   *
   * @param number the number of characters to be read again. This number must not be greater than
   *     yylength()!
   */
  public void yypushback(int number) {
    if (number > yylength()) zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }

  /**
   * Resumes scanning until the next regular expression is matched, the end of input is encountered
   * or an I/O-Error occurs.
   *
   * @return the next token
   * @exception java.io.IOException if any I/O-Error occurs
   */
  public int getNextToken() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    char[] zzBufferL = zzBuffer;
    char[] zzCMapL = ZZ_CMAP;

    int[] zzTransL = ZZ_TRANS;
    int[] zzRowMapL = ZZ_ROWMAP;
    int[] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      yychar += zzMarkedPosL - zzStartRead;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ((zzAttributes & 1) == 1) {
        zzAction = zzState;
      }

      zzForAction:
      {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
            zzCurrentPosL += Character.charCount(zzInput);
          } else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          } else {
            // store back cached positions
            zzCurrentPos = zzCurrentPosL;
            zzMarkedPos = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL = zzCurrentPos;
            zzMarkedPosL = zzMarkedPos;
            zzBufferL = zzBuffer;
            zzEndReadL = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            } else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL, zzEndReadL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[zzRowMapL[zzState] + zzCMapL[zzInput]];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ((zzAttributes & 1) == 1) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ((zzAttributes & 8) == 8) break zzForAction;
          }
        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        {
          return YYEOF;
        }
      } else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1:
            {
              /* Break so we don't hit fall-through warning: */
              break; /* Not numeric, word, ideographic, hiragana, emoji or SE Asian -- ignore it. */
            }
            // fall through
          case 10:
            break;
          case 2:
            {
              return EMOJI_TYPE;
            }
            // fall through
          case 11:
            break;
          case 3:
            {
              return WORD_TYPE;
            }
            // fall through
          case 12:
            break;
          case 4:
            {
              return HANGUL_TYPE;
            }
            // fall through
          case 13:
            break;
          case 5:
            {
              return NUMERIC_TYPE;
            }
            // fall through
          case 14:
            break;
          case 6:
            {
              return KATAKANA_TYPE;
            }
            // fall through
          case 15:
            break;
          case 7:
            {
              return IDEOGRAPHIC_TYPE;
            }
            // fall through
          case 16:
            break;
          case 8:
            {
              return HIRAGANA_TYPE;
            }
            // fall through
          case 17:
            break;
          case 9:
            {
              return SOUTH_EAST_ASIAN_TYPE;
            }
            // fall through
          case 18:
            break;
          default:
            zzScanError(ZZ_NO_MATCH);
        }
      }
    }
  }
}
