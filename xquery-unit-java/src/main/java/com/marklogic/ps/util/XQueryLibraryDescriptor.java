package com.marklogic.ps.util;

import java.util.HashMap;
import java.util.Map;

import com.marklogic.xcc.types.ValueType;

public class XQueryLibraryDescriptor {
	private String modulePath;
	private String moduleNamespace;
	private Map<String, Function> functions;

	public XQueryLibraryDescriptor(String modulePath, String moduleNamespace) {
		this.modulePath = modulePath;
		this.moduleNamespace = moduleNamespace;
		
		functions = new HashMap<String, Function>();
	}
	
	public String getModulePath() {
		return modulePath;
	}
	
	public String getModuleNamespace() {
		return moduleNamespace;
	}
	
	public Function getFunctionDescriptor(String name) {
		return functions.get(name);
	}
	
	public void addFunction(Function function) {
		functions.put(function.getName(), function);
	}
	
	public static class Function {
		String name;
		ValueType[] types;
		ValueType returnType;
		
		public Function(String name, ValueType[] types, ValueType returnType) {
			this.name = name;
			this.types = types;
			this.returnType = returnType;
		}
		
		public String getName() {
			return name;
		}
		
		public ValueType[] getParameterTypes() {
			return types;
		}
		
		public ValueType getReturnType() {
			return returnType;
		}
	}
}
