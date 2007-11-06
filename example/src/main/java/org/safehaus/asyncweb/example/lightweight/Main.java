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
package org.safehaus.asyncweb.example.lightweight;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.safehaus.asyncweb.codec.HttpServerCodecFactory;

public class Main {
  public static void main(String[] args) throws Exception {
    Executor threadPool = Executors.newCachedThreadPool();
    SocketAcceptor acceptor = new NioSocketAcceptor(
    		Runtime.getRuntime().availableProcessors() + 1,
    		threadPool);

    acceptor.getFilterChain().addLast(
        "codec",
        new ProtocolCodecFilter(new HttpServerCodecFactory()));

    acceptor.setReuseAddress(true);
    acceptor.getSessionConfig().setReuseAddress(true);
    acceptor.getSessionConfig().setReceiveBufferSize(1024);
    acceptor.getSessionConfig().setSendBufferSize(1024);
    acceptor.getSessionConfig().setTcpNoDelay(true);
    acceptor.getSessionConfig().setSoLinger(-1);
    acceptor.setBacklog(10240);
    
    acceptor.setLocalAddress(new InetSocketAddress(9012));
    acceptor.setHandler(new HttpProtocolHandler());
    
    acceptor.bind();
  }
}
