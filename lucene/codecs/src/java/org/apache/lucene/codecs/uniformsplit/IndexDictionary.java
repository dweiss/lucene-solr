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
package org.apache.lucene.codecs.uniformsplit;

import java.io.IOException;
import org.apache.lucene.store.DataOutput;
import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOSupplier;

/**
 * Immutable stateless index dictionary kept in RAM.
 *
 * <p>Implementations must be immutable.
 *
 * <p>Use {@link IndexDictionary.Builder} to build the {@link IndexDictionary}.
 *
 * <p>Create a stateful {@link IndexDictionary.Browser} to seek a term in this {@link
 * IndexDictionary} and get its corresponding block file pointer to the terms block file.
 *
 * <p>There is a single implementation of this interface, {@link FSTDictionary}. However this
 * interface allows you to plug easily a new kind of index dictionary to experiment and improve the
 * existing one.
 *
 * @lucene.experimental
 */
public interface IndexDictionary extends Accountable {

  /**
   * Writes this dictionary to the provided output.
   *
   * @param blockEncoder The {@link BlockEncoder} for specific encoding of this index dictionary; or
   *     null if none.
   */
  void write(DataOutput output, BlockEncoder blockEncoder) throws IOException;

  /** Creates a new {@link IndexDictionary.Browser}. */
  Browser browser() throws IOException;

  /** Builds an immutable {@link IndexDictionary}. */
  interface Builder {

    /**
     * Adds a [block key - block file pointer] entry to the dictionary.
     *
     * <p>The Uniform Split technique adds block keys in the dictionary. See {@link BlockReader} and
     * {@link TermBytes} for more info about block key and minimal distinguishing prefix (MDP).
     *
     * <p>All block keys are added in strictly increasing order of the block file pointers, this
     * allows long encoding optimizations such as with {@link
     * org.apache.lucene.util.fst.PositiveIntOutputs} for {@link org.apache.lucene.util.fst.FST}.
     *
     * @param blockKey The block key which is the minimal distinguishing prefix (MDP) of the first
     *     term of a block.
     * @param blockFilePointer Non-negative file pointer to the start of the block in the block
     *     file.
     */
    void add(BytesRef blockKey, long blockFilePointer) throws IOException;

    /** Builds the immutable {@link IndexDictionary} for the added entries. */
    IndexDictionary build() throws IOException;
  }

  /**
   * Stateful {@link IndexDictionary.Browser} to seek a term in this {@link IndexDictionary} and get
   * its corresponding block file pointer in the block file.
   */
  interface Browser {

    /**
     * Seeks the given term in the {@link IndexDictionary} and returns its corresponding block file
     * pointer.
     *
     * @return The block file pointer corresponding to the term if it matches exactly a block key in
     *     the dictionary. Otherwise the floor block key, which is the greatest block key present in
     *     the dictionary that is alphabetically preceding the searched term. Otherwise {@code -1}
     *     if there is no floor block key because the searched term precedes alphabetically the
     *     first block key of the dictionary.
     */
    long seekBlock(BytesRef term) throws IOException;
  }

  /**
   * Supplier for a new stateful {@link Browser} created on the immutable {@link IndexDictionary}.
   *
   * <p>The immutable {@link IndexDictionary} is lazy loaded thread safely. This lazy loading allows
   * us to load it only when {@link org.apache.lucene.index.TermsEnum#seekCeil} or {@link
   * org.apache.lucene.index.TermsEnum#seekExact} are called (it is not loaded for a direct
   * all-terms enumeration).
   */
  interface BrowserSupplier extends IOSupplier<Browser>, Accountable {}
}
