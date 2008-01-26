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
package org.safehaus.asyncweb.util;

/**
 * A time limited permit granting access to a target object.
 * A <code>TimedPermit</code> is issued for a target object by a <code>TimedPermitIssue</code>.
 * When the time limit (determined by the issuer) is reached, any listeners attached to
 * the issuer are notified.
 *
 * @author irvingd
 *
 */
public interface TimedPermit {

    /**
     * Extends the lifetime of this permit.
     * This is typically used as a "keep-alive" mechanism. For example, if a permit is
     * issued to manage the idle expiry time of an http session, the permit might be
     * extended each time the client issues a request associated with the session.<br/>
     * The amount of time added to the lifetime of this permit by invoking this method
     * is determined by the <code>TimedPermitIssuer</code> which issued this permit.
     *
     * Invoking this method has no effect if this permit is already expired
     */
    public void renew();

    /**
     * Cancels this permit. After invoking this method, it is guaranteed that
     * no expiry notification will be made for the target object by this permits
     * associated <code>TimedPermitIssuer</code>.
     *
     * @return <code>true</code> if invoking this method prevented expiry notification
     *         from taking place, <code>false</code> if expiry notification has already
     *         taken place
     */
    public boolean cancel();

}
