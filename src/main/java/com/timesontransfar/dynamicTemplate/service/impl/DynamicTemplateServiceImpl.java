package com.timesontransfar.dynamicTemplate.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.redis.pojo.dynamicTemplate.AttrEle;
import com.redis.pojo.dynamicTemplate.TemplateEle;
import com.templet.pojo.TemplateAttrPojo;
import com.templet.pojo.TemplateElementAttrPojo;
import com.templet.pojo.TemplateElementPojo;
import com.templet.pojo.TemplateRmp;
import com.timesontransfar.dynamicTemplate.dao.IDynamicTemplateDao;
import com.timesontransfar.dynamicTemplate.service.IDynamicTemplateService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Component(value="dynamicTemplateService")
public class DynamicTemplateServiceImpl implements IDynamicTemplateService {
	protected Logger log = LoggerFactory.getLogger(DynamicTemplateServiceImpl.class);

	@Autowired
	private IDynamicTemplateDao dynamicTemplateDao;

	@Autowired
	private RedisUtils redisUtils;
	
	@Override
	public String loadTemplateInfoHtml(String orderId, String hisFlag) {
		if(StringUtils.isEmpty(orderId))return "";
		
		boolean flag = "1".equals(hisFlag);
		//1:取出模板对应所有行 
		JSONArray arr = new JSONArray();
		List<String> rowList = dynamicTemplateDao.loadRowListByOrderId(orderId, flag);
		if(rowList.isEmpty())return arr.toString();
		for(int i=0;i<rowList.size();i++) {
			String rowId = rowList.get(i);
			//2:根据行取所有元素及元素对应得选择值
			List elList = dynamicTemplateDao.queryTemplateldServiceContent(rowId, orderId, flag);
			if(!elList.isEmpty()){
				arr.add(elList);
			}
		}
		return arr.toString();
	}
	
	@Override
	public String loadTemplateBySixId(String sixId, String prodId, String portalFlag) {
		if(StringUtils.isEmpty(sixId))return null;
		
		String keyId = StringUtils.isNotBlank(portalFlag) ? "__"+portalFlag : "";
		String o = redisUtils.get("TEMPLATE_ID__"+sixId+"__"+prodId+keyId, RedisType.WORKSHEET);
		if(o != null) {
			log.info("redis获取建单模板 sixId: {} prodId: {}", sixId, prodId);
			return o;
		}
		
		String templateId = dynamicTemplateDao.queryTemplateId(sixId, prodId);
		if(StringUtils.isEmpty(templateId)) {
			templateId = dynamicTemplateDao.queryTemplateId(sixId, null);
		}
		if(StringUtils.isEmpty(templateId)) {
			return null;
		}
		
		//1:取出模板对应所有行 
		JSONArray arr = new JSONArray();
		List<String> rowList = dynamicTemplateDao.loadRowListByTemplateId(templateId);
		if(rowList.isEmpty())return null;
		for(int i=0;i<rowList.size();i++) {
			String rowId = rowList.get(i);
			//2:根据行取所有元素
			arr.add(getRowArr(rowId, templateId, portalFlag));
		}
		
		int templateType = dynamicTemplateDao.queryTemplateType(templateId);
		String formatContent = dynamicTemplateDao.queryFormatContent(templateId);
		JSONObject obj = new JSONObject();
		obj.put("arr", arr);
		obj.put("template_id", templateId);
		obj.put("template_type", templateType);
		obj.put("format_content", formatContent);
		
		redisUtils.setex("TEMPLATE_ID__"+sixId+"__"+prodId+keyId, 1800, obj.toString(), RedisType.WORKSHEET);
		return obj.toString();
	}
	
	private JSONArray getRowArr(String rowId,String templateId,String portalFlag) {
		List<TemplateEle> eleList = dynamicTemplateDao.loadEleByRowId(rowId);
		JSONArray rowArr = new JSONArray();
		for(int i=0;i<eleList.size();i++) {
			JSONObject obj = new JSONObject();

			String eleId = eleList.get(i).getEleId();
			String aliasName = eleList.get(i).getAliasName();
			String label = eleList.get(i).getEleName();
			int type = eleList.get(i).getEleType();
			int colSpan = eleList.get(i).getColSpan();
			int height = eleList.get(i).getHeight();
			int necessary = eleList.get(i).getIsNecessary();
			int hidden = eleList.get(i).getIsHidden();
			int disabled = eleList.get(i).getIsDisabled();
			String desc = eleList.get(i).getEleDesc();
			
			log.info("eleId: {} eleName: {}", eleId, label);
			obj.put("id", eleId);
			obj.put("key", "");
			obj.put("ele_id", eleId);
			obj.put("alias_name", aliasName);
			obj.put("lable", label);
			obj.put("type", type);
			obj.put("colSpan", colSpan);
			obj.put("height", height);
			obj.put("desc", desc == null ? "" : desc);
			obj.put("necessary", necessary);
			obj.put("is_hidden", hidden);
			obj.put("is_disabled", "1".equals(portalFlag) ? 1 : disabled);//一键受理页面，所有元素禁用
			obj.put("template_id", templateId);
			this.setEleAttr(obj, type, eleId, eleList.get(i));
			rowArr.add(obj);
		}
		return rowArr;
	}
	
