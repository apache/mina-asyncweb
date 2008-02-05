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
package org.apache.asyncweb.examples.file;

import org.apache.asyncweb.examples.helloworld.HelloWorldHttpService;
import org.apache.asyncweb.server.BasicServiceContainer;
import org.apache.asyncweb.server.HttpServiceHandler;
import org.apache.asyncweb.server.resolver.PatternMatchResolver;
import org.apache.asyncweb.server.transport.mina.DefaultHttpIoHandler;
import org.apache.asyncweb.server.transport.mina.MinaTransport;

public class FileMain {
    
    /**
     * Starting an AsyncWeb server on port 9021, service files from ./data to base url /static
     */
    public static void main( String[] args ) throws Exception
    {
        // Setup the default container with a service handler that contains the helloWorldService
        BasicServiceContainer container = new BasicServiceContainer();
        HttpServiceHandler handler = new HttpServiceHandler();
        handler.addHttpService( "fileExample", new FileHttpService("/static","./data") );
        handler.addHttpService( "helloWorldExample", new HelloWorldHttpService() );
        container.addServiceFilter( handler );

        PatternMatchResolver resolver = new PatternMatchResolver();
        resolver.addPatternMapping("/static/.*", "fileExample");
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
