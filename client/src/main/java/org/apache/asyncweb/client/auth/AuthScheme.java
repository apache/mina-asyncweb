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

public interface AuthScheme {
    /**
     * Processes the given challenge token. Some authentication schemes
     * may involve multiple challenge-response exchanges. Such schemes must be able
     * to maintain the state information when dealing with sequential challenges
     *
     * @param challenge the challenge string
     */
    void processChallenge(final String challenge) throws MalformedChallengeException;

    /**
     * Returns textual designation of the given authentication scheme.
     *
     * @return the name of the given authentication scheme
     */
    String getSchemeName();

    /**
     * Returns authentication parameter with the given name, if available.
     *
     * @param name The name of the parameter to be returned
     *
     * @return the parameter with the given name
     */
    String getParameter(final String name);

    /**
     * Returns authentication realm. If the concept of an authentication
     * realm is not applicable to the given authentication scheme, returns
     * <code>null</code>.
     *
     * @return the authentication realm
     */
    String getRealm();

    /**
     * Tests if the authentication scheme is provides authorization on a per
     * connection basis instead of usual per request basis
     *
     * @return <tt>true</tt> if the scheme is connection based, <tt>false</tt>
     * if the scheme is request based.
     */
    boolean isConnectionBased();

    /**
     * Authentication process may involve a series of challenge-response exchanges.
     * This method tests if the authorization process has been completed, either
     * successfully or unsuccessfully, that is, all the required authorization
     * challenges have been processed in their entirety.
     *
     * @return <tt>true</tt> if the authentication process has been completed,
     * <tt>false</tt> otherwise.
     */
    boolean isComplete();

    /**
     * Produces an authorization string for the given set of {@link Credentials}.
     *
     * @param credentials The set of credentials to be used for athentication
     * @param method The method being authenticated
     * @throws AuthenticationException if authorization string cannot
     *   be generated due to an authentication failure
     *
     * @return the authorization string
     */
    String authenticate(Credentials credentials, HttpRequestMessage method) throws AuthenticationException;

}
