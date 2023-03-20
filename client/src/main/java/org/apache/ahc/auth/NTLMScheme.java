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

package org.apache.ahc.auth;

import org.apache.ahc.codec.HttpRequestMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the Microsoft proprietary NTLM authentication scheme.
 *
 * @author <a href="mailto:remm@apache.org">Remy Maucherat</a>
 * @author Rodney Waldhoff
 * @author <a href="mailto:jsdever@apache.org">Jeff Dever</a>
 * @author Ortwin Gl???ck
 * @author Sean C. Sullivan
 * @author <a href="mailto:adrian@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:mbowler@GargoyleSoftware.com">Mike Bowler</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 */
public class NTLMScheme implements AuthScheme {

    /**
     * Log object for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(NTLMScheme.class);

    /**
     * NTLM challenge string.
     */
    private String ntlmchallenge = null;

    private static final int UNINITIATED = 0;
    private static final int INITIATED = 1;
    private static final int TYPE1_MSG_GENERATED = 2;
    private static final int TYPE2_MSG_RECEIVED = 3;
    private static final int TYPE3_MSG_GENERATED = 4;
    private static final int FAILED = Integer.MAX_VALUE;

    /**
     * Authentication process state
     */
    private int state;

    /**
     * Default constructor for the NTLM authentication scheme.
     */
    public NTLMScheme() {
        super();
        this.state = UNINITIATED;
    }

    /**
     * Constructor for the NTLM authentication scheme.
     *
     * @param challenge The authentication challenge
     * @throws MalformedChallengeException is thrown if the authentication challenge
     *                                     is malformed
     */
    public NTLMScheme(final String challenge) throws MalformedChallengeException {
        super();
        processChallenge(challenge);
    }

    /**
     * Processes the NTLM challenge.
     *
     * @param challenge the challenge string
     * @throws MalformedChallengeException is thrown if the authentication challenge
     *                                     is malformed
     */
    public void processChallenge(final String challenge) throws MalformedChallengeException {
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException("Invalid NTLM challenge: " + challenge);
        }
        int i = challenge.indexOf(' ');
        if (i != -1) {
            s = challenge.substring(i, challenge.length());
            this.ntlmchallenge = s.trim();
            this.state = TYPE2_MSG_RECEIVED;
        } else {
            this.ntlmchallenge = "";
            if (this.state == UNINITIATED) {
                this.state = INITIATED;
            } else {
                this.state = FAILED;
            }
        }
    }

    /**
     * Tests if the NTLM authentication process has been completed.
     *
     * @return <tt>true</tt> if Basic authorization has been processed,
     *         <tt>false</tt> otherwise.
     */
    public boolean isComplete() {
        return this.state == TYPE3_MSG_GENERATED || this.state == FAILED;
    }

    /**
     * Returns textual designation of the NTLM authentication scheme.
     *
     * @return <code>ntlm</code>
     */
    public String getSchemeName() {
        return "ntlm";
    }

    /**
     * The concept of an authentication realm is not supported by the NTLM
     * authentication scheme. Always returns <code>null</code>.
     *
     * @return <code>null</code>
     */
    public String getRealm() {
        return null;
    }

    /**
     * Returns the authentication parameter with the given name, if available.
     * <p>There are no valid parameters for NTLM authentication so this method always returns
     * <tt>null</tt>.</p>
     *
     * @param name The name of the parameter to be returned
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null");
        }
        return null;
    }

    /**
     * Returns <tt>true</tt>. NTLM authentication scheme is connection based.
     *
     * @return <tt>true</tt>.
     */
    public boolean isConnectionBased() {
        return true;
    }

    /**
     * Produces NTLM authorization string for the given set of {@link Credentials}.
     *
     * @param credentials The set of credentials to be used for athentication
     * @param request     The request being authenticated
     * @return an NTLM authorization string
     * @throws InvalidCredentialsException if authentication credentials
     *                                     are not valid or not applicable for this authentication scheme
     * @throws AuthenticationException     if authorization string cannot
     *                                     be generated due to an authentication failure
     */
    public String authenticate(Credentials credentials, HttpRequestMessage request)
        throws AuthenticationException {
        LOG.trace("enter NTLMScheme.authenticate(Credentials, HttpMethod)");

        if (this.state == UNINITIATED) {
            throw new IllegalStateException("NTLM authentication process has not been initiated");
        }

        NTCredentials ntcredentials = null;
        try {
            ntcredentials = (NTCredentials)credentials;
        } catch (ClassCastException e) {
            throw new InvalidCredentialsException(
                "Credentials cannot be used for NTLM authentication: "
                    + credentials.getClass().getName());
        }
        NTLM ntlm = new NTLM();
        ntlm.setCredentialCharset(request.getCredentialCharset());
        String response = null;
        if (this.state == INITIATED || this.state == FAILED) {
            response = ntlm.getType1Message(
                ntcredentials.getHost(),
                ntcredentials.getDomain());
            this.state = TYPE1_MSG_GENERATED;
        } else {
            response = ntlm.getType3Message(
                ntcredentials.getUserName(),
                ntcredentials.getPassword(),
                ntcredentials.getHost(),
                ntcredentials.getDomain(),
                ntlm.parseType2Message(this.ntlmchallenge));
            this.state = TYPE3_MSG_GENERATED;
        }
        return "NTLM " + response;
    }
}

