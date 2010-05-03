package com.marklogic.ps.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marklogic.ps.xquery.builder.XQueryBuilder;
import com.marklogic.ps.xquery.builder.XQueryBuilderFactory;
import com.marklogic.xcc.Request;
import com.marklogic.xcc.ResultItem;
import com.marklogic.xcc.ResultSequence;
import com.marklogic.xcc.Session;
import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.exceptions.RequestException;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XSAnyURI;
import com.marklogic.xcc.types.XSBase64Binary;
import com.marklogic.xcc.types.XSBoolean;
import com.marklogic.xcc.types.XSDate;
import com.marklogic.xcc.types.XSDateTime;
import com.marklogic.xcc.types.XSDecimal;
import com.marklogic.xcc.types.XSDouble;
import com.marklogic.xcc.types.XSDuration;
import com.marklogic.xcc.types.XSFloat;
import com.marklogic.xcc.types.XSHexBinary;
import com.marklogic.xcc.types.XSInteger;
import com.marklogic.xcc.types.XSTime;
import com.marklogic.xcc.types.XdmBinary;
import com.marklogic.xcc.types.XdmDuration;
import com.marklogic.xcc.types.XdmItem;
import com.marklogic.xcc.types.XdmNode;
import com.marklogic.xcc.types.XdmSequence;
import com.marklogic.xcc.types.XdmValue;

public class Xcc {
	
	protected static final Logger logger = LoggerFactory.getLogger( Xcc.class );
	
    public static final SimpleDateFormat XSDATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    public static final SimpleDateFormat XSDATETIME_FORMAT_NO_SEC_FRACT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static final SimpleDateFormat XSDATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private static final String SEQUENCE_DELIMITER = "][";
    
	/**
	 * Utility method that can be used to invoke a function in a module library.
	 * It dynamically generates the required XQuery and invokes an adhoc query
	 * using the given session and variables.
	 * @param session the xcc session to use
	 * @param module the path and name of the module that contains the function
	 * @param namespace the namespace of the module
	 * @param function the name of the function
	 * @param params the variables to pass to the function (must be in the order 
	 * that they are declared in the function's method signature)
	 * @return the restult of the function invocation
	 * @throws RequestException
	 */
	public static ResultSequence invokeModuleFunction(Session session, String module, String namespace, String function, XdmValue... params) throws RequestException {
		XQueryBuilder query = XQueryBuilderFactory.newXQueryBuilder();
		query.importModule("mod", namespace, module);
		//query.append("import module namespace mod = '").append(namespace).append("' at '").append(module).append("'\n");
		
		if (params != null) {
	  		for (int i = 0; i < params.length; i++) {
	  			query.defineExternalVariable("v"+i, params[i]);
	  		}
		}
		
		query.append("mod:").append(function).append("(");
		if (params != null) {
	  		for (int i = 0; i < params.length; i++) {
	  			if (i != 0) {
	  				query.append(", ");
	  			}
	  			query.append("$v").append(i);
	  		}
		}
		query.append(")");
		
		Request r = session.newAdhocQuery(query.toString());
		if (params != null) {
	  		for (int i = 0; i < params.length; i++) {
	  			if (params[i] == null || params[i] instanceof XdmSequence) {
	  				// do not set as they are passed in above
	  			} else if (params[i] instanceof XdmNode) {
	  				r.setVariable(
	  					ValueFactory.newVariable(
	  						new XName("v" + i + "_string"), 
	  						ValueFactory.newXSString(params[i].asString())
	  					)
	  			    );				
	  			} else {
	  				r.setVariable(
	  					ValueFactory.newVariable(new XName("v" + i), params[i])
	  		        );
	  			}
	  		}
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug("Xcc.invokeModuleFunction():");
			String[] lines = query.toString().split("\n");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 75; i++) sb.append('-');
			sb.append("\n");
			int lineNo = 1;
			for (String line : lines) {
				sb.append(String.format("%4d: %s\n", lineNo++, line));
			}
			for (int i = 0; i < 75; i++) sb.append('-');
			logger.debug(sb.toString());
		}
		
