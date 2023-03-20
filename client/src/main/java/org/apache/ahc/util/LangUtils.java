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
package org.apache.ahc.util;

/**
 * A set of utility methods to help produce consistent Object#equals(Object) and
 * Object#hashCode methods.
 * 
 * @author oleg at ural.ru Oleg Kalnichevski
 */
public final class LangUtils {

    /** The Constant HASH_SEED. */
    public static final int HASH_SEED = 17;
    
    /** The Constant HASH_OFFSET. */
    public static final int HASH_OFFSET = 37;

    /**
     * Instantiates a new LangUtils object.
     */
    private LangUtils() {
        super();
    }

    /**
     * Hash code.
     * 
     * @param seed the seed
     * @param hashcode the hashcode
     * 
     * @return the calculated hash value
     */
    public static int hashCode(final int seed, final int hashcode) {
        return seed * HASH_OFFSET + hashcode;
    }

    /**
     * Hash code.
     * 
     * @param seed the seed
     * @param obj the obj
     * 
     * @return the calculated hash value
     */
    public static int hashCode(final int seed, final Object obj) {
        return hashCode(seed, obj != null ? obj.hashCode() : 0);
    }

    /**
     * Hash code.
     * 
     * @param seed the seed
     * @param b the b
     * 
     * @return the calculated hash value
     */
    public static int hashCode(final int seed, final boolean b) {
        return hashCode(seed, b ? 1 : 0);
    }

    /**
     * Equals.
     * 
     * @param obj1 the obj1
     * @param obj2 the obj2
     * 
     * @return true, if the objects equal
     */
    public static boolean equals(final Object obj1, final Object obj2) {
        return obj1 == null ? obj2 == null : obj1.equals(obj2);
    }

}

