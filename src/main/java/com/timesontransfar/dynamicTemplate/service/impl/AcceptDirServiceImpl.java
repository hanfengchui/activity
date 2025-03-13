package com.timesontransfar.dynamicTemplate.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.dynamicTemplate.dao.IDynamicTemplateDao;
import com.timesontransfar.dynamicTemplate.service.IAcceptDirService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component(value="acceptDirService")
public class AcceptDirServiceImpl implements IAcceptDirService {
	protected Logger log = LoggerFactory.getLogger(AcceptDirServiceImpl.class);

	@Autowired
	private IDynamicTemplateDao dynamicTemplateDao;
	@Autowired
	private RedisUtils redisUtils;
	
	@SuppressWarnings("rawtypes")
	@Override
	public String loadDirOne2Two() {
		String key = "loadDirOne2Two";
		String o = redisUtils.get(key, RedisType.WORKSHEET);
		if(o != null) {
			log.info("走redis返回受理目录 :{}",key);
			return  o;
		}
		
		
		List oneList = dynamicTemplateDao.loadDirOne2Two();
		if(null == oneList || oneList.isEmpty())return "";
		JSONArray s1 =  JSONArray.fromObject(oneList);
		for(int i=0;i<s1.size();i++) {
			JSONObject temp = s1.optJSONObject(i);
			String id = temp.optString("REFER_ID");
			List tempList = dynamicTemplateDao.loadSubDirByEntityId(id);
			s1.optJSONObject(i).put("child", JSONArray.fromObject(tempList));
		}
		log.info("一级二级返回:{}" ,s1.toString());
		redisUtils.setex(key,1800,s1.toString(),RedisType.WORKSHEET);
		return s1.toString();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String loadSixDirList(String id) {
        if(StringUtils.isEmpty(id))return null;
        String o = redisUtils.get("loadDirThree2Six_"+id,RedisType.WORKSHEET);
		if(o != null) {
			log.info("走redis返回受理目录 :{}",id);
			return  o;
		}
		
		
		String str = "";
		List tempList = dynamicTemplateDao.loadSubDir(id,true);
		
		//查出父级用于拼装返回
		/*
		List parentList = dynamicTemplateDao.queryFirstTwoObj(id);
		Map p1 = (Map)parentList.get(0);
		List ls2 = pubFunc.getDir("CC_SERVICE_CONTENT_ASK",null,id);
		Map p2 = (Map)ls2.get(0);
		String parentName = p1.get("COL_VALUE_NAME").toString() +"@"+ p2.get("COL_VALUE_NAME").toString();
		String parentId = p1.get("REFER_ID").toString() +"@"+ p2.get("REFER_ID").toString();
		*/
		
		List ls = new ArrayList();
		ls.addAll(tempList);
		for(int i=0;i<tempList.size();i++) {
			Map m1 = (Map)tempList.get(i);
//			m1.put("parentId", parentId);
//			m1.put("parentName", parentName);
//			ls.add(m1);
//			tempList.set(i, m1);
			
			String threeId =  m1.get("id").toString();
			List fourList = dynamicTemplateDao.loadSubDir(threeId,false);
			ls.addAll(fourList);
			
			for(int j=0;j<fourList.size();j++) {
				Map m2 = (Map)fourList.get(j);
				String threeId2 =  m2.get("id").toString();
				List fiveList = dynamicTemplateDao.loadSubDir(threeId2,false);
				ls.addAll(fiveList);
				
				for(int x=0;x<fiveList.size();x++) {
					Map m3 = (Map)fiveList.get(x);
					String id3 =  m3.get("id").toString();
					List sixist = dynamicTemplateDao.loadSubDir(id3,false);
					ls.addAll(sixist);
				}
			}
			
		}
		str = JSONArray.fromObject(ls).toString();
		log.info("loadSixDirList: \n" ,str);

		redisUtils.setex("loadDirThree2Six_"+id,1800,str,RedisType.WORKSHEET);
		return str;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public String loadBanjieDirOne2Two() {
		String key = "loadBanjieDirOne2Two";
		String o = redisUtils.get(key,RedisType.WORKSHEET);
		if(o != null) {
			log.info("走redis返回办结原因 :{}",key);
			return  o;
		}
		
		List oneList = dynamicTemplateDao.loadBanjieDirOne2Two();
		if(null == oneList || oneList.isEmpty())return "";
		JSONArray s1 =  JSONArray.fromObject(oneList);
		for(int i=0;i<s1.size();i++) {
			JSONObject temp = s1.optJSONObject(i);
			String id = temp.optString("REFER_ID");
			List tempList = dynamicTemplateDao.loadSubDirByEntityId(id);
			s1.optJSONObject(i).put("child", JSONArray.fromObject(tempList));
		}
		log.info("一级二级返回:{}" ,s1.toString());
		redisUtils.setex(key,1800,s1.toString(),RedisType.WORKSHEET);
		return s1.toString();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String loadBanjieSixDirList(String id) {
        if(StringUtils.isEmpty(id))return null;
        String o = redisUtils.get("loadBanjieDirThree2Six_"+id,RedisType.WORKSHEET);
		if(o != null) {
			log.info("走redis返回办结原因:{}",id);
			return  o;
		}
		
		String str = "";
		List tempList = dynamicTemplateDao.loadSubDir(id,true);
		
		List ls = new ArrayList();
		ls.addAll(tempList);
		for(int i=0;i<tempList.size();i++) {
			Map m1 = (Map)tempList.get(i);
		
			String threeId =  m1.get("id").toString();
			List fourList = dynamicTemplateDao.loadSubDir(threeId,false);
			ls.addAll(fourList);
			
			for(int j=0;j<fourList.size();j++) {
				Map m2 = (Map)fourList.get(j);
				String threeId2 =  m2.get("id").toString();
				List fiveList = dynamicTemplateDao.loadSubDir(threeId2,false);
				ls.addAll(fiveList);
				
				for(int x=0;x<fiveList.size();x++) {
					Map m3 = (Map)fiveList.get(x);
					String id3 =  m3.get("id").toString();
					List sixist = dynamicTemplateDao.loadSubDir(id3,false);
					ls.addAll(sixist);
				}
			}
			
		}
		str = JSONArray.fromObject(ls).toString();
		log.info("loadBanjieSixDirList: \n" ,str);

		redisUtils.setex("loadBanjieDirThree2Six_"+id,1800,str,RedisType.WORKSHEET);
		return str;
	}

}
