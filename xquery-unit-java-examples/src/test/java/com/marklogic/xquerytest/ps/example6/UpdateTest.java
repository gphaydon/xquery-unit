package com.marklogic.xquerytest.ps.example6;

import com.marklogic.ps.test.XQueryTestCase;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmVariable;

/**
 * This example is a continuation of example3.
 * 
 * @author mhelmstetter
 *
 */
public class UpdateTest extends XQueryTestCase {
	private String docUri = "/medline1.xml";
	protected void setUp() throws Exception {
		super.setUp();
		this.insertTestContent("resource/medline1.xml", docUri);
	}
	
	public void testUpdate() throws Exception {
		String newTitle = "NEW_TITLE";
		XdmVariable[] variables = new XdmVariable[] { 
	      ValueFactory.newVariable(new XName("uri"), 
		    ValueFactory.newXSString(docUri)),
		  ValueFactory.newVariable(new XName("new-title"), 
			ValueFactory.newXSString(newTitle))};
		this.executeMainModule("example6/update.xqy", null, variables);
		
		// Verify update by running a query
		String q = "fn:doc('/medline1.xml')"
			+ "/MedlineCitation/Article/ArticleTitle/text()";
		ResultSequence rs = executeQuery(q, null, null);
		String updatedTitle = rs.asString();
		assertEquals(newTitle, updatedTitle);
	}
}
