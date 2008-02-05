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
package org.apache.ahc.ssl;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

/**
 * The Class TrustManagerFactoryImpl.  Sets up the Trust support for SSL contexts.
 */
public class TrustManagerFactoryImpl extends TrustManagerFactorySpi {
    
    /** The Constant X509. */
    public static final X509TrustManager X509 = new X509TrustManager() {


        public void checkClientTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {

        }


        public void checkServerTrusted(X509Certificate[] x509Certificates, String s)
            throws CertificateException {

        }


        public X509Certificate[] getAcceptedIssuers() {

            return new X509Certificate[0];

        }

    };

    /** The Constant X509_MANAGERS. */
    public static final TrustManager[] X509_MANAGERS = new TrustManager[] {X509};

    /* Default stub for the <code>TrustManagerFactorySpi</code> parent.
     * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(java.security.KeyStore)
     */
    protected void engineInit(KeyStore keyStore) throws KeyStoreException {
    }

    /* Default stub for the <code>TrustManagerFactorySpi</code> parent.
     * @see javax.net.ssl.TrustManagerFactorySpi#engineInit(javax.net.ssl.ManagerFactoryParameters)
     */
    protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
        throws InvalidAlgorithmParameterException {
    }

    /* Default stub for the <code>TrustManagerFactorySpi</code> parent.
     * @return array of <code>TrustManager</code> objects.
     * @see javax.net.ssl.TrustManagerFactorySpi#engineGetTrustManagers()
     */
    protected TrustManager[] engineGetTrustManagers() {
        return X509_MANAGERS;
    }
}
