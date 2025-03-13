package com.timesontransfar.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.templet.pojo.TemplateAttrPojo;
import com.templet.pojo.TemplateElementAttrPojo;
import com.templet.pojo.TemplateElementPojo;
import com.templet.pojo.TemplateRmp;
import com.timesontransfar.dynamicTemplate.dao.IDynamicTemplateDao;
import com.timesontransfar.dynamicTemplate.service.IAcceptDirService;
import com.timesontransfar.dynamicTemplate.service.IDynamicTemplateService;

import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class DynamicTemplatController {
	private static Logger log = LoggerFactory.getLogger(DynamicTemplatController.class);
	
	@Autowired
	private IDynamicTemplateService dynamicTemplateService;
	@Autowired
	private IAcceptDirService acceptDirService;
	@Autowired
	private IDynamicTemplateDao dynamicTemplateDao;
	
	
	@PostMapping(value = "/workflow/template/loadDirOne2Two")
	public String loadDirOne2Two() {
		return acceptDirService.loadDirOne2Two();
	}
	
	@PostMapping(value = "/workflow/template/loadDirThree2Six")
	public String loadDirThree2Six(@RequestParam(value="id", required=true) String id) {
		return acceptDirService.loadSixDirList(id);
	}
	
	@PostMapping(value = "/workflow/template/loadBanjieDirOne2Two")
	public String loadBanjieDirOne2Two() {
		return acceptDirService.loadBanjieDirOne2Two();
	}
	
	@PostMapping(value = "/workflow/template/loadBanjieDirThree2Six")
	public String loadBanjieDirThree2Six(@RequestParam(value="id", required=true) String id) {
		return acceptDirService.loadBanjieSixDirList(id);
	}
	
	@PostMapping(value = "/workflow/template/loadTemplateBySixId")
	public String loadTemplateBySixId(
			@RequestParam(value="sixId", required=true) String sixId, 
			@RequestParam(value="prodId", required=true) String prodId, 
			@RequestParam(value="portalFlag", required=false) String portalFlag) {
		String arr = dynamicTemplateService.loadTemplateBySixId(sixId, prodId, portalFlag);
		if(arr != null) {
			log.info("loadTemplateBySixId >>>\n {}",arr);
		}
		return arr == null ? "" : arr;
	}
	
	@PostMapping(value = "/workflow/template/loadTemplateInfoHtml")
	public String loadTemplateInfoHtml(@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="hisFlag", required=false) String hisFlag) {
		return dynamicTemplateService.loadTemplateInfoHtml(orderId, hisFlag);
	}
	
	@PostMapping(value = "/workflow/template/queryAllTemplateAnswer")
	public List queryAllTemplateAnswer(@RequestParam(value="orderId", required=true) String orderId, @RequestParam(value="hisFlag", required=false) boolean hisFlag) {
		return dynamicTemplateDao.queryAllTemplateAnswer(orderId, hisFlag);
	}
	
	@PostMapping(value = "/workflow/template/queryAllTemplate")
	public List<TemplateRmp> queryAllTemplate(
			@RequestParam(value="name", required=false) String name,
			@RequestParam(value="dir", required=false) String dir) {
		List<TemplateRmp> list = dynamicTemplateService.queryAllTemplate(name,dir);
		return list;
	}
	
	@PostMapping(value = "/workflow/template/saveContent")
	public int saveContent(
			@RequestParam(value="id", required=true) String id,
			@RequestParam(value="content", required=true) String content) {
		log.info("id:{}   content:{}",id,content);
		return dynamicTemplateService.saveContent(id,content);
	}
	
	@PostMapping(value = "/workflow/template/queryRowInfo")
	public List queryRowInfo(@RequestParam(value="template_Id", required=false) String templateId) {
		List list = dynamicTemplateService.queryRowInfo(templateId);
		return list;
	}
	
	@PostMapping(value = "/workflow/template/queryEle")
	public List queryAllEle(@RequestParam(value="rowId", required=false) String rowId,
							@RequestParam(value="eleName", required=false) String eleName) {
		List list = dynamicTemplateService.queryAllEle(rowId,eleName);
		return list;
	}
	
	@PostMapping(value = "/workflow/template/updateEle")
	public int updateEle(@RequestParam(value="rowId", required=false) String rowId,
							@RequestParam(value="eleId", required=false) String eleId,
							@RequestParam(value="flag", required=false) String flag) {
		return dynamicTemplateService.updateEle(rowId, eleId, flag);
	}
	
	@PostMapping(value = "/workflow/template/deleteRow")
	public int deleteRow(@RequestParam(value="rowId", required=false) String rowId) {
		return dynamicTemplateService.deleteRow(rowId);
	}
	
	@PostMapping(value = "/workflow/template/createRow")
	public int createRow(@RequestParam(value="templateId", required=false) String templateId) {
		return dynamicTemplateService.createRow(templateId);
	}
	
	@PostMapping(value = "/workflow/template/updateRow")
	public int updateRow(@RequestParam(value="rowId", required=false) String rowId,
							@RequestParam(value="sort", required=false) String sort) {
		return dynamicTemplateService.updateRow(rowId,Integer.parseInt(sort));
	}
	
	@PostMapping(value = "/workflow/template/saveTemplate")
	public int saveTemplate(@RequestParam(value="template_name", required=false) String templateName,
							@RequestParam(value="six_id", required=false) String sixId,
							@RequestParam(value="content", required=false) String content) {
		return  dynamicTemplateService.saveTemplate(templateName, Integer.parseInt(sixId), content);
	}
	
	@PostMapping(value = "/cs/workflow/template/removeTemplate")
	public int removeTemplate(@RequestParam(value="template_id", required=false) String templateId) {
		return  dynamicTemplateService.removeTemplate(templateId);
	}
	
	@PostMapping(value = "/workflow/template/loadFinishTemplateBySixId")
	public String loadFinishTemplateBySixId(@RequestParam(value="sixId", required=true) String sixId) {
		String arr = dynamicTemplateService.loadFinishTemplateBySixId(sixId);
		if(arr != null) {
			log.info("loadFinishTemplateBySixId >>>\n {}",arr);
		}
		return arr == null ? "" : arr;
	}
	
	@PostMapping(value = "/workflow/template/queryAllFinishTemplateAnswer")
	public List queryAllFinishTemplateAnswer(@RequestParam(value="orderId", required=true) String orderId) {
		return dynamicTemplateDao.queryAllFinishTemplateAnswer(orderId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/queryJudgeIdByOrderId")
	public Object queryJudgeIdByOrderId(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String orderId = json.optString("orderId");
		return dynamicTemplateDao.queryJudgeIdByOrderId(orderId);
	}
	
	@GetMapping(value = "/workflow/template/queryTemplateAttr")
	public List<TemplateAttrPojo> queryTemplateAttr(@RequestParam(value="attrId", required=false)String attrId,
													@RequestParam(value="attrName", required=false)String attrName){
		return this.dynamicTemplateService.queryTemplateAttr(attrId,attrName);
	}
	
	@GetMapping(value = "/workflow/template/addTemplateAttr")
	public int addTemplateAttr(@RequestParam(value="attrId", required=false)String attrId,
							   @RequestParam(value="attrName", required=false)String attrName,
							   @RequestParam(value="attrDesc", required=false)String attrDesc){
		TemplateAttrPojo attrPojo = new TemplateAttrPojo();
		attrPojo.setAttrId(attrId);
		attrPojo.setAttrName(attrName);
		attrPojo.setAttrDesc(attrDesc);
		return this.dynamicTemplateService.addTemplateAttr(attrPojo);
	}
	
	@GetMapping(value = "/workflow/template/updateTemplateAttr")
	public int updateTemplateAttr(@RequestParam(value="attrId", required=false)String attrId,
								  @RequestParam(value="attrName", required=false)String attrName,
								  @RequestParam(value="attrDesc", required=false)String attrDesc){
		TemplateAttrPojo attrPojo = new TemplateAttrPojo();
		attrPojo.setAttrId(attrId);
		attrPojo.setAttrName(attrName);
		attrPojo.setAttrDesc(attrDesc);
		return this.dynamicTemplateService.updateTemplateAttr(attrPojo);
	}
	
	@GetMapping(value = "/workflow/template/delTemplateAttr")
	public int delTemplateAttr(@RequestParam(value="attrId", required=false)String attrId){
		TemplateAttrPojo attrPojo = new TemplateAttrPojo();
		attrPojo.setAttrId(attrId);
		return this.dynamicTemplateService.delTemplateAttr(attrPojo);
	}
	
	@GetMapping(value = "/workflow/template/queryTemplateElement")
	public List<TemplateElementPojo> queryTemplateElement(@RequestParam(value="eleId", required=false)String eleId,
														  @RequestParam(value="eleName", required=false)String eleName){
		return this.dynamicTemplateService.queryTemplateElement(eleId,eleName);
	}

	@GetMapping(value = "/workflow/template/addTemplateElement")
	public int addTemplateElement(@RequestParam(value="eleId", required=false)String eleId,
								  @RequestParam(value="eleName", required=false)String eleName,
								  @RequestParam(value="eleType", required=false)String eleType,
								  @RequestParam(value="eleBtn", required=false)String eleBtn,
								  @RequestParam(value="eleEvent", required=false)String eleEvent,
								  @RequestParam(value="eleDesc", required=false)String eleDesc,
								  @RequestParam(value="height", required=false)String height,
								  @RequestParam(value="aliasName", required=false)String aliasName,
								  @RequestParam(value="inputType", required=false)String inputType){
		TemplateElementPojo elePojo = new TemplateElementPojo();
		elePojo.setEleId(eleId);
		elePojo.setEleName(eleName);
		elePojo.setEleType(eleType);
		elePojo.setEleBtn(eleBtn);
		elePojo.setEleEvent(eleEvent);
		elePojo.setEleDesc(eleDesc);
		elePojo.setHeight(height);
		elePojo.setAliasName(aliasName);
		elePojo.setInputType(inputType);
		return this.dynamicTemplateService.addTemplateElement(elePojo);
	}
	
	@GetMapping(value = "/workflow/template/updateTemplateElement")
	public int updateTemplateElement(@RequestParam(value="eleId", required=false)String eleId,
									@RequestParam(value="eleName", required=false)String eleName,
									@RequestParam(value="eleType", required=false)String eleType,
									@RequestParam(value="eleBtn", required=false)String eleBtn,
									@RequestParam(value="eleEvent", required=false)String eleEvent,
									@RequestParam(value="eleDesc", required=false)String eleDesc,
									@RequestParam(value="height", required=false)String height,
									@RequestParam(value="aliasName", required=false)String aliasName,
									@RequestParam(value="inputType", required=false)String inputType){
		TemplateElementPojo elePojo = new TemplateElementPojo();
		elePojo.setEleId(eleId);
		elePojo.setEleName(eleName);
		elePojo.setEleType(eleType);
		elePojo.setEleBtn(eleBtn);
		elePojo.setEleEvent(eleEvent);
		elePojo.setEleDesc(eleDesc);
		elePojo.setHeight(height);
		elePojo.setAliasName(aliasName);
		elePojo.setInputType(inputType);
		return this.dynamicTemplateService.updateTemplateElement(elePojo);
	}
	
	@GetMapping(value = "/workflow/template/delTemplateElement")
	public int delTemplateElement(@RequestParam(value="eleId", required=false)String eleId){
		TemplateElementPojo elePojo = new TemplateElementPojo();
		elePojo.setEleId(eleId);
		return this.dynamicTemplateService.delTemplateElement(elePojo);
	}
	
	@GetMapping(value = "/workflow/template/queryTemplateRmp")
	public List<TemplateRmp> queryTemplateRmp(@RequestParam(value="tempId", required=false)String tempId,
											  @RequestParam(value="tempName", required=false)String tempName){
		return this.dynamicTemplateService.queryTemplateRmp(tempId,tempName);
	}

	@GetMapping(value = "/workflow/template/queryTemplateAttrByEle")
	public List<TemplateAttrPojo> queryTemplateAttrByEle(@RequestParam(value="eleId", required=true)String eleId){
		return this.dynamicTemplateService.queryTemplateAttrByEle(eleId);
	}

	@GetMapping(value = "/workflow/template/queryTemplateAttrAndByEle")
	public List queryTemplateAttrAndByEle(@RequestParam(value="eleId", required=true)String eleId,
										  @RequestParam(value="attrName", required=false)String attrName){
		return this.dynamicTemplateService.queryTemplateAttrAndByEle(eleId,attrName);
	}

	@GetMapping(value = "/workflow/template/updateTemplateEleRelaAttr")
	public int updateTemplateEleRelaAttr(@RequestParam(value="eleId", required=false)String eleId,
								  		@RequestParam(value="attrId", required=false)String attrId,
								  		@RequestParam(value="flag", required=false)String flag,
										@RequestParam(value="parentAttr", required=false)String parentAttr){
		TemplateElementAttrPojo pojo = new TemplateElementAttrPojo();
		pojo.setEleId(eleId);
		pojo.setAttrId(attrId);
		pojo.setFlag(flag);
		pojo.setParentAttr(parentAttr);
		return this.dynamicTemplateService.updateTemplateEleRelaAttr(pojo);
	}
}