package com.marklogic.ps.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.marklogic.ps.util.JDomUtils;
import com.marklogic.ps.util.Xcc;
import com.marklogic.xcc.Content;
import com.marklogic.xcc.ContentCreateOptions;
import com.marklogic.xcc.ContentFactory;
import com.marklogic.xcc.ContentSource;
import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.RequestOptions;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

/**
 * Utility base class for XQuery test cases.
 * 
 * @author Mark Helmstetter
 * @author James Kerr
 * @author Will LaForest
 */
public abstract class XQueryTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(XQueryTestCase.class);

    private Session session;
    private HashSet<URL> insertedDocuments = new HashSet<URL>();

    public static final String XQUERY_UNIT_TEST_COLLECTION = "xqueryUnitTest";
    private static final XMLOutputter OUTPUTTER = new XMLOutputter(Format.getPrettyFormat());

    private static final String[] DEFAULT_PROPS_LOCATIONS = { System.getProperty("marklogic.xdbc"),
            "/xdbc.properties", "resource/xdbc.properties", "resource/xdbc.props",
            "/service/resource/xdbc.properties", "/service/resource/xdbc.props",
            "/services/resource/xdbc.properties", "/services/resource/xdbc.props", "xdbc.props",
            "xdbc.properties" };

    public XQueryTestCase(String name) {

    }

    public XQueryTestCase() {
        super();
    }

    @Before
    public void setUp() throws Exception {

        Properties props = new Properties();
        InputStream is = TestCaseUtil.getDefaultResourceAsInputStream(this.getClass(),
                DEFAULT_PROPS_LOCATIONS);
        Class<?> c = this.getClass();
        if (is == null)
            throw new Exception("Unable to find XDBC properties file");
        props.load(is);

        // get this from resource properties
        String connectionUri = System.getProperty("marklogic.xdbc.connectionUri");
        if (connectionUri == null || connectionUri.isEmpty()) {
            connectionUri = props.getProperty("connectionUri");
        }
        URI uri = new URI(connectionUri);
        ContentSource cs = ContentSourceFactory.newContentSource(uri);
        session = cs.newSession();
    }

    @After
    public void tearDown() throws Exception {
        removeTestContent();

        if (session != null) {
            session.close();
        }
    }

    /**
     * Reads the content of the given resource file into a string. The resource must be in the
     * "resource" sub-directory of the package of the concrete test case. For instance, if the test
     * case is com.marklogic.foo.FooTestCase, the resources must be in the
     * com/marklogic/foo/resource directory somewhere in the classpath.
     * 
     * @param name
     *            the path of the resource relative to the class
     * @return the resource as a string
     * @throws Exception
     *             if a problem occurs
     */
    protected String getResource(String name) throws Exception {
        // TODO, this is kinda bad, most subclasses won't be in this package
        // we could cheat and use sun.reflect.Reflection.getCallerClass?
        return TestCaseUtil.getResource(this.getClass(), name);
    }

    /**
     * Reads the content of the given resource file into a string. The resource must be in the
     * "resource" sub-directory of the package of the concrete test case. For instance, if the test
     * case is com.marklogic.foo.FooTestCase, the resources must be in the
     * com/marklogic/foo/resource directory somewhere in the classpath.
     * 
     * @param name
     *            the path of the resource relative to the class
     * @return the resource as a string
     * @throws Exception
     *             if a problem occurs
     */
    protected String getResource(Class<?> c, String name) throws Exception {
        return TestCaseUtil.getResource(c, name);
    }

    /**
     * Reads the content of the given resource file into a JDOM Document. The resource must be well
     * formed XML or an exception will be thrown. The resource must be in the "resource"
     * sub-directory of the package of the concrete test case. For instance, if the test case is
     * com.marklogic.foo.FooTestCase, the resources must be in the com/marklogic/foo/resource
     * directory somewhere in the classpath.
     * 
     * @param name
     *            the path of the resource relative to the class
     * @return the resource as a string
     * @throws Exception
     *             if a problem occurs
     */
    protected Document getResourceAsDocument(String name) throws Exception {
        Reader reader = TestCaseUtil.getResourceReader(this.getClass(), name);
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = builder.build(reader);
        return doc;
    }

    protected org.w3c.dom.Document getResourceAsDomDocument(String name) throws Exception {
        Reader reader = TestCaseUtil.getResourceReader(this.getClass(), name);
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(new InputSource(reader));
        return doc;
    }

    /**
     * Returns an InputStream corresponding to the specified resource. The resource must be in the
     * "resource" subdirectory of the package of the concrete test case. For instance, if the test
     * case is com.marklogic.foo.FooTestCase, the resources must be in the
     * com/marklogic/foo/resource directory somewhere in the classpath.
     * 
     * @param name
     * @return
     * @throws IOException
     * @throws Exception
     */
    protected InputStream getResourceAsInputStream(String name) throws IOException {
        return TestCaseUtil.getResourceAsInputStream(this.getClass(), name);
    }

    /**
     * Gets the current XCC session that is setup by this test. This is useful if you need to do
     * some direct XCC work for your unit test.
     * 
     * @return a XCC Session object.
     */
    protected Session getSession() {
        return session;
    }

    protected void checkSession() throws XQueryTestCaseException {
        if (session == null) {
            throw new XQueryTestCaseException(
                    "Session is null, did you forget to call super.setUp() in your test case?");
        }
    }

    /**
     * Gets the set of documents that have been inserted for this test case.
     * 
     * @return a set containing the URIs for the documents that have so far been ingested for the
     *         test.
     */
    protected Set<URL> getInsertedDocuments() {
        return insertedDocuments;
    }

    /**
     * Executes the given adhoc query using the given options and variables
     * 
     * @param query
     *            A string containing valid XQuery
     * @param options
     *            XCC request options to use
     * @param variables
     *            any variables you need to bind to a variable marked external in the XQuery
     * @return XCC ResultSequence for the query
     * @throws Exception
     *             if exception occurrs
     */
    protected ResultSequence executeQuery(String query, RequestOptions options,
            XdmVariable... variables) throws Exception {
        checkSession();
        Request req = session.newAdhocQuery(query, options);

        if (variables != null) {
            for (XdmVariable v : variables) {
                if (v != null)
                    req.setVariable(v);
            }
        }
        return session.submitRequest(req);
    }

    /**
     * Executes the given adhoc query using the given options and variables
     * 
     * @param query
     *            A string containing valid XQuery
     * @param options
     *            XCC request options to use
     * @param variables
     *            any variables you need to bind to a variable marked external in the XQuery
     * @return A JDOM document for the results. This assumes the result of the query is a single
     *         element.
     * @throws Exception
     *             if exception occurrs
     */
    protected Document executeQueryAsDocument(String query, RequestOptions options,
            XdmVariable... variables) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("executeQueryAsDocument(): query: " + query);
        }
        ResultSequence rs = executeQuery(query, options, variables);
        Document result = null;
        if (rs.hasNext()) {
            InputStream is = rs.next().asInputStream();
            SAXBuilder builder = new SAXBuilder(false);
            result = builder.build(is);
        }
        if (logger.isDebugEnabled() && result != null) {
            logger.debug("executeQueryAsDocument(): response: "
                    + JDomUtils.convertDocumentToString(result, Format.getPrettyFormat()));
        }
        return result;
    }

    /**
     * Invokes the main module with the given name using the given request options and optional
     * variable list.
     * 
     * @param moduleName
     *            the path (relative to the xcc app server root) to a XQuery main module
     * @param options
     *            XCC request options to use
     * @param variables
     *            any variables you need to bind to a variable marked external in the XQuery
     * @return XCC ResultSequence for the query
     * @throws Exception
     */
    protected ResultSequence executeMainModule(String moduleName, RequestOptions options,
            XdmVariable... variables) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("executeMainModule(): " + moduleName);
        }
        checkSession();
        Request req = session.newModuleInvoke(moduleName);
        if (options != null) {
            req.setOptions(options);
        }

        if (variables != null) {
            for (XdmVariable v : variables) {
                req.setVariable(v);
            }
        }
        return session.submitRequest(req);
    }

    protected Document executeMainModuleAsDocument(String moduleName, RequestOptions options,
            XdmVariable... variables) throws Exception {
        ResultSequence rs = executeMainModule(moduleName, options, variables);
        Document result = null;
        if (rs.hasNext()) {
            InputStream is = rs.next().asInputStream();
            SAXBuilder builder = new SAXBuilder(false);
            result = builder.build(is);
        }
        if (logger.isDebugEnabled() && result != null) {
            logger.debug("executeMainModuleAsDocument(): response: "
                    + JDomUtils.convertDocumentToString(result));
        }
        return result;
    }

    /**
     * Executes a function in library module.
     * 
     * @param modulePath
     *            the path (relative to the xcc app server root) to a XQuery main module
     * @param namespace
     *            the namespace for the library module
     * @param function
     *            the functions local name
     * @param params
     *            the parameters for the function
     * @return XCC ResultSequence for the query
     * @throws RequestException
     */
    protected ResultSequence executeLibraryModule(String modulePath, String namespace,
            String function, XdmValue... params) throws RequestException {
        checkSession();
        ResultSequence rs = Xcc.invokeModuleFunction(session, modulePath, namespace, function,
                params);
        if (logger.isDebugEnabled()) {
            logger.debug("executeLibraryModule(): response: " + rs.asString());
        }
        return rs;
    }

    /**
     * Executes a function in library module.
     * 
     * @param modulePath
     *            the path (relative to the xcc app server root) to a XQuery main module
     * @param namespace
     *            the namespace for the library module
     * @param function
     *            the functions local name
     * @param params
     *            the parameters for the function
     * @return JDOM document for the results. Assumes the result of the function is a single
     *         element.
     * @throws RequestException
     */
    protected Document executeLibraryModuleAsDocument(String modulePath, String namespace,
            String function, XdmValue... params) throws RequestException, JDOMException,
            IOException {
        checkSession();
        ResultSequence rs = Xcc.invokeModuleFunction(session, modulePath, namespace, function,
                params);
        SAXBuilder builder = new SAXBuilder(false);
        Document doc = null;
        if (rs.hasNext()) {
            InputStream is = rs.next().asInputStream();
            doc = builder.build(is);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("executeLibraryModuleAsDocument(): response: "
                    + JDomUtils.convertDocumentToString(doc, Format.getPrettyFormat()));
        }
        if (rs.hasNext()) {
            logger.warn("executeLibraryModuleAsDocument(): Multiple results returned");
        }
        return doc;
    }

    protected void insertTestContent(String resourceLoc) throws IOException, RequestException {
        insertTestContent(resourceLoc, resourceLoc);
    }

    protected void insertTestContent(String resourceLoc, String uri) throws IOException,
            RequestException {
        URL contentUrl = this.getClass().getResource(resourceLoc);
        if (contentUrl == null) {
            throw new IOException("Resource " + resourceLoc + " not found.");
        }
        insertTestContent(uri, contentUrl);
    }

    protected void insertTestContent(String resourceLoc, String uri, List<String> collections)
            throws IOException, RequestException {
        URL contentUrl = this.getClass().getResource(resourceLoc);
        insertTestContent(uri, contentUrl, collections);
    }

    /**
     * Insert a document into the configured MarkLogic database. At tear down all documents inserted
     * will be removed. This is done by invoking the {@link #removeTestContent()} method.
     * Additionally all documents inserted will be added to a collection called
     * <code>xqueryUnitTest</code>
     * 
     * @param contentUrl
     *            URL to the content you want to insert.
     */
    protected void insertTestContent(URL contentUrl) throws IOException, RequestException {
        insertTestContent(contentUrl.toString(), contentUrl, null, null);
    }

    /**
     * Insert a document into the configured MarkLogic database. At tear down all documents inserted
     * will be removed. This is done by invoking the {@link #removeTestContent()} method.
     * Additionally all documents inserted will be added to a collection called
     * <code>xqueryUnitTest</code>
     * 
     * @param contentUrl
     *            URL to the content you want to insert.
     * @param forestName
     *            an optional forest name that indicates what forest to insert the content into
     */
    protected void insertTestContent(URL contentUrl, String forestName) throws IOException,
            RequestException {
        insertTestContent(contentUrl.toString(), contentUrl, null, forestName);
    }

    /**
     * Insert a document into the configured MarkLogic database. At tear down all documents inserted
     * will be removed. This is done by invoking the {@link #removeTestContent()} method.
     * Additionally all documents inserted will be added to a collection called
     * <code>xqueryUnitTest</code>
     * 
     * @param contentUrl
     *            URL to the content you want to insert.
     * @param uri
     *            the URI which should be used for the document in the repository
     */
    protected void insertTestContent(String uri, URL contentUrl) throws IOException,
            RequestException, XQueryTestCaseException, XQueryTestCaseException {
        insertTestContent(uri, contentUrl, null, null);
    }

    /**
     * Insert a document into the configured MarkLogic database. At tear down all documents inserted
     * will be removed. This is done by invoking the {@link #removeTestContent()} method.
     * Additionally all documents inserted will be added to a collection called
     * <code>xqueryUnitTest</code>
     * 
     * @param contentUrl
     *            URL to the content you want to insert.
     * @param uri
     *            the URI which should be used for the document in the repository
     * @param forestName
     *            an optional forest name that indicates what forest to insert the content into
     */
    protected void insertTestContent(String uri, URL contentUrl, String forestName)
            throws IOException, RequestException, XQueryTestCaseException {
        insertTestContent(uri, contentUrl, null, forestName);
    }

    /**
     * Insert a document into the configured MarkLogic database. At tear down all documents inserted
     * will be removed. This is done by invoking the {@link #removeTestContent()} method.
     * Additionally all documents inserted will be added to a collection called
     * <code>xqueryUnitTest</code>
     * 
     * @param contentUrl
     *            URL to the content you want to insert.
     * @param uri
     *            the URI which should be used for the document in the repository
     * @param collections
     *            a list of the collections that the content should be added to. <code>null</code>
     *            or empty equates to no collections.
     */
    protected void insertTestContent(String uri, URL contentUrl, List<String> collections)
            throws IOException, RequestException, XQueryTestCaseException {
        insertTestContent(uri, contentUrl, collections, null);
    }

    /**
     * Insert a document into the configured MarkLogic database. At tear down all documents inserted
     * will be removed. This is done by invoking the {@link #removeTestContent()} method.
     * Additionally all documents inserted will be added to a collection called
     * <code>xqueryUnitTest</code>
     * 
     * @param contentUrl
     *            URL to the content you want to insert.
     * @param uri
     *            the URI which should be used for the document in the repository
     * @param collections
     *            a list of the collections that the content should be added to. <code>null</code>
     *            or empty equates to no
     * @param forestName
     *            an optional forest name that indicates what forest to insert the content into
     *            collections.
     */
    protected void insertTestContent(String uri, URL contentUrl, List<String> collections,
            String forestName) throws IOException, RequestException, XQueryTestCaseException {

        if (uri == null) {
            throw new IllegalArgumentException("URI cannot be null");
        }
        if (contentUrl == null) {
            throw new IllegalArgumentException("contentUrl cannot be null");
        }
        checkSession();

        insertedDocuments.add(contentUrl);
        ContentCreateOptions options = new ContentCreateOptions();
        options.setFormatXml();

        // get the place key if a forest name is specified
        if (forestName != null) {
            try {
                options.setPlaceKeys(new BigInteger[] { new BigInteger(this.executeQuery(
                        "xdmp:forest('" + forestName + "')", null, (XdmVariable) null).asString()) });
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error parsing forest place key", e);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error retrieving forest place key from name: "
                        + forestName, e);
            }
        }

        if (collections != null && collections.size() > 0) {
            String[] collectionStrings = new String[collections.size() + 1];
            for (int i = 0; i < collections.size(); i++) {
                collectionStrings[i] = collections.get(i);

            }
            collectionStrings[collectionStrings.length - 1] = XQUERY_UNIT_TEST_COLLECTION;
            options.setCollections(collectionStrings);
        } else {
            options.setCollections(new String[] { XQUERY_UNIT_TEST_COLLECTION });
        }

        Content content = ContentFactory.newContent(uri, contentUrl, options);
        session.insertContent(content);
    }

    /**
     * Remove all documents that were inserted with the {@link #insertedDocuments} method.
     * 
     * @throws Exception
     */
    protected void removeTestContent() throws Exception {
        if (insertedDocuments.size() < 1)
            return;

        String removeQuery = " xdmp:collection-delete(\"" + XQUERY_UNIT_TEST_COLLECTION + "\")";
        executeQuery(removeQuery, null, (XdmVariable[]) null);
        insertedDocuments.clear();
    }

    /**
     * Remove all documents. This is currently done in a single transaction so if there are a lot of
     * documents in the repository there is a chance this will time out.
     * 
     * @throws Exception
     */
    protected void removeAllContent() throws Exception {
        String query = "for $u in cts:uris('', ('document')) return xdmp:document-delete($u)";
        executeQuery(query, null, (XdmVariable[]) null);
        insertedDocuments.clear();
    }

    /**
     * Quote the Document. Takes a JDOM document and returns a serialized version in a string.
     * 
     * @param document
     *            the <code>Document</code> to quote.
     * @return a <code>String</code> value.
     */
    public static String quote(Document document) throws IOException {
        StringWriter sw = new StringWriter();
        OUTPUTTER.output(document, sw);
        return sw.toString();
    }

    /**
     * Unquote the string into a <code>Document</code>.
     * 
     * @param xml
     *            the <code>Document</code> to quote.
     * @return a <code>String</code> value.
     */
    public static Document unquote(String xml) throws IOException, JDOMException {
        SAXBuilder builder = new SAXBuilder(false);
        return builder.build(new StringReader(xml));
    }
}
