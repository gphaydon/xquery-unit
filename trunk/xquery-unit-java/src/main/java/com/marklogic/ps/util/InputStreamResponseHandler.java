
package com.marklogic.ps.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


/**
 * 
 * @author Mark Helmstetter
 *
 */
public class InputStreamResponseHandler implements ResponseHandler<InputStream> {

	public InputStream handleResponse(HttpResponse response)
			throws ClientProtocolException, IOException {
		StatusLine statusLine = response.getStatusLine();
        if (statusLine.getStatusCode() >= 300) {
    		String body = getResponseBody(response);
            throw new HttpResponseException(statusLine.getStatusCode(), body);
        }

        HttpEntity entity = response.getEntity();
        return entity == null ? null : entity.getContent();
	}
	
	private String getResponseBody(HttpResponse response) {
		String message = null;
		try {
			StatusLine statusLine = response.getStatusLine();
			HttpEntity e = response.getEntity();
			String responseHtml = EntityUtils.toString(e);
			if (responseHtml.contains("<html")) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	            DocumentBuilder builder = factory.newDocumentBuilder();
	            Document document = builder.parse(new InputSource(new StringReader(responseHtml)));    
	            String xpath = "/html/body";
				XObject select = XPathAPI.eval(document, xpath);
				String result = select.toString().trim();
				message = (result == null) ? statusLine.getReasonPhrase() : result;
			}
		} catch (Exception ex) {
		} 
		return message;
	}

}
