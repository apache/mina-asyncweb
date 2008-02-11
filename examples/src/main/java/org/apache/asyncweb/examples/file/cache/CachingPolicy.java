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

import org.apache.asyncweb.common.HttpRequest;
import org.apache.asyncweb.common.MutableHttpResponse;
/**
 * 
 * Caching strategies, under design class :)
 * @author The Apache MINA Project (dev@mina.apache.org)
 *
 */
public interface CachingPolicy {
    /**
     * Is the file is cachable or we just return no-cache directives
     * @param requestedFile
     * @param request
     * @return
     */
    public boolean isCacheable(File requestedFile, HttpRequest request);
    /**
     * Test if it's a cache hit from file infos and headers infos
     * @param requestedFile
     * @param request
     * @return
     */
    public boolean testAndSetCacheHit(File requestedFile, HttpRequest request, MutableHttpResponse response);
}
