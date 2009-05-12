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
package org.apache.asyncweb.server;


/**
 * An HTTP client connection listener. Since Asyncweb allows asynchronous
 * handling of responses (response need not be committed in the HttpService
 * handleRequest() method), connections can be held open for some time.  For
 * this reason a mechanism is needed to be informed of client disconnects or
 * for notifications when the connection idles.
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public interface HttpClientListener
{
    /**
     * Gets notification of client disconnects.
     *
     * @param ctx the context associated with the disconnect event
     */
    void clientDisconnected( HttpServiceContext ctx );


    /**
     * Gets notification of client idling.
     *
     * @param ctx the context of the client which has gone idle
     * @param idleTime the time at which a client began idling
     * @param idleCount the number of times we were informed of idling
     */
    void clientIdle( HttpServiceContext ctx, long idleTime, int idleCount );
}
