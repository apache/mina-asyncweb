/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.ahc.util;

/**
 * The Class AsyncHttpClientException.  Generic unchecked exception used with the 
 * AsyncHttpClient API.
 */
public class AsyncHttpClientException extends Error {

    /**
     * Instantiates a new async http client exception.
     */
    public AsyncHttpClientException() {
    }

    /**
     * Instantiates a new async http client exception.
     * 
     * @param string the string
     */
    public AsyncHttpClientException(String string) {
        super(string);
    }

    /**
     * Instantiates a new async http client exception.
     * 
     * @param string the string
     * @param throwable the throwable
     */
    public AsyncHttpClientException(String string, Throwable throwable) {
        super(string, throwable);
    }

    /**
     * Instantiates a new async http client exception.
     * 
     * @param throwable the throwable
     */
    public AsyncHttpClientException(Throwable throwable) {
        super(throwable);
    }
}
