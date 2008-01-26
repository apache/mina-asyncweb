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
package org.safehaus.asyncweb.example.session;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.mina.common.IoBuffer;
import org.apache.mina.filter.codec.http.DefaultHttpResponse;
import org.apache.mina.filter.codec.http.MutableHttpResponse;
import org.safehaus.asyncweb.service.HttpService;
import org.safehaus.asyncweb.service.HttpServiceContext;
import org.safehaus.asyncweb.service.HttpSession;

/**
 * A very simple example which demonstrates session usage
 *
 * @author irvingd
 *
 */
public class SessionExample implements HttpService {

    private static final String COUNT_PROPERTY = "accessCount";

    private static final String DESTROY_PARAM = "destroySession";

    /**
     * Provides a response showing some session details for the current request.
     * If no session is assocaited with the request, a new session is created.
     */
    public void handleRequest(HttpServiceContext context) throws Exception {
        MutableHttpResponse response = new DefaultHttpResponse();
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");

        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println("<html><body>");
        if (!checkDestroy(context, writer)) {
            showSessionDetails(context, writer);
            createNewSessionIfRequired(context, writer);
        }
        writer.println("</body></html>");
        writer.flush();

        IoBuffer bb = IoBuffer.allocate(1024);
        bb.setAutoExpand(true);
        bb.putString(buf.toString(), Charset.forName("UTF-8").newEncoder());
        bb.flip();
        response.setContent(bb);

        context.commitResponse(response);
    }

    private void showSessionDetails(HttpServiceContext context,
            PrintWriter writer) {
        HttpSession session = context.getSession(false);
        if (session != null) {
            writer.println("<ul>");
            writer.println("<li>A session was retrieved for your request</li>");
            writer.println("<li>Is session attached? " + session.isAttached()
                    + "</li>");
            writer.println("<li>Is session valid? " + session.isValid()
                    + "</li>");
            Integer count = (Integer) session.getValue(COUNT_PROPERTY);
            int newAccessCount;
            if (count == null) {
                writer
                        .println("<li>This is the first time the new session has been accessed</li>");
                newAccessCount = 1;
            } else {
                newAccessCount = count.intValue() + 1;
                writer.println("<li>The session has been accessed "
                        + newAccessCount + " times</li>");
            }
            session.setValue(COUNT_PROPERTY, new Integer(newAccessCount));
        }
    }

    private void createNewSessionIfRequired(HttpServiceContext context,
            PrintWriter writer) {
        HttpSession session = context.getSession(false);
        if (session == null) {
            session = context.getSession(true);
            writer
                    .println("<i>No current session was located. A new session has been created</i>");
        }
    }

    private boolean checkDestroy(HttpServiceContext context, PrintWriter writer) {
        boolean foundParam = false;
        if (context.getRequest().containsParameter(DESTROY_PARAM)) {
            foundParam = true;
            HttpSession session = context.getSession(false);
            if (session == null) {
                writer
                        .println("<i>There is no current session to destroy!</li>");
            } else {
                session.destroy();
                writer
                        .println("<i>The current session has been destroyed</li>");
            }
        }
        return foundParam;
    }

    public void start() {
        // Dont care
    }

    public void stop() {
        // Dont care
    }

}
