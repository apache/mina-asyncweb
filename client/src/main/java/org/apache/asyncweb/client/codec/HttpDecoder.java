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
package org.apache.asyncweb.client.codec;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Date;

import org.apache.asyncweb.client.util.DateUtil;
import org.apache.asyncweb.client.util.NameValuePair;
import org.apache.mina.common.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for helping to decode the HTTP Protocol.
 */
public class HttpDecoder {
    static final Logger LOG = LoggerFactory.getLogger(HttpDecoder.class);

    /** The Constant CHUNKED. */
    public static final String CHUNKED = "chunked";

    /** The Constant CONNECTION. */
    public static final String CONNECTION = "Connection";
    
    /** The Constant CLOSE as a value for the Connection header */
    public static final String CLOSE = "close";

    /** The Constant COOKIE_COMMENT. */
    public static final String COOKIE_COMMENT = "comment";
    
    /** The Constant COOKIE_DOMAIN. */
    public static final String COOKIE_DOMAIN = "domain";
    
    /** The Constant COOKIE_EXPIRES. */
    public static final String COOKIE_EXPIRES = "expires";
    
    /** The Constant COOKIE_MAX_AGE. */
    public static final String COOKIE_MAX_AGE = "max-age";
    
    /** The Constant COOKIE_PATH. */
    public static final String COOKIE_PATH = "path";
    
    /** The Constant COOKIE_SECURE. */
    public static final String COOKIE_SECURE = "secure";
    
    /** The Constant COOKIE_VERSION. */
    public static final String COOKIE_VERSION = "version";

    /** The Constant LOCATION. */
    public static final String LOCATION = "Location";
    
    /** The Constant SET_COOKIE. */
    public static final String SET_COOKIE = "Set-Cookie";

    /** The Constant WWW_AUTH. */
    public static final String WWW_AUTH = "WWW-Authenticate";

    /** The Constant TRANSFER_ENCODING. */
    public static final String TRANSFER_ENCODING = "Transfer-Encoding";

    /** Carriage return character. */
    private static final byte CR = 13;

    /** Line feed character. */
    private static final byte LF = 10;
    
    /** Single space character. */
    private static final char SP = 32;
    
    /** Horizontal tab character. */
    private static final char HT = 9;


    /** The decoder. */
    private CharsetDecoder decoder = Charset.forName(HttpMessage.HTTP_ELEMENT_CHARSET).newDecoder();

    /**
     * Finds a line from a ByteBuffer that ends with a CR/LF and returns the line as a String.
     * 
     * @param in ByteBuffer containing data
     * 
     * @return a <code>String</code> representing the decoded line
     * 
     * @throws Exception for any Exception that is encountered
     */
    public String decodeLine(ByteBuffer in) throws Exception {
        int beginPos = in.position();
        int limit = in.limit();
        boolean lastIsCR = false;
        int terminatorPos = -1;

        for (int i = beginPos; i < limit; i++) {
            byte b = in.get(i);
            if (b == CR) {
                lastIsCR = true;
            } else {
                if (b == LF && lastIsCR) {
                    terminatorPos = i;
                    break;
                }
                lastIsCR = false;
            }
        }

        //Check if we don't have enough data to process or found a full readable line
        if (terminatorPos == -1) {
            return null;
        }

        String result = null;
        if (terminatorPos > 1) {
            ByteBuffer line = in.slice();
            line.limit(terminatorPos - beginPos - 1);
            result = line.getString(decoder);
        }

        in.position(terminatorPos + 1);

        return result;
    }

    /**
     * Finds a header line from a ByteBuffer that ends with a CR/LF and returns 
     * the line as a String.  Header lines are different from other lines in one
     * important aspect; a single header can span multiple lines.  Such folding
     * occurs if a CR/LF is followed by a LWSP characters (a space or a
     * horizontal tab).  This method returns complete lines that correspond to
     * a single header if the header is folded.
     * 
     * @param in ByteBuffer containing data
     * 
     * @return a <code>String</code> representing the decoded line
     * 
     * @throws Exception for any Exception that is encountered
     */
    public String decodeHeaderLine(ByteBuffer in) throws Exception {
        int beginPos = in.position();
        int limit = in.limit();
        boolean lastIsCR = false;
        boolean lastIsCRLF = false;
        int terminatorPos = -1;

        for (int i = beginPos; i < limit; i++) {
            byte b = in.get(i);
            if (!lastIsCR && b == CR) {
                lastIsCR = true;
            } else if (lastIsCR && !lastIsCRLF && b == LF) {
                // still need to peek one more byte... unless we begin with
                // CRLF in which case we have encountered the end of the header
                // section
                if (i != beginPos + 1) {
                    lastIsCRLF = true;
                } else {
                    // it is an empty line
                    terminatorPos = i;
                    break;
                }
            } else if (lastIsCRLF) {
                // we have read until CRLF: see if there is continuation
                if (isLWSPChar((char)b)) {
                    // folded header; reset the flags and continue
                    lastIsCR = false;
                    lastIsCRLF = false;
                } else {
                    // we have reached the end of the line; move back by one and 
                    // break
                    terminatorPos = i - 1;
                    break;
                }
            }
        }

        // Check if we don't have enough data to process or found a full 
        // readable line
        if (terminatorPos == -1) {
            return null;
        }

        String result = null;
        if (terminatorPos > 1) {
            ByteBuffer line = in.slice();
            line.limit(terminatorPos - beginPos - 1);
            result = line.getString(decoder);
        }

        in.position(terminatorPos + 1);

        return result;
    }

