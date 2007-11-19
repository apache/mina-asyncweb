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
package org.safehaus.asyncweb.example.lightweight;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoBuffer;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoSessionLogger;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.filter.codec.http.DefaultHttpResponse;
import org.apache.mina.filter.codec.http.HttpHeaderConstants;
import org.apache.mina.filter.codec.http.HttpRequest;
import org.apache.mina.filter.codec.http.HttpResponseStatus;
import org.apache.mina.filter.codec.http.MutableHttpResponse;

public class HttpProtocolHandler implements IoHandler {
    private static final int CONTENT_PADDING = 0; // 101

    private final Map<Integer, IoBuffer> buffers = new ConcurrentHashMap<Integer, IoBuffer>();

    private final Timer timer;

    public HttpProtocolHandler() {
        timer = new Timer(true);
    }

    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        if (!(cause instanceof IOException)) {
            IoSessionLogger.getLogger(session).warn(cause);
        }
        session.close();
    }

    public void messageReceived(IoSession session, Object message)
            throws Exception {
        HttpRequest req = (HttpRequest) message;
        String path = req.getRequestUri().getPath();

        MutableHttpResponse res;
        if (path.startsWith("/size/")) {
            doDataResponse(session, req);
        } else if (path.startsWith("/delay/")) {
            doAsynchronousDelayedResponse(session, req);
        } else if (path.startsWith("/adelay/")) {
            doAsynchronousDelayedResponse(session, req);
        } else {
            res = new DefaultHttpResponse();
            res.setStatus(HttpResponseStatus.OK);
            writeResponse(session, req, res);
        }
    }

    private void writeResponse(IoSession session, HttpRequest req,
            MutableHttpResponse res) {
        res.normalize(req);
        WriteFuture future = session.write(res);
        if (!HttpHeaderConstants.VALUE_KEEP_ALIVE.equalsIgnoreCase(
                res.getHeader(HttpHeaderConstants.KEY_CONNECTION))) {
            future.addListener(IoFutureListener.CLOSE);
        }
    }

    private void doDataResponse(IoSession session, HttpRequest req) {
        String path = req.getRequestUri().getPath();
        int size = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1))
                + CONTENT_PADDING;

        MutableHttpResponse res = new DefaultHttpResponse();
        res.setStatus(HttpResponseStatus.OK);
        res.setHeader("ETag", "W/\"" + size + "-1164091960000\"");
        res.setHeader("Last-Modified", "Tue, 31 Nov 2006 06:52:40 GMT");

        IoBuffer buf = buffers.get(size);
        if (buf == null) {
            buf = IoBuffer.allocate(size);
            buffers.put(size, buf);
        }

        res.setContent(buf.duplicate());
        writeResponse(session, req, res);
    }

    private void doAsynchronousDelayedResponse(final IoSession session,
            final HttpRequest req) {
        String path = req.getRequestUri().getPath();
        int delay = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));

        final MutableHttpResponse res = new DefaultHttpResponse();
        res.setStatus(HttpResponseStatus.OK);
        res.setHeader("ETag", "W/\"0-1164091960000\"");
        res.setHeader("Last-Modified", "Tue, 31 Nov 2006 06:52:40 GMT");

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                writeResponse(session, req, res);
            }
        }, delay);
    }

    public void messageSent(IoSession session, Object message) throws Exception {
    }

    public void sessionClosed(IoSession session) throws Exception {
    }

    public void sessionCreated(IoSession session) throws Exception {
    }

    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        session.close();
    }

    public void sessionOpened(IoSession session) throws Exception {
        session.getConfig().setIdleTime(IdleStatus.BOTH_IDLE, 30);
    }
}
