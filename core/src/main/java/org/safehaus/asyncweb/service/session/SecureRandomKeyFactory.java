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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureRandomKeyFactory implements HttpSessionKeyFactory {

  /**
   * The default key length provided by this factory
   */
  private static final int DEFAULT_KEY_LENGTH = 16;
  
  /**
   * The minimum key length supported by this factory
   */
  private static final int MINIMUM_KEY_LENGTH = 8;
  
  /**
   * The default algorithm employed
   */
  private static final String DEFAULT_ALGORITHM = "SHA1PRNG";
  
  private static final Logger LOG = LoggerFactory.getLogger(SecureRandomKeyFactory.class);
  
  private SecureRandom secureRandom;
  private int keyLength = DEFAULT_KEY_LENGTH;
  private volatile boolean isStarted;
  private String algorithm = DEFAULT_ALGORITHM;
  
  /**
   * Creates a session key based on bytes provided from an underlying
   * <code>SecureRandom</code>
   * 
   * @return The created key
   */
  public String createSessionKey() {
    byte[] keyBytes = new byte[keyLength];
    synchronized (secureRandom) {
      secureRandom.nextBytes(keyBytes);
    }
    return bytesToSessionKey(keyBytes);
  }

  /**
   * Sets the number of <i>bytes</i> used when forming keys created by this factory.
   * A hex encoding is applied to the generated key bytes such that the number of
   * <i>characters</i> in keys created by this factory is twice the key length
   * 
   * @param keyLength  The number of bytes employed in keys created by this factory
   */
  public void setKeyLength(int keyLength) {
    if (keyLength < MINIMUM_KEY_LENGTH) {
      throw new IllegalArgumentException("Key length must be >= " + MINIMUM_KEY_LENGTH);
    }
    if (isStarted) { // sanity check
      throw new IllegalStateException("Key factory started");
    }
    this.keyLength = keyLength;  
  }
  
  /**
   * Sets the algorithm employed by the underlying <code>SecureRandom</code>.
   * The default is <code>SHA1PRNG</code>
   * 
   * @param algorithm The algorithm to be employed
   */
  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }
  
  /**
   * Starts this factory.
   */
  public void start() {
    isStarted = true;
    LOG.info("Attempting to obtain SecureRandom using algorithim: " + algorithm);
    try {
      secureRandom = SecureRandom.getInstance(algorithm);
      LOG.info("Ok - using algorithm: " + algorithm);
    } catch (NoSuchAlgorithmException e) {
      LOG.info("Failed to obtain secure random with algorithm: " + algorithm + 
               ". Resorting to default");
    }
    secureRandom = new SecureRandom();
    secureRandom.nextBytes(new byte[keyLength]); // seed
  }

  private static String bytesToSessionKey(byte[] bytes) {
    char[] keyChars = new char[bytes.length * 2];
    for (int i = 0; i < bytes.length; ++i) {
      byte b1 = (byte) ((bytes[i] & 0xf0) >> 4);
      byte b2 = (byte) (bytes[i] & 0x0f);
      keyChars[2 * i] = toKeyStringChar(b1);
      keyChars[(2 * i) + 1] = toKeyStringChar(b2);
    }
    return new String(keyChars);
  }
  
  private static final char toKeyStringChar(byte b) {
    if (b < 10) {
      return (char) ('0' + b);
    } else {
      return (char) ('A' + (b - 10));
    }
  }
  
}
