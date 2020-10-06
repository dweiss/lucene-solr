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

package org.apache.lucene.analysis.util;

import org.apache.lucene.util.Bits;
import org.apache.lucene.util.SparseFixedBitSet;

/**
 * This file contains unicode properties used by various {@link CharTokenizer}s. The data was
 * created using ICU4J v62.2.0.0
 *
 * <p>Unicode version: 11.0.0.0
 */
public final class UnicodeProps {
  private UnicodeProps() {}

  /** Unicode version that was used to generate this file: {@value} */
  public static final String UNICODE_VERSION = "11.0.0.0";

  /** Bitset with Unicode WHITESPACE code points. */
  public static final Bits WHITESPACE =
      createBits(
          0x0009, 0x000A, 0x000B, 0x000C, 0x000D, 0x0020, 0x0085, 0x00A0, 0x1680, 0x2000, 0x2001,
          0x2002, 0x2003, 0x2004, 0x2005, 0x2006, 0x2007, 0x2008, 0x2009, 0x200A, 0x2028, 0x2029,
          0x202F, 0x205F, 0x3000);

  private static Bits createBits(final int... codepoints) {
    final int len = codepoints[codepoints.length - 1] + 1;
    final SparseFixedBitSet bitset = new SparseFixedBitSet(len);
    for (int i : codepoints) bitset.set(i);
    return new Bits() {
      @Override
      public boolean get(int index) {
        return index < len && bitset.get(index);
      }

      @Override
      public int length() {
        return 0x10FFFF + 1;
      }
    };
  }
}
