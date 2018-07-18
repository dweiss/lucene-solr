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
package org.apache.lucene.store;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.PagedBytes;

/** 
 * Represents a file in RAM as a list of byte[] buffers.
 * @lucene.internal */
public class RAMFile implements Accountable {
  private volatile PagedBytes content;

  void setContent(PagedBytes newContent) {
    this.content = content;
  }

  public long getLength() {
    PagedBytes local = this.content;
    return local == null ? 0 : local.getPointer();
  }

  @Override
  public long ramBytesUsed() {
    PagedBytes local = this.content;
    return local == null ? 0 : local.ramBytesUsed();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(length=" + getLength() + ")";
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException();
  }

  IndexInput openInput() {
    PagedBytes local = this.content;
    if (local == null) {
      return new ByteArrayIndexInput("content", new byte [0]);
    } else {
      return new IndexInput() {
        DataInput in = local.getDataInput();

        @Override
        public void close() throws IOException {
          in = null;
        }

        @Override
        public long getFilePointer() {
          return 0;
        }

        @Override
        public void seek(long pos) throws IOException {
        }

        @Override
        public long length() {
          return 0;
        }

        @Override
        public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
          return null;
        }

        @Override
        public byte readByte() throws IOException {
          return 0;
        }

        @Override
        public void readBytes(byte[] b, int offset, int len) throws IOException {

        }
      };
    }
  }
}
