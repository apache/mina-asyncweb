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
package org.apache.ahc.codec;

import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import javax.net.ssl.SSLContext;

import org.apache.ahc.AsyncHttpClientCallback;
import org.apache.ahc.auth.AuthScope;
import org.apache.ahc.auth.AuthState;
import org.apache.ahc.auth.Credentials;
import org.apache.ahc.proxy.ProxyConfiguration;

/**
 * The Class HttpRequestMessage. This is an object representation of an HTTP request.
 */
public class HttpRequestMessage extends HttpMessage {

    /**
     * The Constant DEFAULT_REQUEST_TIMEOUT.
     */
    public static final int DEFAULT_REQUEST_TIMEOUT = 30000;

    /**
     * The Constant DEFAULT_CREDENTIAL_CHARSET.
     */
    public static final String DEFAULT_CREDENTIAL_CHARSET = "US-ASCII";

    /**
     * The Constant REQUEST_GET.
     */
    public static final String REQUEST_GET = "GET";

    /**
     * The Constant REQUEST_POST.
     */
    public static final String REQUEST_POST = "POST";

    /**
     * The Constant REQUEST_HEAD.
     */
    public static final String REQUEST_HEAD = "HEAD";

    /**
     * The Constant REQUEST_OPTIONS.
     */
    public static final String REQUEST_OPTIONS = "OPTIONS";

    /**
     * The Constant REQUEST_PUT.
     */
    public static final String REQUEST_PUT = "PUT";

    /**
     * The Constant REQUEST_DELETE.
     */
    public static final String REQUEST_DELETE = "DELETE";

    /**
     * The Constant REQUEST_TRACE.
     */
    public static final String REQUEST_TRACE = "TRACE";

    public static final String REQUEST_CONNECT = "CONNECT";

    /**
     * The request method.
     */
    private String requestMethod = REQUEST_GET;

    /**
     * The request url.
     */
    private URL url;

    /**
     * The parameters.
     */
    private Map<String, String> parameters = new HashMap<String, String>();

    /**
     * The user agent.
     */
    private String userAgent = "AsyncHttpClient 1.0";

    /**
     * The follow redirects.
     */
    private boolean followRedirects = true;

    /**
     * The timeout handle.
     */
    private ScheduledFuture<?> timeoutHandle;

    /**
     * The response future.
     */
    private volatile ResponseFuture responseFuture;
    
    /**
     * The callback.
     */
    private AsyncHttpClientCallback callback;

    /**
     * The time out.
     */
    private int timeOut = DEFAULT_REQUEST_TIMEOUT;

    /**
     * The character credential charset *
     */
    protected String credentialCharset = DEFAULT_CREDENTIAL_CHARSET;

    /**
     * The challenge map *
     */
    private AuthState authState;

    /**
     * The credentials map *
     */
    private HashMap<AuthScope, Credentials> credentials = new HashMap<AuthScope, Credentials>();


    /**
     * Auth attempt count
     */
    private int authCount = 0;
    
    /**
     * SSL context for https
     */
    private SSLContext sslContext;

    private ProxyConfiguration proxyConfig;
    
    private volatile long requestStartTime = 0L;
    
    private volatile long connectStartTime = 0L;

    /**
     * Instantiates a new http request message.
     *
     * @param url      the complete url for which the request including scheme, host, port[optional], and query
     *                 (i.e. <code>http://www.example.com:8080/example.cgi?test=me</code>).
     * @param callback the {@link org.apache.ahc.AsyncHttpClientCallback} callback class to receive notifications when they occur.
     */
    public HttpRequestMessage(URL url, AsyncHttpClientCallback callback) {
        this.url = url;
        this.callback = callback;
    }

    /**
     * Gets the time out.
     *
     * @return the time out in milliseconds.  Defaults to {@link #DEFAULT_REQUEST_TIMEOUT} if not set.
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * Sets the time out.
     *
     * @param timeOut the new time out in milliseconds. Defaults to {@link #DEFAULT_REQUEST_TIMEOUT} if not set.
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * Gets the timeout handle.
     *
     * @return the timeout <code>ScheduledFuture</code> handle
     */
    protected ScheduledFuture<?> getTimeoutHandle() {
        return timeoutHandle;
    }

    /**
     * Sets the timeout handle.
     *
     * @param timeoutHandle the new <code>ScheduledFuture</code> timeout handle
     */
    protected void setTimeoutHandle(ScheduledFuture<?> timeoutHandle) {
        this.timeoutHandle = timeoutHandle;
    }