	private void setEleAttr(JSONObject obj, int type, String eleId, TemplateEle ele) {
		if(type == 1) {
			List<AttrEle> ls = dynamicTemplateDao.loadAttrListByRowId(eleId);
			for(int x = 0;x<ls.size();x++) {
				String name = ls.get(x).getAttrName();
				String attrId = ls.get(x).getAttrId();
				String desc = ls.get(x).getAttrDesc();
				obj.put("rl_"+ (x + 1), name);
				obj.put("id_"+ (x + 1), attrId);
				obj.put("rl_"+ (x + 1) + "_desc", desc);
			}
		}
		else if(type == 2 || type == 3 || type == 9) {
			List<AttrEle> ls = dynamicTemplateDao.loadAttrListByRowId(eleId);
			obj.put("cks", getCks(type,ls));
			if(type == 2) {
				obj.put("checkList", "[]");
			} else if(type == 9) {
				obj.put("selectList", "[]");
			}
		}
		else if(type == 10) {
			List<AttrEle> ls = dynamicTemplateDao.loadAttrListByRowId(eleId);
			obj.put("cks", this.getCascaderCks(ls));
		}
		else if(type == 11) {//数字类型，null表示控件值为空
			obj.put("key", null);
		}
		obj.put("btn", ele.getEleBtn());
		obj.put("func", ele.getEleEvent());
		obj.put("inputType", ele.getInputType());
	}
	
	private JSONArray getCks(int type,List<AttrEle> ls) {
		JSONArray attrArr = new JSONArray();
		if(type == 3 || type == 9) {
			for(int i =0;i<ls.size();i++) {
				JSONObject o = new JSONObject();
				o.put("label", ls.get(i).getAttrName());
				o.put("value", ls.get(i).getAttrId());
				attrArr.add(o);
			}
		}
		if(type == 2) {
			for(int i =0;i<ls.size();i++) {
				JSONObject o = new JSONObject();
				o.put("label", ls.get(i).getAttrName());
				o.put("key", ls.get(i).getAttrId());
				attrArr.add(o);
			}
		}
		return attrArr;
	}
	
	private JSONArray getCascaderCks(List<AttrEle> ls) {
		JSONArray attrArr = new JSONArray();
		for(int i=0;i<ls.size();i++) {
			JSONObject o = new JSONObject();
			o.put("value", ls.get(i).getAttrId());
			o.put("label", ls.get(i).getAttrName());
			if(ls.get(i).getParentAttr() == 1) {
				List<AttrEle> childls = dynamicTemplateDao.loadAttrListByRowId(ls.get(i).getAttrId());
				o.put("children", this.getCascaderCks(childls));
			}
			attrArr.add(o);
		}
		return attrArr;
	}

	@Override
	public List<TemplateRmp> queryAllTemplate(String name,String dir) {
		return dynamicTemplateDao.queryAllTemplate(name,dir);
	}
	
	@Override
	public List queryRowInfo(String tamplateId) {
		return dynamicTemplateDao.queryRowInfo(tamplateId);
	}
	
	@Override
	public int saveContent(String id,String content) {
		return dynamicTemplateDao.saveContent(id, content);
	}

	@Override
	public List queryAllEle(String rowId,String eleName) {
		return dynamicTemplateDao.queryAllEle(rowId,eleName);
	}
	
	@Override
	@SuppressWarnings("static-access")
	public int updateEle(String rowId, String eleId, String flag) {
		JSONArray json = new JSONArray();
		JSONArray arr = json.fromObject(eleId);
		
		int[] num = new int[0];
		if("left".contentEquals(flag)) {
			num = dynamicTemplateDao.deleteEle(rowId, arr);
		}else if("right".contentEquals(flag)) {
			num = dynamicTemplateDao.addEle(rowId, arr);
		}
		return num.length;
	}

	@Override
	public int deleteRow(String rowId) {
		return dynamicTemplateDao.deleteRow(rowId);
	}

