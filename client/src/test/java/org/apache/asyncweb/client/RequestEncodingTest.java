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

import java.net.URL;

import junit.framework.TestCase;

import org.apache.asyncweb.client.codec.HttpMessage;
import org.apache.asyncweb.client.codec.HttpRequestEncoder;
import org.apache.asyncweb.client.codec.HttpRequestMessage;

public class RequestEncodingTest extends TestCase {
    public void testMultiValueParam() throws Exception {
        HttpRequestMessage request = new HttpRequestMessage(new URL("http://www.foo.com"), null);
        request.setParameter("name", "value1");
        request.setParameter("name", "value2");
        
        FakeProtocolEncoderOutput out = new FakeProtocolEncoderOutput();
        HttpRequestEncoder encoder = new HttpRequestEncoder();
        encoder.encode(null, request, out);
        
        byte[] bytes = out.getBytes();
        String str = new String(bytes, HttpMessage.HTTP_ELEMENT_CHARSET);
        // check both parameters are encoded into the request
        assertTrue(str.contains("name=value1"));
        assertTrue(str.contains("name=value2"));
    }
}
