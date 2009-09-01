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
package org.apache.asyncweb.client.codec;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
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
     * @see org.apache.mina.filter.codec.CumulativeProtocolDecoder#doDecode(org.apache.mina.common.IoSession, org.apache.mina.common.ByteBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput)
     */
    protected boolean doDecode(IoSession ioSession, ByteBuffer in, ProtocolDecoderOutput out)
            throws Exception {

        HttpResponseMessage response = (HttpResponseMessage)ioSession.getAttribute(HttpIoHandler.CURRENT_RESPONSE);
        if (response == null) {
            HttpRequestMessage request = (HttpRequestMessage)ioSession.getAttribute(HttpIoHandler.CURRENT_REQUEST);
            response = new HttpResponseMessage(request.getUrl());
            ioSession.setAttribute(HttpIoHandler.CURRENT_RESPONSE, response);
        }

        //Test if we need the response...
        if (response.getState() == HttpResponseMessage.STATE_START) {

            if (!processStatus(response, in)) {
                return false;
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
                    return false;
                }

                //Check if the entire response headers have been read
                if (line.length() == 0) {
                    //The next line should be a header
                    if (!processStatus(response, in)) {
                        // the continue response is completely read but we
                        // didn't get the full status line from the next
                        // response; reset the state to STATE_START
                        response.setState(HttpResponseMessage.STATE_START);
                        return false;
                    }
                    // status was processed
                    response.setState(HttpResponseMessage.STATE_STATUS_READ);
                    break;
                }
            }
        }

        //Are we reading headers?
        if (response.getState() == HttpResponseMessage.STATE_STATUS_READ) {
            if (!processHeaders(response, in)) {
                return false;
            }
        }

        //Are we reading content?
        if (response.getState() == HttpResponseMessage.STATE_HEADERS_READ) {
            if (!processContent(response, ioSession, in)) {
                return false;
            }
        }

        //If we are chunked and we have read all the content, then read the footers if there are any
        if (response.isChunked() && response.getState() == HttpResponseMessage.STATE_CONTENT_READ) {
            if (!processFooters(response, in)) {
                return false;
            }
        }

        completeResponse(ioSession, out, response);

        return true;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
        // if the response was still being decoded when the session is getting
        // closed, we finish decoding and hand off the response
        HttpResponseMessage response = 
                (HttpResponseMessage)session.getAttribute(HttpIoHandler.CURRENT_RESPONSE);
        // we're only interested in non-chunked responses with no content length
        // specified, in which case connection close marks the end of the body
        if (response != null &&
                !response.isChunked() &&
                response.getContentLength() < 0 &&
                response.getState() == HttpResponseMessage.STATE_HEADERS_READ) {
            completeResponse(session, out, response);
        }
    }

    private void completeResponse(IoSession ioSession, ProtocolDecoderOutput out, HttpResponseMessage response) {
        response.setState(HttpResponseMessage.STATE_FINISHED);
        out.write(response);
        ioSession.removeAttribute(HttpIoHandler.CURRENT_RESPONSE);
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
    private boolean processHeaders(HttpResponseMessage response, ByteBuffer in) throws Exception {
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
    private boolean processFooters(HttpResponseMessage response, ByteBuffer in) throws Exception {
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
    private boolean findHeaders(HttpResponseMessage response, ByteBuffer in) throws Exception {
        //Read the headers and process them
        while (true) {
            String line = httpDecoder.decodeHeaderLine(in);

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
     * @return <code>true</code> if it can read all of the data, or <code>false</code> if not.
     * 
     * @throws Exception if any exception occurs
     */
    private boolean processContent(HttpResponseMessage response, IoSession session, ByteBuffer in) 
            throws Exception {
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
        } else if (!noContentExpected(session, response)) {
            // it neither is chunked nor has content length; read until the
            // session closes
            httpDecoder.decodeRemainingContent(in, response);
            // keep looping until the session closes
            return false;
        }

        response.setState(HttpResponseMessage.STATE_CONTENT_READ);

        return true;
    }
    
    /**
     * Returns whether content is not expected for this response.  Responses
     * with certain status codes must not have contents, as well as responses to
     * proxy connect requests.
     */
    private boolean noContentExpected(IoSession session, HttpResponseMessage response) {
        int status = response.getStatusCode();
        // if status is no content or not modified, content is not allowed
        if (status == 204 || status == 304) {
            return true;
        }
        
        // explicit 0 content length
        if (response.getContentLength() == 0) {
            return true;
        }
        
        // proxy connect handshake is in progress, in which case the connection
        // will be persistent but no content is expected
        if (session.getAttribute(HttpIoHandler.PROXY_CONNECT_IN_PROGRESS) != null) {
            return true;
        }
        
        return false;
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
    private boolean processStatus(HttpResponseMessage response, ByteBuffer in) throws Exception {
        //Read the status header
        String header = httpDecoder.decodeLine(in);
        if (header == null) {
            return false;
        }

        httpDecoder.decodeStatus(header, response);

        return true;
    }
}