	@Override
	public int createRow(String templateId) {
		return dynamicTemplateDao.createRow(templateId);
	}

	@Override
	public int updateRow(String rowId, int sort) {
		return dynamicTemplateDao.updateRow(rowId,sort);
	}

	@Override
	public int saveTemplate(String templateName, int sixId, String content) {
		return dynamicTemplateDao.saveTemplate(templateName, sixId, content);
	}

	@Override
	public int removeTemplate(String templateId) {
		return dynamicTemplateDao.removeTemplate(templateId);
	}
	
	public String loadFinishTemplateBySixId(String sixId) {
		if(StringUtils.isEmpty(sixId))return null;
		
		String o = redisUtils.get("FINISH_TEMPLATE_ID__"+sixId, RedisType.WORKSHEET);
		if(o != null) {
			log.info("redis获取结案模板 sixId: {}", sixId);
			return o;
		}
		
		String templateId = dynamicTemplateDao.queryFinishTemplateId(sixId);
		if(StringUtils.isEmpty(templateId))return null;
		
		//1:取出模板对应所有行 
		JSONArray arr = new JSONArray();
		List<String> rowList = dynamicTemplateDao.loadRowListByTemplateId(templateId);
		if(rowList.isEmpty())return null;
		for(int i=0;i<rowList.size();i++) {
			String rowId = rowList.get(i);
			//2:根据行取所有元素
			arr.add(getRowArr(rowId,templateId,null));
		}
		
		String formatContent = dynamicTemplateDao.queryFormatContent(templateId);
		JSONObject obj = new JSONObject();
		obj.put("arr", arr);
		obj.put("template_id", templateId);
		obj.put("format_content", formatContent);
		redisUtils.setex("FINISH_TEMPLATE_ID__"+sixId, 1800, obj.toString(), RedisType.WORKSHEET);
		return obj.toString();
	}
	
	public List<TemplateAttrPojo> queryTemplateAttr(String attrId, String attrName){
		return this.dynamicTemplateDao.queryTemplateAttr(attrId,attrName);
	}
	
	public int addTemplateAttr(TemplateAttrPojo attrPojo) {
		List<TemplateAttrPojo> attrList = this.dynamicTemplateDao.queryTemplateAttr(attrPojo.getAttrId(), "");
		if (attrList != null && !attrList.isEmpty()){
			log.info("存在相同的主键的属性");
			return 101;
		}else{
			return this.dynamicTemplateDao.addTemplateAttr(attrPojo);
		}
	}

	public int updateTemplateAttr(TemplateAttrPojo attrPojo){
		return this.dynamicTemplateDao.updateTemplateAttr(attrPojo);
	}
	
	public int delTemplateAttr(TemplateAttrPojo attrPojo){
		return this.dynamicTemplateDao.delTemplateAttr(attrPojo);
	}

	public int updateTemplateEleRelaAttr(TemplateElementAttrPojo pojo){
		return this.dynamicTemplateDao.updateTemplateEleRelaAttr(pojo);
	}
	
	public List<TemplateElementPojo> queryTemplateElement(String eleId, String eleName){
		return this.dynamicTemplateDao.queryTemplateElement(eleId,eleName);
	}

	public int addTemplateElement(TemplateElementPojo elePojo){
		return this.dynamicTemplateDao.addTemplateElement(elePojo);
	}
	
	public int updateTemplateElement(TemplateElementPojo elePojo){
		return this.dynamicTemplateDao.updateTemplateElement(elePojo);
	}
	
	public int delTemplateElement(TemplateElementPojo elePojo){
		return this.dynamicTemplateDao.delTemplateElement(elePojo);
	}
	public List<TemplateRmp> queryTemplateRmp(String tempId, String tempName){
		return this.dynamicTemplateDao.queryTemplateRmp(tempId,tempName);
	}

	public List<TemplateAttrPojo> queryTemplateAttrByEle(String eleId){
		if(StringUtils.isEmpty(eleId)) {
			log.info("eleId为空，请输入element id");
			return Collections.emptyList();
		}
		return this.dynamicTemplateDao.queryTemplateAttrByEle(eleId);
	}

	public List queryTemplateAttrAndByEle(String eleId,String attrName){
		if(StringUtils.isEmpty(eleId)) {
			log.info("eleId为空，请输入element id");
			return Collections.emptyList();
		}
		List list = new ArrayList();
		list.add(this.dynamicTemplateDao.queryTemplateAttr("",attrName));
		list.add(this.dynamicTemplateDao.queryTemplateAttrByEle(eleId));
		return list;
	}
	
}