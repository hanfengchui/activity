package com.timesontransfar.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.labelConfig.Constant;
import com.timesontransfar.labelConfig.PingYin4j;
import com.timesontransfar.labelConfig.ResultObj;
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
import com.timesontransfar.labelLib.service.ILabelService;
import com.transfar.common.web.ResultUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class LabelManagerController {
	protected Logger log = LoggerFactory.getLogger(LabelManagerController.class);
	@Autowired
	private ILabelManagerService labelManagerService;
	@Autowired
	private ILabelService labelServiceImpl;
	@Autowired
	private PubFunc pubFunc;

	@RequestMapping(value = "/workflow/dynamic/labelManageTree")
	public Object labelManageTree(@RequestBody(required = false) String parm) {
		JSONArray initDTreeData = labelManagerService.initDTreeDataNew();
		List<LabelClass> labelClassList = labelManagerService.queryLabelClass();
		List<LabelWay> labelWayList = labelManagerService.queryLabelWay();
		List<LabelDepartment> labelDepartmentList = labelManagerService.queryLabelDepartment("10");
		JSONObject obj = new JSONObject();
		obj.put("labelTree", initDTreeData);
		obj.put("labelClass", labelClassList);
		obj.put("LabelWayList", labelWayList);
		obj.put("LabelDepartmentList", labelDepartmentList);
		return ResultUtil.success(obj);
	}

	@RequestMapping(value = "/workflow/dynamic/editLabelTemplateInitData")
	public Object editLabelTemplateInitData(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String labelId = obj.optString("labelId");
		LabelTemplate label = labelManagerService.queryLabelByLabelId(labelId);
		JSONObject result = new JSONObject();
		result.put("label", label);
		return ResultUtil.success(result);
	}

	@RequestMapping(value = "/workflow/dynamic/queryLabelGroupByLabelClass")
	public Object queryLabelGroupByLabelClass(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String labelClassId = obj.optString("labelClassId");
		List<LabelGroup> labelGroupList = labelManagerService.queryLabelGroup();
		JSONArray arr = new JSONArray();
		for (LabelGroup labelGroup : labelGroupList) {
			if (labelGroup.getLabelClassId().equals(labelClassId)) {
				arr.add(labelGroup);
			}
		}
		return arr;
	}

	@RequestMapping(value = "/workflow/dynamic/queryAllLabelTemplate")
	public Object queryAllLabelTemplate(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		List<LabelTemplate> list = new ArrayList<LabelTemplate>();
		int count = 0;
		try {
			JSONObject strWhere = obj.optJSONObject("strWhere");
			String labelGroupId = strWhere.optString("labelGroupId");
			String labelClassId = strWhere.optString("labelClassId");
			String labelWayId = strWhere.optString("labelWayId");
			String labelName = strWhere.optString("labelName");
			String labelDepartmentId = strWhere.optString("labelDepartmentId");
			String pageStr = obj.optString("currentPage");
			String rowsStr = obj.optString("pageSize");// 接受参数page和rows
			int page = 1;
			int rows = 10;
			if (null != pageStr && !"".equals(pageStr)) {
				page = Integer.parseInt(pageStr);
			}
			if (null != rowsStr && !"".equals(rowsStr)) {
				rows = Integer.parseInt(rowsStr);
			}
			list = labelManagerService.queryAllLabelTemplate(page, rows, labelName, labelWayId, labelClassId,
					labelGroupId, labelDepartmentId);
			count = labelManagerService.queryLabelTemplateCount(labelName, labelWayId, labelClassId,
					labelGroupId, labelDepartmentId);
			JSONObject resultMap = new JSONObject();
			resultMap.put("total", count);
			resultMap.put("rows", list);
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@RequestMapping(value = "/workflow/dynamic/queryLabelInsertPointListNew")
	public Object queryLabelInsertPointListNew(@RequestBody(required = false) String parm) {
		List<LabelInsertPoint> labelInsertPoint = labelManagerService.queryLabelInsertPointForTree();
		return ResultUtil.success(labelInsertPoint);
	}

	@RequestMapping(value = "/workflow/dynamic/queryRulesByLabelId")
	public Object queryRulesByLabelId(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String labelId = obj.optString("labelId");
		List<LabelRules> labelRulesList = labelManagerService.queryLabelRulesByLabelId(labelId);
		return labelRulesList;
	}

	@RequestMapping(value = "/workflow/dynamic/queryLabelLeftField")
	public Object queryLabelLeftField(@RequestBody(required = false) String parm) {
		List<LabelRules> labelLeftList = labelManagerService.queryLabelRuleLeftField();
		return ResultUtil.success(labelLeftList);
	}

	@RequestMapping(value = "/workflow/dynamic/queryLogicSymbol")
	public Object queryLogicSymbol(@RequestBody(required = false) String parm) {
		List<Map<String, String>> list = labelManagerService.queryLogicSymbol();
		return ResultUtil.success(list);
	}
	
	private static final String ZEROTIME = " 00:00:00";

	@RequestMapping(value = "/workflow/dynamic/addLabelTemplate")
	public Object addLabelTemplate(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);

		// 标签ID----32为字符
		String labelId = labelManagerService.queryLabelId();
		ResultObj result = new ResultObj();
		if (null != labelId && !"".equals(labelId)) {
			// 标签规则集合
			String rules = obj.optString("rules");
			// 规则字符串
			String rulesStr = obj.optString("rulesStr");
			if (null == rulesStr || rulesStr.equals("")) {
				rulesStr = "null";
			}
			String rulesStrDesc = obj.optString("rulesStrDesc");
			if (null == rulesStrDesc || rulesStrDesc.equals("")) {
				rulesStrDesc = "null";
			}

			List<LabelRules> ruleList = null;
			if (null != rules && !"".equals(rules) && !"[]".equals(labelId)) {
				ruleList = JSON.parseArray(rules, LabelRules.class);
			}
			// 获取参数
			String labelName = obj.optString("labelName"); // 标签名
			String labelNameSpy = PingYin4j.getPinyin(labelName);
			if (null == labelNameSpy || labelNameSpy.equals("")) {
				labelNameSpy = labelName;
			}
			String labelClass = obj.optString("labelClass"); // 标签类别
			String labelGroup = obj.optString("labelGroup"); // 标签组
			if (null != labelGroup && labelGroup.equals("0")) {
				labelGroup = "null";
			}
			String labelWay = obj.optString("labelWay"); // 标签识别方式
			String effDate = obj.optString("effDate"); // 生效时间
			String expDate = obj.optString("expDate"); // 失效时间
			String note = obj.optString("note"); // 描述
			if (null == note || note.equals("")) {
				note = "null";
			}

			String labelProId = obj.optString("labelProId"); // 标签属性ID
			if (null == labelProId || labelProId.equals("")) {
				labelProId = "null";
			}
			String labelProName = obj.optString("labelProName"); // 标签属性名
			if (null == labelProName || labelProName.equals("")) {
				labelProName = "null";
			}

			String labelDepartment = obj.optString("labelDepartment"); // 适用部门
			String labelLabelInsertPoint = obj.optString("labelLabelInsertPoint"); // 标签嵌入点

			LabelTemplate template = new LabelTemplate();
			template.setLabelId(labelId);
			template.setLabelWayId(labelWay); // 1手动 2自动
			template.setLabelName(labelName);
			template.setLabelNameSpy(labelNameSpy);
			template.setLabelClassId(labelClass); //
			template.setLabelGroupId(labelGroup); //
			template.setLabelGlobalMarkId("1");

			template.setRuleLevelId("1");
			template.setRegionId(labelDepartment);
			template.setState("1");
			template.setStaffId("2181");
			template.setStateDate(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
			template.setEffDate(effDate.substring(0, 10) + ZEROTIME);
			template.setExpDate(expDate.substring(0, 10) + ZEROTIME);
			template.setNote(note);
			template.setRulesStr("null");
			template.setRuleDesc("null");
			template.setLabelProId(labelProId);
			template.setLabelProName(labelProName);

			if (template.getLabelWayId().equals(Constant.Label_way_Auto)
					|| template.getLabelWayId().equals(Constant.Label_way_Auto_operation)) {
				template.setRulesStr(rulesStr);
				template.setRuleDesc(rulesStrDesc);

				int s = 1;
				for (int i = 0; i < ruleList.size(); i++) {
					String labelRulesId = labelManagerService.queryLabelId();
					LabelRules r = ruleList.get(i);
					r.setLabelRulesId(labelRulesId);
					r.setLabelId(labelId);
					r.setRulesSort(String.valueOf(s));
					ruleList.set(i, r);
					s++;
				}

				template.setRuleList(ruleList);
				template.setPropFlag("0");
			} else {
				if ("null".equals(labelProId)) {
					template.setPropFlag("0");
				} else {
					String[] labelProIdArr = labelProId.split(",");
					if (null != labelProIdArr && labelProIdArr.length > 0) {
						List<LabelProRef> labelProIdList = new ArrayList<LabelProRef>();
						for (int i = 0; i < labelProIdArr.length; i++) {
							LabelProRef in = new LabelProRef();
							in.setId(labelManagerService.queryLabelId());
							in.setLabelId(labelId);
							in.setLabelProId(labelProIdArr[i]);
							labelProIdList.add(in);
						}
						template.setLabelProRefList(labelProIdList);
						template.setPropFlag("1");
					} else {
						template.setPropFlag("0");
					}
				}
			}
			// 标签模板---标签嵌入点
			String[] labelLabelInsertPointList = labelLabelInsertPoint.split(",");
			List<LabelInsertPointreference> labelInsertPointList = new ArrayList<LabelInsertPointreference>();
			if (null != labelLabelInsertPointList && labelLabelInsertPointList.length > 0) {
				for (int i = 0; i < labelLabelInsertPointList.length; i++) {
					LabelInsertPointreference in = new LabelInsertPointreference();
					in.setLabelInsertId(labelManagerService.queryLabelId());
					in.setLabelId(labelId);
					in.setLabelInsertPointsId(labelLabelInsertPointList[i]);
					labelInsertPointList.add(in);
				}
				template.setLabelInsertPointreferenceList(labelInsertPointList);
			}

			try {
				result = labelManagerService.addLabelTemplate(template);
			} catch (Exception e) {
				// 保存失败
				e.printStackTrace();
			}

		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("添加标签模板失败！");
		}
		return result;
	}

	@RequestMapping(value = "/workflow/dynamic/editLabelTemplate")
	public Object editLabelTemplate(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		ResultObj result = new ResultObj();
		TsmStaff staff=pubFunc.getLogonStaff();
		// 标签ID----32为字符
		String labelId = obj.optString("labelId");
		if (null != labelId && !"".equals(labelId) && !"[]".equals(labelId)) {
			// 标签规则集合
			String rules = obj.optString("rules");
			List<LabelRules> ruleList = null;
			if (null != rules && !"".equals(rules)) {
				ruleList = JSON.parseArray(rules, LabelRules.class);
			}
			// 规则字符串
			String rulesStr = obj.optString("rulesStr");
			if (null == rulesStr || rulesStr.equals("")) {
				rulesStr = "null";
			}
			String rulesStrDesc = obj.optString("rulesStrDesc");
			if (null == rulesStrDesc || rulesStrDesc.equals("")) {
				rulesStr = "null";
			}

			// 获取参数
			String labelName = obj.optString("labelName"); // 标签名
			String labelNameSpy = PingYin4j.getPinyin(labelName);
			if (null == labelNameSpy || labelNameSpy.equals("")) {
				labelNameSpy = labelName;
			}
			String labelClass = obj.optString("labelClass"); // 标签类别
			String labelGroup = obj.optString("labelGroup"); // 标签组
			if (null != labelGroup && labelGroup.equals("0")) {
				labelGroup = "null";
			}
			String labelWay = obj.optString("labelWay"); // 标签识别方式
			String effDate = obj.optString("effDate"); // 生效时间
			String expDate = obj.optString("expDate"); // 失效时间
			String note = obj.optString("note"); // 描述
			if (null == note || note.equals("")) {
				note = "null";
			}
			String labelDepartment = obj.optString("labelDepartment"); // 适用部门
			String labelLabelInsertPoint = obj.optString("labelLabelInsertPoint"); // 标签嵌入点
			String labelState = obj.optString("labelState");

			String labelProId = obj.optString("labelProId"); // 标签属性ID
			if (null == labelProId || labelProId.equals("")) {
				labelProId = "null";
			}
			String labelProName = obj.optString("labelProName"); // 标签属性名
			if (null == labelProName || labelProName.equals("")) {
				labelProName = "null";
			}

			LabelTemplate template = new LabelTemplate();
			template.setLabelId(labelId);
			template.setLabelWayId(labelWay); // 1手动 2自动
			template.setLabelName(labelName);
			template.setLabelNameSpy(labelNameSpy);
			template.setLabelClassId(labelClass); //
			template.setLabelGroupId(labelGroup); //
			template.setLabelGlobalMarkId("1");

			template.setRuleLevelId("1");
			template.setRegionId(labelDepartment);
			template.setState(labelState);
			template.setStaffId(staff.getId());
			template.setStateDate(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()));
			template.setEffDate(effDate.substring(0, 10) + ZEROTIME);
			template.setExpDate(expDate.substring(0, 10) + ZEROTIME);
			template.setNote(note);
			template.setRulesStr(rulesStr);
			template.setRuleDesc(rulesStrDesc);

			if (template.getLabelWayId().equals(Constant.Label_way_Auto)
					|| template.getLabelWayId().equals(Constant.Label_way_Auto_operation)) {
				template.setRulesStr(rulesStr);
				template.setRuleDesc(rulesStrDesc);

				int s = 1;
				for (int i = 0; i < ruleList.size(); i++) {
					String labelRulesId = labelManagerService.queryLabelId();
					LabelRules r = ruleList.get(i);
					r.setLabelRulesId(labelRulesId);
					r.setLabelId(labelId);
					r.setRulesSort(String.valueOf(s));
					ruleList.set(i, r);
					s++;
				}
				template.setRuleList(ruleList);
				template.setPropFlag("0");
			} else {

				String[] labelProIdArr = labelProId.split(",");
				List<LabelProRef> labelProIdList = new ArrayList<LabelProRef>();
				if (null != labelProIdArr && labelProIdArr.length > 0) {
					for (int i = 0; i < labelProIdArr.length; i++) {
						LabelProRef in = new LabelProRef();
						in.setId(labelManagerService.queryLabelId());
						in.setLabelId(labelId);
						in.setLabelProId(labelProIdArr[i]);
						labelProIdList.add(in);
					}
					template.setLabelProRefList(labelProIdList);
					template.setPropFlag("1");
				} else {
					template.setPropFlag("0");
				}
			}

			// 标签模板---标签嵌入点
			String[] labelLabelInsertPointList = labelLabelInsertPoint.split(",");
			List<LabelInsertPointreference> labelInsertPointList = new ArrayList<LabelInsertPointreference>();
			if (null != labelLabelInsertPointList && labelLabelInsertPointList.length > 0) {
				for (int i = 0; i < labelLabelInsertPointList.length; i++) {
					LabelInsertPointreference in = new LabelInsertPointreference();
					in.setLabelInsertId(labelManagerService.queryLabelId());
					in.setLabelId(labelId);
					in.setLabelInsertPointsId(labelLabelInsertPointList[i]);
					labelInsertPointList.add(in);
				}
				template.setLabelInsertPointreferenceList(labelInsertPointList);
			}
			try {
				result = labelManagerService.editLabelTemplate(template);
			} catch (Exception e) {
				// 保存失败
				e.printStackTrace();
			}

		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("修改标签模板失败！");
		}
		return result;
	}

	@RequestMapping(value = "/workflow/dynamic/deleteLabelGroup")
	public Object deleteLabelGroup(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		ResultObj result = new ResultObj();
		// 标签ID----32为字符
		String labelGroupId = obj.optString("labelGroupId");
		if (null != labelGroupId && !"".equals(labelGroupId)) {
			List<LabelTemplate> list = labelManagerService.queryAllLabelTemplate(1, 1000, "", "", "", labelGroupId,
					"");
			result = labelManagerService.deleteLabelGroup(labelGroupId, list);
		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("删除标签组失败！");
		}
		return result;
	}
	
	@RequestMapping(value = "/workflow/dynamic/deleteLabel")
	public Object deleteLabel(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		ResultObj result = new ResultObj();
		// 标签ID----32为字符
		String labelId = obj.optString("labelId");
		if (null != labelId && !"".equals(labelId)) {
			result = labelManagerService.deleteLabelTemplate(labelId);
		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("删除标签失败！");
		}
		return result;
	}
	
	@RequestMapping(value = "/workflow/dynamic/batchDeleteLabel")
	public Object batchDeleteLabel(@RequestBody(required = false) String parm) {
		JsonObject obj = new Gson().fromJson(parm, JsonObject.class);
		ResultObj result = new ResultObj();
		// 标签ID----32为字符
		JsonArray labelIdArray = obj.getAsJsonArray("labelIdArray");
		List list = new Gson().fromJson(labelIdArray, List.class);
		
		if (null != list && !list.isEmpty()) {
			result = labelManagerService.batchDeleteLabelTemplate(list);
		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("删除标签失败！");
		}
		return result;
	}
	

	@RequestMapping(value = "/workflow/dynamic/cancelLableById")
	public Object cancelLableById(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String labelInstanceId = obj.optString("labelInstanceId");
		return labelServiceImpl.cancelLableById(labelInstanceId);
	}

	@RequestMapping(value = "/workflow/dynamic/saveLabelProAction")
	public Object saveLabelProAction(@RequestBody(required = false) String parm) {
		ResultObj result = new ResultObj();
		JSONObject obj = JSONObject.fromObject(parm);
		// 标签ID----32为字符
		String labelProId = labelManagerService.queryLabelId();
		String labelProName = obj.optString("labelProName");
		if (null != labelProId && !"".equals(labelProId)) {
			result = labelManagerService.addLabelpro(labelProId, labelProName);
		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("删除标签失败！");
		}
		return result;
	}

	@RequestMapping(value = "/workflow/dynamic/saveLabelGroup")
	public Object saveLabelGroup(@RequestBody(required = false) String parm) {
		ResultObj result = new ResultObj();
		JSONObject obj = JSONObject.fromObject(parm);
		// 标签ID----32为字符
		String labelGroupId = labelManagerService.queryLabelId();
		if (null != labelGroupId && !"".equals(labelGroupId)) {
			String labelGroupName = obj.optString("labelGroupName");
			String labelClassId = obj.optString("labelClassId"); // 标签类别
			String labelDepartment = obj.optString("labelDepartment"); // 适用部门
			String labelGroupDesc = obj.optString("labelGroupDesc");
			if (null != labelGroupDesc && labelGroupDesc.equals("")) {
				labelGroupDesc = "null";
			}

			LabelGroup lg = new LabelGroup();
			lg.setLabelGroupId(labelGroupId);
			lg.setLabelClassId(labelClassId);
			lg.setLabelGroupName(labelGroupName);
			lg.setLabelGroupDesc(labelGroupDesc);
			lg.setRegionId(labelDepartment);
			lg.setState("1");
			try {
				result = labelManagerService.addLabelGroup(lg);
				result.setResultObj(lg);
			} catch (Exception e) {
				// 保存失败
				e.printStackTrace();
			}

		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("添加标签模板失败！");
		}
		return result;
	}

	@RequestMapping(value = "/workflow/dynamic/editLabelGroupById")
	public Object editLabelGroupById(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String labelGroupId = obj.optString("labelGroupId");
		LabelGroup lg = labelManagerService.queryLabelGroupByLabelGroupId(labelGroupId);
		JSONObject result = new JSONObject();
		result.put("labelGroup", lg);
		return result;
	}

	@RequestMapping(value = "/workflow/dynamic/updateLabelGroup")
	public Object updateLabelGroup(@RequestBody(required = false) String parm) {
		ResultObj result = new ResultObj();
		JSONObject obj = JSONObject.fromObject(parm);
		String labelGroupId = obj.optString("labelGroupId");
		if (null != labelGroupId && !"".equals(labelGroupId)) {
			String labelGroupName = obj.optString("labelGroupName");
			String labelClassId = obj.optString("labelClassId"); // 标签类别
			String labelDepartment = obj.optString("labelDepartment"); // 适用部门
			String labelGroupDesc = obj.optString("labelGroupDesc");
			String labelGroupState = obj.optString("labelGroupState");
			if (null != labelGroupDesc && labelGroupDesc.equals("")) {
				labelGroupDesc = "null";
			}

			LabelGroup lg = new LabelGroup();
			lg.setLabelGroupId(labelGroupId);
			lg.setLabelClassId(labelClassId);
			lg.setLabelGroupName(labelGroupName);
			lg.setLabelGroupDesc(labelGroupDesc);
			lg.setRegionId(labelDepartment);
			lg.setState(labelGroupState);
			try {
				result = labelManagerService.updateLabelGroup(lg);
			} catch (Exception e) {
				// 保存失败
				e.printStackTrace();
			}
		} else {
			result.setResultCode("99999");
			result.setResultFlag("false");
			result.setResultMsg("添加标签模板失败！");
		}
		return result;
	}

	@RequestMapping(value = "/workflow/dynamic/queryLabelProList")
	public Object queryLabelProList(@RequestBody(required = false) String parm) {
		Map queryLabelProList = labelManagerService.queryLabelProList();
		return queryLabelProList;
	}
}