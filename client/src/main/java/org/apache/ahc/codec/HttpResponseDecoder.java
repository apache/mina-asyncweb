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

import org.apache.ahc.HttpIoHandler;
import org.apache.ahc.util.NeedMoreDataException;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.IoBuffer;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/**
 * The Class HttpResponseDecoder. This handles the decoding of raw bytes into a
 * {@link HttpResponseMessage} object.
 */
public class HttpResponseDecoder extends CumulativeProtocolDecoder {

    /** The http decoder. */
    private HttpDecoder httpDecoder = new HttpDecoder();

    /**
     * Decodes the raw HTTP response from a server into a {@link HttpResponseMessage} object.
     * 
     * @param ioSession the {@link org.apache.mina.common.IoSession} representing the connection to the server.
     * @param in the <code>ByteBuffer</code> that contains the raw bytes from the server
     * @param out {@link org.apache.mina.filter.codec.ProtocolDecoderOutput} used for output
     * 
     * @return <code>true</code> if it can read all of the data, or <code>false</code> if not.  A false tells the API to fetch more data.
     * 
     */
    protected boolean doDecode(IoSession ioSession, IoBuffer in, ProtocolDecoderOutput out)
        throws Exception {

        try {
            HttpResponseMessage response = (HttpResponseMessage)ioSession.getAttribute(HttpIoHandler.CURRENT_RESPONSE);
            if (response == null) {
                response = new HttpResponseMessage();
                ioSession.setAttribute(HttpIoHandler.CURRENT_RESPONSE, response);
            }

            //Test if we need the response...
            if (response.getState() == HttpResponseMessage.STATE_START) {

                if (!processStatus(response, in)) {
                    throw new NeedMoreDataException();
                }

                //Handle HTTP/1.1 100 Continue
                if (response.getStatusCode() == 100) {
                    response.setState(HttpResponseMessage.STATE_STATUS_CONTINUE);
                } else {
                    response.setState(HttpResponseMessage.STATE_STATUS_READ);
                }
            }

            //If we are in a 100 Continue, read until we get the real header
            if (response.getState() == HttpResponseMessage.STATE_STATUS_CONTINUE) {
                //Continue reading until we get a blank line
                while (true) {
                    String line = httpDecoder.decodeLine(in);

                    //Check if the entire response has been read
                    if (line == null) {
                        throw new NeedMoreDataException();
                    }

                    //Check if the entire response headers have been read
                    if (line.length() == 0) {
                        response.setState(HttpResponseMessage.STATE_STATUS_READ);

                        //The next line should be a header
                        if (!processStatus(response, in)) {
                            throw new NeedMoreDataException();
                        }
                        break;
                    }
                }
            }

            //Are we reading headers?
            if (response.getState() == HttpResponseMessage.STATE_STATUS_READ) {
                if (!processHeaders(response, in)) {
                    throw new NeedMoreDataException();
                }
            }

            //Are we reading content?
            if (response.getState() == HttpResponseMessage.STATE_HEADERS_READ) {
                if (!processContent(response, in)) {
                    throw new NeedMoreDataException();
                }
            }

            //If we are chunked and we have read all the content, then read the footers if there are any
            if (response.isChunked() && response.getState() == HttpResponseMessage.STATE_CONTENT_READ) {
                if (!processFooters(response, in)) {
                    throw new NeedMoreDataException();
                }
            }

            response.setState(HttpResponseMessage.STATE_FINISHED);

            out.write(response);

            ioSession.removeAttribute(HttpIoHandler.CURRENT_RESPONSE);

            return true;
        } catch (NeedMoreDataException e) {
            return false;
        }
    }

    /**
     * Reads the headers and processes them as header objects in the {@link HttpResponseMessage} object.
     * 
     * @param response the {@link HttpResponseMessage} response object
     * @param in the <code>ByteBuffer</code> that contains the raw bytes from the server
     * 
     * @return <code>true</code> if it can read all of the data, or <code>false</code> if not.
     * 
     * @throws Exception if any exception occurs
     */
    private boolean processHeaders(HttpResponseMessage response, IoBuffer in) throws Exception {
        if (!findHeaders(response, in)) {
            return false;
        }

        response.setState(HttpResponseMessage.STATE_HEADERS_READ);
        return true;
    }

