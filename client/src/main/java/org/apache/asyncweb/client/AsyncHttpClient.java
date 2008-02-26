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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;

import org.apache.asyncweb.common.HttpCodecFactory;
import org.apache.asyncweb.common.HttpRequest;
import org.apache.asyncweb.common.MutableHttpRequest;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.SslFilter;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

public class AsyncHttpClient {

    public static int DEFAULT_CONNECTION_TIMEOUT = 30;

    public static String DEFAULT_SSL_PROTOCOL = "TLS";

    private boolean followRedirects = true;

    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;

    private String sslProtocol = DEFAULT_SSL_PROTOCOL;

    private IoSession session;

    private SocketConnector connector;
    
    public AsyncHttpClient(SocketConnector connector) {
    	this.connector = connector;
    	connector.getFilterChain().addLast("protocolFilter",
                new ProtocolCodecFilter(new HttpCodecFactory()));
    }

    public void connect(URI url, AsyncHttpClientCallback callback) throws Exception {
        connector.setConnectTimeout(connectionTimeout);

        String scheme = url.getScheme();
        int port = url.getPort();
        
        if (scheme.toLowerCase().equals("http") && port == -1) {
            port = 80;
        } else if (scheme.toLowerCase().equals("https") && port == -1) {
        	port = 443;
        }
        connector.setHandler(new HttpIoHandler(callback));
        ConnectFuture future = connector.connect(new InetSocketAddress(url
                .getHost(), port));
        future.awaitUninterruptibly();
        if (!future.isConnected()) {
            throw new IOException("Cannot connect to " + url.toString());
        }
        session = future.getSession();
        // now add the good filters
        if (scheme.toLowerCase().equals("https")) {
            SslFilter filter = new SslFilter(createClientSSLContext());
            filter.setUseClientMode(true);
            session.getFilterChain().addLast("SSL", filter);
        }
    }
    
    public void disconnect() {
        if (session != null && session.isConnected()) {
            session.close();
        }
        session = null;
    }

    public void sendRequest( HttpRequest message) {
        if (message instanceof MutableHttpRequest) {
            (( MutableHttpRequest ) message).normalize();
        }
        session.write(message);
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    private SSLContext createClientSSLContext() throws GeneralSecurityException {
        SSLContext context = SSLContext.getInstance(sslProtocol);
        context.init(null, SimpleTrustManagerFactory.X509_MANAGERS, null);
        return context;
    }

}
