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
package org.apache.asyncweb.examples.helloworld;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.asyncweb.common.HttpRequest;
import org.apache.asyncweb.common.HttpResponseStatus;
import org.apache.asyncweb.common.MutableHttpResponse;
import org.apache.asyncweb.common.Cookie;
import org.apache.asyncweb.common.DefaultHttpResponse;
import org.apache.asyncweb.server.HttpService;
import org.apache.asyncweb.server.HttpServiceContext;

/**
 * A simple <code>HttpService</code> which sends "hello world"
 * responses to every request.
 *
 * Note that normally we wouldn't be generating html directly in a service :o)
 *
 * @author irvingd
 *
 */
public class HelloWorldHttpService implements HttpService {

    private String message = "Hello from AsyncWeb!!";

    /**
     * Sends the configured message as an HTTP response
     */
    public void handleRequest( HttpServiceContext context) throws Exception {
        MutableHttpResponse response = new DefaultHttpResponse();

        StringWriter buf = new StringWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println("<html><body><b>Your message of the day:</b><br/><br/>");
        writer.println("<h2><i>" + message + "</h2></i><br/><br/>");
        writeHeaders(context.getRequest(), writer);
        writer.println("<br/>");
        writeParameters(context.getRequest(), writer);
        writer.println("<br/>");
        writeCookies(context.getRequest(), writer);
        writer.flush();

        IoBuffer bb = IoBuffer.allocate(1024);
        bb.setAutoExpand(true);
        bb.putString(buf.toString(), Charset.forName("UTF-8").newEncoder());
        bb.flip();
        response.setContent(bb);

        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(HttpResponseStatus.OK);

        context.commitResponse(response);
    }

    /**
     * Sets the message to return in responses.
     * This is called for you by the framework!
     *
     * @param message  The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Writes headers from the request to the specified writer
     *
     * @param request   The request
     * @param writer    The writer
     */
    private void writeHeaders( HttpRequest request, PrintWriter writer) {
        writer.println("You sent these headers with your request:<br/>");
        writer.println("<ul>");
        for (String headerName : request.getHeaders().keySet()) {
            String headerValue = request.getHeader(headerName);
            writer.print("<li>" + headerName + " = " + headerValue + "</li>");
        }
        writer.println("</ul>");
    }

    /**
     * Writes cookies from the request to the specified writer
     *
     * @param request  The request
     * @param writer   The writer
     */
    private void writeCookies(HttpRequest request, PrintWriter writer) {
        Collection<Cookie> cookies = request.getCookies();
        if (!cookies.isEmpty()) {
            writer.println("You sent these cookies with your request:<br/>");
            writer.println("<ul>");
            for ( Cookie cookie : cookies) {
                writer.println("<li>Name = " + cookie.getName() + " Value = "
                        + cookie.getValue());
                writer.println(" Path = " + cookie.getPath() + " Version = "
                        + cookie.getVersion() + "</li>");
            }
            writer.println("</ul>");
        }
    }

    /**
     * Writes request parameters to the specified writer
     *
     * @param request  The request
     * @param writer   The writer
     */
    private void writeParameters(HttpRequest request, PrintWriter writer) {
        if (request.getParameters().size() > 0) {
            writer
                    .println("You sent these parameters with your request:<br/><br/>");
            writer.println("<ul>");

            for (Map.Entry<String, List<String>> entry : request
                    .getParameters().entrySet()) {
                writer.println("<li>");
                writer.print("'" + entry.getKey() + "' =  ");
                for (Iterator<String> i = entry.getValue().iterator(); i
                        .hasNext();) {
                    String value = i.next();
                    writer.print("'" + value + "'");
                    if (i.hasNext()) {
                        writer.print(", ");
                    }
                }
                writer.println("</li/>");
            }

            writer.println("</ul>");
        }
    }

    public void start() {
        // Dont care
    }

    public void stop() {
        // Dont care
    }

}
