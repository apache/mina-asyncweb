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
package org.apache.asyncweb.server.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplifies looking up locale specific format strings.
 *
 *
 */
public class StringBundle {

    private static final String RESOURCE_POSTFIX = ".strings";

    private static final Logger LOG = LoggerFactory
            .getLogger(StringBundle.class);

    private static Map<String, StringBundle> bundles = new HashMap<String, StringBundle>();

    private ResourceBundle bundle;

    private StringBundle(String packageName) {
        String bundleName = packageName + RESOURCE_POSTFIX;
        bundle = ResourceBundle.getBundle(bundleName);
        if (bundle == null) {
            LOG.warn("Cant find resource '" + bundleName + "'");
        }
    }

    public synchronized static StringBundle getBundle(String packageName) {
        StringBundle bundle = bundles.get(packageName);
        if (bundle == null) {
            bundle = new StringBundle(packageName);
            bundles.put(packageName, bundle);
        }
        return bundle;
    }

    public String getString(String key) {
        return MessageFormat.format(getValue(key), (Object) null);
    }

    public String getString(String key, Object param) {
        Object[] params = new Object[] { param };
        return getString(key, params);
    }

    public String getString(String key, Object param1, Object param2) {
        Object[] params = new Object[] { param1, param2 };
        return getString(key, params);
    }

    public String getString(String key, Object[] params) {
        String value = getValue(key);
        String formatted;
        try {
            formatted = MessageFormat.format(value, params);
        } catch (IllegalArgumentException e) {
            formatted = applyDefaultFormat(value, params);
        }
        return formatted;
    }

    private String getValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Null key");
        }
        if (bundle == null) {
            return key;
        }
        String value = bundle.getString(key);
        return value == null ? "Missing resource '" + key + "'" : value;
    }

    private String applyDefaultFormat(String value, Object[] params) {
        StringBuffer buff = new StringBuffer();
        buff.append(value);
        if (params != null) {
            for (int i = 0; i < params.length; ++i) {
                buff.append(" param[").append(i).append(" = ")
                        .append(params[i]).append(" ]");
            }
        }
        return buff.toString();
    }

}
