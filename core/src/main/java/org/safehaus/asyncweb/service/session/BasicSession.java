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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.safehaus.asyncweb.service.HttpSession;
import org.safehaus.asyncweb.util.TimedPermit;

/**
 * A basic <code>Session</code> implementation which holds session values
 * in memory
 * 
 * @author irvingd
 *
 */
class BasicSession implements HttpSession {

  private TimedPermit permit;
  private boolean isAttached;
  private boolean isDestroyed;
  private Object lock = new Object();
  private BasicSessionStore owner;
  private String id;
  
  private Map<String, Object> values = Collections.synchronizedMap(new HashMap<String, Object>());
  
  /**
   * @param owner  The owner of this session
   */
  BasicSession(String id, BasicSessionStore owner) {
    this.owner = owner;
    this.id    = id;
  }
  
  public Object getValue(String key) {
    return values.get(key);
  }

  public void setValue(String key, Object value) {
    values.put(key, value);
  }

  public Object removeValue(String key) {
    return values.remove(key);
  }

  public boolean isAttached() {
    return isAttached;
  }

  public boolean isValid() {
    synchronized (lock) {
      return !isDestroyed;
    }
  }
  
  /**
   * Destroys this session if it is not already destroyed.
   * If destruction takes place, notifications are fired.
   */
  public void destroy() {
    if (destroyIfActive()) {
      owner.sessionDestroyed(this);
    }
  }

  /**
   * @return  The id of this session
   */
  public String getId() {
    return id;
  }
  
  /**
   * Instructs this session to expire.
   * If this session is not already expired, expiration notifications
   * are fired
   */
  void expire() {
    if (destroyIfActive()) {
      owner.sessionExpired(this);
    }
  }
  
  /**
   * Sets the permit associated with this session
   * 
   * @param permit  This sessions access permit
   */
  void setPermit(TimedPermit permit) {
    this.permit = permit;
  }
  
  /**
   * Invoked when this session is referenced by a client request.
   * The permit associated with this session is renewed, and subsequent calls
   * to <code>isAttached</code> will return <code>true</code>
   */
  void access() {
    permit.renew();
    isAttached = true;
  }
  
  /**
   * Destroys this session if it is not destroyed already.
   * 
   * @return  <code>true</code> if this session is successfully
   */
  private boolean destroyIfActive() {
    synchronized (lock) {
      if (isDestroyed) {
        return false;
      }
      isDestroyed = true;
    }
    permit.cancel();
    return true;
  }
  
}
