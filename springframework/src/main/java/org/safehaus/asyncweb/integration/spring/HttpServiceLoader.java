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
package org.safehaus.asyncweb.integration.spring;

import java.util.Iterator;
import java.util.Map;

import org.safehaus.asyncweb.service.HttpService;
import org.safehaus.asyncweb.service.HttpServiceHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Loads all <code>HttpService</code>s defined in an
 * <code>ApplicationContext</code> to an associated
 * <code>HttpServiceHandler</code>
 *
 * @author irvingd
 */
public class HttpServiceLoader implements ApplicationContextAware {

    private HttpServiceHandler handler;

    public void setHandler(HttpServiceHandler httpServiceHandler) {
        this.handler = httpServiceHandler;
    }

    @SuppressWarnings("unchecked")
    public void setApplicationContext(ApplicationContext context) {
        Map<String, HttpService> services = context.getBeansOfType(HttpService.class);
        for (Iterator<Map.Entry<String, HttpService>> iter = services.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, HttpService> entry = iter.next();
            String serviceName = entry.getKey();
            HttpService service = entry.getValue();
            handler.addHttpService(serviceName, service);
        }
    }

}
