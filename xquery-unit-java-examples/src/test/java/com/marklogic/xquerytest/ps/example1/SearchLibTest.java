package com.marklogic.xquerytest.ps.example1;

import org.jdom.Document;

import com.marklogic.ps.test.XQueryTestCase;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.types.XdmValue;

public class SearchLibTest extends XQueryTestCase {

	private String modulePath = "/example1/search-lib.xqy";
	private String moduleNamespace = "http://marklogic.com/search";

	public void testGetQuery() throws Exception {
		XdmValue[] params = new XdmValue[] { 
				ValueFactory.newXSString("foobar") };
		Document doc = executeLibraryModuleAsDocument(modulePath,
				moduleNamespace, "get-query", params);
		assertNotNull(doc);
	}
}
