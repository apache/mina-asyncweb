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
package org.apache.ahc.codec;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.apache.mina.common.IoSession;

/**
 * A factory for creating {@link HttpRequestEncoder} and {@link HttpResponseDecoder} objects.
 */
public class HttpProtocolCodecFactory implements ProtocolCodecFactory {

    /** The encoder. */
    private final ProtocolEncoder encoder;
    
    /** The decoder. */
    private final ProtocolDecoder decoder;

    /**
     * Instantiates a new HttpProtocolCodecFactory.
     */
    public HttpProtocolCodecFactory() {
        encoder = new HttpRequestEncoder();
        decoder = new HttpResponseDecoder();
    }

    /**
     * Returns the {@link org.apache.mina.filter.codec.ProtocolEncoder} encoder.
     * 
     */
    public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
        return encoder;
    }

    /**
     * Returns the {@link org.apache.mina.filter.codec.ProtocolDecoder} encoder.
     * 
     */
    public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
        return decoder;
    }

}
