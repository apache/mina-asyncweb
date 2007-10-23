/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.safehaus.asyncweb.codec.decoder.support;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;


/**
 * A {@link DecodingState} which consumes all received bytes until a configured
 * number of read bytes has been reached.
 * 
 * @author irvingd
 * @author trustin
 * @version $Rev$, $Date$
 */
public abstract class FixedLengthDecodingState implements DecodingState {

  private final int length;
  private ByteBuffer buffer;
  
  /**
   * Constructs with a known decode length.
   * 
   * @param length    The decode length
   */
  public FixedLengthDecodingState(int length) {
    this.length = length;
  }
  
  public DecodingState decode(ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
    if (buffer == null) {
      if (in.remaining() >= length) {
        int limit = in.limit();
        in.limit(in.position() + length);
        ByteBuffer product = in.slice();
        in.position(in.position() + length);
        in.limit(limit);
        return finishDecode(product, out);
      } else {
        buffer = ByteBuffer.allocate(length);
        buffer.put(in);
        return this;
      }
    } else {
      if (in.remaining() >= length - buffer.position()) {
        int limit = in.limit();
        in.limit(in.position() + length - buffer.position());
        buffer.put(in);
        in.limit(limit);
        ByteBuffer product = this.buffer;
        this.buffer = null;
        return finishDecode(product.flip(), out);
      } else {
        buffer.put(in);
        return this;
      }
    }
  }
  
  protected abstract DecodingState finishDecode(ByteBuffer readData, ProtocolDecoderOutput out) throws Exception;
}
