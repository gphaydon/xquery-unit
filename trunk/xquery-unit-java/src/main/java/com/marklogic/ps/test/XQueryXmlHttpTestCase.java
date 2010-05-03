package com.marklogic.ps.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XObject;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

import com.marklogic.ps.util.JDomUtils;

/**
 * Base class for testing XML HTTP services built ontop of xquery. Tests that
 * extend this depend upon a MarkLogic server instance having been setup and the
 * xquery code having been installed at a location that will be accessed using
 * an HTTP server. The URL for the HTTP server is specified in a configuration
 * properties file. The property file is specified with the enviromental
 * variable <code>marklogic.xmlhttp.http</code>. If the environmental variable
 * is not specified it will default to looking for the properties file in
 * several locations including
 * 
 * <ul>
 * <li>/xmlHttpConnection.properties</li>
 * <li>resource/xmlHttpConnection.properties</li>
 * <li>resource/xmlHttpConnection.props</li>
 * <li>/service/resource/xmlHttpConnection.properties</li>
 * <li>/service/resource/xmlHttpConnection.props</li>
 * <li>/services/resource/xmlHttpConnection.properties</li>
 * <li>/services/resource/xmlHttpConnection.props</li>
 * <li>xmlHttpConnection.properties</li>
 * <li>xmlHttpConnection.props</li>
 * </ul>
 * 
 * The <code>connectionUrl</code> property is required and should be the URL for
 * the service or service base (see below). The<code>username</code> and
 * <code>password</code> properties are optional and if not supplied default to
 * <code>admin</code>/<code>admin</code>.
 * 
 * If <code>connectionUrl</code> points directly to the service than nothing
 * more needs to be done for testing by the concrete class. Frequently, though,
 * XML HTTP unit tests will share the connection URL across different services
 * in which case the connectionUrl can be thought off as the base to which we
 * will add the specific <code>servicePath</code> .
 * 
 * @author Will LaForest
 */
public abstract class XQueryXmlHttpTestCase extends XQueryTestCase {

	private static final String[] DEFAULT_PROPS_LOCATIONS = {
			System.getProperty("marklogic.xmlhttp.http"),
			"/xmlHttpConnection.properties",
			"resource/xmlHttpConnection.properties",
			"resource/xmlHttpConnection.props",
			"/service/resource/xmlHttpConnection.properties",
			"/service/resource/xmlHttpConnection.props",
			"/services/resource/xmlHttpConnection.properties",
			"/services/resource/xmlHttpConnection.props",
			"xmlHttpConnection.properties", "xmlHttpConnection.props" };

	protected String connectionUrl;
	protected static String username;
	protected static String password;
	protected String servicePath;

	private static Properties connectionProps;
	private String serviceUrl;

	public XQueryXmlHttpTestCase(String name) {
		super(name);
	}

	public XQueryXmlHttpTestCase() {
		super();
	}

	static {
		loadProps(com.marklogic.ps.test.XQueryXmlHttpTestCase.class);
	}

