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
package org.safehaus.asyncweb.codec.encoder;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.mina.common.ByteBuffer;
import org.safehaus.asyncweb.codec.HttpCodecUtils;
import org.safehaus.asyncweb.common.Cookie;

/**
 * A cookie encoder which writes cookies in a form suitable for maximum
 * compatibility - rather than to strict accordance to RFC2965
 * 
 * @author irvingd
 *
 */
public class CompatibilityCookieEncoder implements CookieEncoder {

  private static final byte[] HEADER_BYTES  = HttpCodecUtils.getASCIIBytes("Set-Cookie: ");
  private static final byte[] VERSION_BYTES = HttpCodecUtils.getASCIIBytes("; version=");
  private static final byte[] PATH_BYTES    = HttpCodecUtils.getASCIIBytes("; path=");
  private static final byte[] EXPIRY_BYTES  = HttpCodecUtils.getASCIIBytes("; max-age=");
  private static final byte[] EXPIRES_BYTES = HttpCodecUtils.getASCIIBytes("; Expires=");
  private static final byte[] SECURE_BYTES  = HttpCodecUtils.getASCIIBytes("; secure;");
  
  private static final TimeZone FORMAT_TIME_ZONE = TimeZone.getTimeZone("GMT");
  
  /**
   * Thread-local DateFormat for old-style cookies
   */
  private static final ThreadLocal EXPIRY_FORMAT_LOACAL = new ThreadLocal() {
  
    protected Object initialValue() {
      SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.US);
      format.setTimeZone(FORMAT_TIME_ZONE);
      return format;
    }
        
  };
  
  /**
   * A date long long ago, formatted in the old style cookie expire format
   */
  private static final String EXPIRED_DATE = getFormattedExpiry(0);
  
  public void encodeCookie(Collection<Cookie> cookies, ByteBuffer buffer) {
    for (Cookie c: cookies) {
      buffer.put(HEADER_BYTES);
      encodeCookieValue(c, buffer);
      HttpCodecUtils.appendCRLF(buffer);
    }  
  }

  private static String getFormattedExpiry(long time) {
    SimpleDateFormat format = (SimpleDateFormat) EXPIRY_FORMAT_LOACAL.get();
    return format.format(new Date(time));
  }
  
  private void encodeCookieValue(Cookie cookie, ByteBuffer buffer) {
    HttpCodecUtils.appendString(buffer, cookie.getName());
    buffer.put(HttpCodecUtils.EQUALS);
    HttpCodecUtils.appendString(buffer, cookie.getValue());
    if (cookie.getVersion() > 0) {
      buffer.put(VERSION_BYTES);
      HttpCodecUtils.appendString(buffer, String.valueOf(cookie.getVersion()));
    }
    String path = cookie.getPath();
    if (path != null) {
      buffer.put(PATH_BYTES);
      HttpCodecUtils.appendString(buffer, path);
    }
    encodeExpiry(cookie, buffer);
    if (cookie.isSecure()) {
      buffer.put(SECURE_BYTES);
    } else {
      buffer.put(HttpCodecUtils.SEMI_COLON);
    }
  }
  
  /**
   * Encodes the cookie expiry time in to the specfied buffer.
   * The format used is selected based on the cookie version.
   * 
   * @param cookie  The cookie
   * @param buffer  The buffer
   */
  private void encodeExpiry(Cookie cookie, ByteBuffer buffer) {
    long expiry = cookie.getMaxAge();
    int version = cookie.getVersion();
    if (expiry >= 0) {
      if (version == 0) {
        String expires = expiry == 0 ? EXPIRED_DATE : 
                                       getFormattedExpiry(System.currentTimeMillis() + (1000 * expiry));
        buffer.put(EXPIRES_BYTES);
        HttpCodecUtils.appendString(buffer, expires);
      } else {
        buffer.put(EXPIRY_BYTES);
        HttpCodecUtils.appendString(buffer, String.valueOf(cookie.getMaxAge()));
      }      
    }
  }
  
}
