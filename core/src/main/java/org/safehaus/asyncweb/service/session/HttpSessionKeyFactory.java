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

/**
 * A factory for creating Session keys.
 * <code>SessionKeyFactory</code> implementations should make a best effort to:
 * <ul>
 *   <li>Avoid the generation of two keys <i>k(1), k(2)</i> 
 *       such that <i>k(1) == k(2)</i></li>
 *   <li>Avoid the generation of a key <i>k</i> such that an adversary can
 *       make an informed guess of the content of any other key created by this factory
 *       at any time in the future</li>
 * </ul>
 *  
 * @author irvingd
 *
 */
public interface HttpSessionKeyFactory {

  /**
   * Returns a new session key String
   * 
   * @return  The session key
   */
  public String createSessionKey();
  
}
