/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.ahc.util;

import java.util.EventListener;

/**
 * The interface for listening for monitoring events.
 */
public interface MonitoringListener extends EventListener {
    /**
     * Process a notification for a MonitoringEvent.
     * 
     * @param event  The particular event to be processed.
     */
    public void notification(MonitoringEvent event);
}

