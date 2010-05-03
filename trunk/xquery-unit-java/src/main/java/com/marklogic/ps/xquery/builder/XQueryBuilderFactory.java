package com.marklogic.ps.xquery.builder;

import com.marklogic.ps.xquery.XQueryVersion;

public class XQueryBuilderFactory {
	
	public static XQueryBuilder newXQueryBuilder() {
		return new ML1_0XQueryBuilder();
	}
	
	public static XQueryBuilder newXQueryBuilder(XQueryVersion version) {
		if (version.equals(XQueryVersion.v1_0_ML)) {
			return new ML1_0XQueryBuilder();
		} else {
			return new ML0_9XQueryBuilder();
		}
		
	}

}
