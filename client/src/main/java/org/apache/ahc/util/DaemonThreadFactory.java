/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ahc.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Simple implementation of a ThreadFactory that 
 * marks all of the threads as daemon threads.
 */
public class DaemonThreadFactory implements ThreadFactory {
    private final ThreadFactory factory = Executors.defaultThreadFactory();

    /**
     * Create a new thread for the thread pool.  The create
     * thread will be a daemon thread.
     * 
     * @param r      The runnable used by the thread pool.
     * 
     * @return The newly created thread. 
     */
    public Thread newThread(Runnable r) {
        Thread t = factory.newThread(r);
        if (!t.isDaemon()) {
            t.setDaemon(true);
        }
        return t;
    }

}
