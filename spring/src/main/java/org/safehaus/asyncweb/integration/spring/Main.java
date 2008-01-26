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
package org.safehaus.asyncweb.integration.spring;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.asyncweb.server.ContainerLifecycleException;
import org.apache.asyncweb.server.ServiceContainer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Simple stand-alone server which loads configuration using Spring
 *
 * @author irvingd
 *
 */
public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);

    private static final String CONFIG_PROPERTY = "asyncWeb.config";

    private static final String SERVICE_CONFIG_PROPERTY = "asyncWeb.config.services";

    private static final String DEFAULT_SERVICE_CONFIG = "httpServiceDefinitions";

    private String configDir;

    private String serviceConfigName;

    /**
     * @param asyncWebDir The async web base directory
     */
    public Main(File asyncWebDir) {
        this(asyncWebDir, DEFAULT_SERVICE_CONFIG);
    }

    /**
     * @param asyncWebDir         The async web configuration directory - containing the
     *                          <code>AsyncWeb.xml</code> configuration file
     * @param serviceConfigName The name of the directory within <code>configDir</code>
     *                          containing service definitions
     */
    public Main(File asyncWebDir, String serviceConfigName) {
        if (!asyncWebDir.isDirectory()) {
            throw new IllegalArgumentException(
                    "Could not find asyncWeb directory: " + asyncWebDir);
        }
        this.configDir = asyncWebDir.getAbsolutePath() + "/";
        this.serviceConfigName = serviceConfigName;
    }

    /**
     * Uses system properties to determine the asyncWeb configuration directory,
     * and (optionally) the service configuration directory contained within
     */
    Main() {
        configDir = System.getProperty(CONFIG_PROPERTY);
        configDir = configDir == null ? "AsyncWeb/" : configDir + "/";
        serviceConfigName = System.getProperty(SERVICE_CONFIG_PROPERTY,
                DEFAULT_SERVICE_CONFIG);
    }

    public void start() {
        String[] configs = new String[] { configDir + "conf/AsyncWeb.xml",
                configDir + "conf/" + serviceConfigName + "/*.xml" };
        ApplicationContext ctx = new FileSystemXmlApplicationContext(configs);
        ServiceContainer container = ( ServiceContainer ) ctx
                .getBean("container");
        try {
            container.start();
        } catch (ContainerLifecycleException e) {
            LOG.error("Failed to start container", e);
            System.exit(1);
        }
        LOG.info("AsyncWeb server started");
    }

    public static void main(String[] args) throws Exception {
        new Main().start();
    }
}
