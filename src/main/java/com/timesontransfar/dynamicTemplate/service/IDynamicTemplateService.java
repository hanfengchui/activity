package com.timesontransfar.dynamicTemplate.service;

import java.util.List;

import com.templet.pojo.TemplateAttrPojo;
import com.templet.pojo.TemplateElementAttrPojo;
import com.templet.pojo.TemplateElementPojo;
import com.templet.pojo.TemplateRmp;

@SuppressWarnings("rawtypes")
public interface IDynamicTemplateService {
	
	String loadTemplateBySixId(String sixId, String prodId, String portalFlag);
	
	List<TemplateRmp> queryAllTemplate(String name,String dir);
	
	int saveContent(String id,String content);
	
	List queryRowInfo(String tamplateId);
	
	List queryAllEle(String rowId,String eleName);
	
	int updateEle(String rowId, String eleId,String flag);
	
	int deleteRow(String rowId);//删除行
	
	int createRow(String templateId);//新建行
	
	int updateRow(String rowId,int sort);//修改行序号
	
	int saveTemplate(String templateName,int sixId,String content);//修改行序号
	
	int removeTemplate(String templateId);//修改行序号

	/**
	 * Description: 新查询动态模板展示内容<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param orderId
	 * @return <br>
	 * @CreateDate 2020年8月10日 下午2:18:43 <br>
	 */
	String loadTemplateInfoHtml(String orderId, String hisFlag);
	
	public String loadFinishTemplateBySixId(String sixId);
	
	public List<TemplateAttrPojo> queryTemplateAttr(String attrId, String attrName);
	
	public int addTemplateAttr(TemplateAttrPojo attrPojo);
	
	public int updateTemplateAttr(TemplateAttrPojo attrPojo);
	
	public int delTemplateAttr(TemplateAttrPojo attrPojo);
	
	public List<TemplateElementPojo> queryTemplateElement(String eleId, String eleName);

	public int addTemplateElement(TemplateElementPojo elePojo);
	
	public int updateTemplateElement(TemplateElementPojo elePojo);
	
	public int delTemplateElement(TemplateElementPojo elePojo);

	public List<TemplateRmp> queryTemplateRmp(String tempId, String tempName);

	public List<TemplateAttrPojo> queryTemplateAttrByEle(String eleId);

	public List queryTemplateAttrAndByEle(String eleId,String attrName);
	
	public int updateTemplateEleRelaAttr(TemplateElementAttrPojo pojo);

}