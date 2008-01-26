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
package org.safehaus.asyncweb.service.pipeline;

import org.safehaus.asyncweb.service.HttpServiceContext;

public interface RequestPipeline {

    /**
     * Adds a request to this pipeline.
     *
     * @return <code>true</code> iff the pipeline accepts the request
     */
    public boolean addRequest(HttpServiceContext context);

    /**
     * Frees any responses which may now be provided to the client as a result
     * of the specified response becoming available.
     * If the associated request has not been previously added to this pipeline,
     * it joins the pipeline at the back of the queue: All previously added
     * requests must be responded to before the new request can take its turn<br/>
     */
    public void releaseResponse(HttpServiceContext context);

    /**
     * Sets the <code>PipelineListener</code> to be notified when
     * a request is released from this pipeline
     *
     * @param listener  The listener
     */
    public void setPipelineListener(RequestPipelineListener listener);

    /**
     * Disposes of any requests still living in the pipeline
     */
    public void disposeAll();

    /**
     * Runs the scheduled command the next time the pipeline is empty.
     * Run immediately if the pipeline is currently empty;
     *
     * @param r  The command to run
     */
    public void runWhenEmpty(Runnable r);

}
