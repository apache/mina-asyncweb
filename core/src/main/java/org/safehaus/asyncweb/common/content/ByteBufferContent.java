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
package org.safehaus.asyncweb.common.content;

import org.apache.mina.common.IoBuffer;
import org.safehaus.asyncweb.common.Content;

/**
 * A {@link Content} which contains a byte array with offset and length parameter.
 * 
 * @author trustin
 * @version $Rev$, $Date$
 */
public class ByteBufferContent implements Content {

  private static final long serialVersionUID = -3456547908555235715L;
  
  private final IoBuffer buffer;
  
  public ByteBufferContent(IoBuffer buffer) {
    if (buffer == null) {
      throw new NullPointerException("buffer");
    }
    
    this.buffer = buffer;
  }
  
  public IoBuffer getByteBuffer() {
    return buffer.duplicate();
  }
  
  public int size() {
    return buffer.remaining();
  }
}
