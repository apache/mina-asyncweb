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
package org.safehaus.asyncweb.service.filter;

import org.safehaus.asyncweb.service.HttpServiceContext;
import org.safehaus.asyncweb.service.HttpServiceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import EDU.oswego.cs.dl.util.concurrent.BoundedLinkedQueue;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * A <code>ServiceHandler</code> which offloads requests and optionally
 * responses to a thread pool
 *
 * @author irvingd
 * @author trustin
 * @version $Rev$, $Date$
 */
public class ThreadPoolFilter implements HttpServiceFilter {

    private static final Logger LOG = LoggerFactory
            .getLogger(ThreadPoolFilter.class);

    private static final int DEFAULT_MAX_QUEUE_SIZE = 1000;

    private static final int DEFAULT_MIN_THREADS = 1;

    private static final int DEFAULT_MAX_THREADS = 10;

    private PooledExecutor pool;

    private boolean poolResponses;

    private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;

    private int minThreads = DEFAULT_MIN_THREADS;

    private int maxThreads = DEFAULT_MAX_THREADS;

    /**
     * Handles the specified request.
     * A task is scheduled with our thread pool to
     * initiate onward filter invocation asyncronously
     */
    public void handleRequest(final NextFilter next, HttpServiceContext context) {
        enqueue(new Runnable() {
            public void run() {
                next.invoke();
            }
        });
    }

    /**
     * Handles the specified response.
     * If we are configured to offload responses, a task is scheduled
     * with our thread pool to initiate onward filter invocation
     * asyncronously
     */
    public void handleResponse(final NextFilter next, HttpServiceContext context) {
        if (poolResponses) {
            enqueue(new Runnable() {
                public void run() {
                    next.invoke();
                }
            });
        } else {
            next.invoke();
        }
    }

    /**
     * Starts this filter - creating and configuring the underlying
     * thread pool
     */
    public void start() {
        LOG.info("ThreadPoolHandler starting: maxQueueSize=" + maxQueueSize
                + " minThreads= " + minThreads + " maxThreads= " + maxThreads
                + " poolResponses=" + poolResponses);
        pool = new PooledExecutor(new BoundedLinkedQueue(maxQueueSize));
        pool.setMaximumPoolSize(maxThreads);
        pool.setMinimumPoolSize(minThreads);
        pool.runWhenBlocked();
    }

    public void stop() {
        // TODO: Clean shut-down
    }

    /**
     * Sets the minimum number of threads to be employed
     *
     * @param minThreads  The minimum number of threads
     */
    public void setMinThreads(int minThreads) {
        this.minThreads = minThreads;
    }

    /**
     * Sets the maximum number of threads to be employed
     *
     * @param maxThreads  The maximum number of threads
     */
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    /**
     * Sets the maximum number of requests which are queued for
     * asyncronous execution, before tasks are run on the calling
     * thread
     *
     * @param maxQueueSize  The maximum queue size
     */
    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    private void enqueue(Runnable task) {
        try {
            pool.execute(task);
        } catch (InterruptedException e) {
            LOG.error("Failed to schedule pool task", e);
        }
    }

}
