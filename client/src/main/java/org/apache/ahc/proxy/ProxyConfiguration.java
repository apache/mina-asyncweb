package org.apache.ahc.proxy;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.ahc.auth.AuthScheme;

public class ProxyConfiguration {
    private final String httpProxyHost;
    private final int httpProxyPort;
    private final String httpsProxyHost;
    private final int httpsProxyPort;
    private String exclusionList = null;
    private String proxyUser = null;
    private String proxyPassword = null;
    private AuthScheme scheme = null;
    private List<String> wildCardExclusions = new ArrayList<String>(); 
    private Map<String,String> directExclusions = new HashMap<String,String>(); 
    
    /**
     * Construct a ProxyConfiguration instance.
     * 
     * @param proxyHost The host to be used for both http and https connections.
     * @param proxyPort The port to be used for both http and https connections.
     */
    public ProxyConfiguration(String proxyHost, int proxyPort) {
        this.httpProxyHost = proxyHost;
        this.httpProxyPort = proxyPort;
        this.httpsProxyHost = proxyHost;
        this.httpsProxyPort = proxyPort;
    }
    
    /**
     * Construct a proxy configuration that uses separate 
     * http and https proxy targets.
     * 
     * @param httpProxyHost
     *               The host to be used for http requests.
     * @param httpProxyPort
     *               The port to be used for http requests.
     * @param httpsProxyHost
     *               The host to use for https requests.
     * @param httpsProxyPort
     */
    public ProxyConfiguration(String httpProxyHost, int httpProxyPort, String httpsProxyHost, int httpsProxyPort) {
        this.httpProxyHost = httpProxyHost;
        this.httpProxyPort = httpProxyPort;
        this.httpsProxyHost = httpsProxyHost;
        this.httpsProxyPort = httpsProxyPort;
    }
    
    
    /**
     * Get the target connection for a proxied request. 
     * The target will depend on whether this is an http 
     * or an https request.
     * 
     * @param target The target URL
     * 
     * @return An InetSocketAddress for the appropriate proxy target. 
     */
    public InetSocketAddress getProxyAddress(URL target) 
    {
        if (target.getProtocol().equalsIgnoreCase("https")) {
            return new InetSocketAddress(getHttpsProxyHost(), getHttpsProxyPort()); 
        }
        else { 
            return new InetSocketAddress(getHttpProxyHost(), getHttpProxyPort()); 
        }
    }
    
    /**
     * Get the proxy host name for http connections.
     * 
     * @return The string name of the proxy host. 
     */
    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    /**
     * Get the port of the proxy server used for servicing 
     * http requests.
     * 
     * @return The port number of the configured http proxy server.
     */
    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    /**
     * Get the host of the proxy server for handling 
     * https requests.  If not explicitly set this defaults 
     * to the http proxy host.
     * 
     * @return The string name of the proxy host. 
     */
    public String getHttpsProxyHost() {
        return httpsProxyHost;
    }

    /**
     * Get the port used to connect to the https proxy 
     * server.  If not explictly set, this is the same as the 
     * http server.
     * 
     * @return The connection port number for handling https requests.
     */
    public int getHttpsProxyPort() {
        return httpsProxyPort;
    }

    /**
     * Retrieve the exclusion list used for this configuration.
     * If set, this returns a string containing the 
     * individual exclusion domains separated by ";".
     * 
     * @return The string value of the exclusion list, or null 
     *         if this has not been set.
     */
    public String getExclusionList() {
        return exclusionList;
    }

    /**
     * Set the exclusion list for the proxy configuration.
     * The exclusion list is a set of explicit hosts and/or
     * wildcard domains ("*.apache.org") separated by ";". 
     * 
     * @param exclusionList
     */
    public void setExclusionList(String exclusionList) {
        this.exclusionList = exclusionList;
        // we clear these out regardless 
        wildCardExclusions.clear(); 
        directExclusions.clear(); 
        
        if (exclusionList != null) {
            // now divide the exclusion list into the explict and wildcard lists 
            StringTokenizer tokenizer = new StringTokenizer(exclusionList, ";"); 
            while (tokenizer.hasMoreTokens()) {
                String domain = tokenizer.nextToken(); 
                // wild card versions we just create a matching list that we run through 
                if (domain.startsWith("*.")) {
                    wildCardExclusions.add(domain.substring(1)); 
                }
                else {
                    // the direct exlusions are names we can look up via a map. 
                    directExclusions.put(domain, domain); 
                }
            }
        }
    }

    /**
     * Returns the proxy authentication userid.
     * 
     * @return The name of the authentication userid.  Returns null 
     *         if no user has been specified.
     */
    public String getProxyUser() {
        return proxyUser;
    }

    /**
     * Set the userid used for proxy server authentication.
     * 
     * @param proxyUser The userid to be used by the authentication schema.
     */
    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    /**
     * Returns the configured password used to access the 
     * proxy server.
     * 
     * @return The configured password.  Returns null if no password is 
     *         set.
     */
    public String getProxyPassword() {
        return proxyPassword;
    }

    /**
     * Set the password to be used for accessing the 
     * proxy server.
     * 
     * @param proxyPassword
     *               The password to be used for authentication.
     */
    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }
    
    /**
     * Returns the authentication scheme used for logging 
     * in the proxy server. 
     * 
     * @return The configured authentication scheme.  Returns null 
     *         if one has not been set.
     */
    public AuthScheme getAuthScheme() {
        return scheme;
    }
    
    /**
     * Set the authentication scheme to be used for logging 
     * in the proxy server.
     * 
     * @param scheme The scheme to be used for logging in.  If null,
     *               no login will be attempted with the proxy server.
     */
    public void setAuthScheme(AuthScheme scheme) {
        this.scheme = scheme;
    }
    
    /**
     * Tests if the host in a target URL is specified in 
     * the proxy configuration exclusion list.
     * 
     * @param target The target URL of the connection.
     * 
     * @return true if the host is included in the configuration 
     *         exclusion list.  false indicates the connection
     *         needs to go through the proxy server.
     */
    public boolean isExcluded(URL target) {
        String host = target.getHost(); 
        
        // if the host is explicitly listed, this is easy 
        if (directExclusions.get(host) != null) {
            return true; 
        }
        // the wildcard elements are stored as ".apache.org", so 
        // a simple endsWith() test will gives a match on something 
        // like "people.apache.org". 
        for (String domain : wildCardExclusions) {
            if (host.endsWith(domain)) {
                return true; 
            }
        }
        // not found in any host 
        return false; 
    }
}
