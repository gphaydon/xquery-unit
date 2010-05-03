package com.marklogic.ps.xquery.builder;

import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;

public interface XQueryBuilder {

	/**
	 * Adds an "import module namespace" line to the given query.  This function is useful for building up XQuery
	 * in a string for adhoc querying with
	 * {@link #executeQuery(String, com.marklogic.xcc.RequestOptions, com.marklogic.xcc.types.XdmVariable...)}.
	 * @param query Existing query to add to.
	 * @param prefix the prefix to be used in calling function from the imported module
	 * @param namespace the namespace for the library module
	 * @param location the path to the library module relative to the app server root.
	 */
	public XQueryBuilder importModule(String prefix, String namespace, String location);
	
	public XQueryBuilder defineExternalVariable(String name, XdmValue value);

	/**
	 * Adds an external string variable definition to the give query and
	 * returns a variable who's name is the given name and value is the
	 * given value.  The variable that is returned should be set on the
	 * Request that executes the query. This function is useful for building up XQuery
	 * in a string for adhoc querying with
	 * {@link #executeQuery(String, com.marklogic.xcc.RequestOptions, com.marklogic.xcc.types.XdmVariable...)}.
	 *
	 * @param query The <code>XQueryBuilder</code> being used to build up the query
	 * @param name the variable name.
	 * @return The newly "bound" XdmVariable.
	 */
	public XdmVariable defineExternalStringVariable(String name, String value);

	/**
	 * Adds an external node() variable definition to the give query.  This is
	 * a convenience method that adds code to the query that accepts an external
	 * string variable, parses it and sets the variable with the given name to
	 * the first node of the parsed result. Due to the indirection required to
	 * pass the XML as a string and then parse it, the variable that is
	 * returned will not have the name that is given.  However, a query variable
	 * with the given name and of type node() will be created and can be
	 * referenced by other code in the query.
	 * The variable that is returned should be set on the Request that executes
	 * the query. This function is useful for building up XQuery
	 * in a string for adhoc querying with
	 * {@link #executeQuery(String, com.marklogic.xcc.RequestOptions, com.marklogic.xcc.types.XdmVariable...)}.
	 *
	 * @param name the variable name.
	 * @return The newly "bound" XdmVariable.
	 */
	public XdmVariable defineExternalNodeVariable(String name, String value);

	/**
	 * Addes the given line to the given query. This function is useful for building up XQuery
	 * in a string for adhoc querying with
	 * {@link #executeQuery(String, com.marklogic.xcc.RequestOptions, com.marklogic.xcc.types.XdmVariable...)}.
	 *
	 * @param line the new line to add
	 * @return
	 */
	public XQueryBuilder addLine(String line);

	/**
	 * Adds a "declare namespace" line to the given query. This function is useful for building up XQuery
	 * in a string for adhoc querying with
	 * {@link #executeQuery(String, com.marklogic.xcc.RequestOptions, com.marklogic.xcc.types.XdmVariable...)}.
	 * @param query
	 * @param prefix
	 * @param namespace
	 */
	public XQueryBuilder declareNamespace(String prefix, String namespace);

	public XQueryBuilder append(boolean b);

	public XQueryBuilder append(char c);

	public XQueryBuilder append(char[] str, int offset, int len);

	public XQueryBuilder append(char[] str);

	public XQueryBuilder append(CharSequence s, int start, int end);

	public XQueryBuilder append(CharSequence s);

	public XQueryBuilder append(double d);

	public XQueryBuilder append(float f);

	public XQueryBuilder append(int i);

	public XQueryBuilder append(long lng);

	public XQueryBuilder append(Object obj);

	public XQueryBuilder append(String str);

	public XQueryBuilder append(StringBuffer sb);

	public XQueryBuilder appendCodePoint(int codePoint);

	public int capacity();

	public char charAt(int index);

	public int codePointAt(int index);

	public int codePointBefore(int index);

	public int codePointCount(int beginIndex, int endIndex);

//	public XQueryBuilder delete(int start, int end);
//
//	public XQueryBuilder deleteCharAt(int index);

	public void ensureCapacity(int minimumCapacity);

	public boolean equals(Object obj);

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin);

	public int hashCode();

	public int indexOf(String str, int fromIndex);

	public int indexOf(String str);

//	public XQueryBuilder insert(int offset, boolean b);
//
//	public XQueryBuilder insert(int offset, char c);
//
//	public XQueryBuilder insert(int index, char[] str, int offset, int len);
//
//	public XQueryBuilder insert(int offset, char[] str);
//
//	public XQueryBuilder insert(int dstOffset, CharSequence s, int start, int end);
//
//	public XQueryBuilder insert(int dstOffset, CharSequence s);
//
//	public XQueryBuilder insert(int offset, double d);
//
//	public XQueryBuilder insert(int offset, float f);
//
//	public XQueryBuilder insert(int offset, int i);
//
//	public XQueryBuilder insert(int offset, long l);
//
//	public XQueryBuilder insert(int offset, Object obj);
//
//	public XQueryBuilder insert(int offset, String str);

	public int lastIndexOf(String str, int fromIndex);

	public int lastIndexOf(String str);

	public int length();

	public int offsetByCodePoints(int index, int codePointOffset);

	//public XQueryBuilder replace(int start, int end, String str);

	//public XQueryBuilder reverse();

	public void setCharAt(int index, char ch);

	public void setLength(int newLength);

	public CharSequence subSequence(int start, int end);

	public String substring(int start, int end);

	public String substring(int start);

	public String toString();

	public void trimToSize();

}