    /**
     * Gets the request method.
     *
     * @return the request method.  Defaults to {@link #REQUEST_GET} if not set.
     */
    public String getRequestMethod() {
        return requestMethod;
    }

    /**
     * Returns the response future object associated with the request.
     */
    public ResponseFuture getResponseFuture() {
        return responseFuture;
    }

    /**
     * Sets the response future object.
     */
    public void setResponseFuture(ResponseFuture result) {
        responseFuture = result;
    }

    /**
     * Gets the callback.
     *
     * @return the {@link org.apache.ahc.AsyncHttpClientCallback} callback
     */
    public AsyncHttpClientCallback getCallback() {
        return callback;
    }

    /**
     * Sets the request method.
     *
     * @param requestMethod the new request method
     * @throws ProtocolException if the request method is not of type {@link #REQUEST_GET},
     *                           {@link #REQUEST_POST},{@link #REQUEST_HEAD},{@link #REQUEST_OPTIONS},
     *                           {@link #REQUEST_PUT},{@link #REQUEST_DELETE}, or {@link #REQUEST_TRACE}
     */
    public void setRequestMethod(String requestMethod) throws ProtocolException {
        if (requestMethod.equals(REQUEST_GET)
            || requestMethod.equals(REQUEST_POST)
            || requestMethod.equals(REQUEST_HEAD)
            || requestMethod.equals(REQUEST_OPTIONS)
            || requestMethod.equals(REQUEST_PUT)
            || requestMethod.equals(REQUEST_DELETE)
            || requestMethod.equals(REQUEST_TRACE)
            || requestMethod.equals(REQUEST_CONNECT)) {
            this.requestMethod = requestMethod;
            return;
        }

        throw new ProtocolException("Invalid request method type.");
    }

    /**
     * Gets the url.
     *
     * @return the url
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url the new url
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Gets the path part of the url.
     *
     * @return the path part of the url
     */
    public String getPath() {
        return url.getPath();
    }

    /**
     * Gets the host part of the url.
     *
     * @return the host part of the url
     */
    public String getHost() {
        return url.getHost();
    }

    /**
     * Gets the port part of the url.
     *
     * @return the port part of the url
     */
    public int getPort() {
        String scheme = url.getProtocol();
        int port = url.getPort();
        if (scheme.toLowerCase().equals("https")) {
            if (port == -1) {
                port = 443;
            }
        }
        if (scheme.toLowerCase().equals("http") && (port == -1)) {
            port = 80;
        }
        return port;
    }

    /**
     * Gets the protocol part of the url.
     *
     * @return the protocol part of the url
     */
    public String getProtocol() {
        return url.getProtocol();
    }

    /**
     * Gets the query part of the url.
     *
     * @return the query part of the url
     */
    public String getQuery() {
        return url.getQuery();
    }

    /**
     * Gets a parameter from the parameter map.
     *
     * @param name the parameter name
     * @return the parameter value
     */
    public String getParameter(String name) {
        return parameters.get(name);
    }

    /**
     * Gets the parameter map.
     *
     * @return the parameter map
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameter map.
     *
     * @param parameters the parameter map
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters.putAll(parameters);
    }

    /**
     * Sets a single parameter.
     *
     * @param name  the parameter name
     * @param value the value of parameter
     */
    public void setParameter(String name, String value) {
        parameters.put(name, value);
    }
    
    /**
     * Clears all parameters.
     *
     */
    public void clearAllParameters() {
        parameters.clear();
    }

    /**
     * Gets the user agent string.
     *
     * @return the user agent <code>String</code>
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Sets the user agent string.
     *
     * @param userAgent the new user agent <code>String</code>
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * Checks if the request will follow redirects (301,302 and 307 HTTP status).
     *
     * @return <code>true</code>, if the request will follow redirects, <code>false</code> if not.
     *         Defaults to <code>true</code> if not set.
     */
    public boolean isFollowRedirects() {
        return followRedirects;
    }

    /**
     * Sets whether the request will follow redirects (301,302 and 307 HTTP status).
     *
     * @param followRedirects the new follow redirects.  Set to <code>true</code>, if the request
     *                        will follow redirects, <code>false</code> if not.
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    /**
     * Gets the credential character set
     *
     * @return the credential character set. Defaults to {@link #DEFAULT_CREDENTIAL_CHARSET} if not set.
     */
    public String getCredentialCharset() {
        return credentialCharset;
    }

