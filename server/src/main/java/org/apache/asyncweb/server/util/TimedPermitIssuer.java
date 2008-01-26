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
package org.apache.asyncweb.server.util;

import org.apache.asyncweb.server.util.PermitExpirationListener;

/**
 * Issues permits for accessing objects for a limited period of time.
 * Users may renew the lifetime of an issued permit by invoking its <code>renew</code>
 * method. If a permit issued by this issuer expires before it is renewed, all
 * <code>PermitExpirationListener</code>s associated with this issuer are notified of
 * the expiry.
 *
 * @author irvingd
 *
 */
public interface TimedPermitIssuer {

    /**
     * Closes this permit issuer
     */
    public void close();

    /**
     * Issues a new permit for a specified object
     *
     * @param o  The object for which a permit is required
     * @return   The new permit
     */
    public TimedPermit issuePermit(Object o);

    /**
     * Adds a listener to be notified when any permit supplied by this issuer
     * expires
     *
     * @param listener  The listener
     */
    public void addPermitExpirationListener( PermitExpirationListener listener);

}
