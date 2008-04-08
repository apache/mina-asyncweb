package org.apache.asyncweb.client.proxy;

import org.apache.asyncweb.client.AsyncHttpClient;
import org.apache.asyncweb.client.auth.UsernamePasswordCredentials;
import org.apache.asyncweb.client.codec.HttpIoHandler;
import org.apache.asyncweb.client.codec.HttpRequestMessage;
import org.apache.asyncweb.client.codec.HttpResponseMessage;
import org.apache.mina.common.IoFilterAdapter;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.SSLFilter;

public class ProxyFilter extends IoFilterAdapter {
    public static final String PROXY_AUTHORIZATION_HEADER = "Proxy-Authorization";
    
    private final SSLFilter sslFilter;

    /**
     * Create a proxy filter for a plain (http request) 
     * to a proxy server. 
     */
    public ProxyFilter() {
        this(null);
    }
    
    /**
     * Create a proxy filter that works in conjunction with 
     * an SSLFilter for a secure connection.
     * 
     * @param sslFilter The SSL filter to use for the secure connection.  If
     *                  null, a plain connection will be used.
     */
    public ProxyFilter(SSLFilter sslFilter) {
        this.sslFilter = sslFilter;
    }
    
    @Override
    /**
     * Process a message send event to the proxy.  This 
     * will add any required proxy authentication headers
     * to the request. 
     * 
     * @param nextFilter
     * @param session
     * @param message
     * 
     * @exception Exception
     */
    public void messageSent(NextFilter nextFilter, IoSession session,
            Object message) throws Exception {
        HttpRequestMessage request = (HttpRequestMessage)message;
        ProxyConfiguration proxyConfig = request.getProxyConfiguration();
        if (proxyConfig != null && 
                proxyConfig.getProxyUser() != null && 
                proxyConfig.getProxyPassword() != null &&
                proxyConfig.getAuthScheme() != null) { // can proxy config ever be null?
            // add the proxy authorization header
            UsernamePasswordCredentials cred = 
                    new UsernamePasswordCredentials(proxyConfig.getProxyUser(), 
                            proxyConfig.getProxyPassword());
            String authHeader = proxyConfig.getAuthScheme().authenticate(cred, request);
            request.setHeader(PROXY_AUTHORIZATION_HEADER, authHeader);
        }
        // always forward
        super.messageSent(nextFilter, session, message);
    }

    /**
     * Returns the HttpRequestMessage instance currently 
     * being processed by the IoSession.
     * 
     * @param session The current session using the filter.
     * 
     * @return The request message being processed. 
     */
    private HttpRequestMessage getRequest(IoSession session) {
        HttpRequestMessage request = 
                (HttpRequestMessage)session.getAttribute(HttpIoHandler.CURRENT_REQUEST);
        return request;
    }

    @Override
    /**
     * Process the messageReceived() filter response.  this
     * will handle any proxy handshaking and SSL connection
     * creation required.
     * 
     * @param nextFilter The next filter in the chain.
     * @param session    The IoSession this operation is associated with.
     * @param message    The message object under construction.
     * 
     * @exception Exception
     */
    public void messageReceived(NextFilter nextFilter, IoSession session,
            Object message) throws Exception {
        if (connectInProgress(session)) {
            // we need to complete the connect handshake
            handleConnectResponse(session, message);
        } else {
            // the connection is established, just allow this 
            // to flow down the chain. 
            super.messageReceived(nextFilter, session, message);
        }
    }
    
    /**
     * Tests if we are performing the connect handshake for SSL tunneling.  If 
     * this is an https request, we need to handle the tunneling here.  If we've 
     * already established this, then the processing can continue with the next 
     * filter. 
     * 
     * @return true if the connect handshake is in progress.
     */
    private boolean connectInProgress(IoSession session) {
        return session.getAttribute(HttpIoHandler.PROXY_CONNECT_IN_PROGRESS) != null;
    }

    /**
     * Handle the response from a CONNECT request sent to 
     * the proxy server.  If we got a good CONNECT request 
     * back, we add the SSL filter to the chain and 
     * write the original request back to the proxy 
     * server. 
     * 
     * @param session The current session.
     * @param message The message object representing the request.
     */
    private void handleConnectResponse(IoSession session, Object message) {
        HttpResponseMessage response = (HttpResponseMessage)message;
        int status = response.getStatusCode();
        if (status == 200) {
            // layer the SSL socket by inserting the SSL filter
            session.getFilterChain().addBefore(AsyncHttpClient.PROTOCOL_FILTER, "SSL", sslFilter);
            // handshake is done
            session.removeAttribute(HttpIoHandler.PROXY_CONNECT_IN_PROGRESS);
            HttpRequestMessage request = getRequest(session);
            // write the original request intended for the remote target
            session.write(request);
        } else {
            session.close();
        }
    }
}
