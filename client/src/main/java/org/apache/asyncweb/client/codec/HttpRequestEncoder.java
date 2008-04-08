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

import java.nio.charset.CharacterCodingException;
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
    private static final byte[] CRLF = new byte[] {0x0D, 0x0A};
    
    /** The Constant FORM_POST_CONTENT_TYPE. */
    private static final String FORM_POST_CONTENT_TYPE = "application/x-www-form-urlencoded";

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

        try {
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

            CharsetEncoder encoder = Charset.forName(HttpMessage.HTTP_ELEMENT_CHARSET).newEncoder();
            String method = msg.getRequestMethod();
            buf.putString(method, encoder);
            buf.putString(" ", encoder);
            if (method.equals(HttpRequestMessage.REQUEST_CONNECT)) {
                buf.putString(msg.getHost(), encoder);
                buf.putString(":", encoder);
                buf.putString(msg.getPort() + "", encoder);
            } else {
                if (msg.isProxyEnabled() && !msg.getProtocol().toLowerCase().equals("https")) {
                    buf.putString(msg.getUrl().toString(), encoder);
                } else {
                    buf.putString(msg.getUrl().getFile(), encoder);
                }
                //If its a GET, append the attributes
                if (method.equals(HttpRequestMessage.REQUEST_GET) && attrCount > 0) {
                    //If there is not already a ? in the query, append one, otherwise append a &
                    if (!msg.getUrl().getFile().contains("?")) {
                        buf.putString("?", encoder);
                    } else {
                        buf.putString("&", encoder);
                    }
                    buf.putString(urlAttrs, encoder);
                }
            }
            buf.putString(" HTTP/1.1", encoder);
            buf.put(CRLF);

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
            processHeaders(msg, buf, encoder);

            //Process cookies
            //NOTE: I am just passing the name value pairs and not doing management of the expiration or path
            //As that will be left up to the user.  A possible enhancement is to make use of a CookieManager
            //to handle these issues for the request
            processCookies(msg, buf, encoder);

            //Blank line indicates end of the headers 
            buf.put(CRLF);
            
            //If this is a POST, then we have content to attach after the blank line 
            if (content != null) {
                buf.put(content);
            } 
        } catch (CharacterCodingException ex) {
            ex.printStackTrace();
        }

        buf.flip();

        out.write(buf);
        out.flush();

    }

    /**
     * Process header encoding.
     * 
     * @param msg the {@link HttpRequestMessage} message object
     * @param buf the <code>ByteBuffer</code> in which to place the raw bytes
     * @param encoder the character set encoder
     * 
     * @throws Exception if any exception occurs.
     */
    private void processHeaders(HttpRequestMessage msg, ByteBuffer buf, CharsetEncoder encoder)
        throws Exception {
        List<NameValuePair> headers = msg.getHeaders();
        for (NameValuePair header : headers) {
            String name = header.getName();
            String value = header.getValue();

            buf.putString(name, encoder);
            buf.putString(": ", encoder);
            buf.putString(value, encoder);
            buf.put(CRLF);
        }

        //Process authentication
        AuthState state = msg.getAuthState();
        if (state != null){
            String auth = state.getAuthScheme().authenticate(msg.getCredential(new AuthScope(msg.getHost(), msg.getPort(), state.getAuthScheme().getRealm())),msg);
            buf.putString("Authorization", encoder);
            buf.putString(": ", encoder);
            buf.putString(auth, encoder);
            buf.put(CRLF);
            state.setAuthAttempted(true);
        }
    }

    /**
     * Process cookies.
     * 
     * @param msg the msg
     * @param buf the buf
     * @param encoder the encoder
     * 
     * @throws Exception the exception
     */
    private void processCookies(HttpRequestMessage msg, ByteBuffer buf, CharsetEncoder encoder)
        throws Exception {
        Collection<Cookie> cookies = msg.getCookies();
        if (cookies.size() > 0) {
            buf.putString("Cookie: ", encoder);
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                String value = cookie.getValue();

                buf.putString(name, encoder);
                buf.putString("=", encoder);
                buf.putString(value, encoder);
                buf.putString("; ", encoder);
            }
            buf.put(CRLF);
        }
    }

}
