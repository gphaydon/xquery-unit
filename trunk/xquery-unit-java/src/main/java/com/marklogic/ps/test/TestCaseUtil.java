package com.marklogic.ps.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import junit.framework.TestCase;

/**
 * Provides utility functions often needed when writing XQuery unit tests.
 */
public class TestCaseUtil {
    
    /**
     * Reads the content of the given resource file into a string.  The resource
     * must be in the "resource" subdirectory of the package of the concrete test
     * case.  For instance, if the test case is com.marklogic.foo.FooTestCase,
     * the resources must be in the com/marklogic/foo/resource directory
     * somewhere in the classpath.
     * @param filename the name of the file in the resource subdirectory
     * @return The contents of a file as a string
     * @throws Exception
     */
    public static String getResource(Class testCaseClass, String filename) throws Exception {
        InputStream is = getResourceAsInputStream(testCaseClass, filename);
        InputStreamReader in = new InputStreamReader(is, "UTF-8");
        StringWriter contentBuffer = new StringWriter();

        char[] buff = new char[1024];
        int len = 0;
        while ((len = in.read(buff)) > -1) {
            contentBuffer.write(buff, 0, len);
        }

        return contentBuffer.toString();
    }
    
    /**
     * Returns an InputStream corresponding to the specified resource.  The resource
     * must be in the "resource" sub-directory of the package of the concrete test
     * case.  For instance, if the test case is com.marklogic.foo.FooTestCase,
     * the resources must be in the com/marklogic/foo/resource directory
     * somewhere in the classpath.
     * @param filename the name of the file in the resource subdirectory
     * @return an InputStream for the resource
     * @throws IOException if an IOException occurs
     */
    public static InputStream getResourceAsInputStream(Class testCaseClass, String filename) throws IOException {
	URL contentUrl = testCaseClass.getResource("resource/" + filename);
        if ((contentUrl) == null)
            contentUrl = testCaseClass.getResource(filename);

        TestCase.assertNotNull("Unable to find resource " + filename, contentUrl);
        InputStream is = contentUrl.openStream();
        return is;
    }

    /**
     * Returns an InputStream for the first valid path specified in <code>loctions</code>
     * must be in the "resource" sub-directory of the package of the concrete test
     * case.  For instance, if the test case is com.marklogic.foo.FooTestCase,
     * the resources must be in the com/marklogic/foo/resource directory
     * somewhere in the classpath.
     * @return an InputStream for the resource
     * @throws IOException if an IOException occurs
     */
    public static InputStream getDefaultResourceAsInputStream(Class clazz, String[] locations)
    {
        InputStream is = null;
        String location = null;

        for (int i = 0; i < locations.length; i++)
        {
            location = locations[i];
            if (location == null) continue;

            is = clazz.getResourceAsStream(location);
            if (is != null) break;
        }

        return is;
    }
}
