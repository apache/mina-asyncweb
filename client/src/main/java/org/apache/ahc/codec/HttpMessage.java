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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.ahc.util.NameValuePair;

/**
 * The Class HttpMessage.  The base class for {@link HttpRequestMessage} and {@link HttpResponseMessage}.
 */
public class HttpMessage {

    /** The Constant HTTP_ELEMENT_CHARSET used for encoding the HTTP elements. */
    public static final String HTTP_ELEMENT_CHARSET = "US-ASCII";
    
    /** The Constant DEFAULT_URL_ENCODING_CHARSET use for URL encoding. */
    public static final String DEFAULT_URL_ENCODING_CHARSET = "UTF-8";

    /** The Constant CONTENT_TYPE. */
    public static final String CONTENT_TYPE = "Content-Type";
    
    /** The Constant CONTENT_LENGTH. */
    public static final String CONTENT_LENGTH = "Content-Length";

    /** The headers associated with the message. */
    protected List<NameValuePair> headers = new ArrayList<NameValuePair>();
    
    /** The cookies associated with the message. */
    protected Map<String,Cookie> cookies = new HashMap<String,Cookie>();
    
    /** The content type. */
    protected String contentType;
    
    /** The content length. */
    protected int contentLength = -1;
    
    /** The content container. */
    protected ByteArrayOutputStream content;

    /** The character charset for URL encoding **/
    protected String urlEncodingCharset = DEFAULT_URL_ENCODING_CHARSET;

    /**
     * Gets the <code>String</code> content.  This method should only be
     * used if the content is certain to be of type html or text as determined by the {@link #getContentType}. 
     * 
     * @return the <code>String</code> content or <code>null</code> if there is no content.
     */
    public String getStringContent() {
        if (content == null) {
            return null;
        }

        return new String(content.toByteArray());
    }

    /**
     * Gets the content as a <code>byte[]</code> array.
     * 
     * @return the <code>byte[]</code> content or <code>null</code> if there is no content. 
     */
    public byte[] getContent() {
        if (content == null) {
            return null;
        }

        return content.toByteArray();
    }

    /**
     * Appends <code>byte[]</code> to the content.
     * 
     * @param byteContent the byte content
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void addContent(byte[] byteContent) throws IOException {
        if (this.content == null) {
            this.content = new ByteArrayOutputStream();
        }

        this.content.write(byteContent);
    }
    
    /**
     * Removes content, if any.
     */
    public void clearContent() {
        // simply nulling the content will do
        if (content != null) {
            content = null;
        }
    }

    /**
     * Gets the cookies.  Returns all existing cookies without filtering.
     * 
     * @return the cookies
     */
    public Collection<Cookie> getCookies() {
        return cookies.values();
    }

    /**
     * Sets the cookies on the message.  Any existing cookies will be completely
     * discarded.  Checks on whether the cookies are acceptable may be
     * performed.
     * 
     * @param cookies the new cookies
     * @see #addCookies(Collection)
     */
    public void setCookies(Collection<Cookie> cookies) {
        if (cookies == null) {
            throw new IllegalArgumentException("null cookie set was passed in");
        }
        
        Map<String,Cookie> newCookies = new HashMap<String,Cookie>();
        for (Cookie cookie : cookies) {
            if (canAcceptCookie(cookie)) {
                newCookies.put(cookie.getName(), cookie);
            }
        }
        this.cookies = newCookies;
    }
    
    /**
     * Adds the cookies to the message.  If the cookie with the same name
     * already exists, the cookie will be replaced.  Checks on whether the
     * cookies are acceptable may be performed.
     * 
     * @see #setCookies(Collection)
     */
    public void addCookies(Collection<Cookie> cookies) {
        if (cookies == null) {
            return;
        }
        
        for (Cookie cookie : cookies) {
            addCookie(cookie);
        }
    }

    /**
     * Adds a cookie to the {@link Cookie} list.
     * 
     * @param cookie the cookie
     */
    public void addCookie(Cookie cookie) {
        if (cookie == null) {
            return;
        }
        
        if (canAcceptCookie(cookie)) {
            this.cookies.put(cookie.getName(), cookie);
        }
    }
    
