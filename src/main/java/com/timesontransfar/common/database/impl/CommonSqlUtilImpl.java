/*
 * 创建日期 2006-1-18
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.database.impl;

import java.io.BufferedReader;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.ResultSet;

import com.timesontransfar.common.database.ISqlUtil;

/**
 * @author Administrator
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class CommonSqlUtilImpl implements ISqlUtil {

	/**
	 * 
	 */
	public CommonSqlUtilImpl() {
		super();
		// 自动生成构造函数存根
	}

	/* （非 Javadoc）
	 * @see com.timesontransfar.common.database.ISqlUtil#getClob(java.sql.ResultSet, java.lang.String)
	 */
	public String getClob(ResultSet rs, String column) {
		// 自动生成方法存根
		String clobString="";
		try{
			//Clob clob1=rs.getClob(column);
			Clob clob = rs.getClob(column);
	        if(clob!=null){
	        	Reader is=clob.getCharacterStream();
	        	BufferedReader br=new BufferedReader(is);
	        	String s=br.readLine();
	        	while(s!=null){
	        		clobString += s;
	        		s=br.readLine();
	        	}
	         }
		}catch(Exception e){
			e.printStackTrace();

		}
		return clobString;
	}

	public String getClob(ResultSet rs, int column) {
		// 自动生成方法存根
		String clobString="";
		try{
			//Clob clob1=rs.getClob(column);
			Clob clob = rs.getClob(column);
	        if(clob!=null){
	        	Reader is=clob.getCharacterStream();
	        	BufferedReader br=new BufferedReader(is);
	        	String s=br.readLine();
	        	while(s!=null){
	        		clobString += s;
	        		s=br.readLine();
	        	}
	         }
		}catch(Exception e){
			e.printStackTrace();

		}
		return clobString;
	}
	
	/* （非 Javadoc）
	 * @see com.timesontransfar.common.database.ISqlUtil#setClob(java.sql.ResultSet, java.lang.String, java.lang.String)
	 */
	public void setClob(ResultSet rs, String column, String content) {
		// 自动生成方法存根
		try{
			Clob clob = rs.getClob(column);
	        if(clob!=null){
	        	Writer writer=clob.setCharacterStream(0);
	        	//Writer writer=clob.getCharacterOutputStream();
	        	writer.write(content);
	        	writer.flush();
	        	writer.close();
	         }
		}catch(Exception e){
			e.printStackTrace();

		}		

	}

}
