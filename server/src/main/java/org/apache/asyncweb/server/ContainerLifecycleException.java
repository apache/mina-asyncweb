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
package org.apache.asyncweb.server;

/**
 * Exception thrown when a problem is encountered whilst transitioning
 * a <code>ServiceContainer</code> through its lifecycle
 *
 * @author irvingd
 *
 */
public class ContainerLifecycleException extends Exception {

    private static final long serialVersionUID = 3257564018624574256L;

    /**
     * Constructs with a description of the problem
     *
     * @param desc  description of the problem
     */
    public ContainerLifecycleException(String desc) {
        super(desc);
    }

    /**
     * Constructs with a description and a root cause
     *
     * @param desc   description of the problem
     * @param cause  the root cause
     */
    public ContainerLifecycleException(String desc, Throwable cause) {
        super(desc, cause);
    }

}
