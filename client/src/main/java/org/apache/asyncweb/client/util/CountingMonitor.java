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

package org.apache.asyncweb.client.util;

import java.util.concurrent.atomic.AtomicInteger;
 
/**
 * Simple implementation of an AsyncHttpMonitor that 
 * just keeps a count of all of the events. 
 */
public class CountingMonitor implements MonitoringListener {
    private AtomicInteger[] counters = 
            new AtomicInteger[MonitoringEvent.CONNECTION_CLOSED + 1];
    
    /**
     * Default constructor 
     */
    public CountingMonitor() {
        for (int i = 0; i < counters.length; i++) {
            counters[i] = new AtomicInteger(0);
        }
    }
    
    /**
     * Handle a notification event from an AsyncHttpClient.
     * 
     * @param event  The triggered event.
     */
    public void notification(MonitoringEvent event) {
        int type = event.getType();  
        if (type < counters.length) {
            counters[type].incrementAndGet();
        }
    }
    
    
    /**
     * Get the counter from a specific MonitoringEvent.
     * 
     * @param type   The type number of the event.
     * 
     * @return The current counter value. 
     */
    public int getCount(int type) {
        // we only return values for ones in the defined range.  Anything else is 
        // zero. 
        if (type < counters.length) {
            return counters[type].get();
        } else {
            return 0;
        }
    }
    
    
    /**
     * Zero all of the event counters. 
     */
    public void clearCounters() {
        AtomicInteger[] newCounters = new AtomicInteger[MonitoringEvent.CONNECTION_CLOSED + 1];
        for (int i = 0; i < newCounters.length; i++) {
            newCounters[i] = new AtomicInteger(0);
        }
        counters = newCounters;
    }
}

