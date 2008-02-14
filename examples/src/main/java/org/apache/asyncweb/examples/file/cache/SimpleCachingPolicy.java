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

package org.apache.asyncweb.examples.file.cache;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.asyncweb.common.HttpRequest;
import org.apache.asyncweb.common.MutableHttpResponse;

/**
 * Very simple caching based on last modification date
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class SimpleCachingPolicy implements CachingPolicy {

    /* formatter for strings like : "Last-Modified  Mon, 17 Dec 2007 00:12:30 GMT" */
    private SimpleDateFormat sdf;

    public SimpleCachingPolicy() {
        sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public boolean isCacheable(File requestedFile, HttpRequest request) {
        return true;
    }

    public boolean testAndSetCacheHit(File requestedFile, HttpRequest request,
            MutableHttpResponse response) {

        long last = requestedFile.lastModified();
        long maxAge = System.currentTimeMillis() - last;
        response.setHeader("Cache-Control", "max-age=" + maxAge);
        response.setHeader("Last-Modified", sdf.format(new Date(last)));
        return true;
    }

}
