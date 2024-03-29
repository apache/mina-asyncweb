/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.ahc.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The home for utility methods that handle various encoding tasks.
 *
 * @author Michael Becke
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * @since 2.0 final
 */
public final class EncodingUtil {

    static final Logger LOG = LoggerFactory.getLogger(EncodingUtil.class);

    /**
     * Default content encoding chatset
     */
    private static final String DEFAULT_CHARSET = "ISO-8859-1";

    /**
     * This class should not be instantiated.
     */
    private EncodingUtil() {
    }

    /**
     * Form-urlencoding routine.
     * <p>
     * The default encoding for all forms is `application/x-www-form-urlencoded'.
     * A form data set is represented in this media type as follows:
     * </p>
     * <p>
     * The form field names and values are escaped: space characters are replaced
     * by `+', and then reserved characters are escaped as per [URL]; that is,
     * non-alphanumeric characters are replaced by `%HH', a percent sign and two
     * hexadecimal digits representing the ASCII code of the character. Line breaks,
     * as in multi-line text field values, are represented as CR LF pairs, i.e. `%0D%0A'.
     * </p>
     * <p>
     * if the given charset is not supported, ISO-8859-1 is used instead.
     * </p>
     *
     * @param pairs   the values to be encoded
     * @param charset the character set of pairs to be encoded
     * @return the urlencoded pairs
     * @since 2.0 final
     */
    public static String formUrlEncode(NameValuePair[] pairs, String charset) {
        try {
            return doFormUrlEncode(pairs, charset);
        } catch (UnsupportedEncodingException e) {
            LOG.error("Encoding not supported: " + charset);
            try {
                return doFormUrlEncode(pairs, DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException fatal) {
                // Should never happen. ISO-8859-1 must be supported on all JVMs
                throw new AsyncHttpClientException("Encoding not supported: "
                    + DEFAULT_CHARSET);
            }
        }
    }

    /**
     * Form-urlencoding routine.
     * <p/>
     * The default encoding for all forms is `application/x-www-form-urlencoded'.
     * A form data set is represented in this media type as follows:
     * <p/>
     * The form field names and values are escaped: space characters are replaced
     * by `+', and then reserved characters are escaped as per [URL]; that is,
     * non-alphanumeric characters are replaced by `%HH', a percent sign and two
     * hexadecimal digits representing the ASCII code of the character. Line breaks,
     * as in multi-line text field values, are represented as CR LF pairs, i.e. `%0D%0A'.
     *
     * @param pairs   the values to be encoded
     * @param charset the character set of pairs to be encoded
     * @return the urlencoded pairs
     * @throws UnsupportedEncodingException if charset is not supported
     * @since 2.0 final
     */
    private static String doFormUrlEncode(NameValuePair[] pairs, String charset)
        throws UnsupportedEncodingException {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < pairs.length; i++) {
            URLCodec codec = new URLCodec();
            NameValuePair pair = pairs[i];
            if (pair.getName() != null) {
                if (i > 0) {
                    buf.append("&");
                }
                buf.append(codec.encode(pair.getName(), charset));
                buf.append("=");
                if (pair.getValue() != null) {
                    buf.append(codec.encode(pair.getValue(), charset));
                }
            }
        }
        return buf.toString();
    }

    /**
     * Converts the byte array of HTTP content characters to a string. If
     * the specified charset is not supported, default system encoding
     * is used.
     *
     * @param data    the byte array to be encoded
     * @param offset  the index of the first byte to encode
     * @param length  the number of bytes to encode
     * @param charset the desired character encoding
     * @return The result of the conversion.
     * @since 3.0
     */
    public static String getString(
        final byte[] data,
        int offset,
        int length,
        String charset
    ) {

        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        }

        if (charset == null || charset.length() == 0) {
            throw new IllegalArgumentException("charset may not be null or empty");
        }

        try {
            return new String(data, offset, length, charset);
        } catch (UnsupportedEncodingException e) {

            if (LOG.isWarnEnabled()) {
                LOG.warn("Unsupported encoding: " + charset + ". System encoding used");
            }
            return new String(data, offset, length);
        }
    }


    /**
     * Converts the byte array of HTTP content characters to a string. If
     * the specified charset is not supported, default system encoding
     * is used.
     *
     * @param data    the byte array to be encoded
     * @param charset the desired character encoding
     * @return The result of the conversion.
     * @since 3.0
     */
    public static String getString(final byte[] data, String charset) {
        return getString(data, 0, data.length, charset);
    }

    /**
     * Converts the specified string to a byte array.  If the charset is not supported the
     * default system charset is used.
     *
     * @param data    the string to be encoded
     * @param charset the desired character encoding
     * @return The resulting byte array.
     * @since 3.0
     */
    public static byte[] getBytes(final String data, String charset) {

        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }

        if (charset == null || charset.length() == 0) {
            throw new IllegalArgumentException("charset may not be null or empty");
        }

        try {
            return data.getBytes(charset);
        } catch (UnsupportedEncodingException e) {

            if (LOG.isWarnEnabled()) {
                LOG.warn("Unsupported encoding: " + charset + ". System encoding used.");
            }

            return data.getBytes();
        }
    }

    /**
     * Converts the specified string to byte array of ASCII characters.
     *
     * @param data the string to be encoded
     * @return The string as a byte array.
     * @since 3.0
     */
    public static byte[] getAsciiBytes(final String data) {

        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        }

        try {
            return data.getBytes("US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AsyncHttpClientException("HttpClient requires ASCII support");
        }
    }

    /**
     * Converts the byte array of ASCII characters to a string. This method is
     * to be used when decoding content of HTTP elements (such as response
     * headers)
     *
     * @param data   the byte array to be encoded
     * @param offset the index of the first byte to encode
     * @param length the number of bytes to encode
     * @return The string representation of the byte array
     */
    public static String getAsciiString(final byte[] data, int offset, int length) {

        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        }

        try {
            return new String(data, offset, length, "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AsyncHttpClientException("HttpClient requires ASCII support");
        }
    }

    /**
     * Converts the byte array of ASCII characters to a string. This method is
     * to be used when decoding content of HTTP elements (such as response
     * headers)
     *
     * @param data the byte array to be encoded
     * @return The string representation of the byte array
     */
    public static String getAsciiString(final byte[] data) {
        return getAsciiString(data, 0, data.length);
    }


}

