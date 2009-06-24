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
package org.apache.asyncweb.client;

import java.util.LinkedList;
import java.util.List;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class FakeProtocolEncoderOutput implements ProtocolEncoderOutput {
    private final List<ByteBuffer> buffers = new LinkedList<ByteBuffer>();

    public void write(ByteBuffer buffer) {
        buffers.add(buffer);
    }

    public WriteFuture flush() {
        return null;
    }

    public void mergeAll() {}
    
    /**
     * Gathers bytes from all buffers.
     */
    public byte[] getBytes() {
        int size = 0;
        for (ByteBuffer b : buffers) {
            size += b.limit();
        }
        byte[] result = new byte[size];
        int position = 0;
        for (ByteBuffer b : buffers) {
            int length = b.limit();
            System.arraycopy(b.array(), 0, result, position, length);
            position += length;
        }
        return result;
    }
}
