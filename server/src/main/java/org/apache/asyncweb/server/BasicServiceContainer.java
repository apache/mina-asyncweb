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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.asyncweb.server.context.KeepAliveStrategy;
import org.apache.asyncweb.server.errorReporting.ErrorResponseFormatter;
import org.apache.asyncweb.server.errorReporting.StandardResponseFormatter;
import org.apache.asyncweb.server.session.DefaultSessionAccessor;
import org.apache.asyncweb.server.session.HttpSessionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.asyncweb.server.context.BasicKeepAliveStrategy;
import org.apache.asyncweb.server.context.CounterKeepAliveStrategy;

/**
 * Basic <code>ServiceContainer</code> implementation.
 *
 * 
 */
public class BasicServiceContainer implements ServiceContainer {

    private static final Logger LOG = LoggerFactory
            .getLogger(BasicServiceContainer.class);

    /**
     * The default number of keep-alive requests
     */
    private static final int DEFAULT_KEEP_ALIVE_REQUESTS = 75;

    /**
     * Represents no limit on the number of keep-alive requests
     */
    private static final int INFINITE_KEEP_ALIVES = -1;

    private boolean isStarted;

    private int maxKeepAlives = DEFAULT_KEEP_ALIVE_REQUESTS;

    private KeepAliveStrategy keepAliveStrategy = new CounterKeepAliveStrategy(
            maxKeepAlives);

    private HttpSessionAccessor sessionAccessor;

    private ErrorResponseFormatter errorResponseFormatter = new StandardResponseFormatter();

    private boolean sendServerHeader = true;

    private List<HttpServiceFilter> filters = new LinkedList<HttpServiceFilter>();

    private List<Transport> transports = new LinkedList<Transport>();

    public boolean isSendServerHeader() {
        return sendServerHeader;
    }

    public void setSendServerHeader(boolean sendServerHeader) {
        this.sendServerHeader = sendServerHeader;
    }

    public ErrorResponseFormatter getErrorResponseFormatter() {
        return errorResponseFormatter;
    }

    public void setErrorResponseFormatter(
            ErrorResponseFormatter errorResponseFormatter) {
        if (errorResponseFormatter == null) {
            throw new NullPointerException("errorResponseFormatter");
        }
        this.errorResponseFormatter = errorResponseFormatter;
    }

    public int getMaxKeepAlives() {
        return maxKeepAlives;
    }

    /**
     * Returns the employed {@link KeepAliveStrategy} of this container.
     */
    public KeepAliveStrategy getKeepAliveStrategy() {
        return keepAliveStrategy;
    }

    /**
     * Sets the maximum number of keep-alive requests
     *
     * @param maxKeepAlives  THe maximum number of keep alive requests
     */
    public void setMaxKeepAlives(int maxKeepAlives) {
        if (maxKeepAlives < INFINITE_KEEP_ALIVES) {
            throw new IllegalArgumentException("Invalid keep alives: "
                    + maxKeepAlives);
        }
        this.maxKeepAlives = maxKeepAlives;
        if (maxKeepAlives == INFINITE_KEEP_ALIVES) {
            LOG.info("Infinite keep-alives configured");
        } else {
            LOG.info("Max keep-alives configured: " + maxKeepAlives);
        }

        keepAliveStrategy = maxKeepAlives == -1 ? new BasicKeepAliveStrategy()
                : new CounterKeepAliveStrategy(maxKeepAlives);
    }

    /**
     * Adds a <code>ServiceHandler</code> to this container
     *
     * @param handler  The handler to add
     * @throws IllegalStateException If this container has been started
     */
    public void addServiceFilter(HttpServiceFilter handler) {
        if (isStarted) {
            throw new IllegalStateException(
                    "Attempt to add filter to running container");
        }
        LOG.info("Adding service handler '" + handler + "'");
        synchronized (filters) {
            filters.add(handler);
        }
    }

    /**
     * Adds a <code>Transport</code> to this container
     *
     * @param transport  The transport to add
     * @throws IllegalStateException If this container has been started
     */
    public void addTransport(Transport transport) {
        if (isStarted) {
            throw new IllegalStateException(
                    "Attempt to add transport to running container");
        }
        LOG.info("Adding transport '" + transport + "'");
        transport.setServiceContainer(this);
        synchronized (transports) {
            transports.add(transport);
        }
    }

    public List<HttpServiceFilter> getServiceFilters() {
        return Collections.unmodifiableList(this.filters);
    }

