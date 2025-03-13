/**
 * <p>类名：ServiceOrderAskImpl.java</p>
 * <p>功能描叙：业务受理功能实现类</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：1、增加定单预警/告警查询方法getServOrderByAlarm cjw April 9, 2008</p>
 * <p>  Create by:	lifeng	Mar 19, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.labelConfig.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.labelConfig.Constant;
import com.timesontransfar.labelConfig.ResultObj;
import com.timesontransfar.labelConfig.dao.ILabelRulesDao;
import com.timesontransfar.labelConfig.dao.ILabelTemplateDao;
import com.timesontransfar.labelConfig.pojo.LabelClass;
import com.timesontransfar.labelConfig.pojo.LabelDepartment;
import com.timesontransfar.labelConfig.pojo.LabelGroup;
import com.timesontransfar.labelConfig.pojo.LabelInsertPoint;
import com.timesontransfar.labelConfig.pojo.LabelInsertPointreference;
import com.timesontransfar.labelConfig.pojo.LabelProRef;
import com.timesontransfar.labelConfig.pojo.LabelRules;
import com.timesontransfar.labelConfig.pojo.LabelTemplate;
import com.timesontransfar.labelConfig.pojo.LabelWay;
import com.timesontransfar.labelConfig.service.ILabelManagerService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author lifeng
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Component(value="labelManagerService")
public class LabelManagerServiceImpl implements ILabelManagerService {
    /**
     * Logger for this class
     */
    private static final Logger logger = LoggerFactory.getLogger(LabelManagerServiceImpl.class);
    @Resource
    private ILabelRulesDao  labelRulesDao;
    @Resource
	private ILabelTemplateDao labelTemplateDao;
    @Autowired
	private PubFunc pubFunc;
	private List<Map<String,String>> logicSymbolList=new ArrayList<>();
	private List<Map<String,String>> nextLogicsymbolList=new ArrayList<>();

	public LabelManagerServiceImpl(){
		Map map1=new HashMap();
		map1.put("logicSymbol", "in");
		map1.put("logicSymbolValue", "IN");
		Map map2=new HashMap();
		map2.put("logicSymbol", ">");
		map2.put("logicSymbolValue", "大于");
		Map map3=new HashMap();
		map3.put("logicSymbol", "<");
		map3.put("logicSymbolValue", "小于");
		Map map4=new HashMap();
		map4.put("logicSymbol", "like");
		map4.put("logicSymbolValue", "包含");
		Map map5=new HashMap();
		map5.put("logicSymbol", "=");
		map5.put("logicSymbolValue", "等于");
		logicSymbolList.add(map4);
		logicSymbolList.add(map5);
		
		
		
		Map map11=new HashMap();
		map11.put("nextLogicsymbol", "and");
		map11.put("nextLogicsymbolValue", "AND");
		Map map12=new HashMap();
		map12.put("nextLogicsymbol", "or");
		map12.put("nextLogicsymbolValue", "OR");
		nextLogicsymbolList.add(map11);
		nextLogicsymbolList.add(map12);
		
	}
	
    //标签树数据--完
	@Override
	public JSONArray initDTreeDataNew() {
		try{
			//查寻标签类别信息--父节点
			List<Map> list=labelTemplateDao.queryLabelClass();
			//查寻标签组信息--子节点
			List<Map> childList=labelTemplateDao.queryLabelGroupNode();
			//查寻标签信息--子节点
			List<Map> labelTemplateList=labelTemplateDao.queryLabelTemplate();
			
			if(!list.isEmpty()){
				JSONArray arr=new JSONArray();
				for(Map m:list){
					JSONObject obj=new JSONObject();
					obj.put("id", String.valueOf(m.get("REFER_ID")));
					obj.put("label", String.valueOf(m.get("COL_VALUE_NAME")));
					JSONArray arrK=new JSONArray();
					//组转子节点信息
					if(!childList.isEmpty()){
						for(Map k:childList){
							JSONObject objK=new JSONObject();
							if(String.valueOf(k.get("LABEL_CLASS_ID")).equals(String.valueOf(m.get("REFER_ID")))){
								objK.put("id", String.valueOf(k.get("LABEL_GROUP_ID")));
								objK.put("lavel", "2");
								String state=String.valueOf(k.get("state"));
								if(null!=state&&!"".equals(state)){
									if(!state.equals("1")){
										objK.put("label",(String.valueOf(k.get("LABEL_GROUP_NAME"))+" (停用)"));
									}else{
										objK.put("label",String.valueOf(k.get("LABEL_GROUP_NAME")));
									}
								}
								JSONArray arrJ=new JSONArray();
								if(!labelTemplateList.isEmpty()){
									for(Map j:labelTemplateList){
										if(String.valueOf(j.get("LABEL_GROUP_ID")).equals(String.valueOf(k.get("LABEL_GROUP_ID")))){
											JSONObject objJ=new JSONObject();
											objJ.put("id", String.valueOf(j.get("label_id")));
											String stateJ=String.valueOf(j.get("state"));
											if(null!=stateJ&&!"".equals(stateJ)){
												if(!state.equals("1")){
													objJ.put("label",(String.valueOf(j.get("label_name"))+" (停用)"));
												}else{
													objJ.put("label",String.valueOf(j.get("label_name")));
												}
											}
											arrJ.add(objJ);
											objJ.clear();
										}
									}
								}
								if(!arrJ.isEmpty()){
									objK.put("children", arrJ);
								}
								arrK.add(objK);
								objK.clear();
							}
						}
						if(!arrK.isEmpty()){
							obj.put("children", arrK);
						}
					}
					arr.add(obj);
					obj.clear();
				}
				return arr;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return new JSONArray();
	}
	
	
	
	//标签模板操作----开始
	
	/**
	 * 增加标签模板
	 */
	@Override
	public ResultObj addLabelTemplate(LabelTemplate label) throws Exception {
		ResultObj result=new ResultObj();
		result.setResultCode("0000");
		result.setResultMsg("标签添加成功！");
		result.setResultFlag("true");
		if(!labelTemplateDao.isLabelTemplateCreate(label.getLabelName(),label.getLabelId())){
			String regionId=label.getRegionId();
			String [] listRegionId=regionId.split(",");
			for(int i=0;i<listRegionId.length;i++){
				//部门已经配置的标签数
				int addlabelCount=labelTemplateDao.queryLabelConfiguredCount(listRegionId[i]);
				//部门可以配置的标签数
				int addlabelValue=labelTemplateDao.queryLabelConfigureValue(listRegionId[i]);
				if(addlabelValue-addlabelCount<=0){
					result.setResultCode("99999");
					result.setResultFlag("false");
					String orgName=pubFunc.getOrgName(listRegionId[i]);
					result.setResultMsg("["+orgName+"]超过允许配置的标签数量！");
					logger.warn("["+orgName+"]超过允许配置的标签数量！");
					return result;
				}
			}
			
			labelTemplateDao.saveLabelTemplate(label);
			//添加标签规则
			if(label.getLabelWayId().equals(Constant.Label_way_Auto)||label.getLabelWayId().equals(Constant.Label_way_Auto_operation)){
				if(!label.getRuleList().isEmpty()){
					List<LabelRules> list=label.getRuleList();
					for(int i=0;i<list.size();i++){
						if(list.get(i).getText().indexOf("受理目录")!=-1){
							if(list.get(i).getSixId()!=null&&!"".equals(list.get(i).getSixId())){
								labelRulesDao.saveLabelRules(list.get(i));
							}else{
								result.setResultCode("99999");
								result.setResultFlag("false");
								result.setResultMsg("缺少6级受理目录！");
								logger.warn("缺少6级受理目录！");
								throw new RuntimeException("缺少6级受理目录！");
							}
						}else{
							labelRulesDao.saveLabelRules(list.get(i));
						}
						
					}
					
				}else{
					result.setResultCode("99999");
					result.setResultFlag("false");
					result.setResultMsg("标签缺少识别规则！");
					logger.warn("标签缺少识别规则！");
					throw new RuntimeException("标签缺少识别规则！");
				}
				
				
			}else{
				//添加标签属性
				if(!label.getLabelProRefList().isEmpty()){
					List<LabelProRef> list=label.getLabelProRefList();
					for(int i=0;i<list.size();i++){
						labelTemplateDao.saveLabelProRef(list.get(i));
					}
				}
			}
			//添加标签嵌入点
			if(!label.getLabelInsertPointreferenceList().isEmpty()){
				List<LabelInsertPointreference> list=label.getLabelInsertPointreferenceList();
				for(int i=0;i<list.size();i++){
					labelTemplateDao.saveLabelInsertPointreference(list.get(i));
				}
			}else{
				result.setResultCode("99999");
				result.setResultFlag("false");
				result.setResultMsg("标签缺少嵌入点！");
				logger.warn("标签缺少嵌入点！");
				throw new RuntimeException("标签缺少嵌入点！");
			}
		}else{
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("标签名称已经存在！");
			logger.warn("标签名称已经存在！");
		}
		return result;
	}
	
	/**
	 * 修改标签模板
	 */
	@Override
	public ResultObj editLabelTemplate(LabelTemplate label) throws Exception {
		ResultObj result=new ResultObj();
		result.setResultCode("0000");
		result.setResultMsg("修改成功！");
		result.setResultFlag("true");
		//查询所属标签组状态是否为停用
		if(!labelTemplateDao.labelGroupState(label.getLabelId(),label.getLabelGroupId())){
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("该标签所在标签组状态为停用，该标签不可操作！");
			logger.warn("该标签所在标签组状态为停用，该标签不可操作！");
			return result;
		}
		
		if(!labelTemplateDao.isLabelTemplateCreate(label.getLabelName(),label.getLabelId())){
			labelTemplateDao.updateLabelTemplate(label);
			labelTemplateDao.deleteLabelRelusByLabelId(label.getLabelId());
			labelTemplateDao.deleteLabelInsertPointsByLabelId(label.getLabelId());
			//添加标签规则
			if(label.getLabelWayId().equals(Constant.Label_way_Auto)||label.getLabelWayId().equals(Constant.Label_way_Auto_operation)){
				if(!label.getRuleList().isEmpty()){
					List<LabelRules> list=label.getRuleList();
					for(int i=0;i<list.size();i++){
						if(list.get(i).getLeftFieldValue().indexOf("受理目录")!=-1){
							if(list.get(i).getSixId()!=null&&!"".equals(list.get(i).getSixId())){
								labelRulesDao.saveLabelRules(list.get(i));
							}else{
								result.setResultCode("99999");
								result.setResultFlag("false");
								result.setResultMsg("缺少6级受理目录！");
								logger.warn("缺少6级受理目录！");
								throw new RuntimeException("缺少6级受理目录！");
							}
						}else{
							labelRulesDao.saveLabelRules(list.get(i));
						}
					}
					
				}else{
					result.setResultCode("99999");
					result.setResultFlag("false");
					result.setResultMsg("标签缺少识别规则！");
					logger.warn("标签缺少识别规则！");
					throw new RuntimeException("标签缺少识别规则！");
				}
			}else{
				
				//添加标签属性
				if(!label.getLabelProRefList().isEmpty()){
					//删除标签属性
					labelTemplateDao.deleteLabelProRefByLabelId(label.getLabelId());
					List<LabelProRef> list=label.getLabelProRefList();
					for(int i=0;i<list.size();i++){
						labelTemplateDao.saveLabelProRef(list.get(i));
					}
				}
				
			}
			//添加标签嵌入点
			if(!label.getLabelInsertPointreferenceList().isEmpty()){
				List<LabelInsertPointreference> list=label.getLabelInsertPointreferenceList();
				for(int i=0;i<list.size();i++){
					labelTemplateDao.saveLabelInsertPointreference(list.get(i));
				}
			}else{
				result.setResultCode("99999");
				result.setResultFlag("false");
				result.setResultMsg("标签缺少嵌入点！");
				logger.warn("标签缺少嵌入点！");
				throw new RuntimeException("标签缺少嵌入点！");
			}
			
		}else{
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("标签名称已经存在！");
			logger.warn("标签名称已经存在！");
		}
		
		
		return result;
	}
	
	@Override
	public List<LabelTemplate> queryAllLabelTemplate(int page,int rows,String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId) {
		List<LabelTemplate> resultList=new ArrayList<>();
		try{
			List<Map> labelTemplateList = labelTemplateDao.queryAllLabelTemplate(page,rows,labelName, labelWayId, labelClassId, labelGroupId,labelDepartmentId);
			if(!labelTemplateList.isEmpty()){
				for(Map m:labelTemplateList){
					LabelTemplate lt=new LabelTemplate();
					lt.setLabelId( String.valueOf(m.get("label_id")) );
					lt.setLabelWayId(String.valueOf((m.get("label_way_id"))).trim() );
					
					String state=String.valueOf(m.get("state"));
					if(StringUtils.isNotEmpty(state)){
						if(!state.equals("1")){
							lt.setLabelName( String.valueOf(m.get("label_name")) +" (停用)");
						}else{
							lt.setLabelName( String.valueOf(m.get("label_name")) );
						}
					}
					
					lt.setLabelWayName(String.valueOf(m.get("label_way_name")));
					lt.setLabelNameSpy( String.valueOf(m.get("label_name_spy")));
					lt.setLabelGroupId( String.valueOf(m.get("label_group_id")));
					String labelGroupName=this.getLabelGroupName(String.valueOf(m.get("label_group_name")));
					lt.setLabelGroupName(labelGroupName);
					lt.setLabelClassId(String.valueOf((m.get("label_class_id"))).trim()); //
					lt.setLabelClassName(String.valueOf(m.get("label_class_name")));
					lt.setLabelGlobalMarkId( String.valueOf(m.get("label_global_mark_id")));
					lt.setRuleDesc(String.valueOf(m.get("rule_desc")));
					String regionId = String.valueOf(m.get("region_id"));
					lt.setRegionId(regionId);
					lt.setOrgName(this.getOrgName(regionId));
					lt.setState(String.valueOf(m.get("state")));
					lt.setEffDate(String.valueOf(m.get("eff_date")));
					lt.setExpDate(String.valueOf(m.get("exp_date")));
					lt.setStaffId(String.valueOf(m.get("staff_id")));
					lt.setNote(String.valueOf(m.get("note")));
					resultList.add(lt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	
	private String getLabelGroupName(String labelGroupName) {
		return "null".equals(labelGroupName) ? "无" : labelGroupName;
	}
	
	private String getOrgName(String regionId) {
		String[] reginList = regionId.split(",");
		StringBuilder sb = new StringBuilder();
		for(int i=0;i<reginList.length;i++){
			sb.append(pubFunc.getOrgName(reginList[i])+",");
		}
		return sb.toString();
	}

	@Override
	public int queryLabelTemplateCount(String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId) {
		int result=0;
		try{
			result=labelTemplateDao.queryLabelTemplateCount(labelName, labelWayId, labelClassId, labelGroupId, labelDepartmentId);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	/**查询标签组
	 * 
	 */
	//REGION_ID 适用部门ID 根据用户部门是否有权限显示
	@Override
	public List<LabelGroup> queryLabelGroup(){
		List<LabelGroup> resultList = new ArrayList<>();
		try{
			List<Map> list = labelTemplateDao.queryLabelGroup();
			if(null!=list && !list.isEmpty()){
				for(Map m:list){
					LabelGroup lt = new LabelGroup();
					lt.setLabelClassId(String.valueOf((m.get("LABEL_CLASS_ID"))).trim());
					lt.setLabelGroupId(String.valueOf(m.get("LABEL_GROUP_ID")));
					
					String state = String.valueOf(m.get("STATE"));
					if(null!=state && !"".equals(state)){
						if(!state.equals("1")){
							lt.setLabelGroupName(String.valueOf(m.get("LABEL_GROUP_NAME"))+" (停用)");
						}else{
							lt.setLabelGroupName(String.valueOf(m.get("LABEL_GROUP_NAME")));
						}
					}
					lt.setRegionId(String.valueOf(m.get("REGION_ID")));
					lt.setLabelGroupDesc(String.valueOf(m.get("LABEL_GROUP_DESC")));
					resultList.add(lt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	
	/**查询标签类别------静态资源表
	 * 
	 */
	@Override
	public List<LabelClass> queryLabelClass(){
		List<LabelClass> resultList=new ArrayList<>();
		try{
			List<Map> list=labelTemplateDao.queryLabelClass();
			if(!list.isEmpty()){
				for(Map m:list){
					LabelClass lt=new LabelClass();
					lt.setLabelClassID(String.valueOf(m.get("refer_id")));
					lt.setLabelClassName(String.valueOf(m.get("col_value_name")));
					lt.setValue(String.valueOf(m.get("col_value")));
					resultList.add(lt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	
	/**查询识别方式------静态资源表
	 * 
	 */
	@Override
	public List<LabelWay> queryLabelWay(){
		List<LabelWay> resultList=new ArrayList<>();
		try{
			List<Map> list=labelTemplateDao.queryLabelWay();
			if(!list.isEmpty()){
				for(Map m:list){
					LabelWay lt=new LabelWay();
					lt.setLabelWayId(String.valueOf(m.get("refer_id")));
					lt.setLabelWayName(String.valueOf(m.get("col_value_name")));
					lt.setLabelWayValue(String.valueOf(m.get("col_value")));
					resultList.add(lt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultList;
	}

	@Override
	public List<LabelInsertPoint> queryLabelInsertPointForTree() {
		List<LabelInsertPoint> resultList=new ArrayList<>();
		List<LabelInsertPoint> nodeList=new ArrayList<>();
		List<LabelInsertPoint> nodeListTemp=new ArrayList<>();
		List<LabelInsertPoint> clNodeList=new ArrayList<>();
		try{
			List<Map> list=labelTemplateDao.queryLabelInsertPoint();
			if(!list.isEmpty()){
				for(Map m:list){
					LabelInsertPoint lt=new LabelInsertPoint();
					lt.setLabelInsertId(String.valueOf(m.get("refer_id")));
					lt.setLabelInsertPointsName(String.valueOf(m.get("col_value_name")));
					lt.setId(String.valueOf(m.get("refer_id")));
					lt.setText(String.valueOf(m.get("col_value_name")));
					lt.setLabelInsertPointsValue(String.valueOf(m.get("col_name")));
					resultList.add(lt);
				}
			}
			for(int i=0;i<resultList.size();i++){
				LabelInsertPoint l=resultList.get(i);
				String value=l.getLabelInsertPointsValue();
				if(null!=value&&!"".equals(value)&&!"null".equals(value)){
					if(value.lastIndexOf("?")==-1){
						l.setChecked("disabled");
						nodeListTemp.add(l);
						l.setId(pubFunc.crtGuid());
						nodeList.add(l);
					}else{
						clNodeList.add(l);
					}
				}else{
					l.setChecked("disabled");
					nodeListTemp.add(l);
					l.setId(pubFunc.crtGuid());
					nodeList.add(l);
				}
			}
			
			
			for(int i=0;i<nodeList.size();i++){
				LabelInsertPoint l=nodeList.get(i);
				String value=l.getLabelInsertPointsValue();
				List<LabelInsertPoint> childList=null;
				if(null!=value&&!"".equals(value)&&!"null".equals(value)){
					childList=new ArrayList<>();
				    for(int k=0;k<clNodeList.size();k++){
						LabelInsertPoint la=clNodeList.get(k);
						String valueTemp=la.getLabelInsertPointsValue();
						if(value.equals(valueTemp.substring(0, (valueTemp.indexOf("jsp")+3)))){
							LabelInsertPoint lc=new LabelInsertPoint();
							lc.setId(la.getLabelInsertId());
							lc.setText(la.getText());
							lc.setLabelInsertId(la.getLabelInsertId());
							lc.setLabelInsertPointsName(la.getLabelInsertPointsName());
							lc.setLabelInsertPointsValue(la.getLabelInsertPointsValue());
							childList.add(lc);
						}
					}
				}
				l.setChildren(childList);
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return nodeList;
	}

	
	/**查询规则左边变量值------静态资源表
	 * 
	 */
	@Override
	public List<LabelRules> queryLabelRuleLeftField(){
		List<LabelRules> resultList=new ArrayList<>();
		try{
			List<Map> list=labelTemplateDao.queryLabelRuleLeftField();
			if(!list.isEmpty()){
				for(Map m:list){
					LabelRules lt=new LabelRules();
					lt.setLeftFieldId(String.valueOf(m.get("refer_id")));
					lt.setLeftFieldValue(String.valueOf(m.get("col_value_name")));
					resultList.add(lt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	
	/**查询适用部门
	 * 
	 */
	@Override
	public List<LabelDepartment> queryLabelDepartment(String regionId){
		List<LabelDepartment> resultList=new ArrayList<>();
		try{
			List<Map> list=labelTemplateDao.queryLabelDepartment(regionId);
			if(!list.isEmpty()){
				for(Map m:list){
					LabelDepartment lt=new LabelDepartment();
					lt.setOrgId(String.valueOf(m.get("org_id")));
					lt.setOrgName(String.valueOf(m.get("org_name")));
					resultList.add(lt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return resultList;
	}
	
	
	//修改标签模板

	/**
	 * 通过标签模板ID查询标签模板的信息
	 * @param label_id
	 * @return
	 */
	public LabelTemplate queryLabelByLabelId(String labelId){
		Map m=labelTemplateDao.queryLabelByLabelId(labelId);
		LabelTemplate lt=new LabelTemplate();
		lt.setLabelId( String.valueOf(m.get("label_id")) );
		lt.setLabelWayId(String.valueOf((m.get("label_way_id"))).trim() );
		lt.setLabelWayName(String.valueOf(m.get("label_way_name")));
		lt.setLabelName( String.valueOf(m.get("label_name")) );
		lt.setLabelNameSpy( String.valueOf(m.get("label_name_spy")));
		lt.setLabelGroupId( String.valueOf(m.get("label_group_id")));
		String labelGroupName=this.getLabelGroupName(String.valueOf(m.get("label_group_name")));
		lt.setLabelGroupName(labelGroupName);
		lt.setLabelClassId(String.valueOf((m.get("label_class_id"))).trim()); //
		lt.setLabelClassName(String.valueOf(m.get("label_class_name")));
		lt.setLabelGlobalMarkId( String.valueOf(m.get("label_global_mark_id")));
		lt.setRuleDesc(String.valueOf(m.get("rule_desc")));
		String labelRules=String.valueOf(m.get("label_rules"));
		if(this.isEmpty(labelRules)){
			lt.setRulesStr("");
		}else{
			lt.setRulesStr(String.valueOf(m.get("label_rules")));
		}
		String labelRulesDesc=String.valueOf(m.get("rule_desc"));
		if(this.isEmpty(labelRulesDesc)){
			lt.setRuleDesc("");
		}else{
			lt.setRuleDesc(String.valueOf(m.get("rule_desc")));
		}

		String regionId = String.valueOf(m.get("region_id"));
		lt.setRegionId(regionId);
		lt.setOrgName(this.getOrgName(regionId));
		
		lt.setState(String.valueOf(m.get("state")));
		lt.setEffDate(String.valueOf(m.get("eff_date")));
		lt.setExpDate(String.valueOf(m.get("exp_date")));
		lt.setStaffId(String.valueOf(m.get("staff_id")));
		String note=String.valueOf(m.get("note"));
		if(this.isEmpty(note)){
			lt.setNote("");
		}else{
			lt.setNote(String.valueOf(m.get("note")));
		}
		
		List<Map> insertPoints = labelTemplateDao.queryLabelInsertPoints(lt.getLabelId());
		StringBuilder insertPointId = new StringBuilder();
		if(!insertPoints.isEmpty()){
			for(int i=0;i<insertPoints.size();i++){
				insertPointId.append(insertPoints.get(i).get("LABEL_INSERT_POINTS_ID")+",");
			}
		}
		lt.setInsertPointId(insertPointId.toString());
		
		List<Map> labelPro = labelTemplateDao.queryLabelProRef(lt.getLabelId());
		StringBuilder labelProId = new StringBuilder();
		StringBuilder labelProName = new StringBuilder();
		if(!labelPro.isEmpty()){
			for(int i=0;i<labelPro.size();i++){
				labelProId.append(labelPro.get(i).get("LABEL_PRO_ID")+",");
				labelProName.append(labelPro.get(i).get("LABEL_PROP_NAME")+",");
			}
		}
		if(labelProId.length() == 0){
			lt.setLabelProId("");
			lt.setLabelProName("");
		}else{
			lt.setLabelProId(labelProId.substring(0, labelProId.length()-1));
			lt.setLabelProName(labelProName.substring(0, labelProName.length()-1));
		}
		return lt;
	}
	
	private boolean isEmpty(String str) {
		return null==str || "".equals(str) || "null".equals(str);
	}
	
	 /**
     * 查询Label包含的规则
     * @return
     */
	public List<LabelRules> queryLabelRulesByLabelId(String labelId){
		List<LabelRules> labelRules = new ArrayList<>();
		try{
			List<Map> list = labelTemplateDao.queryLabelRulesByLabelId(labelId);
			if(!list.isEmpty()){
				for(Map m : list){
					LabelRules lt = new LabelRules();
					lt.setLabelId(String.valueOf(m.get("LABEL_ID")));
					lt.setLabelRulesId(String.valueOf(m.get("LABEL_RULES_ID")));
					lt.setLeftFieldId(String.valueOf(m.get("LEFT_FIELD_ID")));
					lt.setLogicSymbol(String.valueOf(m.get("LOGIC_SYMBOL")));
					lt.setNextLogicsymbol(String.valueOf(m.get("NEXT_LOGICSYMBOL")));
					lt.setRightContent(String.valueOf(m.get("RIGHT_CONTENT")));
					lt.setSixId(String.valueOf(m.get("SIXTH_DIR_ID")));
					String note = String.valueOf(m.get("RULES_REMARK"));
					if(this.isEmpty(note)){
						lt.setRulesRemark("");
					}else{
						lt.setRulesRemark(note);
					}
					lt.setRulesSort(String.valueOf(m.get("RULES_SORT")));
					lt.setLeftFieldValue(String.valueOf(m.get("LEFT_FIELD_VALUE")));
					
					this.setLogicSymbolValue(lt);
					this.setNextLogicsymbolValue(lt);
					
					labelRules.add(lt);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return labelRules;
		
	}
	
	private void setLogicSymbolValue(LabelRules lt) {
		for(int i=0; i<logicSymbolList.size(); i++){
			Map mm = logicSymbolList.get(i);
			if(mm.get("logicSymbol").toString().equals(lt.getLogicSymbol())){
				lt.setLogicSymbolValue(mm.get("logicSymbolValue").toString());
			}
		}
	}
	
	private void setNextLogicsymbolValue(LabelRules lt) {
		for(int i=0; i<nextLogicsymbolList.size(); i++){
			Map mm = nextLogicsymbolList.get(i);
			if(mm.get("nextLogicsymbol").toString().equals(lt.getNextLogicsymbol())){
				lt.setNextLogicsymbolValue(mm.get("nextLogicsymbolValue").toString());
			}
		}
	}
	
	/**
	 * 查询标签规则---关系符号
	 */
	@Override
	public List<Map<String,String>> queryLogicSymbol(){
		return this.logicSymbolList;
	}
	
	/**
	 * 查询标签规则---逻辑符
	 */
	@Override
	public List<Map<String,String>> queryNextLogicSymbol(){
		return this.nextLogicsymbolList;
	}
	
	//标签模板操作----完
	
	/**
	 * 新增标签组
	 */
	@Override
	public ResultObj addLabelGroup(LabelGroup lg) {
		ResultObj result=new ResultObj();
		result.setResultCode("99999");
		result.setResultFlag("false");
		result.setResultMsg("保存失败！");
		if(!labelTemplateDao.isLabelGroupCreate(lg.getLabelGroupName(),lg.getLabelGroupId())){
			int res=labelTemplateDao.saveLabelGroup(lg);
			if(res>0){
				result.setResultCode("0000");
				result.setResultFlag("true");
				result.setResultMsg("保存成功！");
			}
		}else{
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("标签组名称已经存在！");
			logger.warn("标签组名称已经存在！");
		}
		return result;
	}
	
	/**
	 * 查询标签组详细信息
	 */
	@Override
	public LabelGroup queryLabelGroupByLabelGroupId(String labelGroupId) {
		LabelGroup lt=new LabelGroup();
		try{
			Map m=labelTemplateDao.queryLabelGroupByGroupId(labelGroupId);
			lt.setLabelClassId( String.valueOf((m.get("label_class_id"))));
			lt.setLabelGroupId(String.valueOf(m.get("label_group_id")));
			lt.setLabelGroupName(String.valueOf(m.get("label_group_name")));
			lt.setRegionId(String.valueOf(m.get("region_id")));
			
			String note=String.valueOf(m.get("label_group_desc"));
			if(null==note||"".equals(note)||"null".equals(note)){
				lt.setLabelGroupDesc("");
			}else{
				lt.setLabelGroupDesc(String.valueOf(m.get("label_group_desc")));
			}
			lt.setState(String.valueOf(m.get("state")));
		}catch(Exception e){
			e.printStackTrace();
		}
		return lt;
	}

	/**
	 * 更新标签组信息
	 * @param lg
	 * @return
	 */
	public ResultObj updateLabelGroup(LabelGroup lg){
		ResultObj result=new ResultObj();
		result.setResultCode("99999");
		result.setResultFlag("false");
		result.setResultMsg("修改失败！");
		if(!labelTemplateDao.isLabelGroupCreate(lg.getLabelGroupName(),lg.getLabelGroupId())){
			int res=labelTemplateDao.updateLabelGroup(lg);
			if(res>0){
				result.setResultCode("0000");
				result.setResultFlag("true");
				result.setResultMsg("修改成功！");
			}
		}else{
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("标签组名称已经存在！");
			logger.warn("标签组名称已经存在！");
		}
		return result;
	}
	
	/**
	 * 删除标签组和包含的标签
	 * @param label_group_id
	 * @param list
	 * @return
	 */
	@Override
	public ResultObj deleteLabelGroup(String labelGroupId,
			List<LabelTemplate> list) {
		ResultObj result=new ResultObj();
		result.setResultCode("99999");
		result.setResultFlag("false");
		result.setResultMsg("删除失败！");
		try{
			int res=labelTemplateDao.deleteLabelGroup(labelGroupId);
			if(res<0){
				result.setResultCode("99999");
				result.setResultFlag("false");
				result.setResultMsg("删除失败！");
				logger.warn("删除失败！");
				throw new RuntimeException("删除失败！");
			}
			if(!list.isEmpty()){
				for(int i=0;i<list.size();i++){
					labelTemplateDao.deleteLabelTemplate(list.get(i).getLabelId());
				}
			}
			result.setResultCode("0000");
			result.setResultFlag("true");
			result.setResultMsg("删除成功！");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 删除标签
	 * @param label_group_id
	 * @param list
	 * @return
	 */
	@Override
	public ResultObj deleteLabelTemplate(String labelId) {
		ResultObj result=new ResultObj();
		result.setResultCode("99999");
		result.setResultFlag("false");
		result.setResultMsg("删除失败！");
		try{
			
			labelTemplateDao.deleteLabelTemplate(labelId);
			result.setResultCode("0000");
			result.setResultFlag("true");
			result.setResultMsg("删除成功！");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public ResultObj batchDeleteLabelTemplate(List list) {
		ResultObj result=new ResultObj();
		result.setResultCode("99999");
		result.setResultFlag("false");
		result.setResultMsg("删除失败！");
		try{
			
			labelTemplateDao.batchDeleteLabelTemplate(list);
			result.setResultCode("0000");
			result.setResultFlag("true");
			result.setResultMsg("删除成功！");
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ResultObj addLabelpro(String labelProId, String labelProName) {
		ResultObj result=new ResultObj();
		result.setResultCode("99999");
		result.setResultFlag("false");
		result.setResultMsg("新增失败！");
		try{
			
			int res=labelTemplateDao.addLabelpro( labelProId,  labelProName);
			if(res>0){
				result.setResultCode("0000");
				result.setResultFlag("true");
				result.setResultMsg("新增成功！");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}	
	
	
	/**标签主键获取-32随机字符
	 * 
	 */
	@Override
	public String queryLabelId(){
		return pubFunc.crtGuid();
	}
	
	public Map queryLabelProList(){
		Map map=new HashMap();
		try{
			List<Map> list=labelTemplateDao.queryLabelProList();
			if(list!=null){
				for(Map m:list){
					map.put(String.valueOf(m.get("label_prop_id")),String.valueOf(m.get("label_prop_name")));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return map;
	}
	
	public List<Map<String, String>> getLogicSymbolList() {
		return logicSymbolList;
	}

	public void setLogicSymbolList(List<Map<String, String>> logicSymbolList) {
		this.logicSymbolList = logicSymbolList;
	}

	public List<Map<String, String>> getNextLogicsymbolList() {
		return nextLogicsymbolList;
	}

	public void setNextLogicsymbolList(List<Map<String, String>> nextLogicsymbolList) {
		this.nextLogicsymbolList = nextLogicsymbolList;
	}





	

	
}
