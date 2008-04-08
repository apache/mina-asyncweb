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

import java.net.URL;
import java.util.ArrayList;

import org.apache.ahc.util.NameValuePair;

/**
 * The Class HttpResponseMessage. This is an object representation of an HTTP response.
 */
public class HttpResponseMessage extends HttpMessage {

    /** The Constant EXPECTED_NOT_READ. */
    static final int EXPECTED_NOT_READ = -1;

    /** The Constant STATE_START. */
    static final int STATE_START = 0;
    
    /** The Constant STATE_STATUS_CONTINUE. */
    static final int STATE_STATUS_CONTINUE = 1;
    
    /** The Constant STATE_STATUS_READ. */
    static final int STATE_STATUS_READ = 2;
    
    /** The Constant STATE_HEADERS_READ. */
    static final int STATE_HEADERS_READ = 3;
    
    /** The Constant STATE_CONTENT_READ. */
    static final int STATE_CONTENT_READ = 4;
    
    /** The Constant STATE_FOOTERS_READ. */
    static final int STATE_FOOTERS_READ = 5;
    
    /** The Constant STATE_FINISHED. */
    static final int STATE_FINISHED = 6;
    
    private final URL url;

    /** The status code. */
    private int statusCode;
    
    /** The status message. */
    private String statusMessage;
    
    /** The connection. */
    private String connection;

    /** The chunked. */
    private boolean chunked;
    
    /** The expected to read. */
    private int expectedToRead = -1;
    
    /** The state. */
    private int state = STATE_START;
    
    /** The location. */
    private String location;

    /** The attachment. */
    private Object attachment;

    /** The challenge list **/
    private ArrayList<NameValuePair> challenges = new ArrayList<NameValuePair>();
    
    public HttpResponseMessage(URL url) {
        this.url = url;
    }
    
    public URL getRequestURL() {
        return url;
    }
    
    /**
     * Gets the HTTP status code.
     * 
     * @return the HTTP status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Sets the HTTP status code.
     * 
     * @param statusCode the new HTTP status code
     */
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets the HTTP status message.
     * 
     * @return the HTTP status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Sets the HTTP status message.
     * 
     * @param statusMessage the new HTTP status message
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    /**
     * Checks if the response is transcoded as chunked.
     * 
     * @return <code>true</code> if the transcoding is chunked
     */
    boolean isChunked() {
        return chunked;
    }

    /**
     * Flags the response transcoding chunked.
     * 
     * @param chunked <code>true</code> if the transcoding is chunked
     */
    void setChunked(boolean chunked) {
        this.chunked = chunked;
    }

    /**
     * Gets the expected number of bytes to read.  Used during decoding chunked transcoding.
     * 
     * @return the expected to read
     */
    int getExpectedToRead() {
        return expectedToRead;
    }

    /**
     * Sets the expected number of bytes to read. Used during decoding chunked transcoding.
     * 
     * @param expectedToRead the new expected number of bytes to read
     */
    void setExpectedToRead(int expectedToRead) {
        this.expectedToRead = expectedToRead;
    }

    /**
     * Gets the state of the response during decoding.
     * 
     * @return the state
     */
    int getState() {
        return state;
    }

    /**
     * Sets the state of the response during decoding.
     * 
     * @param state the new state
     */
    void setState(int state) {
        this.state = state;
    }

    /**
     * Gets the location that is typically set on redirects.
     * 
     * @return the location or <code>null</code> if one does not exist.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location that is typically set on redirects.
     * 
     * @param location the new location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the <code>Connection</code> value from an HTTP header.
     * 
     * @return the connection
     */
    public String getConnection() {
        return connection;
    }

    /**
     * Sets the <code>Connection</code> value from an HTTP header.
     * 
     * @param connection the new connection
     */
    public void setConnection(String connection) {
        this.connection = connection;
    }


    /**
     * Gets the attachment.
     * 
     * @return the attachment
     */
    public Object getAttachment() {
        return attachment;
    }

    /**
     * Sets the attachment.
     * 
     * @param attachment the new attachment
     */
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

        /**
     * Gets the challenges map
     * @return the challenges map.
     */
    public ArrayList<NameValuePair> getChallenges() {
        return challenges;
    }

    /**
     * Adds a challenge to the challenge list
     *
     * @param challenge the challenge name value pair
     */
    public void addChallenge(NameValuePair challenge){
        challenges.add(challenge);
    }
}