    /**
     * Sets all <code>ServiceHandler</code>s employed by this container.
     * Any existing handlers are removed
     *
     * @param filters  A list of <code>ServiceHandler</code>s
     * @throws IllegalStateException If this container has been started
     */
    public void setServiceFilters(List<HttpServiceFilter> filters) {
        if (isStarted) {
            throw new IllegalStateException(
                    "Attempt to add filter to running container");
        }

        synchronized (this.filters) {
            this.filters.clear();

            for (HttpServiceFilter filter : filters) {
                addServiceFilter(filter);
            }
        }
    }

    /**
     * Sets all <code>Transport</code>s employed by this container.
     * Any existing transport are removed
     *
     * @param transports  A list of <code>Transport</code>s
     * @throws IllegalStateException If this container has been started
     */
    public void setTransports(List<Transport> transports) {
        if (isStarted) {
            throw new IllegalStateException(
                    "Attempt to add transport to running container");
        }

        synchronized (this.transports) {
            this.transports.clear();

            for (Transport transport : transports) {
                addTransport(transport);
            }
        }
    }

    public HttpSessionAccessor getSessionAccessor() {
        return this.sessionAccessor;
    }

    public void setSessionAccessor(HttpSessionAccessor sessionAccessor) {
        if (sessionAccessor == null) {
            throw new NullPointerException("sessionAccessor");
        }
        this.sessionAccessor = sessionAccessor;
    }

    public void start() throws ContainerLifecycleException {
        if (!isStarted) {
            if (LOG.isDebugEnabled())
                LOG.debug("BasicServiceContainer starting");
            startSessionAccessor();
            startHandlers();
            startTransports();
            if (LOG.isDebugEnabled())
                LOG.debug("BasicServiceContainer started");
            isStarted = true;
        }
    }

    public void stop() {
        if (isStarted) {
            isStarted = false;
            if (LOG.isDebugEnabled())
                LOG.debug("BasicServiceContainer stopping");
            stopHandlers();
            stopTransports();
            stopSessionAccessor();
            if (LOG.isDebugEnabled())
                LOG.debug("BasicServiceContainer stopped");
        }
    }

    /**
     * Starts our session accessor.
     * If no session accessor has been configured, a default accessor is employed
     */
    private void startSessionAccessor() {
        if (sessionAccessor == null) {
            LOG.info("No SessionAccessor configured. Using default");
            sessionAccessor = new DefaultSessionAccessor();
        }
        sessionAccessor.init();
    }

    /**
     * Starts all added handlers
     */
    private void startHandlers() {
        if (LOG.isDebugEnabled())
            LOG.debug("Starting handlers");
        synchronized (filters) {
            for (HttpServiceFilter handler : filters) {
                handler.start();
            }
        }
        if (LOG.isDebugEnabled())
            LOG.debug("Handlers started");
    }

    private void stopHandlers() {
        if (LOG.isDebugEnabled())
            LOG.debug("Stopping handlers");
        synchronized (filters) {
            for (HttpServiceFilter handler : filters) {
                LOG.info("Stopping handler '" + handler + "'");
                handler.stop();
                LOG.info("Handler '" + handler + "' stopped");
            }
        }
        if (LOG.isDebugEnabled())
            LOG.debug("Handlers stopped");
    }

    private void stopSessionAccessor() {
        if (LOG.isDebugEnabled())
            LOG.debug("Disposing session accessor");
        sessionAccessor.dispose();
        if (LOG.isDebugEnabled())
            LOG.debug("Session accessor disposed");
    }

    /**
     * Starts all added transports
     *
     * @throws ContainerLifecycleException If we fail to start a transport
     */
    private void startTransports() throws ContainerLifecycleException {
        if (LOG.isDebugEnabled())
            LOG.debug("Starting transports");
        synchronized (transports) {
            for (Transport transport : transports) {
                LOG.info("Starting transport '" + transport + "'");
                try {
                    transport.start();
                } catch (TransportException e) {
                    LOG.info("Transport '" + transport + "' failed to start");
                    throw new ContainerLifecycleException(
                            "Failed to start transport ' " + transport + "'", e);
                }
            }
        }
        if (LOG.isDebugEnabled())
            LOG.debug("Transports started");
    }

    private void stopTransports() {
        if (LOG.isDebugEnabled())
            LOG.debug("Stopping transports");
        boolean isError = false;
        synchronized (transports) {
            for (Transport transport : transports) {
                LOG.info("Stopping transport '" + transport + "'");
                try {
                    transport.stop();
                    LOG.info("Transport '" + transport + "' stopped");
                } catch (TransportException e) {
                    LOG.warn("Failed to stop transport '" + transport + "'", e);
                    isError = true;
                }
            }
        }
        String errorString = isError ? " (One or more errors encountered)" : "";
        if (LOG.isDebugEnabled())
            LOG.debug("Transports stopped" + errorString);
    }
}
