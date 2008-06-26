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
package org.apache.ahc;

import junit.framework.TestCase;
import org.apache.ahc.codec.HttpResponseDecoder;
import org.apache.ahc.codec.HttpResponseMessage;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

import java.util.Arrays;

public class ChunkedTest extends TestCase {

    private static final String FAKE_HTTP =
        "HTTP/1.1 200 OK\r\n" 
            + "Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n"
            + "Content-Type: text/plain\r\n"
            + "Transfer-Encoding: chunked\r\n"
            + "\r\n"
            + "1a; ignore-stuff-here\r\n"
            + "abcdefghijklmnopqrstuvwxyz\r\n"
            + "10\r\n"
            + "1234567890abcdef\r\n"
            + "0\r\n"
            + "some-footer: some-value\r\n"
            + "another-footer: another-value\r\n\r\n";


    private static final String  FAKE_HTTP_CONTINUE =
        "HTTP/1.1 100 Continue\r\n" 
            + "Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n" 
            + "Content-Type: text/plain\r\n"
            + "\r\n" + FAKE_HTTP;

    public void testChunking() throws Exception {
        IoBuffer buffer = IoBuffer.allocate(FAKE_HTTP.length());
        buffer.put(FAKE_HTTP.getBytes());
        buffer.flip();

        IoSession session = new FakeIoSession();
        HttpResponseDecoder decoder = new HttpResponseDecoder();
        FakeProtocolDecoderOutput out = new FakeProtocolDecoderOutput();
        decoder.decode(session, buffer, out);

        HttpResponseMessage response = (HttpResponseMessage)out.getObject();
        assertTrue(
            Arrays.equals(response.getContent(), "abcdefghijklmnopqrstuvwxyz1234567890abcdef".getBytes()));
    }

    public void testChunkingContinue() throws Exception {
        IoBuffer buffer = IoBuffer.allocate(FAKE_HTTP_CONTINUE.length());
        buffer.put(FAKE_HTTP.getBytes());
        buffer.flip();

        IoSession session = new FakeIoSession();
        HttpResponseDecoder decoder = new HttpResponseDecoder();
        FakeProtocolDecoderOutput out = new FakeProtocolDecoderOutput();
        decoder.decode(session, buffer, out);

        HttpResponseMessage response = (HttpResponseMessage)out.getObject();
        assertTrue(
            Arrays.equals(response.getContent(), "abcdefghijklmnopqrstuvwxyz1234567890abcdef".getBytes()));
    }

}
