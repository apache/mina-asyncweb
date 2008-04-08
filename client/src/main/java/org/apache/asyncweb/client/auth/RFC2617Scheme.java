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

import java.util.Map;

public abstract class RFC2617Scheme implements AuthScheme{
        /**
     * Authentication parameter map.
     */
    private Map params = null;

    /**
     * Default constructor for RFC2617 compliant authetication schemes.
     *
     * @since 3.0
     */
    public RFC2617Scheme() {
        super();
    }

    /**
     * Default constructor for RFC2617 compliant authetication schemes.
     *
     * @param challenge authentication challenge
     *
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     *
     * @deprecated Use parameterless constructor and {@link AuthScheme#processChallenge(String)}
     *             method
     */
    public RFC2617Scheme(final String challenge) throws MalformedChallengeException {
        super();
        processChallenge(challenge);
    }

    /**
     * Processes the given challenge token. Some authentication schemes
     * may involve multiple challenge-response exchanges. Such schemes must be able
     * to maintain the state information when dealing with sequential challenges
     *
     * @param challenge the challenge string
     *
     * @throws MalformedChallengeException is thrown if the authentication challenge
     * is malformed
     *
     * @since 3.0
     */
    public void processChallenge(final String challenge) throws MalformedChallengeException {
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException("Invalid " + getSchemeName() + " challenge: " + challenge);
        }
        this.params = AuthChallengeParser.extractParams(challenge);
    }

    /**
     * Returns authentication parameters map. Keys in the map are lower-cased.
     *
     * @return the map of authentication parameters
     */
    protected Map getParameters() {
        return this.params;
    }

    /**
     * Returns authentication parameter with the given name, if available.
     *
     * @param name The name of the parameter to be returned
     *
     * @return the parameter with the given name
     */
    public String getParameter(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Parameter name may not be null");
        }
        if (this.params == null) {
            return null;
        }
        return (String) this.params.get(name.toLowerCase());
    }

    /**
     * Returns authentication realm. The realm may not be null.
     *
     * @return the authentication realm
     */
    public String getRealm() {
        return getParameter("realm");
    }
}
