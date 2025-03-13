package com.timesontransfar.dynamicTemplate.dao;

import java.util.List;

import net.sf.json.JSONArray;

import com.redis.pojo.dynamicTemplate.*;
import com.templet.pojo.TemplateAttrPojo;
import com.templet.pojo.TemplateElementAttrPojo;
import com.templet.pojo.TemplateElementPojo;
import com.templet.pojo.TemplateRmp;

@SuppressWarnings("rawtypes")
public interface IDynamicTemplateDao {

	List queryAllTemplateAnswer(String orderId, boolean hisFlag);

	List loadDirOne2Two();

	List loadSubDirByEntityId(String id);

	List queryFirstTwoObj(String threeId);
	
	List<String> loadRowListByOrderId(String orderId,boolean hisFlag);
	
	public List loadBanjieDirOne2Two();

	List loadSubDir(String id,boolean boo);
		
	List<TemplateEle> loadEleByRowId(String rowId);
	
	List<AttrEle> loadAttrListByRowId(String eleId);
	
	String queryTemplateId(String sixId, String prodId);
	
	List<TemplateRmp> queryAllTemplate(String name,String dir);
	
	int saveContent(String id,String content);

	List queryRowInfo(String templateId);

	List queryAllEle(String tamplateId,String eleName);
	
	int[] deleteEle(String rowId,JSONArray eleArr);
	
	int[] addEle(String rowId,JSONArray eleArr);
	
	int deleteRow(String rowId);//删除行
	
	int createRow(String templateId);//新建行
	
	int updateRow(String rowId,int sort);//修改行序号
	
	int saveTemplate(String templateName, int sixId, String content);
	
	int removeTemplate(String templateId);//修改行序号
	/**
	 * 
	 * Description: 根据行查询对应行保存得内容<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param rowId
	 * @return <br>
	 * @CreateDate 2020年8月10日 下午2:27:29 <br>
	 */
	List queryTemplateldServiceContent(String rowId,String orderId, boolean hisFlag);
	
	int queryTemplateType(String templateId);
	
	public String queryFinishTemplateId(String sixId);
	
	public List<String> loadRowListByTemplateId(String templateId);
	
	public List queryAllFinishTemplateAnswer(String orderId);
	
	public String queryFormatContent(String templateId);
	
	public String queryJudgeIdByOrderId(String orderId);
	
	public List<TemplateAttrPojo> queryTemplateAttr(String attrId,String attrName);

	public int addTemplateAttr(TemplateAttrPojo attrPojo);
	
	public int updateTemplateAttr(TemplateAttrPojo attrPojo);
	
	public int delTemplateAttr(TemplateAttrPojo attrPojo);

	public List<TemplateElementPojo> queryTemplateElement(String eleId, String eleName);

	public int addTemplateElement(TemplateElementPojo elePojo);
	
	public int updateTemplateElement(TemplateElementPojo elePojo);
	
	public int delTemplateElement(TemplateElementPojo elePojo);
	
	public List<TemplateRmp> queryTemplateRmp(String tempId, String tempName);
	
	public List<TemplateAttrPojo> queryTemplateAttrByEle(String eleId);

	public int updateTemplateEleRelaAttr(TemplateElementAttrPojo pojo);

}
