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
package org.safehaus.asyncweb.codec;

import java.util.List;

import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.ProtocolEncoder;
import org.safehaus.asyncweb.codec.decoder.HttpRequestDecodingState;
import org.safehaus.asyncweb.codec.decoder.support.DecodingState;
import org.safehaus.asyncweb.codec.decoder.support.StateMachineProtocolDecoder;
import org.safehaus.asyncweb.codec.encoder.OneShotHttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerCodecFactory implements ProtocolCodecFactory {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private boolean parseCookies = true;

    public boolean isParseCookies() {
        return parseCookies;
    }

    public void setParseCookies(boolean parseCookies) {
        this.parseCookies = parseCookies;
    }

    public ProtocolDecoder getDecoder() throws Exception {
        // TODO Use a parser pool if this performs bad.

        HttpRequestDecodingState topLevelState = new HttpRequestDecodingState() {
            @Override
            protected DecodingState finishDecode(List<Object> childProducts,
                    ProtocolDecoderOutput out) throws Exception {
                if (log.isDebugEnabled()) {
                    log.debug("Finished decoding a message: " + childProducts);
                }
                out.write(childProducts.get(0));
                return this;
            }
        };
        topLevelState.setParseCookies(parseCookies);

        return new StateMachineProtocolDecoder(topLevelState);
    }

    public ProtocolEncoder getEncoder() throws Exception {
        return new OneShotHttpResponseEncoder();
    }
}
