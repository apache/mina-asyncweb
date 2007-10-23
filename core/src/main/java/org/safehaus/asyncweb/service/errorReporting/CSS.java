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
package org.safehaus.asyncweb.service.errorReporting;

import java.io.UnsupportedEncodingException;

import org.apache.mina.common.ByteBuffer;

/**
 * Manages the stylesheet info used for generated pages
 * 
 * TODO: Should be moved out to a configuration file when we sort out a
 *       standard resource strategy
 *       
 * @author irvingd
 *
 */
public class CSS {
  //525D76
  private static final String BG = "3300cc";
  
  private static final String CSS_STRING = 
    "H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#" + BG + ";font-size:22px;} " +
    "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#" + BG + ";font-size:16px;} " +
    "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#" + BG + ";font-size:14px;} " +
    "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
    "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#" + BG + ";} " +
    "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
    "A {color : black;}" +
    "A.name {color : black;}\n" +
    "TABLE {cellpadding:20;border-color:black;border-width:1px;border-style:solid;border-collapse:collapse}" + 
    "TD {border-width:1px;border-color:black;border-style:solid;font-family:Tahoma,Arial,sans-serif;color:black;font-size:12px;}" +
    "TH {border-width:1px;border-color:black;border-style:solid;font-family:Tahoma,Arial,sans-serif;background-color:FF99FF;color:black;font-size:12px;}" +
    "HR {color : #" + BG + ";}";
  
  private static byte[] BYTES;
  
  static {
    try {
      BYTES = CSS_STRING.getBytes("US-ASCII");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Must support US-ASCII");
    }
  }
  
  public static void writeTo(ByteBuffer buf) {
    buf.put(BYTES);
  }
  
  public static StringBuilder appendTo(StringBuilder buff) {
    buff.append(CSS_STRING);
    return buff;
  }
  
}
