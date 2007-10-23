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
package org.safehaus.asyncweb.service.session;

import org.safehaus.asyncweb.service.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple <code>SessionListener</code> which logs the various lifecycle events
 * 
 * @author irvingd
 *
 */
public class LoggingSessionListener implements HttpSessionListener {

  private static final Logger LOG = LoggerFactory.getLogger(LoggingSessionListener.class);
  
  public void sessionCreated(HttpSession session) {
    doLog("New Session Created", session);
  }

  public void sessionDestroyed(HttpSession session) {
    doLog("Session Destroyed", session);
  }

  public void sessionExpired(HttpSession session) {
    doLog("Session Expired", session);
  }

  private void doLog(String msg, HttpSession session) {
    if (LOG.isInfoEnabled()) {
      LOG.info(msg + " [SessionId = " + session.getId() + "]");
    }
  }
  
}