    /**
     * Reads the footers and processes them as header objects in the {@link HttpResponseMessage} 
     * object.  This is only used in chunked transcoding.
     * 
     * @param response the {@link HttpResponseMessage} response object
     * @param in the <code>ByteBuffer</code> that contains the raw bytes from the server
     * 
     * @return <code>true</code> if it can read all of the data, or <code>false</code> if not.
     * 
     * @throws Exception if any exception occurs
     */
    private boolean processFooters(HttpResponseMessage response, IoBuffer in) throws Exception {
        if (!findHeaders(response, in)) {
            return false;
        }

        response.setState(HttpResponseMessage.STATE_FOOTERS_READ);
        return true;
    }

    /**
     * Decodes the raw headers/footers.
     * 
     * @param response the {@link HttpResponseMessage} response object
     * @param in the <code>ByteBuffer</code> that contains the raw bytes from the server
     * 
     * @return <code>true</code> if it can read all of the data, or <code>false</code> if not.
     * 
     * @throws Exception if any exception occurs
     */
    private boolean findHeaders(HttpResponseMessage response, IoBuffer in) throws Exception {
        //Read the headers and process them
        while (true) {
            String line = httpDecoder.decodeLine(in);

            //Check if the entire response has been read
            if (line == null) {
                return false;
            }

            //Check if the entire response headers have been read
            if (line.length() == 0) {
                break;
            }

            httpDecoder.decodeHeader(line, response);
        }
        return true;
    }

    /**
     * Process and read the content.
     * 
     * @param response the {@link HttpResponseMessage} response object
     * @param in the <code>ByteBuffer</code> that contains the raw bytes from the server
     * 
     * @return <code>true</code> if it can read all of the data, or <code>false</code> if not.
     * 
     * @throws Exception if any exception occurs
     */
    private boolean processContent(HttpResponseMessage response, IoBuffer in) throws Exception {
        if (response.isChunked()) {
            while (true) {
                //Check what kind of record we are reading (content or size)
                if (response.getExpectedToRead() == HttpResponseMessage.EXPECTED_NOT_READ) {
                    //We haven't read the size, so we are expecting a size
                    String line = httpDecoder.decodeLine(in);

                    //Check if the entire line has been read
                    if (line == null) {
                        return false;
                    }

                    response.setExpectedToRead(httpDecoder.decodeSize(line));

                    //Are we done reading the chunked content? (A zero means we are done)
                    if (response.getExpectedToRead() == 0) {
                        break;
                    }
                }

                //Now read the content chunk

                //Be sure all of the data is there for us to retrieve + the CRLF...
                if (response.getExpectedToRead() + 2 > in.remaining()) {
                    //Need more data
                    return false;
                }

                //Read the content
                httpDecoder.decodeChunkedContent(in, response);

                //Flag that it's time to read a size record
                response.setExpectedToRead(HttpResponseMessage.EXPECTED_NOT_READ);

            }

        } else if (response.getContentLength() > 0) {
            //Do we have enough data?
            if ((response.getContentLength()) > in.remaining()) {
                return false;
            }
            httpDecoder.decodeContent(in, response);
        }

        response.setState(HttpResponseMessage.STATE_CONTENT_READ);

        return true;
    }

    /**
     * Process  and read the status header.
     * 
     * @param response the {@link HttpResponseMessage} response object
     * @param in the <code>ByteBuffer</code> that contains the raw bytes from the server
     * 
     * @return <code>true</code> if it can read all of the data, or <code>false</code> if not.
     * 
     * @throws Exception if any exception occurs
     */
    private boolean processStatus(HttpResponseMessage response, IoBuffer in) throws Exception {
        //Read the status header
        String header = httpDecoder.decodeLine(in);
        if (header == null) {
            return false;
        }

        httpDecoder.decodeStatus(header, response);

        return true;
    }
}
