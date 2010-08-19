package com.marklogic.ps.xquery.builder;

import com.marklogic.xcc.ValueFactory;
import com.marklogic.xcc.types.ValueType;
import com.marklogic.xcc.types.XName;
import com.marklogic.xcc.types.XdmItem;
import com.marklogic.xcc.types.XdmNode;
import com.marklogic.xcc.types.XdmSequence;
import com.marklogic.xcc.types.XdmValue;
import com.marklogic.xcc.types.XdmVariable;
import com.marklogic.xcc.types.impl.ElementImpl;

public class ML1_0XQueryBuilder extends AbstractXQueryBuilder implements XQueryBuilder {
	
	// default visibility (package-private), intended that only the factory instantiates this class
	ML1_0XQueryBuilder() {
		super(new StringBuilder("xquery version \"1.0-ml\";\n"));
	}
	
    /* (non-Javadoc)
	 * @see com.marklogic.ps.xquery.XQueryBuilder#importModule(java.lang.String, java.lang.String, java.lang.String)
	 */
    public XQueryBuilder importModule(String prefix, String namespace, String location) {
        sb.append("import module namespace ").append(prefix).append(" = \"");
        sb.append(namespace).append("\" at \"").append(location).append("\";\n");
        return this;
    }

    /**
     * Adds a "declare namespace" line to the given query. This function is useful for building up XQuery
     * in a string for adhoc querying with
     * {@link #executeQuery(String, com.marklogic.xcc.RequestOptions, com.marklogic.xcc.types.XdmVariable...)}.
     * @param query
     * @param prefix
     * @param namespace
     */
    public XQueryBuilder declareNamespace(String prefix, String namespace) {
        sb.append("declare namespace ").append(prefix).append(" = \"");
        sb.append(namespace).append("\";\n");
        return this;
    }
    
    public XQueryBuilder defineExternalVariable(String name, XdmValue value) {
		sb.append("declare variable $").append(name);

		if (value == null) {
			sb.append(" := ();");
		} else if (value instanceof XdmSequence) {
			sb.append(" := (");
			XdmItem[] items = ((XdmSequence) value).toArray();
			for (int j = 0; j < items.length; j++) {
				if (j > 0) {
					sb.append(", ");
				}
				ValueType valueType = items[j].getValueType();
				if (valueType.equals(ValueType.ELEMENT)) {
					// query.append("element ");
					ElementImpl e = (ElementImpl) items[j];
					String foo = items[j].asString();
					sb.append(items[j].asString());
				} else {
					sb.append(items[j].getValueType().toString());
					sb.append("('").append(items[j].asString()).append("')");
				}
			}
			sb.append(");\n");
		} else if (value instanceof XdmNode) {
			sb.append("_string external;\n");
			sb.append("declare variable $").append(name);
			sb.append(" := xdmp:unquote($").append(name).append(
					"_string)/node();\n");
		} else {
			sb.append(" external;\n");
		}
		return this;
    }
	
    /* (non-Javadoc)
	 * @see com.marklogic.ps.xquery.XQueryBuilder#defineExternalStringVariable(java.lang.String, java.lang.String)
	 */
    public XdmVariable defineExternalStringVariable(String name, String value) {
        sb.append("declare variable $").append(name).append(" as ");
        sb.append("xs:string").append(" external;\n");

        XdmVariable variable = ValueFactory.newVariable(
                new XName(name), ValueFactory.newXSString(value)
        );

        return variable;
    }
    
    /* (non-Javadoc)
	 * @see com.marklogic.ps.xquery.XQueryBuilder#defineExternalNodeVariable(java.lang.String, java.lang.String)
	 */
    public XdmVariable defineExternalNodeVariable(String name, String value) {
        String externalName = name + "-string";
        sb.append("declare variable $").append(externalName).append(" as xs:string external;\n");
        sb.append("declare variable $").append(name).append(" as ");
        sb.append("node() := xdmp:unquote($");
        sb.append(externalName);
        sb.append(")/node()[1];\n");

        XdmVariable variable = ValueFactory.newVariable(
                new XName(externalName), ValueFactory.newXSString(value)
        );

        return variable;
    }

    /* (non-Javadoc)
	 * @see com.marklogic.ps.xquery.XQueryBuilder#addLine(java.lang.String)
	 */
    public XQueryBuilder addLine(String line) {
        sb.append(line).append("\n");
        return this;
    }

}