    /**
     * Decodes the status code and message from a HTTP response and places the values in a
     * {@link HttpResponseMessage} object.
     * 
     * @param line <code>String</code> containing <code>HTTP/1.<i>X</i> <i>Message</i></code>
     * @param msg the <code>HttpResponseMessage</code> for which to place the result
     * 
     * @throws Exception on any Exception that may occur
     * 
     * @see HttpResponseMessage
     */
    public void decodeStatus(String line, HttpResponseMessage msg) throws Exception {
        String magic = line.substring(0, 8);
        if (!magic.equals("HTTP/1.1") && !magic.equals("HTTP/1.0")) {
            throw new IOException("Invalid HTTP response");
        }

        String status = line.substring(9, 12);
        msg.setStatusCode(Integer.parseInt(status));
        msg.setStatusMessage(line.substring(13));
    }

    /**
     * Decodes headers and footers (for HTTP/1.1) and stuffs them into a {@link HttpResponseMessage} response.
     * 
     * @param line the <code>String</code> line containing the header or footer
     * @param msg the {@link HttpResponseMessage} response message
     * 
     * @throws Exception if any exception occurs
     */
    public void decodeHeader(String line, HttpResponseMessage msg) throws Exception {
        LOG.debug("Processing Header Line: " + line);
        // first, get rid of the CRLF from linear whitespace
        line = line.replaceAll("\\r\\n([ \\t])", "$1");
        int pos = line.indexOf(":");
        String name = line.substring(0, pos);
        String value = trimHeaderValue(line.substring(pos + 1));
        NameValuePair nvp = new NameValuePair(name, value);
        msg.addHeader(nvp);

        if (name.equalsIgnoreCase(SET_COOKIE)) {
            Cookie cookie = decodeCookie(value, msg);
            if (cookie != null) {
                msg.addCookie(cookie);
            }
        }

        if (name.equalsIgnoreCase(HttpMessage.CONTENT_TYPE)) {
            msg.setContentType(value);
        }

        if (name.equalsIgnoreCase(HttpMessage.CONTENT_LENGTH)) {
            msg.setContentLength(Integer.parseInt(value));
        }

        if (name.equalsIgnoreCase(CONNECTION)) {
            msg.setConnection(value);
        }

        if (name.equalsIgnoreCase(LOCATION)) {
            msg.setLocation(value);
        }

        if (name.equalsIgnoreCase(TRANSFER_ENCODING) && value != null && value.equalsIgnoreCase(CHUNKED)) {
            msg.setChunked(true);
        }

        if (name.equalsIgnoreCase(WWW_AUTH)) {
            msg.addChallenge(nvp);
        }

    }
    
    private String trimHeaderValue(String original) {
        int start = 0;
        int end = original.length();
        
        // remove any preceding LWSP chars from the string
        while (start < end && isLWSPChar(original.charAt(start))) {
            start++;
        }
        
        // remove any trailing LWSP chars from the string
        while (start < end && isLWSPChar(original.charAt(end - 1))) {
            end--;
        }
        
        return original.substring(start, end);
    }
    
    /**
     * Returns whether the character is a LWSP character (SPACE | HTAB) as 
     * specified by RFC 822.
     */
    boolean isLWSPChar(char ch) {
        return ch == SP || ch == HT;
    }

    /**
     * Decodes size records for chunked HTTP transcoding.
     * 
     * @param line the line containing the size
     * 
     * @return the <code>int</code> representing the size
     * 
     * @throws Exception if any exception occurs
     */
    public int decodeSize(String line) throws Exception {
        String strippedLine = line.trim().toLowerCase();
        for (int i = 0; i < strippedLine.length(); i++) {
            char ch = strippedLine.charAt(i);
            //Once we hit a non-numeric character, parse the number we have
            if (ch < '0' || (ch > '9' && ch < 'a') || ch > 'f') {
                return Integer.parseInt(strippedLine.substring(0, i), 16);
            }
        }

        //We got here, so the entire line passes
        return Integer.parseInt(strippedLine, 16);
    }

    /**
     * Decodes content from non-chunked transcoding.  It adds exactly the amount
     * of data specified in the content length header.
     * 
     * @param in the <code>ByteBuffer</code> containing the content at the the 
     * current position
     * @param msg the <code>HttpResponseMessage</code> message to place the 
     * decoded content
     * 
     * @throws Exception if any exception occurs
     */
    public void decodeContent(ByteBuffer in, HttpResponseMessage msg) throws Exception {
        addContent(in, msg, msg.getContentLength());
    }
    
