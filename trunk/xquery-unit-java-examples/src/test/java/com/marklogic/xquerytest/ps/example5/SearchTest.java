package com.marklogic.xquerytest.ps.example5;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Text;
import org.jdom.xpath.XPath;

import com.marklogic.ps.test.XQueryXmlHttpTestCase;
import com.marklogic.ps.util.JDomUtils;

/**
 * This example is a continuation of example3.
 * 
 * @author mhelmstetter
 *
 */
public class SearchTest extends XQueryXmlHttpTestCase {
	protected void setUp() throws Exception {
		this.setServicePath("example5/search.xqy");
		super.setUp();
		insertTestContent("resource/medline1.xml", "/medline1.xml");
		insertTestContent("resource/medline2.xml", "/medline2.xml");
	}
	
	protected void tearDown() {
		
	}
	
	public void testSearch() throws Exception {
		// Build request and execute
		String req = "<request><search>cardiac</search></request>";
		Document doc = this.executeQueryAsDocument(req);
		
		// Use XPath to retrieve the response body, assert not null
		XPath xpath = XPath.newInstance("/response/s:response");
		xpath.addNamespace("s", "http://marklogic.com/appservices/search");
	    Element response = (Element) xpath.selectSingleNode(doc);
	    assertNotNull(response);
	    
	    // Use JDOM to assert the total number of results
	    int total = response.getAttribute("total").getIntValue();
	    assertEquals(1, total);
	    
	    // Execute more XPath to assert the highlighted keyword match
	    xpath = XPath.newInstance("s:result/s:snippet/s:match/s:highlight/text()");
	    xpath.addNamespace("s", "http://marklogic.com/appservices/search");
	    Text highlightText = (Text)xpath.selectSingleNode(response);
	    assertEquals("cardiac", highlightText.getText());
	}
	
	public void testSearch2() throws Exception {
		// Build request and execute
		String req = "<request><search>patient</search></request>";
		Document doc = this.executeQueryAsDocument(req);
		Document expected = getResourceAsDocument("resource/response1.xml");
		JDomUtils.assertJdomEquals(expected, doc);
	}
}
