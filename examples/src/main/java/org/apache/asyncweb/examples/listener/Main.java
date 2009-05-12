/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.asyncweb.examples.listener;


import org.apache.asyncweb.server.BasicServiceContainer;
import org.apache.asyncweb.server.HttpServiceHandler;
import org.apache.asyncweb.server.transport.mina.MinaTransport;
import org.apache.asyncweb.server.transport.mina.DefaultHttpIoHandler;
import org.apache.asyncweb.server.resolver.ExactMatchURIServiceResolver;


/**
 * An application launcher which runs the ClientListenerExample example.
 *
 * @author <a href="mailto:dev@mina.apache.org">Apache MINA Project</a>
 */
public class Main
{
    public static void main( String[] args ) throws Exception
    {
        // Setup the default container with a service handler that contains the helloWorldService
        BasicServiceContainer container = new BasicServiceContainer();
        HttpServiceHandler handler = new HttpServiceHandler();
        handler.addHttpService( "clientListenerExample", new ClientListenerExample() );
        container.addServiceFilter( handler );

        // Set up a resolver for the HttpServiceHandler
        ExactMatchURIServiceResolver resolver = new ExactMatchURIServiceResolver();
        resolver.addURIMapping( "/listener", "clientListenerExample" );
        handler.setServiceResolver( resolver );

        // Create the mina transport and enable the container with it
        MinaTransport transport = new MinaTransport();
        container.addTransport( transport );
        DefaultHttpIoHandler ioHandler = new DefaultHttpIoHandler();
        ioHandler.setReadIdle( 10 );
        transport.setIoHandler( ioHandler );

        // Fire it up and go
        container.start();
    }
}
