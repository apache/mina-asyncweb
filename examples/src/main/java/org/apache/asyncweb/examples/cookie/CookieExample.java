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
package org.apache.asyncweb.examples.cookie;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.mina.common.buffer.IoBuffer;
import org.apache.asyncweb.common.HttpRequest;
import org.apache.asyncweb.common.MutableCookie;
import org.apache.asyncweb.common.MutableHttpResponse;
import org.apache.asyncweb.common.DefaultCookie;
import org.apache.asyncweb.common.DefaultHttpResponse;
import org.apache.asyncweb.server.HttpService;
import org.apache.asyncweb.server.HttpServiceContext;

public class CookieExample implements HttpService
{

    private static final String ADD_COOKIE_NAME = "cookieName";

    private static final String ADD_COOKIE_VALUE = "cookieValue";

    private static final String ADD_COOKIE_PATH = "cookiePath";

    private static final String ADD_COOKIE_EXPIRY = "cookieExpiry";

    private String form;

    public void handleRequest(HttpServiceContext context) throws Exception {
        MutableHttpResponse response = new DefaultHttpResponse();

        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println("<html><body>");
        writer.println(form);
        MutableCookie addedCookie = addCookieIfPresent(context, response);
        if (addedCookie != null) {
            writeAddedCookie(addedCookie, writer);
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

    /**
     * Sets the string containing our cookie form
     *
     * @param form  The form string
     */
    public void setForm(String form) {
        this.form = form;
    }

    public void start() {
        // Dont care
    }

    public void stop() {
        // Dont care
    }

    private void writeAddedCookie(MutableCookie cookie, PrintWriter writer) {
        writer.println("<b><i>Cookie Added:</i></b>");
        writer.println(cookie);
    }

    private MutableCookie addCookieIfPresent(HttpServiceContext context,
            MutableHttpResponse response) {
        HttpRequest request = context.getRequest();
        String name = getParamValue(request, ADD_COOKIE_NAME);
        String value = getParamValue(request, ADD_COOKIE_VALUE);
        if (name == null || value == null) {
            return null;
        }
        MutableCookie cookie = new DefaultCookie(name);
        cookie.setValue(value);

        if (getParamValue(request, ADD_COOKIE_PATH) != null) {
            cookie.setPath(request.getParameter(ADD_COOKIE_PATH));
        }
        if (getParamValue(request, ADD_COOKIE_EXPIRY) != null) {
            try {
                int expiry = Integer.parseInt(request
                        .getParameter(ADD_COOKIE_EXPIRY));
                cookie.setMaxAge(expiry);
            } catch (NumberFormatException e) {
                // ignore expiry
            }
        }
        response.addCookie(cookie);
        return cookie;
    }

    private static final String getParamValue(HttpRequest request, String name) {
        String value = request.getParameter(name);
        return value == null || "".equals(value) ? null : value;
    }

}
