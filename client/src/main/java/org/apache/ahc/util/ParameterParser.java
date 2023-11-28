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

package org.apache.ahc.util;

import java.util.ArrayList;
import java.util.List;

public class ParameterParser {

    /**
     * String to be parsed
     */
    private char[] chars = null;

    /**
     * Current position in the string
     */
    private int pos = 0;

    /**
     * Maximum position in the string
     */
    private int len = 0;

    /**
     * Start of a token
     */
    private int i1 = 0;

    /**
     * End of a token
     */
    private int i2 = 0;

    /**
     * Default ParameterParser constructor
     */
    public ParameterParser() {
        super();
    }


    /**
     * Are there any characters left to parse?
     */
    private boolean hasChar() {
        return this.pos < this.len;
    }


    /**
     * A helper method to process the parsed token.
     */
    private String getToken(boolean quoted) {
        // Trim leading white spaces
        while ((i1 < i2) && (Character.isWhitespace(chars[i1]))) {
            i1++;
        }
        // Trim trailing white spaces
        while ((i2 > i1) && (Character.isWhitespace(chars[i2 - 1]))) {
            i2--;
        }
        // Strip away quotes if necessary
        if (quoted) {
            if (((i2 - i1) >= 2)
                && (chars[i1] == '"')
                && (chars[i2 - 1] == '"')) {
                i1++;
                i2--;
            }
        }
        String result = null;
        if (i2 >= i1) {
            result = new String(chars, i1, i2 - i1);
        }
        return result;
    }


    /**
     * Is given character present in the array of characters?
     */
    private boolean isOneOf(char ch, char[] charray) {
        boolean result = false;
        for (int i = 0; i < charray.length; i++) {
            if (ch == charray[i]) {
                result = true;
                break;
            }
        }
        return result;
    }


    /**
     * Parse out a token until any of the given terminators
     * is encountered.
     */
    private String parseToken(final char[] terminators) {
        char ch;
        i1 = pos;
        i2 = pos;
        while (hasChar()) {
            ch = chars[pos];
            if (isOneOf(ch, terminators)) {
                break;
            }
            i2++;
            pos++;
        }
        return getToken(false);
    }


    /**
     * Parse out a token until any of the given terminators
     * is encountered. Special characters in quoted tokens
     * are escaped.
     */
    private String parseQuotedToken(final char[] terminators) {
        char ch;
        i1 = pos;
        i2 = pos;
        boolean quoted = false;
        boolean charEscaped = false;
        while (hasChar()) {
            ch = chars[pos];
            if (!quoted && isOneOf(ch, terminators)) {
                break;
            }
            if (!charEscaped && ch == '"') {
                quoted = !quoted;
            }
            charEscaped = (!charEscaped && ch == '\\');
            i2++;
            pos++;

        }
        return getToken(true);
    }

    /**
     * Extracts a list of {@link NameValuePair}s from the given string.
     *
     * @param str the string that contains a sequence of name/value pairs
     * @return a list of {@link NameValuePair}s
     */
    public List<NameValuePair> parse(final String str, char separator) {

        if (str == null) {
            return new ArrayList<NameValuePair>();
        }
        return parse(str.toCharArray(), separator);
    }

    /**
     * Extracts a list of {@link NameValuePair}s from the given array of
     * characters.
     *
     * @param chars the array of characters that contains a sequence of
     *              name/value pairs
     * @return a list of {@link NameValuePair}s
     */
    public List<NameValuePair> parse(final char[] chars, char separator) {

        if (chars == null) {
            return new ArrayList<NameValuePair>();
        }
        return parse(chars, 0, chars.length, separator);
    }


    /**
     * Extracts a list of {@link NameValuePair}s from the given array of
     * characters.
     *
     * @param chars  the array of characters that contains a sequence of
     *               name/value pairs
     * @param offset - the initial offset.
     * @param length - the length.
     * @return a list of {@link NameValuePair}s
     */
    public List<NameValuePair> parse(final char[] chars, int offset, int length, char separator) {

        if (chars == null) {
            return new ArrayList<NameValuePair>();
        }
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        this.chars = chars;
        this.pos = offset;
        this.len = length;

        String paramName = null;
        String paramValue = null;
        while (hasChar()) {
            paramName = parseToken(new char[] {'=', separator});
            paramValue = null;
            if (hasChar() && (chars[pos] == '=')) {
                pos++; // skip '='
                paramValue = parseQuotedToken(new char[] {separator});
            }
            if (hasChar() && (chars[pos] == separator)) {
                pos++; // skip separator
            }
            if (paramName != null && !(paramName.isEmpty() && paramValue == null)) {
                params.add(new NameValuePair(paramName, paramValue));
            }
        }
        return params;
    }
}
