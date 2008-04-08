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
package org.apache.ahc.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple monitoring listener for tracking average request times and average
 * connect times.  This is provided mainly to illustrate how one can collect 
 * timing data. 
 */
public final class TimeMonitor extends CountingMonitor {
    private final AtomicInteger requestCount = new AtomicInteger();
    private final AtomicLong requestTimes = new AtomicLong();
    
    private final AtomicInteger connectCount = new AtomicInteger();
    private final AtomicLong connectTimes = new AtomicLong();

    @Override
    /**
     * Process a notification event.  If this is a 
     * REQUEST_COMPLETED event, the request timing 
     * information is added to the accumulators to that 
     * average response time can be calculated.
     * 
     * @param event  The notification event.
     */
    public void notification(MonitoringEvent event) {
        super.notification(event);
        
        // get the response time
        int type = event.getType();
        if (type == MonitoringEvent.REQUEST_COMPLETED) {
            long requestStartTime = event.getRequest().getRequestStartTime();
            if (requestStartTime != 0L) {
                requestCount.incrementAndGet();
                long elapsed = event.getTimeStamp() - requestStartTime;
                requestTimes.addAndGet(elapsed);
            }
        } else if (type == MonitoringEvent.CONNECTION_SUCCESSFUL) {
            long connectStartTime = event.getRequest().getConnectStartTime();
            if (connectStartTime != 0L) {
                connectCount.incrementAndGet();
                long elapsed = event.getTimeStamp() - connectStartTime;
                connectTimes.addAndGet(elapsed);
            }
        }
    }
    
    /**
     * Return the average calculated response time for 
     * the processed requests. 
     * 
     * @return The average response time, in milliseconds, for 
     *         all recorded completed requests.
     */
    public long getAverageResponseTime() {
        if (requestCount.get() == 0) {
            return 0L;
        }
        return requestTimes.get()/requestCount.get();
    }
    
    
    /**
     * Return the average calculated connect time for 
     * the processed requests. 
     * 
     * @return The average connect time, in milliseconds, for 
     *         all recorded completed requests.
     */
    public long getAverageConnectTime() {
        if (connectCount.get() == 0) {
            return 0L;
        }
        return connectTimes.get()/connectCount.get();
    }
}