    /**
     * Decodes content from non-chunked transcoding.  It adds all the remaining
     * bytes from the buffer into the response body.
     * 
     * @param in the <code>ByteBuffer</code> containing the content at the
     * current position
     * @param msg the <code>HttpResponseMessage</code> message to place the 
     * decoded content
     * 
     * @throws Exception if any exception occurs
     */
    public void decodeRemainingContent(ByteBuffer in, HttpResponseMessage msg) throws Exception {
        if (in.hasRemaining()) {
            addContent(in, msg, in.remaining());
        }
    }
    
    /**
     * Transfer a specific size of content from the buffer 
     * to the message. 
     * 
     * @param in     The input buffer
     * @param msg    The message that will receive the content.
     * @param size   The number of bytes to transfer.
     * 
     * @exception Exception
     */
    private void addContent(ByteBuffer in, HttpResponseMessage msg, int size) throws Exception {
        byte[] content = new byte[size];
        in.get(content);
        msg.addContent(content);
    }

    /**
     * Decodes content from chunked transcoding.
     * 
     * @param in the <code>ByteBuffer</code> containing the content at the the current position
     * @param msg the <code>HttpResponseMessage</code> message to place the decoded content
     * 
     * @throws Exception if any exception occurs
     */
    public void decodeChunkedContent(ByteBuffer in, HttpResponseMessage msg) throws Exception {
        int toRead = msg.getExpectedToRead();
        if ((in.get(in.position() + toRead) != CR) && (in.get(in.position() + toRead + 1) != LF)) {
            throw new IOException("Invalid HTTP response - chunk does not end with CRLF");

        }
        byte content[] = new byte[toRead];
        in.get(content);
        msg.addContent(content);

        //Pop the CRLF
        in.get();
        in.get();
    }

    /**
     * Decodes a cookie header and returns the decoded cookie.
     * 
     * @param cookieStr the cookie <code>String</code> header line
     * 
     * @return the decoded <code>Cookie</cookie>
     * 
     * @throws Exception if any exception occurs
     * @see Cookie
     */
    public Cookie decodeCookie(String cookieStr, HttpResponseMessage msg) throws Exception {
        LOG.debug("Processing Cookie Line: " + cookieStr);
        Cookie cookie = null;

        String pairs[] = cookieStr.split(";");
        for (int i = 0; i < pairs.length; i++) {
            String nameValue[] = pairs[i].trim().split("=");
            String name = nameValue[0].trim();

            //Check to see if the value was passed-in.
            //According to rfc-2109 value is an optional property and could be blank.
            boolean valuePresent = nameValue.length == 2;

            //First one is the cookie name/value pair
            if (i == 0) {
                if (valuePresent) {
                    cookie = new Cookie(name, nameValue[1].trim());
                } else {
                    //Value is not passed-in, so creating a cookie with an empty value.
                    cookie = new Cookie(name, "");
                }
                continue;
            }

            if (name.equalsIgnoreCase(COOKIE_SECURE)) {
                cookie.setSecure(true);
            }

            if (valuePresent) {
                if (name.equalsIgnoreCase(COOKIE_COMMENT)) {
                    cookie.setComment(nameValue[1].trim());
                    continue;
                }

                if (name.equalsIgnoreCase(COOKIE_PATH)) {
                    cookie.setPath(nameValue[1].trim());
                }

                if (name.equalsIgnoreCase(COOKIE_VERSION)) {
                    cookie.setVersion(Integer.parseInt(nameValue[1]));
                }

                if (name.equalsIgnoreCase(COOKIE_MAX_AGE)) {
                    int age = Integer.parseInt(nameValue[1]);
                    cookie.setExpires(new Date(System.currentTimeMillis() + age * 1000L));
                }

                if (name.equalsIgnoreCase(COOKIE_EXPIRES)) {
                    cookie.setExpires(DateUtil.parseDate(nameValue[1]));
                }

                if (name.equalsIgnoreCase(COOKIE_DOMAIN)) {
                    cookie.setDomain(nameValue[1]);
                }
            }
        }

        // supply the hostname as the domain if it is missing
        if (cookie.getDomain() == null) {
            cookie.setDomain(msg.getRequestURL().getHost());
        }
        
        // use the path (up to the rightmost "/") as the path attribute if it is
        // missing
        if (cookie.getPath() == null) {
            String path = msg.getRequestURL().getPath();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash == -1) {
                // if the slash is absent, treat it as the root context
                path = "/";
            } else if (lastSlash < path.length() - 1) {
                // get it up to the rightmost slash
                path = path.substring(0, lastSlash+1);
            }
            cookie.setPath(path);
        }
        
        return cookie;
    }
}
