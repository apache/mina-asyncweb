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
package org.safehaus.asyncweb.service.errorReporting;

import org.apache.mina.filter.codec.http.HttpRequest;
import org.apache.mina.filter.codec.http.MutableHttpResponse;

/**
 * Formats error responses to include a descriptive body where appropriate
 *
 * @author irvingd
 *
 */
public interface ErrorResponseFormatter {

    /**
     * Applies any appropriate formatting to a <code>Response</code> based on its
     * response status code
     *
     * @param response  The response to format
     */
    public void formatResponse(HttpRequest request, MutableHttpResponse response);

}
