package com.javax0.aptools;

public class StringTool {
	private final String s;

	StringTool(String s) {
		this.s = s;
	}
	public String unquoted(){
		return s.substring(1, s.length() - 1);
	}
	
	public String replace(String... arg) {
		if (arg.length % 2 != 0)
			throw new RuntimeException(
					"replace was called with odd number of strings");
		String result = s;
		for (int i = 0; i < arg.length; i += 2) {
			result = result.replaceAll(arg[i], arg[i + 1]);
		}
		return result;
	}

}
