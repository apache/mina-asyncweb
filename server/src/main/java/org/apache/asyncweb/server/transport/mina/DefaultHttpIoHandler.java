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


import org.apache.mina.common.session.IoSession;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;
import org.apache.mina.handler.multiton.SingleSessionIoHandlerDelegate;
import org.apache.mina.handler.multiton.SingleSessionIoHandlerFactory;
import org.apache.asyncweb.server.ServiceContainer;


/**
 * The default HttpIoHandler used when one is not provided.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Rev$, $Date$
 */
public class DefaultHttpIoHandler extends SingleSessionIoHandlerDelegate implements HttpIoHandler
{
    /** the default idle time in seconds - 5 minutes */
    public static final int DEFAULT_IDLE_TIME = 300;


    public DefaultHttpIoHandler()
    {
        super( new Factory() );
    }


    public ServiceContainer getContainer()
    {
        return ( ( Factory ) getFactory() ).getContainer();
    }


    public void setContainer( ServiceContainer container )
    {
        ( ( Factory ) getFactory() ).setContainer( container );
    }


    public void setReadIdle( int idleTime )
    {
        ( ( Factory ) getFactory() ).setReadIdle( idleTime );
    }


    private static class Factory implements SingleSessionIoHandlerFactory
    {
        private ServiceContainer container;
        private int readIdleTime = DEFAULT_IDLE_TIME;

        public ServiceContainer getContainer()
        {
            return container;
        }

        public void setContainer( ServiceContainer container )
        {
            this.container = container;
        }

        public void setReadIdle( int idleTime )
        {
            this.readIdleTime = idleTime;
        }


        public SingleSessionIoHandler getHandler( IoSession session )
        {
            SingleHttpSessionIoHandler handler = new SingleHttpSessionIoHandler( container, session );
            handler.setReadIdleTime( readIdleTime );
            return handler;
        }
    }
}
