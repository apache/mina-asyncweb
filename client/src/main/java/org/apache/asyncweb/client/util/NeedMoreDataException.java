/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.asyncweb.client.util;

/**
 * The Class NeedMoreDataException.  Represents an exception if the decoders need more data.
 */
public class NeedMoreDataException extends Exception {

    /**
     * Instantiates a new need more data exception.
     */
    public NeedMoreDataException() {
        super();
    }

    /**
     * Instantiates a new need more data exception.
     * 
     * @param string the string
     */
    public NeedMoreDataException(String string) {
        super(string);
    }

    /**
     * Instantiates a new need more data exception.
     * 
     * @param string the string
     * @param throwable the throwable
     */
    public NeedMoreDataException(String string, Throwable throwable) {
        super(string,
            throwable);
    }

    /**
     * Instantiates a new need more data exception.
     * 
     * @param throwable the throwable
     */
    public NeedMoreDataException(Throwable throwable) {
        super(throwable);
    }
}
