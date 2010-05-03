package com.marklogic.ps.util;

import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.output.DOMOutputter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDOM Utlities
 * 
 * Portions of this class borrowed and adapted from ActiveMath:
 * http://www.activemath.org/Software/Download
 * 
 * @author mhelmstetter
 * 
 */
public class JDomUtils {
	
	protected static final Logger logger = LoggerFactory.getLogger( JDomUtils.class );

	public static org.w3c.dom.Document convertDocumentToDOM(Document jdomDoc) throws JDOMException {
		DOMOutputter outputter = new DOMOutputter();
		return outputter.output(jdomDoc);
	}

	public static String convertDocumentToString(Document doc) {
		Format format = Format.getRawFormat();
		format.setOmitDeclaration(true);
		return convertDocumentToString(doc, format);
	}
	
	public static String convertDocumentToString(Document doc, Format format) {
		XMLOutputter outputter = new XMLOutputter(format);
		return outputter.outputString(doc);
	}	

	public static void assertJdomEquals(Document expected, Document obtained)
			throws JDOMException {
		org.w3c.dom.Document expectedDom = convertDocumentToDOM(expected);
		org.w3c.dom.Document obtainedDom = convertDocumentToDOM(obtained);
		Diff diff = new Diff(expectedDom, obtainedDom);
		XMLUnit.setIgnoreWhitespace(true);
		// This might be useful at some point, but this seems to generate
		// "noise"
		// that we don't care about. Probably just need to read up on xmlunit.
		DetailedDiff detailedDiff = new DetailedDiff(diff);
		List l = detailedDiff.getAllDifferences();
		StringBuffer sb = new StringBuffer();
		diff.appendMessage(sb);
		logger.debug("diff output:" + sb.toString());
		XMLAssert.assertXMLEqual(expectedDom, obtainedDom);
	}

}