    /**
     * Sets the credential character set
     *
     * @param credentialCharset the new credential character set
     */
    public void setCredentialCharset(String credentialCharset) {
        this.credentialCharset = credentialCharset;
    }

    /**
     * Gets the authorization state
     *
     * @return the authorization state
     */
    public AuthState getAuthState() {
        return authState;
    }

    /**
     * Sets the authorization state
     *
     * @param authState the authorization state
     */
    public void setAuthState(AuthState authState) {
        this.authState = authState;
    }

    /**
     * Gets the credentials map
     *
     * @return the credential map
     */
    public HashMap<AuthScope, Credentials> getCredentials() {
        return credentials;
    }

    /**
     * Gets a single credential
     *
     * @param scope the (@link org.apache.ahc.AuthScope} object
     * @return (@linkorg.apache.ahc.Credentials} objectiffoundor <code>null</code> ifnotfound
     */
    public Credentials getCredential(AuthScope scope) {
        return matchCredentials(credentials, scope);
    }

    private static Credentials matchCredentials(final HashMap<AuthScope, Credentials> map, final AuthScope authscope) {
        // see if we get a direct hit
        Credentials creds = (Credentials)map.get(authscope);
        if (creds == null) {
            // Nope.
            // Do a full scan
            int bestMatchFactor = -1;
            AuthScope bestMatch = null;
            for (AuthScope current : map.keySet()) {
                int factor = authscope.match(current);
                if (factor > bestMatchFactor) {
                    bestMatchFactor = factor;
                    bestMatch = current;
                }
            }
            if (bestMatch != null) {
                creds = map.get(bestMatch);
            }
        }
        return creds;
    }

    /**
     * Adds a credential
     *
     * @param scope       the (@link org.apache.ahc.AuthScope} object
     * @param credentials (@link org.apache.ahc.Credentials} object
     */
    public void addCredentials(AuthScope scope, Credentials credentials) {
        this.credentials.put(scope, credentials);
    }


    /**
     * Gets the authorization attempt count
     *
     * @return the authorization attempt count
     */
    public int getAuthCount() {
        return authCount;
    }

    /**
     * Sets the authorization attempt count
     * 
     * @param authCount the authorization attempt count
     */
    public void setAuthCount(int authCount) {
        this.authCount = authCount;
    }
    
    /**
     * Gets the SSL context
     * 
     * @return the SSL context
     */
    public SSLContext getSSLContext() {
        return sslContext;
    }
    
    /**
     * Sets the SSL context
     * 
     * @param sslContext the SSL context
     */
    public void setSSLContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }
    
    /**
     * Get the message proxy configuration.  This controls 
     * how downstream filters handle the message connections.
     * 
     * @return The current proxy configuration.  Returns null if 
     *         no proxying support is configured.
     */
    public ProxyConfiguration getProxyConfiguration() {
        return proxyConfig;
    }
    
    /**
     * Set the proxy configuration use to control proxied 
     * connections.
     * 
     * @param config The new proxy configuration.
     */
    public void setProxyConfiguration(ProxyConfiguration config) {
        proxyConfig = config;
    }
    
    /**
     * Test if this request needs to go through a proxy 
     * server.  To be proxied, there must be a proxy configuration 
     * set and the request target must not be specified in 
     * the proxy exclusion list.
     * 
     * @return true if this request must go through a proxy server, 
     *         false if a direct connection can be used.
     */
    public boolean isProxyEnabled() {
        return proxyConfig != null && !proxyConfig.isExcluded(getUrl());
    }
    
    /**
     * Return the time when the request was first initiated.
     * 
     * @return The time, in milliseconds, for when request processing
     *         was initiated.
     */
    public long getRequestStartTime() {
        return requestStartTime;
    }
    
    /**
     * Mark the start of request processing. 
     */
    public void setRequestStartTime() {
        requestStartTime = System.nanoTime()/1000000;
    }
    
    /**
     * Clear the request starting time back to zero.
     */
    public void clearRequestStartTime() {
        requestStartTime = 0L;
    }
    
    /**
     * Get the time stamp for when the connection request was 
     * started.
     * 
     * @return The timestamp (in milliseconds) for when the connection
     *         for this message request was initiated.
     */
    public long getConnectStartTime() {
        return connectStartTime;
    }
    
    /**
     * Set the timestamp for connection initiation.
     */
    public void setConnectStartTime() {
        connectStartTime = System.nanoTime()/1000000;
    }
    
    /**
     * Reset the connection start time.
     */
    public void clearConnectStartTime() {
        connectStartTime = 0L;
    }
}
