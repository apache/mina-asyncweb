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
package org.apache.ahc;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.apache.ahc.codec.HttpResponseMessage;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Embedded;

public abstract class AbstractTest extends TestCase {

    protected static final File BASEDIR = getBaseDir();
    protected static final File CATALINAHOME = new File(BASEDIR, "src/test/catalina");
    protected static final File KEYSTORE = new File(CATALINAHOME, "conf/keystore");
    protected static final File WEBAPPS = new File(CATALINAHOME, "webapps");
    protected static final File ROOT = new File(WEBAPPS, "ROOT");
    protected static final File AUTH_BASIC = new File(WEBAPPS, "auth_basic");
    protected static final File AUTH_DIGEST = new File(WEBAPPS, "auth_digest");
    protected static final File WORK = new File(BASEDIR, "target/work");
    protected Embedded server;

    protected void setUp() throws Exception {
        System.out.println("BASEDIR = " + BASEDIR.getAbsolutePath());
        server = new Embedded();
        server.setCatalinaHome(CATALINAHOME.getAbsolutePath());

        Engine engine = server.createEngine();
        engine.setDefaultHost("localhost");

        Host host = server.createHost("localhost", WEBAPPS.getAbsolutePath());
        ((StandardHost)host).setWorkDir(WORK.getAbsolutePath());
        engine.addChild(host);

        //Ass the Root context
        StandardContext context = (StandardContext)server.createContext("", ROOT.getAbsolutePath());
        context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
        host.addChild(context);

        //Add the auth basic context
        context = (StandardContext)server.createContext("", AUTH_BASIC.getAbsolutePath());
        context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
        context.setRealm(new FakeRealm());
        context.setPath("/authbasic");
        host.addChild(context);

        //Add the auth digest context
        context = (StandardContext)server.createContext("", AUTH_DIGEST.getAbsolutePath());
        context.setParentClassLoader(Thread.currentThread().getContextClassLoader());
        context.setRealm(new FakeRealm());
        context.setPath("/authdigest");
        host.addChild(context);

        server.addEngine(engine);

        //Http
        Connector http = server.createConnector("localhost", 8282, false);
        server.addConnector(http);

        //Https
        Connector https = server.createConnector("localhost", 8383, true);
        https.setAttribute("keystoreFile", KEYSTORE.getAbsolutePath());
        server.addConnector(https);
        server.start();
    }


    protected void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    protected static final File getBaseDir() {
        File dir;

        // If ${basedir} is set, then honor it
        String tmp = System.getProperty("basedir");
        if (tmp != null) {
            dir = new File(tmp);
        } else {
            // Find the directory which this class (or really the sub-class of TestSupport) is defined in.
            String path = AbstractTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();

            // We expect the file to be in target/test-classes, so go up 2 dirs
            dir = new File(path).getParentFile().getParentFile();

            // Set ${basedir} which is needed by logging to initialize
            System.setProperty("basedir", dir.getPath());
        }

        return dir;
    }

    class TestCallback implements AsyncHttpClientCallback {

        private volatile boolean timeout;
        private volatile boolean closed;
        private volatile boolean exception;
        private volatile Throwable throwable;
        private volatile HttpResponseMessage message;
        private final CountDownLatch complete = new CountDownLatch(1);

        public TestCallback() {
            clear();
        }

        public void onResponse(HttpResponseMessage response) {
            this.message = response;
            complete.countDown();
        }

        public void onException(Throwable cause) {
            throwable = cause;
            exception = true;
            complete.countDown();
        }

        public void onClosed() {
            closed = true;
            System.out.println("onClosed()");
        }

        public void onTimeout() {
            timeout = true;
            complete.countDown();
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public void clear() {
            closed = false;
            timeout = false;
            exception = false;
            message = null;
        }

        public boolean isClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        public boolean isTimeout() {
            return timeout;
        }

        public boolean isException() {
            return exception;
        }

        public void setException(boolean exception) {
            this.exception = exception;
        }

        public HttpResponseMessage getMessage() {
            return message;
        }

        public void setMessage(HttpResponseMessage message) {
            this.message = message;
        }
        
        public void await(int timeout, TimeUnit unit) throws InterruptedException {
        	complete.await(timeout, unit);
        }
    }
}
