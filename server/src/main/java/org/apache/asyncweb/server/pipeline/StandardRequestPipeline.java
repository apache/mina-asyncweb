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
package org.apache.asyncweb.server.pipeline;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.asyncweb.common.HttpResponse;
import org.apache.asyncweb.server.HttpServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandardRequestPipeline implements RequestPipeline {

    private static final Logger LOG = LoggerFactory
            .getLogger(StandardRequestPipeline.class);

    private int maxPipelinedRequests;

    private RequestPipelineListener listener;

    private Runnable emptyCommand;

    private Map<HttpServiceContext, HttpResponse> entryMap = new LinkedHashMap<HttpServiceContext, HttpResponse>();

    public StandardRequestPipeline(int maxPipelinedRequests) {
        this.maxPipelinedRequests = maxPipelinedRequests;
    }

    public boolean addRequest(HttpServiceContext context) {
        boolean added = false;
        synchronized (entryMap) {
            if (entryMap.size() < maxPipelinedRequests) {
                entryMap.put(context, null);
                added = true;
            }
        }
        if (added && LOG.isDebugEnabled()) {
            LOG.debug("Request added to pipeline ok");
        }
        return added;
    }

    public void releaseResponse(HttpServiceContext context) {
        if (context.getCommittedResponse() == null) {
            throw new IllegalStateException("response is not committed.");
        }
        synchronized (entryMap) {
            entryMap.put(context, context.getCommittedResponse());
            releaseRequests();
        }
    }

    public void disposeAll() {
        synchronized (entryMap) {
            entryMap.clear();
        }
    }

    public void runWhenEmpty(Runnable command) {
        synchronized (entryMap) {
            if (entryMap.isEmpty()) {
                command.run();
            } else {
                emptyCommand = command;
            }
        }
    }

    /**
     * Sets the pipeline listener associated with this pipeline
     *
     * @param listener The listener
     */
    public void setPipelineListener(RequestPipelineListener listener) {
        this.listener = listener;
    }

    /**
     * Releases any requests which can be freed as a result of a request
     * being freed.
     * We simply iterate through the list (in insertion order) - freeing
     * all responses until we arive at one which has not yet been completed
     */
    private void releaseRequests() {
        for (Iterator<Map.Entry<HttpServiceContext, HttpResponse>> iter = entryMap
                .entrySet().iterator(); iter.hasNext();) {
            Map.Entry<HttpServiceContext, HttpResponse> entry = iter.next();
            HttpResponse response = entry.getValue();
            if (response != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Response freed from pipeline. Notifying");
                }
                listener.responseReleased(entry.getKey());
                iter.remove();
            } else {
                break;
            }
        }
        if (emptyCommand != null && entryMap.isEmpty()) {
            emptyCommand.run();
        }
    }

}