	private final static void loadProps(Class clazz) {
		String propsLocation = null;
		InputStream is = TestCaseUtil.getDefaultResourceAsInputStream(clazz,
				DEFAULT_PROPS_LOCATIONS);

		if (is == null)
			throw new RuntimeException(
					"Unable to find a xmlhttp connection properties file.");

		connectionProps = new Properties();
		try {
			connectionProps.load(is);
		} catch (IOException e) {
			throw new RuntimeException(
					"Unable to load xmlhttp connection properties file "
							+ propsLocation);
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		loadProps(this.getClass());

		username = connectionProps.getProperty("username", "admin");
		password = connectionProps.getProperty("password", "admin");
		connectionUrl = connectionProps.getProperty("connectionUrl");

		if (!connectionUrl.endsWith("/")) {
			connectionUrl = connectionUrl + "/";
		}

		if (servicePath != null && servicePath.length() > 0) {
			if (servicePath.startsWith("/")) {
				servicePath = servicePath.substring(1);
			}
			serviceUrl = connectionUrl + servicePath;
		}
	}

	/**
	 * Take the XML and send it as the <code>request</code> parameter in a
	 * request to the xmlhttp service pressumably at <code>url</code>. Assumes
	 * that the credentials for the xmlhttp services are using standard security
	 * and that the username is <code>admin</code> and the password is
	 * <code>admin</code>.
	 * 
	 * @param xml
	 *            XML that will be sent as the "request" parameter.
	 * @return JDOM parsed response.
	 * @throws URISyntaxException
	 */
	protected Document executeQueryAsDocument(String xml) throws IOException,
			JDOMException, XQueryXmlHttpException, URISyntaxException {
		Document result = getDocumentFromInputStream(executeQueryAsInputStream(xml));
		if (logger.isDebugEnabled()) {
			logger.debug("executeQueryAsDocument(): response: " + JDomUtils.convertDocumentToString(result));
		}
		return result;
	}

	private Document executeQueryAsDocument(URI uri) throws IOException,
			JDOMException, XQueryXmlHttpException, URISyntaxException {
		return getDocumentFromInputStream(executeQueryAsInputStream(uri));
	}
	
	protected Document executeQueryAsDocument(NameValuePair[] parameters) throws XQueryXmlHttpException, IOException, URISyntaxException, JDOMException {
		return getDocumentFromInputStream(executeQueryAsInputStream(parameters));
	}

	/**
	 * Take the XML and send it as the <code>request</code> parameter in a
	 * request to the xmlhttp service pressumably at <code>url</code>. Assumes
	 * that the credentials for the xmlhttp services are using standard security
	 * and that the username is <code>admin</code> and the password is
	 * <code>admin</code>.
	 * 
	 * @param xml
	 *            XML that will be sent as the "request" parameter.
	 * @return String response as a <code>String</code>
	 * @throws URISyntaxException
	 */
	protected String executeQuery(String xml) throws IOException,
			JDOMException, XQueryXmlHttpException, URISyntaxException {
		return getStringFromInputStream(executeQueryAsInputStream(xml));
	}

	private String executeQuery(URI uri) throws IOException, JDOMException,
			XQueryXmlHttpException, URISyntaxException {
		return getStringFromInputStream(executeQueryAsInputStream(uri));
	}

	/**
	 * Take the XML and send it as the <code>request</code> parameter in a
	 * request to the xmlhttp service presumably at <code>url</code>. Username
	 * and passsword credentials for the http connection are obtained via the
	 * <code>sername</code> and <code>password</code> properties. If these are
	 * not specified the default username is <code>admin</code> and the default
	 * password is <code>admin</code>.
	 * 
	 * @param xml
	 *            XML that will be sent as the "request" parameter.
	 * @return InputStream response from service
	 * @throws URISyntaxException
	 * @throws JDOMException
	 * @throws XQueryXmlHttpException
	 */
	protected InputStream executeQueryAsInputStream(String xml)
			throws IOException, URISyntaxException, XQueryXmlHttpException,
			JDOMException {
		NameValuePair nvp = new BasicNameValuePair("request", xml);
		NameValuePair[] nvpArray = new NameValuePair[] { nvp };
		return executeQueryAsInputStream(nvpArray);
	}

	private InputStream executeQueryAsInputStream(URI uri) throws IOException {
		HttpClient httpClient = getHttpClient();
		HttpPost httpPost = new HttpPost(uri);
		HttpResponse response = httpClient.execute(httpPost);
		StatusLine statusLine = response.getStatusLine();
		if (logger.isDebugEnabled()) {
			logger.warn("StatusCode: " + statusLine.getStatusCode() + " executing " + uri);
		}
		if (statusLine.getStatusCode() >= 300) {
			String body = getResponseBody(response);
			String message = "Error executing " + uri + "\n" + body;
			throw new HttpResponseException(statusLine.getStatusCode(), message);
		}
		HttpEntity entity = response.getEntity();
		return entity == null ? null : entity.getContent();
	}

	/**
	 * Allows you to formulate your http parameters if the xml http service
	 * doesn't follow the convention of the XML being sent to the service under
	 * the parameter <code>request</code>
	 * 
	 * @param parameters
	 *            the http parameters
	 * @return Returns the service response as a <cod>String</code>
	 * @throws IOException
	 *             if communication problem occurs.
	 * @throws URISyntaxException
	 * @throws JDOMException
	 * @throws XQueryXmlHttpException
	 */
	protected String executeQuery(NameValuePair[] parameters)
			throws IOException, URISyntaxException, XQueryXmlHttpException,
			JDOMException {
		return this.executeQuery(getUri(parameters));
	}

	/**
	 * Allows you to formulate your http parameters if the xml http service
	 * doesn't follow the convention of the XML being sent to the service under
	 * the parameter <code>request</code>
	 * 
	 * @param parameters
	 *            the http parameters
	 * @return Returns the service response as a <cod>String</code>
	 * @throws IOException
	 *             if communication problem occurs.
	 * @throws URISyntaxException
	 * @throws JDOMException
	 * @throws XQueryXmlHttpException
	 */
	protected InputStream executeQueryAsInputStream(NameValuePair[] parameters)
			throws IOException, URISyntaxException, XQueryXmlHttpException,
			JDOMException {
		return this.executeQueryAsInputStream(getUri(parameters));
	}

	private URI getUri(NameValuePair[] parameters) throws URISyntaxException {
		return new URI(getServiceUrl() + "?"
				+ URLEncodedUtils.format(Arrays.asList(parameters), "UTF-8"));
	}

	/**
	 * Return what the URL for the XML HTTP service is based upon
	 * <code>serviceUrl</code> and <code>servicePath</code>
	 * 
	 * @return The full URL for the service
	 */
	protected String getServiceUrl() {
		return serviceUrl == null ? "" : serviceUrl;
	}

	/**
	 * Determine a servicePath based upon introspection. If the unit test is
	 * service.FooBarTest then the service path would be service/foo-bar.xqy
	 */
	protected void introspectServicePath() {
		String className = this.getClass().getName();
		String name = className.replaceFirst("Test$", "");
		name = name.replaceAll("(\\p{Upper})", "-$1");
		name = name.replaceAll("\\.", "/") + ".xqy";
		name = name.toLowerCase();
		name = name.replaceFirst("-", "");
	}

	protected void setServicePath(String s) {
		this.servicePath = s;
	}

	private static String getResponseBody(HttpResponse response) {
		String message = null;
		try {
			StatusLine statusLine = response.getStatusLine();
			HttpEntity e = response.getEntity();
			String responseHtml = EntityUtils.toString(e);
			if (responseHtml.contains("<html")) {
				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				org.w3c.dom.Document document = builder.parse(new InputSource(
						new StringReader(responseHtml)));
				String xpath = "/html/body";
				XObject select = XPathAPI.eval(document, xpath);
				String result = select.toString().trim();
				message = (result == null) ? statusLine.getReasonPhrase()
						: result;
			}
		} catch (Exception ex) {
		}
		return message;
	}
	
	private static Document getDocumentFromInputStream(InputStream is) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder(false);
		return builder.build(is);
	}
	
	private static String getStringFromInputStream(InputStream is) throws IOException {
		StringBuffer sb = new StringBuffer();
		Reader reader = new InputStreamReader(is, "UTF-8");
		int c;
		while ((c = is.read()) != -1) {
			sb.append((char) c);
		}
		return sb.toString();
	}

	protected HttpClient getHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY,
				new UsernamePasswordCredentials(username, password));
		return httpClient;
	}
}
