package com.marklogic.xquerytest.ps.example3;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;

import com.marklogic.ps.test.XQueryXmlHttpTestCase;

public class SearchTest extends XQueryXmlHttpTestCase {
	protected void setUp() throws Exception {
		this.setServicePath("example3/search.xqy");
		super.setUp();
	}
	public void testSearch() throws Exception {
		String req = "<request><search/></request>";
		Document doc = this.executeQueryAsDocument(req);
		XPath xpath = XPath.newInstance("/response");
	    Element e = (Element) xpath.selectSingleNode(doc);
	    assertNotNull(e);
	}
	public void testInvalidRequest() throws Exception {
		String req = "<request><XXX/></request>";
		Document doc = this.executeQueryAsDocument(req);
		XPath xpath = XPath.newInstance("/error");
	    Element e = (Element) xpath.selectSingleNode(doc);
	    assertNotNull(e);
	}
}
