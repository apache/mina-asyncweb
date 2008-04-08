/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.asyncweb.client.codec;

import java.util.Date;

/**
 * Class is the container for an HTTP Cookie. It is an object representing the cookie header of
 * the HTTP protocol.
 */
public class Cookie {

    /** The comment. */
    private String comment;
    
    /** The domain. */
    private String domain;
    
    /** The name. */
    private String name;
    
    /** The value. */
    private String value;
    
    /** The path. */
    private String path;
    
    /** The secure. */
    private boolean secure;
    
    /** The version. */
    private int version;
    
    /** The expires. */
    private Date expires;

    /**
     * Constructs a cookie identified by a name and value.
     * 
     * @param name the cookie name
     * @param value the cookie value
     */
    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns a cookie's comment or <code>null</code> if no comment exists.
     * 
     * @return The cookie comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets a comment which describes the cookie.
     * 
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns a cookie's domain is one is available or <code>null</code> if it does not exist.
     * 
     * @return the cookie's domain.
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets a cookie's domain as specified by RFC 2109.
     * 
     * @param domain the cookie's domain to set
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }


    /**
     * Returns the name of the cookie.
     * 
     * @return the name of the cookie
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the cookie.
     * 
     * @param name the name of the cookie
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the cookie's value.
     * 
     * @return the cookie's value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the cookie's value.
     * 
     * @param value the cookie value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the cookie path. The path represents all paths, and sub-paths that this cookie is valid.
     * 
     * @return the cookie path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the cookie's path. The path represents all paths, and sub-paths that this cookie is valid.
     * 
     * @param path the cookie path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Returns if the cookie is secure.
     * 
     * @return true, if is secure
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets the cookie secure flag.
     * 
     * @param secure the new secure value (<code>true</code>/<code>false</code>)
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Gets the cookie version.
     * 
     * @return the cookie version
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the cookie version.
     * 
     * @param version the cookie version
     */
    public void setVersion(int version) {
        this.version = version;
    }

    /**
     * Gets the cookie expiration <code>Date</code>.
     * 
     * @return the expiration <code>Date</code>
     */
    public Date getExpires() {
        return expires;
    }

    /**
     * Sets the cookie expiration <code>Date</code>.
     * 
     * @param expires the new expiration <code>Date</code>
     */
    public void setExpires(Date expires) {
        this.expires = expires;
    }
}
