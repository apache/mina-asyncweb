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

package org.apache.asyncweb.examples.file;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.security.InvalidParameterException;

import org.apache.asyncweb.common.DefaultHttpResponse;
import org.apache.asyncweb.common.HttpResponseStatus;
import org.apache.asyncweb.common.MutableHttpResponse;
import org.apache.asyncweb.examples.file.cache.CachingPolicy;
import org.apache.asyncweb.examples.file.cache.SimpleCachingPolicy;
import org.apache.asyncweb.examples.file.mimetype.MimeMap;
import org.apache.asyncweb.server.HttpService;
import org.apache.asyncweb.server.HttpServiceContext;
import org.apache.mina.common.IoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP service, serving files from the filesystem.
 * @author The Apache MINA Project (dev@mina.apache.org)
 *
 */
public class FileHttpService implements HttpService {
    private static final Logger LOG = LoggerFactory
            .getLogger(FileHttpService.class);

    private String baseUrl;

    private String basePath;

    private CachingPolicy cachingPolicy = null;

    private MimeMap mimeMap = new MimeMap();

    public FileHttpService(String baseUrl, String basePath) {
        this.baseUrl = baseUrl;
        this.basePath = basePath;
        if (baseUrl == null || basePath == null)
            throw new InvalidParameterException("Null parameters");
        File f = new File(basePath);
        if (!(f.isDirectory() && f.exists()))
            throw new InvalidParameterException("The base path [ " + basePath
                    + " ] is not a valid path.");
        cachingPolicy = new SimpleCachingPolicy();
    }

    public void handleRequest(HttpServiceContext context) throws Exception {
        URI uri = context.getRequest().getRequestUri();
        String path = uri.getPath();
        LOG.info("handling file request : " + uri);
        if (!path.startsWith(baseUrl)) {
            // error the requested URL is not in the base URL
            //TODO : find the good exception to throw
            throw new InvalidParameterException("Wrong URL");
        }

        path = path.substring(baseUrl.length());
        File f = new File(basePath + File.separator + path);

        MutableHttpResponse response = new DefaultHttpResponse();
        if (f.exists() && (!f.isDirectory())) {
            LOG.info("Serving file [ " + f.getAbsolutePath() + " ]");

            // caching processing
            if (cachingPolicy == null
                    || !cachingPolicy.isCacheable(f, context.getRequest())) {
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control", "no-cache");
            } else {
                cachingPolicy.testAndSetCacheHit(f, context.getRequest(),
                        response);
            }

            // setting mime-type based on the mime-map
            
            String contentType = mimeMap.getContentType(MimeMap.getExtension(f
                    .getName()));

            if (contentType != null)
                response.setHeader("Content-Type", contentType);

            response.setStatus(HttpResponseStatus.OK);

            FileChannel fileChannel = (new RandomAccessFile(f, "r"))
                    .getChannel();

            // TODO : well it's quite explosive on big files, need to change the API
            
            int fileSize = (int) fileChannel.size();
            IoBuffer responseBuffer = IoBuffer.allocate(fileSize);
            
            fileChannel.read(responseBuffer.buf());
            fileChannel.close();
            
            responseBuffer.flip();
            response.setContent(responseBuffer);
            
        } else {
            // the file is not found, we send the famous 404 error
            response.setStatus(HttpResponseStatus.NOT_FOUND);
            response.setStatusReasonPhrase("File \"" + path + "\"");
            LOG.warn("The file [ " + f.getAbsolutePath() + " ] is not found");
        }
        context.commitResponse(response);

    }

    public CachingPolicy getCachingPolicy() {
        return cachingPolicy;
    }

    public void setCachingPolicy(CachingPolicy cachingPolicy) {
        this.cachingPolicy = cachingPolicy;
    }

    public void start() {
        // nothing to do there
    }

    public void stop() {
        // nothing to do there
    }

}
