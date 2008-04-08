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

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.ahc.util.NameValuePair;
import org.apache.ahc.util.ParameterParser;

public class AuthChallengeParser {
        /**
     * Extracts authentication scheme from the given authentication
     * challenge.
     *
     * @param challengeStr the authentication challenge string
     * @return authentication scheme
     *
     * @throws MalformedChallengeException when the authentication challenge string
     *  is malformed
     *
     * @since 2.0beta1
     */
    public static String extractScheme(final String challengeStr)
      throws MalformedChallengeException {
        if (challengeStr == null) {
            throw new IllegalArgumentException("Challenge may not be null");
        }
        int idx = challengeStr.indexOf(' ');
        String s = null;
        if (idx == -1) {
            s = challengeStr;
        } else {
            s = challengeStr.substring(0, idx);
        }
        if (s.equals("")) {
            throw new MalformedChallengeException("Invalid challenge: " + challengeStr);
        }
        return s.toLowerCase();
    }

    /**
     * Extracts a map of challenge parameters from an authentication challenge.
     * Keys in the map are lower-cased
     *
     * @param challengeStr the authentication challenge string
     * @return a map of authentication challenge parameters
     * @throws MalformedChallengeException when the authentication challenge string
     *  is malformed
     *
     * @since 2.0beta1
     */
    public static Map extractParams(final String challengeStr)
      throws MalformedChallengeException {
        if (challengeStr == null) {
            throw new IllegalArgumentException("Challenge may not be null");
        }
        int idx = challengeStr.indexOf(' ');
        if (idx == -1) {
            throw new MalformedChallengeException("Invalid challenge: " + challengeStr);
        }
        Map map = new HashMap();
        ParameterParser parser = new ParameterParser();
        List params = parser.parse( challengeStr.substring(idx + 1, challengeStr.length()), ',');
        for (int i = 0; i < params.size(); i++) {
            NameValuePair param = (NameValuePair) params.get(i);
            map.put(param.getName().toLowerCase(), param.getValue());
        }
        return map;
    }
}
