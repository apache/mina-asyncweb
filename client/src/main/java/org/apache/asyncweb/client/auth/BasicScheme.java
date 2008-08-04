/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.asyncweb.client.auth;

import org.apache.asyncweb.client.codec.HttpRequestMessage;
import org.apache.asyncweb.client.util.EncodingUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicScheme extends RFC2617Scheme{
        /** Log object for this class. */
    private static final Logger LOG = LoggerFactory.getLogger(BasicScheme.class);

    /** Whether the basic authentication process is complete */
    private boolean complete;

    /**
     * Default constructor for the basic authetication scheme.
     *
     * @since 3.0
     */
    public BasicScheme() {
        super();
        this.complete = false;
    }

    /**
     * Returns textual designation of the basic authentication scheme.
     *
     * @return <code>basic</code>
     */
    public String getSchemeName() {
        return "basic";
    }

    /**
     * Processes the Basic challenge.
     *
     * @param challenge the challenge string
     *
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     *
     * @since 3.0
     */
    public void processChallenge(String challenge)
        throws MalformedChallengeException
    {
        super.processChallenge(challenge);
        this.complete = true;
    }

    /**
     * Tests if the Basic authentication process has been completed.
     *
     * @return <tt>true</tt> if Basic authorization has been processed,
     *   <tt>false</tt> otherwise.
     *
     * @since 3.0
     */
    public boolean isComplete() {
        return this.complete;
    }

    /**
     * Returns <tt>false</tt>. Basic authentication scheme is request based.
     *
     * @return <tt>false</tt>.
     *
     * @since 3.0
     */
    public boolean isConnectionBased() {
        return false;
    }

    /**
     * Produces basic authorization string for the given set of {@link Credentials}.
     *
     * @param credentials The set of credentials to be used for athentication
     * @param request The request being authenticated
     * @throws InvalidCredentialsException if authentication credentials
     *         are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException if authorization string cannot
     *   be generated due to an authentication failure
     *
     * @return a basic authorization string
     *
     * @since 3.0
     */
    public String authenticate(Credentials credentials, HttpRequestMessage request) throws AuthenticationException {

        if (LOG.isTraceEnabled()) {
            LOG.trace("enter BasicScheme.authenticate(Credentials, HttpMethod)");
        }

        if (request == null) {
            throw new IllegalArgumentException("Request may not be null");
        }
        UsernamePasswordCredentials usernamepassword = null;
        try {
            usernamepassword = (UsernamePasswordCredentials) credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
                    "Credentials cannot be used for basic authentication: "
                    + credentials.getClass().getName());
        }
        return BasicScheme.authenticate( usernamepassword, request.getCredentialCharset());
    }


    /**
     * Returns a basic <tt>Authorization</tt> header value for the given
     * {@link UsernamePasswordCredentials} and charset.
     *
     * @param credentials The credentials to encode.
     * @param charset The charset to use for encoding the credentials
     *
     * @return a basic authorization string
     *
     * @since 3.0
     */
    public static String authenticate(UsernamePasswordCredentials credentials, String charset) {

        if (LOG.isTraceEnabled()) {
            LOG.trace("enter BasicScheme.authenticate(UsernamePasswordCredentials, String)");
        }

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        }
        if (charset == null || charset.length() == 0) {
            throw new IllegalArgumentException("charset may not be null or empty");
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(credentials.getUserName());
        buffer.append(":");
        buffer.append(credentials.getPassword());

        return "Basic " + EncodingUtil.getAsciiString(
                Base64.encodeBase64(EncodingUtil.getBytes(buffer.toString(), charset)));
    }
}
