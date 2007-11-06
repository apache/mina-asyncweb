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

import org.safehaus.asyncweb.common.DefaultHttpRequest;
import org.safehaus.asyncweb.common.HttpRequest;
import org.safehaus.asyncweb.common.MutableHttpRequest;
import org.safehaus.asyncweb.service.resolver.SimplePrefixResolver;

/**
 * Tests <code>SimplePrefixResolver</code>
 *
 * @author irvingd
 *
 */
public class SimplePrefixResolverTest extends TestCase {

    private SimplePrefixResolver resolver;

    public SimplePrefixResolverTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() {
        resolver = new SimplePrefixResolver();
    }

    /**
     * Tests that if no prefix is configured, the request uri is
     * returned as the resolved service name
     */
    public void testNoPrefixPassThrough() throws Exception {
        assertResolvedValue("a", "a");
        assertResolvedValue("some/service/Name", "some/service/Name");
        assertResolvedValue("", "");
        assertResolvedValue(null, null);
    }

    /**
     * Tests that no resolution is made if a prefix is configured and the
     * request uri does not start with this prefix
     */
    public void testPrefixNoMatch() throws Exception {
        resolver.setUriPrefix("prefix/");
        assertResolvedValue("a", null);
        assertResolvedValue("/prefix/x", null);
        assertResolvedValue("", null);
        assertResolvedValue(null, null);
    }

    /**
     * Tests that a resolution is made if a prefix is configured, and the
     * request uri matches the prefix
     */
    public void testPrefixMatch() throws Exception {
        resolver.setUriPrefix("prefix/");
        assertResolvedValue("prefix/a", "a");
        assertResolvedValue("prefix/a/b", "a/b");
        assertResolvedValue("prefix/", "");
    }

    private void assertResolvedValue(String uri, String expected)
            throws Exception {
        HttpRequest request = createRequestForURI(uri);
        String resolved = resolver.resolveService(request);
        assertEquals("Unexpected service name", expected, resolved);
    }

    private HttpRequest createRequestForURI(String uri)
            throws URISyntaxException {
        MutableHttpRequest req = new DefaultHttpRequest();
        if (uri != null) {
            req.setRequestUri(new URI(uri));
        }
        return req;
    }

}
