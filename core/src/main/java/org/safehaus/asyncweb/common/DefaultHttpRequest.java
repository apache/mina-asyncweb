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
package org.safehaus.asyncweb.common;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.safehaus.asyncweb.util.HttpHeaderConstants;

/**
 * A default implementation of {@link MutableHttpRequest}.
// * 
 * @author trustin
 * @author irvingd
 * @version $Rev$, $Date$
 */
public class DefaultHttpRequest extends DefaultHttpMessage implements MutableHttpRequest {

  private static final long serialVersionUID = 3044997961372568928L;

  private HttpMethod method;
  private URI requestUri;
  
  private Map<String, List<String>> parameters = new HashMap<String, List<String>>();
  
  /**
   * Creates a new instance.
   */
  public DefaultHttpRequest() {
  }
  
  public void setCookies(String headerValue) {
    clearCookies();
    
    int version = -1; // -1 means version is not parsed yet.
    int fieldIdx = 0;
    MutableCookie currentCookie = null;
    
    StringTokenizer tk = new StringTokenizer(headerValue, ";,");
    
    while (tk.hasMoreTokens()) {
      String pair = tk.nextToken();
      String key;
      String value;

      int equalsPos = pair.indexOf('=');
      if (equalsPos >= 0) {
        key = pair.substring(0, equalsPos).trim();
        value = pair.substring(equalsPos + 1).trim();
      } else {
        key = pair.trim();
        value = "";
      }
      
      if (version < 0) {
        if (!key.equalsIgnoreCase("$Version")) {
          // $Version is not specified.  Use the default (0).
          version = 0;
        } else {
          version = Integer.parseInt(value);
          if (version != 0 && version != 1) {
            throw new IllegalArgumentException("Invalid version: " + version + " (" + headerValue + ")");
          }
        }
      }
      
      if (version >= 0) {
        try {
          switch (fieldIdx) {
          case 1:
            if (key.equalsIgnoreCase("$Path")) {
              currentCookie.setPath(value);
              fieldIdx ++;
            } else {
              fieldIdx = 0;
            }
            break;
          case 2:
            if (key.equalsIgnoreCase("$Domain")) {
              currentCookie.setDomain(value);
              fieldIdx ++;
            } else {
              fieldIdx = 0;
            }
            break;
          case 3:
            if (key.equalsIgnoreCase("$Port")) {
              // Ignoring for now
              fieldIdx ++;
            } else {
              fieldIdx = 0;
            }
            break;
          }
        } catch (NullPointerException e) {
          throw new IllegalArgumentException("Cookie key-value pair not found (" + headerValue + ")");
        }
        
        if (fieldIdx == 0) {
          currentCookie = new DefaultCookie(key);
          currentCookie.setVersion(version);
          currentCookie.setValue(value);
          addCookie(currentCookie);
          fieldIdx ++;
        }
      }
    }
  }
  
  public URI getRequestUri() {
    return requestUri;
  }
  
  public void setRequestUri(URI requestUri) {
    if (requestUri == null) {
      throw new NullPointerException("requestUri");
    }
    this.requestUri = requestUri;
  }
  
  public boolean requiresContinuationResponse() {
    if (getProtocolVersion() == HttpVersion.HTTP_1_1) {
      String expectations = getHeader(HttpHeaderConstants.KEY_EXPECT);
      if (expectations != null) {
        return expectations.indexOf(HttpHeaderConstants.VALUE_CONTINUE_EXPECTATION) >= 0;
      }
    }
    return false;
  }
  
  public HttpMethod getMethod() {
    return method;
  }

  public void setMethod(HttpMethod method) {
    if (method == null) {
      throw new NullPointerException("method");
    }
    this.method = method;  
  }

  public void addParameter(String name, String value) {
    List<String> values = parameters.get(name);
    if (values == null) {
      values = new ArrayList<String>();
      parameters.put(name, values);
    }
    values.add(value);
  }
  
  public boolean removeParameter(String name) {
    return parameters.remove(name) != null;
  }

  public void setParameter(String name, String value) {
    List<String> values = new ArrayList<String>();
    values.add(value);
    parameters.put(name, values);
  }

  public void setParameters(Map<String, List<String>> parameters) {
    clearParameters();

    for (Map.Entry<String, List<String>> entry: parameters.entrySet()) {
      for (String value: entry.getValue()) {
        if (value == null) {
          throw new NullPointerException("Parameter '" + entry.getKey() + "' contains null.");
        }
      }
      if (entry.getValue().size() > 0) {
        this.parameters.put(entry.getKey(), entry.getValue());
      }
    }
  }

  public void setParameters(String queryString) {
    try {
      this.setParameters(queryString, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new InternalError("UTF-8 decoder must be provided by JDK.");
    }
  }
  
  public void setParameters(String queryString, String encoding) throws UnsupportedEncodingException {
    clearParameters();
    
    if (queryString == null || queryString.length() == 0) {
      return;
    }
    
    int pos = 0;
    while (pos < queryString.length()) {
      int ampPos = queryString.indexOf('&', pos);

      String value;
      if (ampPos < 0) {
        value = queryString.substring(pos);
        ampPos = queryString.length();
      } else {
        value = queryString.substring(pos, ampPos);
      }
      
      int equalPos = value.indexOf('=');
      if (equalPos < 0) {
        this.addParameter(URLDecoder.decode(value, encoding), "");
      } else {
        this.addParameter(
            URLDecoder.decode(value.substring(0, equalPos), encoding),
            URLDecoder.decode(value.substring(equalPos + 1), encoding));
      }
      
      pos = ampPos + 1;
    }
  }
  
  public void clearParameters() {
    this.parameters.clear();
  }
  
  public boolean containsParameter(String name) {
    return parameters.containsKey(name);
  }

  public String getParameter(String name) {
    List<String> values = parameters.get(name);
    if (values == null) {
      return null;
    }
    
    return values.get(0);
  }

  public Map<String, List<String>> getParameters() {
    return Collections.unmodifiableMap(parameters);
  }
}
