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
package org.safehaus.asyncweb.service;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.safehaus.asyncweb.service.context.BasicKeepAliveStrategy;
import org.safehaus.asyncweb.service.context.CounterKeepAliveStrategy;
import org.safehaus.asyncweb.service.context.KeepAliveStrategy;
import org.safehaus.asyncweb.service.errorReporting.ErrorResponseFormatter;
import org.safehaus.asyncweb.service.errorReporting.StandardResponseFormatter;
import org.safehaus.asyncweb.service.session.DefaultSessionAccessor;
import org.safehaus.asyncweb.service.session.HttpSessionAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Basic <code>ServiceContainer</code> implementation.
 * 
 * @author irvingd
 * @author trustin
 * 
 * @version $Rev$, $Date$
 */
public class BasicServiceContainer implements ServiceContainer {

  private static final Logger LOG = LoggerFactory.getLogger(BasicServiceContainer.class);
  
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
  private KeepAliveStrategy keepAliveStrategy = new CounterKeepAliveStrategy(maxKeepAlives);
  private HttpSessionAccessor sessionAccessor;
  private ErrorResponseFormatter errorResponseFormatter = new StandardResponseFormatter();
  private boolean sendServerHeader = true;

  private List<HttpServiceFilter> filters   = new LinkedList<HttpServiceFilter>();
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

  public void setErrorResponseFormatter(ErrorResponseFormatter errorResponseFormatter) {
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
      throw new IllegalArgumentException("Invalid keep alives: " + maxKeepAlives);
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
      throw new IllegalStateException("Attempt to add filter to running container");
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
      throw new IllegalStateException("Attempt to add transport to running container");
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
      throw new IllegalStateException("Attempt to add filter to running container");
    }

    synchronized (this.filters) {
      this.filters.clear();

      for (HttpServiceFilter filter: filters) {
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
      throw new IllegalStateException("Attempt to add transport to running container");
    }

    synchronized (this.transports) {
      this.transports.clear();  
    
      for (Transport transport: transports) {
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
      LOG.info("BasicServiceContainer starting");
      startSessionAccessor();
      startHandlers();
      startTransports();
      LOG.info("BasicServiceContainer started");
      isStarted = true;
    }
  }
  
  public void stop() {
    if (isStarted) {
      isStarted = false;
      LOG.info("BasicServiceContainer stopping");
      stopHandlers();
      stopTransports();
      stopSessionAccessor();
      LOG.info("BasicServiceContainer stopped");
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
    LOG.info("Starting handlers");
    synchronized (filters) {
      for (Iterator iter=filters.iterator(); iter.hasNext(); ) {
        HttpServiceFilter handler = (HttpServiceFilter) iter.next();
        handler.start();
      } 
    }
    LOG.info("Handlers started");
  }
  
  private void stopHandlers() {
    LOG.info("Stopping handlers");
    synchronized (filters) {
      for (Iterator iter=filters.iterator(); iter.hasNext(); ) {
        HttpServiceFilter handler = (HttpServiceFilter) iter.next();
        LOG.info("Stopping handler '" + handler + "'");
        handler.stop();
        LOG.info("Handler '" + handler + "' stopped");
      }
    }
    LOG.info("Handlers stopped");
  }
  
  private void stopSessionAccessor() {
    LOG.info("Disposing session accessor");
    sessionAccessor.dispose();
    LOG.info("Session accessor disposed");
  }
  
  /**
   * Starts all added transports
   * 
   * @throws ContainerLifecycleException If we fail to start a transport
   */
  private void startTransports() throws ContainerLifecycleException {
    LOG.info("Starting transports");
    synchronized (transports) {
      for (Iterator iter=transports.iterator(); iter.hasNext(); ) {
        Transport transport = (Transport) iter.next();
        LOG.info("Starting transport '" + transport + "'");
        try {
          transport.start();
        } catch (TransportException e) {
          LOG.info("Transport '" + transport + "' failed to start");
          throw new ContainerLifecycleException("Failed to start transport ' " + 
                                                transport + "'", e);
        }
      }
    }
    LOG.info("Transports started");
  }
  
  private void stopTransports() {
    LOG.info("Stopping transports");
    boolean isError = false;
    synchronized (transports) {
      for (Iterator iter=transports.iterator(); iter.hasNext(); ) {
        Transport transport = (Transport) iter.next();
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
    LOG.info("Transports stopped" + errorString);
  }
}
