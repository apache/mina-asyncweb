package org.apache.asyncweb.client;

import java.net.URL;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.asyncweb.client.codec.Cookie;
import org.apache.asyncweb.client.codec.HttpDecoder;
import org.apache.asyncweb.client.codec.HttpIoHandler;
import org.apache.asyncweb.client.codec.HttpRequestMessage;
import org.apache.asyncweb.client.codec.HttpResponseDecoder;
import org.apache.asyncweb.client.codec.HttpResponseMessage;
import org.apache.asyncweb.client.util.DateUtil;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;

public class ResponseHeaderParsingTest extends TestCase {
    private static final String TEST_RESPONSE =
        "HTTP/1.1 200 OK\r\n" 
            + "Date: Fri, 15 Feb 2008 20:00:00 GMT\r\n"
            + "Server:foo-bar\r\n"
            + "Content-Type: text/html\r\n"
            + "Content-Length: 13\r\n"
            + "Connection:             close       \r\n"
            + "Private-Header: test-continue\r\n"
            + "\tit-keeps-going\r\n"
            + " and-going-and-going\r\n"
            + "\r\n"
            + "<html></html>";
    private static final String EMPTY_VALUE_COOKIE = "token=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT";

    public void testParsing() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(TEST_RESPONSE.length());
        buffer.put(TEST_RESPONSE.getBytes());
        buffer.flip();

        HttpRequestMessage request = new HttpRequestMessage(null, null);
        IoSession session = new FakeIoSession();
        session.setAttribute(HttpIoHandler.CURRENT_REQUEST, request);
        HttpResponseDecoder decoder = new HttpResponseDecoder();
        FakeProtocolDecoderOutput out = new FakeProtocolDecoderOutput();
        decoder.decode(session, buffer, out);

        HttpResponseMessage response = (HttpResponseMessage)out.getObject();
        assertEquals("Fri, 15 Feb 2008 20:00:00 GMT", response.getHeader("date"));
        assertEquals("foo-bar", response.getHeader("server"));
        assertEquals("text/html", response.getHeader("content-type"));
        assertEquals("13", response.getHeader("content-length"));
        assertEquals("close", response.getHeader("connection"));
        assertEquals("test-continue\tit-keeps-going and-going-and-going", response.getHeader("private-header"));
        
        assertTrue(Arrays.equals(response.getContent(), "<html></html>".getBytes()));
    }

    public void testParsingOfAnEmptyCookieValue() throws Exception {
        HttpResponseMessage msg = new HttpResponseMessage(new URL("http://www.foo.com"));
        HttpDecoder decoder = new HttpDecoder();
        Cookie c = decoder.decodeCookie(EMPTY_VALUE_COOKIE, msg);

        assertNotNull(c);
        assertEquals( "token", c.getName());
        assertNotNull(c.getValue());
        assertTrue(c.getValue().length() == 0);
        assertEquals("/", c.getPath());
        assertEquals(DateUtil.parseDate("Thu, 01 Jan 1970 00:00:00 GMT"), c.getExpires());
    }
}
