package com.timesontransfar.common.exception;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionTransformerImpl{
	
	private ExceptionTransformerImpl() {
		
	}

	public static String transformException(Throwable e) {
		// Auto-generated method stub
		StringWriter writer = new StringWriter();
		PrintWriter s = new PrintWriter(writer);
		e.printStackTrace(s);
		String exception = "\n错误信息为：" + writer.toString() + " ";
		return exception;
	}

}
