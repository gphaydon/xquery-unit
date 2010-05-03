package com.marklogic.ps.xquery.builder;

/**
 * Wrapper class around {@link XQueryBuilder}.
 * @author mhelmstetter
 *
 */
public abstract class AbstractBuilder implements XQueryBuilder {
	protected StringBuilder sb;
	
	public AbstractBuilder() {
		sb = new StringBuilder();
	}
	
	public AbstractBuilder(StringBuilder sb) {
		this.sb = sb;
	}

	public XQueryBuilder append(boolean b) {
		sb.append(b);
		return this;
	}

	public XQueryBuilder append(char c) {
		sb.append(c);
		return this;
	}

	public XQueryBuilder append(char[] str, int offset, int len) {
		sb.append(str, offset, len);
		return this;
	}

	public XQueryBuilder append(char[] str) {
		sb.append(str);
		return this;
	}

	public XQueryBuilder append(CharSequence s, int start, int end) {
		sb.append(s, start, end);
		return this;
	}

	public XQueryBuilder append(CharSequence s) {
		sb.append(s);
		return this;
	}

	public XQueryBuilder append(double d) {
		sb.append(d);
		return this;
	}

	public XQueryBuilder append(float f) {
		sb.append(f);
		return this;
	}

	public XQueryBuilder append(int i) {
		sb.append(i);
		return this;
	}

	public XQueryBuilder append(long lng) {
		sb.append(lng);
		return this;
	}

	public XQueryBuilder append(Object obj) {
		sb.append(obj);
		return this;
	}

	public XQueryBuilder append(String str) {
		sb.append(str);
		return this;
	}

	public XQueryBuilder append(StringBuffer sb) {
		this.sb.append(sb);
		return this;
	}

	public XQueryBuilder appendCodePoint(int codePoint) {
		sb.appendCodePoint(codePoint);
		return this;
	}

	public int capacity() {
		return sb.capacity();
	}

	public char charAt(int index) {
		return sb.charAt(index);
	}

	public int codePointAt(int index) {
		return sb.codePointAt(index);
	}

	public int codePointBefore(int index) {
		return sb.codePointBefore(index);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return sb.codePointCount(beginIndex, endIndex);
	}

//	public XQueryBuilder delete(int start, int end) {
//		return sb.delete(start, end);
//	}
//
//	public XQueryBuilder deleteCharAt(int index) {
//		return sb.deleteCharAt(index);
//	}

	public void ensureCapacity(int minimumCapacity) {
		sb.ensureCapacity(minimumCapacity);
	}

	public boolean equals(Object obj) {
		return sb.equals(obj);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		sb.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	public int hashCode() {
		return sb.hashCode();
	}

	public int indexOf(String str, int fromIndex) {
		return sb.indexOf(str, fromIndex);
	}

	public int indexOf(String str) {
		return sb.indexOf(str);
	}

//	public XQueryBuilder insert(int offset, boolean b) {
//		return sb.insert(offset, b);
//	}
//
//	public XQueryBuilder insert(int offset, char c) {
//		return sb.insert(offset, c);
//	}
//
//	public XQueryBuilder insert(int index, char[] str, int offset, int len) {
//		return sb.insert(index, str, offset, len);
//	}
//
//	public XQueryBuilder insert(int offset, char[] str) {
//		return sb.insert(offset, str);
//	}
//
//	public XQueryBuilder insert(int dstOffset, CharSequence s, int start,
//			int end) {
//		return sb.insert(dstOffset, s, start, end);
//	}
//
//	public XQueryBuilder insert(int dstOffset, CharSequence s) {
//		return sb.insert(dstOffset, s);
//	}
//
//	public XQueryBuilder insert(int offset, double d) {
//		return sb.insert(offset, d);
//	}
//
//	public XQueryBuilder insert(int offset, float f) {
//		return sb.insert(offset, f);
//	}
//
//	public XQueryBuilder insert(int offset, int i) {
//		return sb.insert(offset, i);
//	}
//
//	public XQueryBuilder insert(int offset, long l) {
//		return sb.insert(offset, l);
//	}
//
//	public XQueryBuilder insert(int offset, Object obj) {
//		return sb.insert(offset, obj);
//	}
//
//	public XQueryBuilder insert(int offset, String str) {
//		return sb.insert(offset, str);
//	}

	public int lastIndexOf(String str, int fromIndex) {
		return sb.lastIndexOf(str, fromIndex);
	}

	public int lastIndexOf(String str) {
		return sb.lastIndexOf(str);
	}

	public int length() {
		return sb.length();
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		return sb.offsetByCodePoints(index, codePointOffset);
	}

//	public XQueryBuilder replace(int start, int end, String str) {
//		return sb.replace(start, end, str);
//	}
//
//	public XQueryBuilder reverse() {
//		return sb.reverse();
//	}

	public void setCharAt(int index, char ch) {
		sb.setCharAt(index, ch);
	}

	public void setLength(int newLength) {
		sb.setLength(newLength);
	}

	public CharSequence subSequence(int start, int end) {
		return sb.subSequence(start, end);
	}

	public String substring(int start, int end) {
		return sb.substring(start, end);
	}

	public String substring(int start) {
		return sb.substring(start);
	}

	public String toString() {
		return sb.toString();
	}

	public void trimToSize() {
		sb.trimToSize();
	}
	
	

}
