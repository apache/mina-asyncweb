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
package org.safehaus.asyncweb.service.filter.resolver;

import java.net.URI;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.asyncweb.codec.DefaultHttpRequest;
import org.apache.asyncweb.codec.HttpRequest;
import org.apache.asyncweb.codec.MutableHttpRequest;
import org.safehaus.asyncweb.service.resolver.ExactMatchURIServiceResolver;

/**
 * Tests the <code>ExactMatchURIServiceResolver</code>
 *
 * @author irvingd
 *
 */
public class ExactMatchURIServiceResolverTest extends TestCase {

    private ExactMatchURIServiceResolver resolver;

    public ExactMatchURIServiceResolverTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() {
        resolver = new ExactMatchURIServiceResolver();
    }

    /**
     * Tests that the resolver returns <code>null</code> if an exact math
     * is not made
     */
    public void testNoMatch() throws Exception {
        resolver.addURIMapping("something", "aService");
        assertNoMatch("somethingElse");
    }

    /**
     * Tests that with a single registered service, we can perform a match
     */
    public void testMatch() throws Exception {
        resolver.addURIMapping("something", "aService");
        assertMatch("something", "aService");
    }

    /**
     * Tests that with multiple registrations, we can perform matches
     */
    public void testMatch_MultipleRegistrations() throws Exception {
        resolver.addURIMapping("uriA", "serviceA");
        resolver.addURIMapping("uriB", "serviceB");
        resolver.addURIMapping("uriC", "serviceC");
        assertMatch("uriA", "serviceA");
        assertMatch("uriB", "serviceB");
        assertMatch("uriC", "serviceC");
        assertNoMatch("uriD");
    }

    /**
     * Tests that we can overwrite existing mappings with new ones
     */
    public void testMappingOverwrite() throws Exception {
        String uri = "uri";
        resolver.addURIMapping(uri, "oldService");
        resolver.addURIMapping(uri, "newService");
        assertMatch(uri, "newService");
    }

    private void assertMatch(String uri, String expectedService)
            throws URISyntaxException {
        HttpRequest request = requestForURI(uri);
        String service = resolver.resolveService(request);
        assertEquals("Unexpected service", expectedService, service);
    }

    private void assertNoMatch(String uri) throws URISyntaxException {
        HttpRequest request = requestForURI(uri);
        assertNull("Unexpected match", resolver.resolveService(request));
    }

    private HttpRequest requestForURI(String uri) throws URISyntaxException {
        MutableHttpRequest request = new DefaultHttpRequest();
        request.setRequestUri(new URI(uri));
        return request;
    }

}
