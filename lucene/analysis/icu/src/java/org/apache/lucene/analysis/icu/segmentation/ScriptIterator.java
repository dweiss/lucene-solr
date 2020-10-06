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
package org.apache.lucene.analysis.icu.segmentation;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterEnums.ECharacterCategory;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.UTF16;

/**
 * An iterator that locates ISO 15924 script boundaries in text.
 *
 * <p>This is not the same as simply looking at the Unicode block, or even the Script property. Some
 * characters are 'common' across multiple scripts, and some 'inherit' the script value of text
 * surrounding them.
 *
 * <p>This is similar to ICU (internal-only) UScriptRun, with the following differences:
 *
 * <ul>
 *   <li>Doesn't attempt to match paired punctuation. For tokenization purposes, this is not
 *       necessary. It's also quite expensive.
 *   <li>Non-spacing marks inherit the script of their base character, following recommendations
 *       from UTR #24.
 * </ul>
 *
 * @lucene.experimental
 */
final class ScriptIterator {
  private char text[];
  private int start;
  private int limit;
  private int index;

  private int scriptStart;
  private int scriptLimit;
  private int scriptCode;

  private final boolean combineCJ;

  /** @param combineCJ if true: Han,Hiragana,Katakana will all return as {@link UScript#JAPANESE} */
  ScriptIterator(boolean combineCJ) {
    this.combineCJ = combineCJ;
  }

  /**
   * Get the start of this script run
   *
   * @return start position of script run
   */
  int getScriptStart() {
    return scriptStart;
  }

  /**
   * Get the index of the first character after the end of this script run
   *
   * @return position of the first character after this script run
   */
  int getScriptLimit() {
    return scriptLimit;
  }

  /**
   * Get the UScript script code for this script run
   *
   * @return code for the script of the current run
   */
  int getScriptCode() {
    return scriptCode;
  }

  /**
   * Iterates to the next script run, returning true if one exists.
   *
   * @return true if there is another script run, false otherwise.
   */
  boolean next() {
    if (scriptLimit >= limit) return false;

    scriptCode = UScript.COMMON;
    scriptStart = scriptLimit;

    while (index < limit) {
      final int ch = UTF16.charAt(text, start, limit, index - start);
      final int sc = getScript(ch);

      /*
       * From UTR #24: Implementations that determine the boundaries between
       * characters of given scripts should never break between a non-spacing
       * mark and its base character. Thus for boundary determinations and
       * similar sorts of processing, a non-spacing mark — whatever its script
       * value — should inherit the script value of its base character.
       */
      if (isSameScript(scriptCode, sc)
          || UCharacter.getType(ch) == ECharacterCategory.NON_SPACING_MARK) {
        index += UTF16.getCharCount(ch);

        /*
         * Inherited or Common becomes the script code of the surrounding text.
         */
        if (scriptCode <= UScript.INHERITED && sc > UScript.INHERITED) {
          scriptCode = sc;
        }

      } else {
        break;
      }
    }

    scriptLimit = index;
    return true;
  }

  /** Determine if two scripts are compatible. */
  private static boolean isSameScript(int scriptOne, int scriptTwo) {
    return scriptOne <= UScript.INHERITED
        || scriptTwo <= UScript.INHERITED
        || scriptOne == scriptTwo;
  }

  /**
   * Set a new region of text to be examined by this iterator
   *
   * @param text text buffer to examine
   * @param start offset into buffer
   * @param length maximum length to examine
   */
  void setText(char text[], int start, int length) {
    this.text = text;
    this.start = start;
    this.index = start;
    this.limit = start + length;
    this.scriptStart = start;
    this.scriptLimit = start;
    this.scriptCode = UScript.INVALID_CODE;
  }

  /** linear fast-path for basic latin case */
  private static final int basicLatin[] = new int[128];

  static {
    for (int i = 0; i < basicLatin.length; i++) basicLatin[i] = UScript.getScript(i);
  }

  /** fast version of UScript.getScript(). Basic Latin is an array lookup */
  private int getScript(int codepoint) {
    if (0 <= codepoint && codepoint < basicLatin.length) {
      return basicLatin[codepoint];
    } else {
      int script = UScript.getScript(codepoint);
      if (combineCJ) {
        if (script == UScript.HAN || script == UScript.HIRAGANA || script == UScript.KATAKANA) {
          return UScript.JAPANESE;
        } else if (codepoint >= 0xFF10 && codepoint <= 0xFF19) {
          // when using CJK dictionary breaking, don't let full width numbers go to it, otherwise
          // they are treated as punctuation. we currently have no cleaner way to fix this!
          return UScript.LATIN;
        } else {
          return script;
        }
      } else {
        return script;
      }
    }
  }
}
