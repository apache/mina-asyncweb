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
package org.safehaus.asyncweb.service.transport.mina;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.apache.mina.common.DefaultWriteRequest;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;
import org.apache.mina.common.WriteRequest;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.ProtocolDecoderException;
import org.apache.asyncweb.common.HttpRequest;
import org.apache.asyncweb.common.HttpRequestDecoderException;
import org.apache.asyncweb.common.HttpResponseStatus;
import org.apache.asyncweb.common.HttpVersion;
import org.apache.asyncweb.common.MutableHttpResponse;
import org.apache.mina.handler.multiton.SingleSessionIoHandler;
import org.apache.asyncweb.common.DefaultHttpRequest;
import org.apache.asyncweb.common.DefaultHttpResponse;
import org.apache.asyncweb.common.*;
import org.safehaus.asyncweb.service.HttpServiceContext;
import org.safehaus.asyncweb.service.HttpServiceFilter;
import org.safehaus.asyncweb.service.ServiceContainer;
import org.safehaus.asyncweb.service.context.AbstractHttpServiceContext;
import org.safehaus.asyncweb.service.pipeline.RequestPipeline;
import org.safehaus.asyncweb.service.pipeline.RequestPipelineListener;
import org.safehaus.asyncweb.service.pipeline.StandardRequestPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleHttpSessionIoHandler implements SingleSessionIoHandler {

    private static final Logger LOG = LoggerFactory
            .getLogger(SingleHttpSessionIoHandler.class);

    /**
     * The number of parsers we pre-allocate
     */
    //  private static final int DEFAULT_PARSERS = 5;
    /**
     * The default idle time
     */
    private static final int DEFAULT_IDLE_TIME = 30000;

    /**
     * Out default pipeline
     */
    private static final int DEFAULT_PIPELINE = 100;

    private final ServiceContainer container;

    protected final IoSession session;

    private final RequestPipeline pipeline;

    private HttpServiceContext currentContext;

    private int readIdleTime = DEFAULT_IDLE_TIME;

    public SingleHttpSessionIoHandler(ServiceContainer container, IoSession session) {
        this.container = container;
        this.session = session;
        this.pipeline = new StandardRequestPipeline(DEFAULT_PIPELINE);

        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, readIdleTime);

        session.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new HttpCodecFactory()));

        session.getFilterChain().addLast("converter", new ContextConverter());

        session.getFilterChain().addLast("pipeline",
                new RequestPipelineAdapter(pipeline));

        int i = 0;
        for (HttpServiceFilter serviceFilter : container.getServiceFilters()) {
            session.getFilterChain().addLast("serviceFilter." + i++,
                    new ServiceFilterAdapter(serviceFilter));
        }
    }

    public void sessionCreated() {
    }

    public void sessionOpened() {
        LOG.info("Connection opened");
    }

    public void sessionClosed() {
        LOG.info("Connection closed");
    }

    /**
     * Invoked when this connection idles out.
     * If we are in the process of parsing a request, the current request
     * is rejected with a {@link HttpResponseStatus#REQUEST_TIMEOUT} response status.
     *
     */
    public void sessionIdle(IdleStatus idleType) {
        if (session.getIdleCount(idleType) == 1) {
            //      // FIXME currentRequest is always null now; we need to cooperate with a decoder.
            //      if (currentContext != null) {
            //        LOG.info("Read idled out while parsing request. Scheduling timeout response");
            //        handleReadFailure(currentContext, HttpResponseStatus.REQUEST_TIMEOUT, "Timeout while reading request");
            //      } else {
            LOG
                    .info("Idled with no current request. Scheduling closure when pipeline empties");
            pipeline.runWhenEmpty(new Runnable() {
                public void run() {
                    LOG.info("Pipeline empty after idle. Closing session");
                    session.close();
                }
            });
            //      }
        }
    }

    public void exceptionCaught(Throwable cause) {
        MutableHttpResponse response = null;
        if (cause instanceof ProtocolDecoderException) {
            HttpResponseStatus status;
            if (cause instanceof HttpRequestDecoderException) {
                status = ((HttpRequestDecoderException) cause).getResponseStatus();
            } else {
                status = HttpResponseStatus.BAD_REQUEST;
            }

            LOG.warn("Bad request:", cause);

            response = new DefaultHttpResponse();
            response.setProtocolVersion(HttpVersion.HTTP_1_1);
            response.setStatus(status);
        } else if (cause instanceof IOException) {
            LOG.warn("IOException on HTTP connection", cause);
            session.close();
        } else {
            response = new DefaultHttpResponse();
            response.setProtocolVersion(HttpVersion.HTTP_1_1);
            response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            LOG.warn("Unexpected exception from a service.", cause);
        }
        if (response != null) {
            HttpServiceContext context = this.currentContext;
            if (context == null) {
                context = createContext(new DefaultHttpRequest());
            }
            context.commitResponse(response);
        }
    }

    public void messageReceived(Object message) {
        // FIXME messageReceived invoked only when whole message is built.

        // When headers were built
        //sendContinuationIfRequested(request);

        // When body has been built
    }

    /**
     * Invoked when we fail to parse an incoming request.
     * We configure our parser to discard any further data received from the client,
     * and schedule a response with the appropriate failure code for the
     * current request
     *
     * @param status  The status
     * @param message Failure message
     */
    private void handleReadFailure(HttpServiceContext context,
            HttpResponseStatus status, String message) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Failed to handle client request. Reason: " + status);
        }
        MutableHttpResponse response = new DefaultHttpResponse();
        response.setStatusReasonPhrase(message);
        response.setStatus(status);
        context.commitResponse(response);
    }

    /**
     * Invoked when data wrote has been fully written.
     * If we have scheduled closure after sending a final response, we will
     * be provided with the <code>CLOSE_MARKER</code> as our marker object.<br/>
     * This signals us to schedule closure of the connection
     *
     * @param message   The marker provided when writing data. If this is
     *                 our closure marker, we schedule closure of the connection
     */
    public void messageSent(Object message) {
    }

    /**
     * Sets the read idle time for all connections
     *
     * @param readIdleTime  The read idle time (seconds)
     */
    public void setReadIdleTime(int readIdleTime) {
        this.readIdleTime = readIdleTime;
    }

    protected HttpServiceContext createContext(HttpRequest request) {
        return new DefaultHttpServiceContext(request);
    }

    private class ContextConverter extends IoFilterAdapter {

        @Override
        public void filterWrite(NextFilter nextFilter, IoSession session,
                WriteRequest writeRequest) throws Exception {
            nextFilter.filterWrite(session, new DefaultWriteRequest(
                    ((HttpServiceContext) writeRequest.getMessage())
                            .getCommittedResponse(), writeRequest.getFuture()));
        }

        @Override
        public void messageReceived(NextFilter nextFilter, IoSession session,
                Object message) throws Exception {
            HttpRequest request = ( HttpRequest ) message;
            HttpServiceContext context = createContext(request);
            currentContext = context;
            nextFilter.messageReceived(session, context);
        }
    }

    private class ServiceFilterAdapter extends IoFilterAdapter {
        private final HttpServiceFilter filter;

        public ServiceFilterAdapter(HttpServiceFilter filter) {
            this.filter = filter;
        }

        @Override
        public void messageReceived(final NextFilter nextFilter,
                final IoSession session, final Object message) throws Exception {
            org.safehaus.asyncweb.service.HttpServiceFilter.NextFilter nextFilterAdapter = new org.safehaus.asyncweb.service.HttpServiceFilter.NextFilter() {
                public void invoke() {
                    nextFilter.messageReceived(session, message);
                }
            };
            filter.handleRequest(nextFilterAdapter,
                    (HttpServiceContext) message);
        }

        @Override
        public void filterWrite(final NextFilter nextFilter,
                final IoSession session, final WriteRequest writeRequest)
                throws Exception {
            org.safehaus.asyncweb.service.HttpServiceFilter.NextFilter nextFilterAdapter = new org.safehaus.asyncweb.service.HttpServiceFilter.NextFilter() {
                public void invoke() {
                    nextFilter.filterWrite(session, writeRequest);
                }
            };

            HttpServiceContext context = (HttpServiceContext) writeRequest
                    .getMessage();

            filter.handleResponse(nextFilterAdapter, context);
        }
    }

    private class RequestPipelineAdapter extends IoFilterAdapter {

        private final RequestPipeline pipeline;

        public RequestPipelineAdapter(final RequestPipeline pipeline) {
            this.pipeline = pipeline;
        }

        @Override
        public void sessionOpened(final NextFilter nextFilter,
                final IoSession session) {
            pipeline.setPipelineListener(new RequestPipelineListener() {
                public void responseReleased(HttpServiceContext context) {
                    nextFilter.filterWrite(session, new DefaultWriteRequest(
                            context, ((DefaultHttpServiceContext) context)
                                    .getWriteFuture()));
                }
            });

            nextFilter.sessionOpened(session);
        }

        @Override
        public void messageReceived(NextFilter nextFilter, IoSession session,
                Object message) throws Exception {
            HttpServiceContext context = (HttpServiceContext) message;
            if (pipeline.addRequest(context)) {
                LOG.debug("Allocated slot in request pipeline");
                nextFilter.messageReceived(session, message);
            } else {
                // The client has filled their pipeline. Currently, this
                // triggers closure. Another option would be to drop read interest
                // until we drain.
                LOG.warn("Could not allocate room in the pipeline for request");
                handleReadFailure(context,
                        HttpResponseStatus.SERVICE_UNAVAILABLE, "Pipeline full");
            }
        }

        @Override
        public void filterWrite(NextFilter nextFilter, IoSession session,
                WriteRequest writeRequest) throws Exception {
            DefaultHttpServiceContext context = (DefaultHttpServiceContext) writeRequest
                    .getMessage();
            context.setWriteFuture(writeRequest.getFuture());
            pipeline.releaseResponse(context);
            // nextFilter will be invoked when pipeline listener is notified.
        }
    }

    private class DefaultHttpServiceContext extends AbstractHttpServiceContext {
        private WriteFuture writeFuture;

        private DefaultHttpServiceContext(HttpRequest request) {
            super((InetSocketAddress) session.getRemoteAddress(), request,
                    container);
        }

        private WriteFuture getWriteFuture() {
            return writeFuture;
        }

        private void setWriteFuture(WriteFuture writeFuture) {
            if (!isResponseCommitted()) {
                throw new IllegalStateException();
            }
            this.writeFuture = writeFuture;
        }

        @Override
        protected void doWrite(boolean requiresClosure) {
            currentContext = null;
            WriteFuture future = session.write(this);
            if (requiresClosure) {
                LOG.debug("Added CLOSE future listener.");
                future.addListener(IoFutureListener.CLOSE);
            }
        }
    }
}
