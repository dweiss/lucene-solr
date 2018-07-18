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
import java.util.Collection;
import java.util.Collections;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.lucene.util.Accountable;
import org.apache.lucene.util.Accountables;
import org.apache.lucene.util.PagedBytes;

/**
 * A memory-resident {@link IndexOutput} implementation.
 *
 * @lucene.internal
 */
public class RAMOutputStream extends IndexOutput implements Accountable {
  private final static int BLOCK_BITS = 12; // 4kB per block by default.
  private final RAMFile file;

  private final PagedBytes content;
  private final PagedBytes.PagedBytesDataOutput writer;
  private final Checksum crc;

  /** Construct an empty output buffer. */
  public RAMOutputStream() {
    this("noname", new RAMFile(), false);
  }

  /** Creates this, with no name. */
  public RAMOutputStream(RAMFile f, boolean checksum) {
    this("noname", f, checksum);
  }

  /** Creates this, with specified name. */
  public RAMOutputStream(String name, RAMFile f, boolean checksum) {
    super("RAMOutputStream(name=\"" + name + "\")", name);
    file = f;
    content = new PagedBytes(BLOCK_BITS);
    writer = content.getDataOutput();

    if (checksum) {
      crc = new BufferedChecksum(new CRC32());
    } else {
      crc = null;
    }
  }

  @Override
  public void close() throws IOException {
    content.freeze(true);
    DataInput in = content.getDataInput();
    file.setContent(in);
  }

  @Override
  public void writeByte(byte b) throws IOException {
    writer.writeByte(b);
    if (crc != null) {
      crc.update(b);
    }
  }

  @Override
  public void writeBytes(byte[] b, int offset, int len) throws IOException {
    assert b != null;
    writer.writeBytes(b, offset, len);
    if (crc != null) {
      crc.update(b, offset, len);
    }
  }

  @Override
  public long getFilePointer() {
    return writer.getPosition();
  }

  @Override
  public long ramBytesUsed() {
    return content.ramBytesUsed();
  }
  
  @Override
  public Collection<Accountable> getChildResources() {
    return Collections.singleton(Accountables.namedAccountable("content", content));
  }

  @Override
  public long getChecksum() throws IOException {
    if (crc == null) {
      throw new IllegalStateException("RAMOutputStream created with checksum disabled");
    } else {
      return crc.getValue();
    }
  }
}
