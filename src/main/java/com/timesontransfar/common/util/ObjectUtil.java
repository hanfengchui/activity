package com.timesontransfar.common.util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import com.caucho.hessian.io.HessianOutput;

public class ObjectUtil {

	private ObjectUtil() {
		super();
	}

	public static int sizeof(Object object){
		try{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(byteOut);
			out.writeObject(object);
			byte[] byteArray=byteOut.toByteArray();
			return byteArray.length;
		}catch(Exception e){
			try{
				ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
				HessianOutput out = new HessianOutput(byteOut);
				out.writeObject(object);
				byte[] byteArray=byteOut.toByteArray();
				return byteArray.length;
			}catch(Exception ex){
				e.printStackTrace();
				ex.printStackTrace();
			}
		}
		return -1;
	}
	
	@SuppressWarnings("rawtypes")
	public static String getMapValue(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

}