    /**
     * Returns whether the cookie can be accepted.  Returns true by default.
     */
    protected boolean canAcceptCookie(Cookie cookie) {
        return true;
    }


    /**
     * Returns all headers.
     * 
     * @return all headers
     */
    public List<NameValuePair> getHeaders() {
        return headers;
    }

    /**
     * Sets the headers.  It removes any headers that were stored before the
     * call.
     * 
     * @param headers the new headers
     */
    public void setHeaders(List<NameValuePair> headers) {
        this.headers = headers;
    }

    /**
     * Adds a header to the {@link org.apache.ahc.util.NameValuePair} header list.
     * 
     * @param header the header
     */
    public void addHeader(NameValuePair header) {
        headers.add(header);
    }

    /**
     * Adds the header as a <code>String</code> name/value pair to the 
     * {@link org.apache.ahc.util.NameValuePair} header list.
     * 
     * @param name the name
     * @param value the value
     */
    public void addHeader(String name, String value) {
        headers.add(new NameValuePair(name, value));
    }

    /**
     * Removes the headers that have the given name.
     * 
     * @param name the name
     */
    public void removeHeader(String name) {
        Iterator<NameValuePair> it = headers.iterator();
        while (it.hasNext()) {
            NameValuePair header = it.next();
            if (header.getName().equalsIgnoreCase(name)) {
                it.remove();
            }
        }
    }
    
    /**
     * Sets the header with the given name and the value.  This differs from
     * <code>addHeader()</code> in that it removes any existing header under the
     * name and adds the new one.
     * 
     * @param name the name
     * @param value the value
     * @throws IllegalArgumentException if either the name or the value is null.
     */
    public void setHeader(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("null name or value was passed in");
        }
        
        // we're resetting the value, so remove it first
        removeHeader(name);
        addHeader(name, value);
    }
    
    /**
     * Returns the value for the header with the given name.  If there are more
     * than one header stored, it returns the first entry it finds in the list.
     * 
     * @param name the name
     * @return the value for the header, or null if it is not found
     * @throws IllegalArgumentException if the name is null
     */
    public String getHeader(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null name was passed in");
        }
        
        Iterator<NameValuePair> it = headers.iterator();
        while (it.hasNext()) {
            NameValuePair header = it.next();
            if (header.getName().equalsIgnoreCase(name)) {
                return header.getValue();
            }
        }
        return null;
    }
    
    /**
     * Returns an array of values for the header with the given name.
     * 
     * @param name the name
     * @return the value for the header.  If there is no entry under the name,
     * an empty array is returned.
     * @throws IllegalArgumentException if the name is null
     */
    public String[] getHeaders(String name) {
        if (name == null) {
            throw new IllegalArgumentException("null name was passed in");
        }
        
        List<String> values = new ArrayList<String>();
        Iterator<NameValuePair> it = headers.iterator();
        while (it.hasNext()) {
            NameValuePair header = it.next();
            if (header.getName().equalsIgnoreCase(name)) {
                values.add(header.getValue());
            }
        }
        return values.toArray(new String[]{});
    }

    /**
     * Gets the content type.
     * 
     * @return the content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * 
     * @param contentType the new content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the content length.
     * 
     * @return the content length
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * Sets the content length.
     * 
     * @param contentLength the new content length
     */
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
    
    /**
     * Gets the charset that is used to do URL encoding/decoding.  It is
     * "UTF-8" by default.
     * 
     * @return the charset name
     */
    public String getUrlEncodingCharset() {
        return urlEncodingCharset;
    }

    /**
     * Sets the charset that is used to do URL encoding/decoding.  In reality,
     * the only recommended values are "UTF-8", and normally it is neither
     * necessary nor recommended to use something other than the default.  <b>Do
     * not reset this value</b> unless there is a very clear interoperability
     * reason to do so.
     * 
     * @param charset the charset name
     */
    public void setUrlEncodingCharset(String charset) {
        this.urlEncodingCharset = charset;
    }
}
