package com.marklogic.xquerytest.ps.junit4examples;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.xpath.XPath;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.marklogic.ps.test.XQueryXmlHttpTestCase;

@RunWith(JUnit4.class)
public class SearchTest extends XQueryXmlHttpTestCase {
	@Before
	public void setUp() throws Exception {
		this.setServicePath("example3/search.xqy");
		super.setUp();
	}
	@Test
	public void search() throws Exception {
		String req = "<request><search/></request>";
		Document doc = this.executeQueryAsDocument(req);
		XPath xpath = XPath.newInstance("/response");
	    Element e = (Element) xpath.selectSingleNode(doc);
	    assertNotNull(e);
	}
//	@Test
//	public void testInvalidRequest() throws Exception {
//		String req = "<request><XXX/></request>";
//		Document doc = this.executeQueryAsDocument(req);
//	}	
}
