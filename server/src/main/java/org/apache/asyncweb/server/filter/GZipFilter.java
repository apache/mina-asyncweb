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
package org.apache.asyncweb.server.filter;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.asyncweb.common.MutableHttpResponse;
import org.apache.asyncweb.server.HttpServiceContext;
import org.apache.asyncweb.server.HttpServiceFilter;
import org.apache.mina.common.buffer.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HttpServiceFilter compressing data using the gzip algorithm if 
 * the client support it. 
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev: 615489 $, $Date: 2008-01-26 21:59:06 +0100 (sam, 26 jan 2008) $
 */
public class GZipFilter implements HttpServiceFilter {

    private static final Logger LOG = LoggerFactory
    .getLogger(GZipFilter.class);
    
    /**
     * Simply moves the request forward in the chain
     */
    public void handleRequest(NextFilter next, HttpServiceContext context)
            throws Exception {
        next.invoke();
    }

    /**
     * Compress the response content if the client support it
     */
    public void handleResponse(NextFilter next, HttpServiceContext context)
            throws Exception {

        String ae = context.getRequest().getHeader("accept-encoding");
        if (ae != null
                && ae.indexOf("gzip") != -1
                && context.getCommittedResponse() instanceof MutableHttpResponse) {
            LOG.debug("Compressing content");
            // compress
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(context.getCommittedResponse().getContent().array());
            gzos.close();
            baos.close();

            // recreate an IoBuffer
            byte[] bytes = baos.toByteArray();
            IoBuffer gzipedResponse = IoBuffer.allocate(bytes.length);
            gzipedResponse.put(bytes);
            gzipedResponse.flip();
            LOG.debug("Old content size {}",context.getCommittedResponse().getContent().remaining());
            LOG.debug("Compressed content size {}",gzipedResponse.remaining());
            
            // change the response content and content type
            MutableHttpResponse mutableResponse = (MutableHttpResponse) context.getCommittedResponse(); 
            mutableResponse.setHeader("Content-Encoding", "gzip");
            mutableResponse.setContent(gzipedResponse);
            mutableResponse.normalize(context.getRequest());
        }
        next.invoke();
    }

    public void start() {
        // nothing to do here
    }

    public void stop() {
        // nothing to do here
    }

}
