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


/**
 * A mechanism over which <code>Request</code>s are received.
 * 
 * @author irvingd
 *
 */
public interface Transport {

  /**
   * Associates this <code>Transport</code> with its container.
   * 
   * @param container  The container to which incoming requests should
   *                   be provided
   */
  public void setServiceContainer(ServiceContainer container);
  
  /**
   * Starts this <code>Transport</code>.
   * Once a <code>Transport</code> has been started, it may begin
   * submitting <code>Request</code>s to its associated 
   * <code>ServiceContainer</code>
   * 
   * @throws TransportException  If the transport can not be started
   */
  public void start() throws TransportException;
  
  /**
   * Stops this <code>Transport</code>.
   * No further requests should be sent to the transports associated
   * <code>ServiceContainer</code>
   * 
   * @throws TransportException  If there were problems stopping the transport
   */
  public void stop() throws TransportException;
  
}
