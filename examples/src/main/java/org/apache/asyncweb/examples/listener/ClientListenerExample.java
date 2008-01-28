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
package org.apache.asyncweb.examples.listener;


import org.apache.asyncweb.server.HttpService;
import org.apache.asyncweb.server.HttpServiceContext;
import org.apache.asyncweb.server.HttpClientListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An <code>HttpService</code> which utilizes the HttpClientListener interface
 * for long polling requests.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class ClientListenerExample implements HttpService
{
    private final Logger log = LoggerFactory.getLogger( ClientListenerExample.class );

    
    /**
     * Does nothing with the request but just listens for disconnects and
     * connection idling.
     */
    public void handleRequest( HttpServiceContext context ) throws Exception
    {
        final long requestTime = System.currentTimeMillis();
        log.info( "Got request from client {} at {}", context.getRemoteAddress(), requestTime );

        context.addClientListener( new HttpClientListener()
        {
            public void clientDisconnected( HttpServiceContext ctx )
            {
                log.info( "Client {} disconnected after {} milliSeconds", ctx.getRemoteAddress(),
                        ( System.currentTimeMillis() - requestTime ) );
            }

            public void clientIdle( HttpServiceContext ctx, long idleTime, int idleCount )
            {
                log.info( "Client {} idle events after {} milliSeconds", ctx.getRemoteAddress(),
                        ( System.currentTimeMillis() - requestTime ) );
                log.info( "IdleTime   = {}", idleTime );
                log.info( "IdleCount  = {}", idleCount );
            }
        });
    }


    public void start()
    {
        log.info( "Started listener service. Goto http://localhost:9012/listener ang give it a try." );
    }


    public void stop()
    {
        log.info( "Stopped listener service." );
    }
}