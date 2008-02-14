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
package org.apache.asyncweb.server.transport.mina;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import org.apache.mina.common.IoEventType;
import org.apache.mina.common.IoFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.logging.LogLevel;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.apache.asyncweb.server.ServiceContainer;
import org.apache.asyncweb.server.Transport;
import org.apache.asyncweb.server.TransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A <code>Transport</code> implementation which receives requests and sends
 * responses using non-blocking selector based IO.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class MinaTransport implements Transport
{
    private static final Logger LOG = LoggerFactory.getLogger( MinaTransport.class );

    private static final int DEFAULT_PORT = 9012;

    private static final int DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors();

    private static final int DEFAULT_EVENT_THREADS = 16;

    private SocketAcceptor acceptor;

    private ExecutorService eventExecutor;

    private int port = DEFAULT_PORT;

    private String address;

    private int ioThreads = DEFAULT_IO_THREADS;

    private int eventThreads = DEFAULT_EVENT_THREADS;

    private HttpIoHandler ioHandler;

    private boolean isLoggingTraffic;
    
    private LogLevel logLevel = LogLevel.WARN;

    private ServiceContainer container;


    /**
     * Sets the port this transport will listen on
     *
     * @param port  The port
     */
    public void setPort( int port )
    {
        this.port = port;
    }


    /**
     * Sets the address this transport will listen on
     *
     * @param address  The address to bind to.
     *                 Specify <tt>null</tt> or <tt>"*"</tt> to listen to all
     *                 NICs (Network Interface Cards).
     */
    public void setAddress( String address )
    {
        if ( "*".equals( address ) )
        {
            address = null;
        }
        this.address = address;
    }


    public int getIoThreads()
    {
        return ioThreads;
    }


    /**
     * Sets the number of worker threads employed by this transport.
     * This should typically be a small number (2 is a good choice) -
     * and is not tied to the number of concurrent connections you wish to
     * support
     *
     * @param ioThreads  The number of worker threads to employ
     */
    public void setIoThreads( int ioThreads )
    {
        this.ioThreads = ioThreads;
    }


    public int getEventThreads()
    {
        return eventThreads;
    }


    public void setEventThreads (int eventThreads )
    {
        this.eventThreads = eventThreads;
    }


    /**
     * Sets whether traffic received through this transport is
     * logged (off by default)
     *
     * @param isLoggingTraffic  <code>true</code> iff traffic should be logged
     */
    public void setIsLoggingTraffic( boolean isLoggingTraffic )
    {
        this.isLoggingTraffic = isLoggingTraffic;
    }
    
    
    public void setLogLevel( String logLevel )
    {
    	this.logLevel = LogLevel.valueOf( logLevel );
    }


    /**
     * Sets the <code>ServiceContainer</code> to which we issue requests
     *
     * @param container  Our associated <code>ServiceContainer</code>
     */
    public void setServiceContainer( ServiceContainer container)
    {
        this.container = container;
    }


    /**
     * Sets the <code>HttpIOHandler</code> to be employed by this transport
     *
     * @param httpIOHandler  The handler to be employed by this transport
     */
    public void setIoHandler( HttpIoHandler httpIOHandler )
    {
        this.ioHandler = httpIOHandler;
    }


    /**
     * Starts this transport
     *
     * @throws TransportException  If the transport can not be started
     */
    public void start() throws TransportException
    {
        initIOHandler();
        acceptor = new NioSocketAcceptor( ioThreads );
        eventExecutor = new OrderedThreadPoolExecutor( this.eventThreads );
        
        boolean success = false;
        try {
            acceptor.getFilterChain().addLast( "threadPool", new ExecutorFilter( eventExecutor ) );
            acceptor.setReuseAddress( true );
            acceptor.getSessionConfig().setReuseAddress( true );

            if ( isLoggingTraffic )
            {
                LOG.debug( "Configuring traffic logging filter" );
                LoggingFilter filter = new LoggingFilter();
                filter.setLogLevel( IoEventType.CLOSE, logLevel );
                filter.setLogLevel( IoEventType.EXCEPTION_CAUGHT, logLevel );
                filter.setLogLevel( IoEventType.MESSAGE_RECEIVED, logLevel );
                filter.setLogLevel( IoEventType.MESSAGE_SENT, logLevel );
                filter.setLogLevel( IoEventType.SESSION_CLOSED, logLevel );
                filter.setLogLevel( IoEventType.SESSION_CREATED, logLevel );
                filter.setLogLevel( IoEventType.SESSION_IDLE, logLevel );
                filter.setLogLevel( IoEventType.SESSION_OPENED, logLevel );
                filter.setLogLevel( IoEventType.SET_TRAFFIC_MASK, logLevel );
                filter.setLogLevel( IoEventType.WRITE, logLevel );
                acceptor.getFilterChain().addFirst( "LoggingFilter", filter );
            }

            // TODO make this configurable instead of hardcoding like this
            acceptor.setBacklog( 100 );
            acceptor.setHandler( ioHandler );

            if ( address != null )
            {
                acceptor.bind( new InetSocketAddress( address, port ) );
            }
            else
            {
                acceptor.bind( new InetSocketAddress( port ) );
            }
            
            success = true;
            LOG.debug( "NIO HTTP Transport bound on port " + port );
        }
        catch ( IOException e )
        {
            throw new TransportException( "NIOTransport Failed to bind to port " + port, e );
        }
        finally
        {
            if ( ! success )
            {
                acceptor.dispose();
                acceptor = null;
            }
        }
    }


    /**
     * Stops this transport
     */
    public void stop() throws TransportException
    {
        if ( acceptor == null )
        {
            return;
        }

        acceptor.dispose();
        eventExecutor.shutdown();
        acceptor = null;
        eventExecutor = null;
    }


    /**
     * @return A string representation of this transport
     */
    @Override
    public String toString()
    {
        return "NIOTransport [port=" + port + "]";
    }


    /**
     * Initializes our handler - creating a new (default) handler if none has
     * been specified
     *
     * @throws IllegalStateException If we have not yet been associated with a
     *                               container
     */
    private void initIOHandler()
    {
        if ( ioHandler == null ) 
        {
            LOG.info( "No http IO Handler associated - using defaults" );
            ioHandler = new DefaultHttpIoHandler();
        }

        if ( container == null )
        {
            throw new IllegalStateException( "Transport not associated with a container" );
        }

        ioHandler.setContainer( container );
    }
}
