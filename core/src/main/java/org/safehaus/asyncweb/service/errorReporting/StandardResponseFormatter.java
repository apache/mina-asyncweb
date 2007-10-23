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
package org.safehaus.asyncweb.service.errorReporting;

import java.io.UnsupportedEncodingException;

import org.apache.mina.common.ByteBuffer;
import org.safehaus.asyncweb.common.HttpRequest;
import org.safehaus.asyncweb.common.HttpResponseStatus;
import org.safehaus.asyncweb.common.MutableHttpResponse;
import org.safehaus.asyncweb.common.HttpResponseStatus.Category;
import org.safehaus.asyncweb.common.content.ByteBufferContent;
import org.safehaus.asyncweb.util.StringBundle;


public class StandardResponseFormatter implements ErrorResponseFormatter {

  private static final StringBundle bundle 
    = StringBundle.getBundle(StandardResponseFormatter.class.getPackage().getName());
  
  public void formatResponse(HttpRequest request, MutableHttpResponse response) {
    if (shouldFormat(response)) {
      doFormat(request, response);
      response.addHeader("content-type", "text/html");
    }
  }
  
  private boolean shouldFormat(MutableHttpResponse response) {
    boolean shouldFormat = false;
    // FIXME Should be able to handler other content types.
    if (!(response.getContent() instanceof ByteBufferContent)) {
      HttpResponseStatus status = response.getStatus();
      HttpResponseStatus.Category category = response.getStatus().getCategory();
      shouldFormat = status.allowsMessageBody() && 
                     (category == Category.CLIENT_ERROR || 
                      category == Category.SERVER_ERROR);
    }
    return shouldFormat;
  }

  private void doFormat(HttpRequest request, MutableHttpResponse response) {
    StringBuilder html = new StringBuilder(1024);
    html.append("<html><head><title>");
    html.append("AsyncWeb Server - ");
    html.append(bundle.getString("errorMessage"));
    html.append("</title><style><!--");
    CSS.appendTo(html).append("--></style>");
    html.append("</head></body>");
    html.append("<h1>");
    html.append(bundle.getString("errorTitle"));
    html.append("</h1>");
    response.getStatusReasonPhrase();
    String code = String.valueOf(response.getStatus().getCode());
    html.append("<h1>");
    html.append(bundle.getString("statusInfo", code));
    html.append("</h1>");
    html.append("<HR size=\"1\" noshade=\"noshade\">");
    
    html.append("<p><table cellpadding=\"5\">");
    appendInfo("statusCode", String.valueOf(response.getStatus().getCode()), html);
    appendInfo("description", getErrorMessage(response), html);
    appendInfo("requestMethod", request.getMethod().toString(), html);
    html.append("</table></p>");
    
    html.append("<HR size=\"1\" noshade=\"noshade\">");
    html.append("<H2>AsyncWeb Server</H2>");
    
    ByteBuffer out = ByteBuffer.allocate(html.length());

    // TODO: Need to sort this out when we start dealing with character encodings
    try {
      byte[] bytes = html.toString().getBytes("US-ASCII");
      out.put(bytes);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    
    out.flip();
    response.setContent(new ByteBufferContent(out));
  }
  
  private void appendInfo(String title, String info, StringBuilder html) {
    html.append("<tr><th>").append(bundle.getString(title)).append("</th>");
    html.append("<td>").append(info).append("</td>");
  }
  
  private String getErrorMessage(MutableHttpResponse response) {
    int responseCode = response.getStatus().getCode();
    String errorMessage = response.getStatusReasonPhrase();
    if (errorMessage == null) {
      errorMessage = "";
    }
    return bundle.getString("http." + responseCode, errorMessage);
  }
  
}
