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
package org.safehaus.asyncweb.service.resolver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.safehaus.asyncweb.common.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>ServiceResolver</code> which applies a list of child
 * resolvers in turn until a match is made or all children have
 * been tried
 * 
 * @author irvingd
 *
 */
public class CompositeResolver implements ServiceResolver {

  private static final Logger LOG = LoggerFactory.getLogger(CompositeResolver.class);
  
  private List<ServiceResolver> resolvers = new ArrayList<ServiceResolver>();
  
  /**
   * Requests all child resolvers to resolve the request until either
   * a resolution is found, or all child resolvers have been tried.
   * 
   * @param  request  The request to resolve
   * @return The service name, or <code>null</code> if no resolution could
   *         be found
   */
  public String resolveService(HttpRequest request) {
    for (int i=0, size=resolvers.size(); i < size; ++i) {
      ServiceResolver resolver = resolvers.get(i);
      String name = resolver.resolveService(request);
      if (name != null) {
        return name;
      }
    }
    return null;
  }

  /**
   * Adds a resolver. Resolvers are applied in the order they are
   * added
   * 
   * @param resolver  The resolver
   */
  public void addResolver(ServiceResolver resolver) {
    resolvers.add(resolver);
    LOG.info("Added resolver: " + resolver.getClass());
  }
  
  /**
   * Sets the resolvers employed by this <code>CompositeResolver</code>
   * 
   * @param resolvers  The resolvers
   */
  public void setResolvers(List<ServiceResolver> resolvers) {
    this.resolvers.clear();
    // Find bad types early
    for (Iterator<ServiceResolver> iter = resolvers.iterator(); iter.hasNext(); ) {
      ServiceResolver resolver = iter.next();
      
      addResolver(resolver);
    }
  }
  
}