		return session.submitRequest(r); 
	}
	
	public static ResultSequence executeAdhocQuery(Session session, String query) throws RequestException {
		Request r = session.newAdhocQuery(query);
		return session.submitRequest(r);
	}
	
	public static XSDateTime newXSDateTime(Date d) {
		return ValueFactory.newXSDateTime(XSDATETIME_FORMAT.format(d), TimeZone.getDefault(), Locale.getDefault());
	}
	
	public static XSDateTime newXSDateTime(String dateString) throws ParseException {
		return ValueFactory.newXSDateTime(dateString, TimeZone.getDefault(), Locale.getDefault());
	}
	
	public static Date dateFromDateTimeString(String str) throws ParseException {
		String val = str.trim();

        ParsePosition pp = new ParsePosition(0);
        Date date = XSDATETIME_FORMAT_NO_SEC_FRACT.parse(val, pp);
        if(date == null)
            throw new IllegalArgumentException("Not a valid date/time string");
        int subseconds = 0;
        if(pp.getIndex() < val.length() && val.charAt(pp.getIndex()) == '.')
        {
            pp.setIndex(pp.getIndex() + 1);
            int end;
            int places = 0;
            for(end = pp.getIndex(); end < val.length() && val.charAt(end) >= '0' && val.charAt(end) <= '9'; end++, places++);
            subseconds = (int)Math.round((Integer.parseInt(val.substring(pp.getIndex(), end)) / Math.pow(10, places) * 1000));
            pp.setIndex(end);
        }
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault(), Locale.getDefault());
        cal.setTime(date);
        cal.set(14, subseconds);
        
        return cal.getTime();
	}
	
	public static Document parseResultItem(ResultItem item) throws JDOMException, IOException {
		return new SAXBuilder(false).build(new StringReader(item.asString()));
	}

	public static <T> T newProxyInstance(Class<T> interfaceClass, XQueryLibraryDescriptor descriptor, Session session) {
		XQueryInvocationHandler handler = new XQueryInvocationHandler(session, descriptor);
		return (T)Proxy.newProxyInstance(Xcc.class.getClassLoader(), new Class[] { interfaceClass }, handler);
	}
	
	public static <T> T newProxyInstance(Class<T>[] interfaces, XQueryLibraryDescriptor descriptor, Session session) {
		XQueryInvocationHandler handler = new XQueryInvocationHandler(session, descriptor);
		return (T)Proxy.newProxyInstance(Xcc.class.getClassLoader(), interfaces, handler);
	}
	
	public static String mapJavaMethodToModuleFunction(String methodName) {
		StringBuilder moduleFunction = new StringBuilder();
		int len = methodName.length();
		char c;
		for (int i = 0; i < len; i++) {
			c = methodName.charAt(i);
			if (Character.isUpperCase(c)) {
				moduleFunction.append("-").append(Character.toLowerCase(c));
			} else {
				moduleFunction.append(c);
			}
		}
		return moduleFunction.toString();
	}

	public static XdmValue mapJavaValueToXQueryValue(Object value, ValueType xqueryType) throws Exception {
		XdmValue xqueryValue = null;

		if (xqueryType.equals(ValueType.ELEMENT)) {
			xqueryValue = ValueFactory.newElement(value);
			
// will not have the "SEQUENCE" type as a parameter will always have some other type and the multiplicity is separate			
//		} else if (xqueryType.equals(ValueType.SEQUENCE)) {
//			XdmItem[] items = null;
//			if (value.getClass().isArray()) {
//				items = new XdmItem[Array.getLength(value)];
//				ValueType componentType = mapJavaTypeToXQueryType(value.getClass().getComponentType());
//
//				for (int i = 0; i < items.length; i++) {
//					items[i] = (XdmItem)mapJavaValueToXQueryValue(Array.get(value, i), componentType);
//				}
//			} else {
//				// I'm not sure how this can happen but let's just wrap it with an array with one element
//				items = new XdmItem[] { 
//					(XdmItem)mapJavaValueToXQueryValue(value, mapJavaTypeToXQueryType(value.getClass())) 
//				};
//			}
//			xqueryValue = ValueFactory.newSequence(items);
			
		} else if (xqueryType.equals(ValueType.TEXT)) {
			xqueryValue = ValueFactory.newTextNode(value);
		} else if (xqueryType.equals(ValueType.XS_BOOLEAN)) {
			if (value instanceof Boolean) {
				xqueryValue = ValueFactory.newXSBoolean((Boolean)value);
			} else {
				throw new ClassCastException("Incompatible types: " + value.getClass() + ", " + xqueryType);
			}
		} else if (xqueryType.equals(ValueType.XS_DATE)) {
			if (value instanceof Date) {
				xqueryValue = ValueFactory.newXSDate(XSDATE_FORMAT.format((Date)value), TimeZone.getDefault(), Locale.getDefault());
			} else if (value instanceof String) {
				xqueryValue = ValueFactory.newXSDate((String)value, TimeZone.getDefault(), Locale.getDefault());
			} else {
				throw new ClassCastException("Incompatible types: " + value.getClass() + ", " + xqueryType);
			}
		} else if (xqueryType.equals(ValueType.XS_DATE_TIME)) {
			if (value instanceof Date) {
				xqueryValue = Xcc.newXSDateTime((Date)value);
			} else if (value instanceof String) {
				xqueryValue = ValueFactory.newXSDateTime((String)value, TimeZone.getDefault(), Locale.getDefault());
			} else {
				throw new ClassCastException("Incompatible types: " + value.getClass() + ", " + xqueryType);
			}
		} else if (xqueryType.equals(ValueType.XS_DURATION)) {
			if (value instanceof XdmDuration) {
				xqueryValue = ValueFactory.newXSDuration((XdmDuration)value); 
			} else if (value instanceof String) {
				xqueryValue = ValueFactory.newXSDuration((String)value);
			} else {
				throw new ClassCastException("Incompatible types: " + value.getClass() + ", " + xqueryType);
			}
		} else if (xqueryType.equals(ValueType.XS_INTEGER)) {
			if (value instanceof BigInteger) {
				xqueryValue = ValueFactory.newXSInteger((BigInteger)value); 
			} else if (value instanceof Long) {
				xqueryValue = ValueFactory.newXSInteger((Long)value);
			} else {
				throw new ClassCastException("Incompatible types: " + value.getClass() + ", " + xqueryType);
			}
		} else if (xqueryType.equals(ValueType.XS_STRING)) {
			if (value instanceof String) {
				xqueryValue = ValueFactory.newXSString((String)value); 
			} else {
				xqueryValue = ValueFactory.newXSString(value.toString()); 
			}
		} else if (xqueryType.equals(ValueType.XS_TIME)) {
			if (value instanceof String) {
				xqueryValue = ValueFactory.newXSTime((String)value, TimeZone.getDefault(), Locale.getDefault());
			} else {
				throw new ClassCastException("Incompatible types: " + value.getClass() + ", " + xqueryType);
			}
		}
		return xqueryValue;
	}

	public static Object mapXQueryValueToJavaValue(XdmItem item, Class javaType) throws Exception {
		Object value = null;
		if (javaType.equals(String.class)) {
			value = item.asString();
		} else if (javaType.equals(Reader.class)) {
			value = item.asReader();
		} else if (javaType.equals(InputStream.class)) {
			value = item.asInputStream();
		} else if (item instanceof XdmSequence) {
			if (javaType.isArray() || javaType.equals(Object.class)) {
				if (javaType.equals(String[].class)) {
					value = ((XdmSequence)item).asStrings();
				} else {
					XdmItem[] items = ((XdmSequence)item).toArray();
					Class componentType = javaType.getComponentType();
					value = Array.newInstance(componentType, items.length);
					for (int i = 0; i < items.length; i++) {
						Array.set(value, i, mapXQueryValueToJavaValue(items[i], componentType));
					}
				}
			}
		} else if (item instanceof XSBoolean) {
			if (javaType.isAssignableFrom(Boolean.class)) {
				value = ((XSBoolean)item).asBoolean();
			} else if (javaType.isAssignableFrom(boolean.class)) {
				value = ((XSBoolean)item).asPrimitiveBoolean();
			}
		} else if (item instanceof XSInteger) {
			if (javaType.isAssignableFrom(Integer.class)) {
				value = ((XSInteger)item).asInteger();
			} else if (javaType.isAssignableFrom(int.class)) {
				value = ((XSInteger)item).asPrimitiveInt();
			} else if (javaType.isAssignableFrom(BigInteger.class)) {
				value = ((XSInteger)item).asBigInteger();
			} else if (javaType.isAssignableFrom(Long.class)) {
				value = ((XSInteger)item).asLong();
			} else if (javaType.isAssignableFrom(long.class)) {
				value = ((XSInteger)item).asPrimitiveLong();
			}
		} else if (item instanceof XSDouble) {
			if (javaType.isAssignableFrom(Double.class)) {
				value = ((XSDouble)item).asDouble();
			} else if (javaType.isAssignableFrom(double.class)) {
				value = ((XSDouble)item).asPrimitiveDouble();
			} else if (javaType.isAssignableFrom(BigDecimal.class)) {
				value = ((XSDouble)item).asBigDecimal();
			}
		} else if (item instanceof XSFloat) {
			if (javaType.isAssignableFrom(Float.class)) {
				value = ((XSFloat)item).asFloat();
			} else if (javaType.isAssignableFrom(float.class)) {
				value = ((XSFloat)item).asPrimitiveFloat();
			} else if (javaType.isAssignableFrom(BigDecimal.class)) {
				value = ((XSFloat)item).asBigDecimal();
			}
		} else if (item instanceof XSDecimal) {
			if (javaType.isAssignableFrom(BigDecimal.class)) {
				value = ((XSDecimal)item).asBigDecimal();
			}
		} else if (item instanceof XSDate) {
			if (javaType.isAssignableFrom(Date.class)) {
				value = ((XSDate)item).asDate();
			}
		} else if (item instanceof XSDateTime) {
			if (javaType.isAssignableFrom(Date.class)) {
				value = ((XSDateTime)item).asDate();
			}
		} else if (item instanceof XSTime) {
			if (javaType.isAssignableFrom(Date.class)) {
				value = ((XSTime)item).asDate();
			}
		} else if (item instanceof XSDuration) {
			if (javaType.isAssignableFrom(XdmDuration.class)) {
				value = ((XSDuration)item).asDuration();
			}
		} else if (item instanceof XSAnyURI) {
			if (javaType.isAssignableFrom(URI.class)) {
				value = ((XSAnyURI)item).asUri();
			}
		} else if (item instanceof XSBase64Binary) {
			if (byte[].class.isAssignableFrom(javaType)) {
				value = ((XSBase64Binary)item).asBinaryData();
			}
		} else if (item instanceof XSBase64Binary) {
			if (byte[].class.isAssignableFrom(javaType)) {
				value = ((XSBase64Binary)item).asBinaryData();
			}
		} else if (item instanceof XSHexBinary) {
			if (byte[].class.isAssignableFrom(javaType)) {
				value = ((XSHexBinary)item).asBinaryData();
			}
		} else if (item instanceof XdmBinary) {
			if (byte[].class.isAssignableFrom(javaType)) {
				value = ((XdmBinary)item).asBinaryData();
			}
		} else if (item instanceof XdmNode) {
			// this covers Attribute, Binary, Comment, Document, Element, ProcessingInstruction and Text 
			if (org.w3c.dom.Node.class.isAssignableFrom(javaType)) {
				value = ((XdmNode)item).asW3cNode();
			}
		}
		
		if (value == null) {
			// should probably create a specific exception for this
			throw new ClassCastException("Could not map XQuery value of type " + item.getClass().getName() + " to " + javaType.getName());
		}
		
		return value;
	}
	
	private static class XQueryInvocationHandler implements InvocationHandler {

		private XQueryLibraryDescriptor descriptor;
		private Session session;
		
		public XQueryInvocationHandler(Session session, XQueryLibraryDescriptor descriptor) {
			this.session = session;
			this.descriptor = descriptor;
		}
		
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			String functionName = mapJavaMethodToModuleFunction(method.getName());
			
			XQueryLibraryDescriptor.Function function = descriptor.getFunctionDescriptor(functionName);

			ValueType[] paramTypes = function.getParameterTypes();
			XdmValue[] params = new XdmValue[paramTypes.length];
			Object value = null;
			XdmValue[] valueList = null;
			ValueType type = null;
			// I think we need to know the types as declared in the XQuery module
			// maybe not if we make sure the types in the Java interface are castable
			// to the types in the module function
			for (int i = 0; i < paramTypes.length; i++) {
				if (args[i] instanceof XdmValue) {
					value = args[i];
					type = ((XdmValue)value).getValueType();
				} else {
					// wrap everything in a sequence to support multiplicity
					if (args[i].getClass().isArray()) {
						valueList = new XdmValue[Array.getLength(args[i])];

						for (int j = 0; j < valueList.length; j++) {
							valueList[j] = mapJavaValueToXQueryValue(Array.get(args[i], j), paramTypes[i]);
						}
					} else {
						valueList = new XdmValue[] { mapJavaValueToXQueryValue(args[i], paramTypes[i]) };
					}
					
					value = valueList;
					type = ValueType.SEQUENCE;
				}

				params[i] = ValueFactory.newValue(type, value);
			}
			
			ResultSequence result = 
				Xcc.invokeModuleFunction(
					session, 
					descriptor.getModulePath(), 
					descriptor.getModuleNamespace(), 
					functionName, 
					params
				);
			
			Object resultValue = null;
			
			if (result.hasNext()) {
				Class returnType = method.getReturnType();
				
				if (returnType.isArray()) {
					if (returnType.equals(String[].class)) {
						value = result.asStrings();
					} else {
						XdmItem[] items = result.toArray();
						Class componentType = returnType.getComponentType();
						value = Array.newInstance(componentType, items.length);
						for (int i = 0; i < items.length; i++) {
							Array.set(value, i, mapXQueryValueToJavaValue(items[i], componentType));
						}
					}
				} else {
					resultValue = mapXQueryValueToJavaValue(result.next().getItem(), returnType);
				}
			}
			
			return resultValue; 
		}
	}
}
