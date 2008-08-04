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
package org.apache.asyncweb.client.codec;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.asyncweb.client.auth.AuthScope;
import org.apache.asyncweb.client.auth.AuthState;
import org.apache.asyncweb.client.util.EncodingUtil;
import org.apache.asyncweb.client.util.NameValuePair;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/**
 * The Class HttpRequestEncoder. This handles the encoding of an {@link HttpRequestMessage} into
 * raw bytes.
 */
public class HttpRequestEncoder extends ProtocolEncoderAdapter {
    
    /** The Constant TYPES. */
    private static final Set<Class<?>> TYPES;
    
    /** The Constant CRLF. */
    private static final String CRLF = "\r\n";
    
    /** The Constant FORM_POST_CONTENT_TYPE. */
    private static final String FORM_POST_CONTENT_TYPE = "application/x-www-form-urlencoded";
    
    /** The encoder instances as thread locals. */
    private static final ThreadLocal<CharsetEncoder> ENCODER =
            new ThreadLocal<CharsetEncoder>() {
                @Override
                protected CharsetEncoder initialValue() {
                    return Charset.forName(HttpMessage.HTTP_ELEMENT_CHARSET).newEncoder();
                }
    };

    static {
        Set<Class<?>> types = new HashSet<Class<?>>();
        types.add(HttpRequestMessage.class);
        TYPES = Collections.unmodifiableSet(types);
    }

    /**
     * Instantiates a new http request encoder.
     */
    public HttpRequestEncoder() {
    }

    /**
     * Gets the message types for the MINA infrastructure.
     * 
     * @return the message types
     */
    public Set<Class<?>> getMessageTypes() {
        return TYPES;
    }

