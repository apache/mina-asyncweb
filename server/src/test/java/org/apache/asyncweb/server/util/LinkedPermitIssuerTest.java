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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.apache.asyncweb.server.util.LinkedPermitIssuer;
import org.apache.asyncweb.server.util.TimedPermit;
import org.apache.asyncweb.server.util.PermitExpirationListener;

/**
 * Tests <code>LinkedPermitIssuer</code>
 *
 * @author irvingd
 *
 */
public class LinkedPermitIssuerTest extends TestCase {

    private static final int LIFETIME = 100;

    private LinkedPermitIssuer issuer;

    private MockListener listener;

    @Override
    protected void setUp() throws Exception {
        issuer = new LinkedPermitIssuer(LIFETIME);
        listener = new MockListener();
    }

    @Override
    protected void tearDown() throws Exception {
        issuer.close();
    }

    /**
     * Tests that we can issue permits
     */
    public void testAddPermits() {
        TimedPermit permit1 = issuer.issuePermit("1");
        assertNotNull(permit1);
        TimedPermit permit2 = issuer.issuePermit("2");
        assertNotNull(permit2);
        assertNotSame(permit1, permit2);
    }

    /**
     * Tests that if we cancel a permit:
     * <ul>
     *   <li>We are informed that the permit has been successfully cancelled on the
     *       first cancellation</li>
     *   <li>A subsequent attempt to cancel the permit indicates that no further
     *       cancellation took place</li>
     * </ul>
     */
    public void testCancelPermit() {
        TimedPermit permit = issuer.issuePermit("permit");
        assertTrue("Expected cancellation", permit.cancel());
        assertFalse("Unexpected calcellation", permit.cancel());
    }

    /**
     * Tests that if we cancel a permit, no expiration notification is fired
     * for it
     */
    public void testCancelAbortsExpiry() {
        registerMockListener();
        TimedPermit permit = issuer.issuePermit("permit");
        assertTrue("Expected cancellation", permit.cancel());
        listener.validate(LIFETIME * 2);
    }

    /**
     * Tests that closing the issuer aborts expiry notifications
     */
    public void testCloseAbortsExpiry() {
        registerMockListener();
        issuer.issuePermit("permit");
        issuer.close();
        listener.validate(LIFETIME * 2);
    }

    /**
     * Tests that renewing a permit causes its lifetime to be extended
     */
    public void testRenewalSingle() {
        registerMockListener();
        TimedPermit permit = issuer.issuePermit("permit");
        listener.validate(LIFETIME / 2);
        permit.renew();
        listener.validate(3 * LIFETIME / 4);
        listener.addExpectedExpiration("permit");
        listener.validate(LIFETIME / 2);
    }

    /**
     * Obtains multiple entries from the issuer.
     * One entry has its lifetime renewed.
     * We check that the entries expire when expected.
     */
    public void testRenewalMulti() {
        registerMockListener();
        issuer.issuePermit("permit1");
        TimedPermit permit2 = issuer.issuePermit("permit2");
        issuer.issuePermit("permit3");
        listener.validate(LIFETIME / 2);
        listener.addExpectedExpiration("permit1");
        listener.addExpectedExpiration("permit3");
        permit2.renew();
        listener.validate(3 * LIFETIME / 4);
        listener.addExpectedExpiration("permit2");
        listener.validate(LIFETIME / 2);
    }

    /**
     * Tests that we can renew the head entry, and then subsequently renew
     * a following entry
     */
    public void testRenewHeadFirst() {
        TimedPermit permit1 = issuer.issuePermit("permit1");
        TimedPermit permit2 = issuer.issuePermit("permit2");
        permit1.renew();
        permit2.renew();
    }

    private void registerMockListener() {
        issuer.addPermitExpirationListener(listener);
    }

    /**
     * A mock <code>PermitExpirationListener</code>
     *
     * @author irvingd
     */
    private class MockListener implements PermitExpirationListener
    {

        private List<Object> expectedToExpire = new ArrayList<Object>();

        private List<Object> expired = new ArrayList<Object>();

        public void permitExpired(Object o) {
            expired.add(o);
        }

        void addExpectedExpiration(Object o) {
            expectedToExpire.add(o);
        }

        /**
         * Validates that exactly those entries expected to expire have expred.
         */
        void validate() {
            assertEquals(expectedToExpire, expired);
        }

        /**
         * Performs validation after a specified peroid
         *
         * @param delay  The delay (in ms)
         */
        void validate(long delay) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            validate();
        }

    }

}
