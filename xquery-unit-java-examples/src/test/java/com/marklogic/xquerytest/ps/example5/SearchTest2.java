package com.marklogic.xquerytest.ps.example5;

import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;

import com.marklogic.ps.test.XQueryXmlHttpTestCase;
import com.marklogic.ps.util.JDomUtils;

/**
 * This example is a continuation of example3.
 * 
 * @author mhelmstetter
 *
 */
public class SearchTest2 extends XQueryXmlHttpTestCase {
	protected void setUp() throws Exception {
		this.setServicePath("example4/search.xqy");
		super.setUp();
		this.insertTestContent("resource/medline1.xml");
		this.insertTestContent("resource/medline2.xml");
	}
	
	
	
	public void testSearch2() throws Exception {
		String req = "<request><search>cardiac</search></request>";
		Document doc = JDomUtils.convertDocumentToDOM(executeQueryAsDocument(req));
		XObject response = XPathAPI.eval(doc, "/response/response");
		assertNotNull(response);
	}	
	
	@Override
	protected void tearDown() throws Exception {
		// TODO Auto-generated method stub
		super.tearDown();
	}
}
