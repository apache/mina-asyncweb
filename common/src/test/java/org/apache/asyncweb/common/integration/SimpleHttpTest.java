package org.apache.asyncweb.common.integration;

import java.nio.charset.Charset;

import org.apache.asyncweb.common.DefaultHttpRequest;
import org.apache.asyncweb.common.HttpResponse;
import org.apache.asyncweb.common.HttpResponseStatus;
import org.apache.asyncweb.common.MutableHttpRequest;

public class SimpleHttpTest extends TomcatTest {

    public void testHelloWorld() throws Exception {
        // Send request
        MutableHttpRequest request = new DefaultHttpRequest();
        request.setRequestUri(getBaseURI().resolve("/helloworld.jsp"));
        request.normalize();
        session.write(request);
        
        // Wait for response
        HttpResponse response = (HttpResponse) session.read().await().getMessage();
        
        // Test response
        assertEquals(HttpResponseStatus.OK, response.getStatus());
        assertEquals(ResponseOutput.HELLO_WORLD, response.getContent().getString(Charset.defaultCharset().newDecoder()));
    }
    
}