    /**
     * Method responsible for encoding a HttpRequestMessage into raw bytes.
     * 
     * @param ioSession the {@link org.apache.mina.common.IoSession} representing the connection to the server.
     * @param message the {@link HttpRequestMessage} object
     * @param out {@link org.apache.mina.filter.codec.ProtocolEncoderOutput} used for output
     * @see org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.common.IoSession, java.lang.Object, org.apache.mina.filter.codec.ProtocolEncoderOutput)
     */
    public void encode(IoSession ioSession, Object message, ProtocolEncoderOutput out) throws Exception {
        HttpRequestMessage msg = (HttpRequestMessage)message;

        ByteBuffer buf = ByteBuffer.allocate(1024, false);

        // Enable auto-expand for easier encoding
        buf.setAutoExpand(true);

        StringBuilder sb = new StringBuilder(1024);
        //If we have content, lets create the query string
        int attrCount = msg.getParameters().size();
        String urlAttrs = "";
        if (attrCount > 0) {
            NameValuePair attrs[] = new NameValuePair[attrCount];
            Set<Map.Entry<String, String>> set = msg.getParameters().entrySet();
            int i = 0;
            for (Map.Entry<String, String> entry : set) {
                attrs[i++] = new NameValuePair(entry.getKey(), entry.getValue());
            }
            urlAttrs = EncodingUtil.formUrlEncode(attrs, msg.getUrlEncodingCharset());
        }

        String method = msg.getRequestMethod();
        sb.append(method).append(' ');
        if (method.equals(HttpRequestMessage.REQUEST_CONNECT)) {
            sb.append(msg.getHost()).append(':').append(msg.getPort());
        } else {
            if (msg.isProxyEnabled() && !msg.getProtocol().toLowerCase().equals("https")) {
                sb.append(msg.getUrl().toString());
            } else {
                sb.append(msg.getUrl().getFile());
            }
            //If its a GET, append the attributes
            if (method.equals(HttpRequestMessage.REQUEST_GET) && attrCount > 0) {
                //If there is not already a ? in the query, append one, otherwise append a &
                if (!msg.getUrl().getFile().contains("?")) {
                    sb.append('?');
                } else {
                    sb.append('&');
                }
                sb.append(urlAttrs);
            }
        }
        sb.append(" HTTP/1.1");
        sb.append(CRLF);

        //This header is required for HTTP/1.1

        String hostHeader = msg.getHost(); 
        if ((msg.getProtocol().equals("http") && msg.getPort() != 80)
                || (msg.getProtocol().equals("https") && msg.getPort() != 443)) {
            hostHeader += ":" + msg.getPort(); 
        }
        // set the host header, removing any Host header that might already exist. 
        msg.setHeader("Host", hostHeader); 

        //User agent
        if (msg.getUserAgent() != null) {
            msg.setHeader("User-Agent", msg.getUserAgent()); 
        }

        // potentially for a POST request.  We need to obtain the content information 
        // so we can add the content headers...but the attaching of the content comes later. 
        byte content[] = null;  

        // If this is a POST and parameters are provided, this is a form
        // post; any existing content is an error and will be ignored and 
        // the content type will be set accordingly
        if (method.equals(HttpRequestMessage.REQUEST_POST) && attrCount > 0) {
            content = urlAttrs.getBytes();

            // these override any headers that might already be in the set 
            msg.setHeader(HttpMessage.CONTENT_TYPE, FORM_POST_CONTENT_TYPE);
        } else if (msg.getContent() != null) {
            // if the message body was provided by the caller, then use it
            // (as long as the method is not among the types for which
            // entities are disallowed)
            if (!method.equals(HttpRequestMessage.REQUEST_TRACE) && 
                    !method.equals(HttpRequestMessage.REQUEST_CONNECT)) {
                content = msg.getContent();
            }
        }

        // set the proper content length header
        if (content != null && content.length > 0) {
            msg.setHeader(HttpMessage.CONTENT_LENGTH, String.valueOf(content.length));
        } else {
            // remove any existing content related headers
            msg.removeHeader(HttpMessage.CONTENT_TYPE);
            msg.removeHeader(HttpMessage.CONTENT_LENGTH);
        }

        //Process authentication
        AuthState state = msg.getAuthState();
        if (state != null){
            String auth = state.getAuthScheme().authenticate(msg.getCredential(new AuthScope(msg.getHost(), msg.getPort(), state.getAuthScheme().getRealm())),msg);
            msg.setHeader("Authorization", auth); 
            state.setAuthAttempted(true);
        }

        //Process any headers we have
        processHeaders(msg, sb);

        //Process cookies
        //NOTE: I am just passing the name value pairs and not doing management of the expiration or path
        //As that will be left up to the user.  A possible enhancement is to make use of a CookieManager
        //to handle these issues for the request
        processCookies(msg, sb);

        //Blank line indicates end of the headers 
        sb.append(CRLF);
        
        // finally encode and add the string to the buffer
        buf.putString(sb, ENCODER.get());

        //If this is a POST, then we have content to attach after the blank line 
        if (content != null) {
            buf.put(content);
        } 

        buf.flip();

        out.write(buf);
        out.flush();

    }

    /**
     * Process header encoding.
     * 
     * @param msg the {@link HttpRequestMessage} message object
     * @param sb the <code>StringBuilder</code> at which to append the data
     * 
     * @throws Exception if any exception occurs.
     */
    private void processHeaders(HttpRequestMessage msg, StringBuilder sb)
        throws Exception {
        List<NameValuePair> headers = msg.getHeaders();
        for (NameValuePair header : headers) {
            String name = header.getName();
            String value = header.getValue();

            sb.append(name).append(": ").append(value).append(CRLF);
        }

        //Process authentication
        AuthState state = msg.getAuthState();
        if (state != null){
            String auth = state.getAuthScheme().authenticate(msg.getCredential(new AuthScope(msg.getHost(), msg.getPort(), state.getAuthScheme().getRealm())),msg);
            sb.append("Authorization").append(": ").append(auth).append(CRLF);
            state.setAuthAttempted(true);
        }
    }

    /**
     * Process cookies.
     * 
     * @param msg the msg
     * @param sb the StringBuilder
     * 
     * @throws Exception the exception
     */
    private void processCookies(HttpRequestMessage msg, StringBuilder sb)
        throws Exception {
        Collection<Cookie> cookies = msg.getCookies();
        if (cookies.size() > 0) {
            sb.append("Cookie: ");
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                String value = cookie.getValue();

                sb.append(name).append('=').append(value).append("; ");
            }
            sb.append(CRLF);
        }
    }

}
