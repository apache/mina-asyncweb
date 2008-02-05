package org.apache.ahc;

import java.net.URL;
import java.util.concurrent.Future;

import org.apache.ahc.codec.HttpRequestMessage;
import org.apache.ahc.codec.HttpResponseMessage;
import org.apache.ahc.proxy.ProxyConfiguration;

public class ProxyTest extends AbstractTest {
    public void testHttpProxy() throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        
        HttpRequestMessage request = 
                new HttpRequestMessage(new URL("http://localhost:8282/"), null);
        ProxyConfiguration config = new ProxyConfiguration("localhost", 8888);
        request.setProxyConfiguration(config);
        
        Future<HttpResponseMessage> future = ahc.sendRequest(request);
        
        HttpResponseMessage response = future.get();
        assertEquals("Hello World!", response.getStringContent());
    }
    
    
    public void testHttpProxyIP() throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        
        HttpRequestMessage request = 
                new HttpRequestMessage(new URL("http://127.0.0.1:8282/"), null);
        ProxyConfiguration config = new ProxyConfiguration("localhost", 8888);
        request.setProxyConfiguration(config);
        
        Future<HttpResponseMessage> future = ahc.sendRequest(request);
        
        HttpResponseMessage response = future.get();
        assertEquals("Hello World!", response.getStringContent());
    }
    
    public void testHttpsProxySame() throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        
        HttpRequestMessage request = 
                new HttpRequestMessage(new URL("https://localhost:8383/"), null);
        ProxyConfiguration config = new ProxyConfiguration("localhost", 8888);
        request.setProxyConfiguration(config);
        
        Future<HttpResponseMessage> future = ahc.sendRequest(request);
        
        HttpResponseMessage response = future.get();
        assertEquals("Hello World!", response.getStringContent());
    }
    
    
    public void testHttpsProxyDifferent() throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        
        HttpRequestMessage request = 
                new HttpRequestMessage(new URL("https://localhost:8383/"), null);
        ProxyConfiguration config = new ProxyConfiguration("people.apache.org", 8889, "localhost", 8888);
        request.setProxyConfiguration(config);
        
        Future<HttpResponseMessage> future = ahc.sendRequest(request);
        
        HttpResponseMessage response = future.get();
        assertEquals("Hello World!", response.getStringContent());
    }
    
    public void testHttpExclusion() throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        
        HttpRequestMessage request = 
                new HttpRequestMessage(new URL("http://localhost:8282/"), null);
        // NOTE:  The proxy server config is invalid, so this will fail if the 
        // exclusion doesn't work. 
        ProxyConfiguration config = new ProxyConfiguration("localhost", 8889);
        config.setExclusionList("localhost");
        request.setProxyConfiguration(config);
        
        Future<HttpResponseMessage> future = ahc.sendRequest(request);
        
        HttpResponseMessage response = future.get();
        assertEquals("Hello World!", response.getStringContent().trim());
    }
    
    public void testHttpExclusionIP() throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        
        HttpRequestMessage request = 
                new HttpRequestMessage(new URL("http://127.0.0.1:8282/"), null);
        // NOTE:  The proxy server config is invalid, so this will fail if the 
        // exclusion doesn't work. 
        ProxyConfiguration config = new ProxyConfiguration("localhost", 8889);
        config.setExclusionList("127.0.0.1;*.apache.org");
        request.setProxyConfiguration(config);
        
        Future<HttpResponseMessage> future = ahc.sendRequest(request);
        
        HttpResponseMessage response = future.get();
        assertEquals("Hello World!", response.getStringContent().trim());
    }
    
    public void testHttpsExclusion() throws Exception {
        AsyncHttpClient ahc = new AsyncHttpClient();
        
        HttpRequestMessage request = 
                new HttpRequestMessage(new URL("https://localhost:8383/"), null);
        ProxyConfiguration config = new ProxyConfiguration("localhost", 8889);
        config.setExclusionList("localhost;*.apache.org");
        request.setProxyConfiguration(config);
        
        Future<HttpResponseMessage> future = ahc.sendRequest(request);
        
        HttpResponseMessage response = future.get();
        assertEquals("Hello World!", response.getStringContent().trim());
    }
}
