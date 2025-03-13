package com.timesontransfar.systemPub.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timesontransfar.common.authorization.model.TsmStaff;

import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.XcFlow;
import com.timesontransfar.systemPub.ISystemPubService;
import com.timesontransfar.systemPub.SystemPubQury;
import com.timesontransfar.systemPub.dao.SystemPubDao;
import com.timesontransfar.systemPub.entity.CapchaInfo;
import com.timesontransfar.systemPub.entity.PubColumn;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.utils.StringUtils;
import com.transfar.common.utils.tool.StaticUtil;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import javax.annotation.Resource;

@Component(value = "systemPubService")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SystemPubServiceImpl implements ISystemPubService {
	private static final Logger log = LoggerFactory.getLogger(SystemPubServiceImpl.class);
	
	@Autowired
    private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private SystemPubDao systemPubDao;
	@Autowired
	private SystemPubQury systemPubQury;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IComplaintWorksheetDeal complaintWorksheetDealImpl;
	@Resource
	private RedisUtils redisUtils;
	@Autowired
	private InoteSenList noteSen;
	
	private static final String KEY_ORG_ID = "ORG_ID";
	private static final String KEY_ORG_NAME = "ORG_NAME";
	private static final String KEY_UP_ORG = "UP_ORG";
	private static final String KEY_PARENTNAME = "parentName";
	private static final String KEY_LABEL = "label";
	private static final String KEY_CHILDREN = "children";
	private static final String KEY_PARENTID = "parentId";

	private String getMapValue(Map map, String keyName) {
		return map.get(keyName) == null ? "" : map.get(keyName).toString();
	}
	
	@Override
	public String getSysttemOrgTree(String flag,String parm) {
		JSONArray array = new JSONArray();
		List orgInfo = systemPubDao.queryOrgInfo(flag, parm);
		if (StringUtils.isNull(orgInfo)) {
			return array.toString();
		}
		for (int i = 0; i < orgInfo.size(); i++) {
			Map map = (Map) orgInfo.get(i);
			String orgId = getMapValue(map, "ORG_ID");
			String orgName = getMapValue(map, "ORG_NAME");
			int orgLevel = map.get("ORG_LEVEL") == null ? 0 : Integer.parseInt(map.get("ORG_LEVEL").toString());
			String upOrg = getMapValue(map, "UP_ORG");
			String parentName = getMapValue(map, "parentName");
			
			JSONObject obj = new JSONObject();
			obj.put("id", orgId);
			obj.put("name", orgName);
			if(StringUtils.isNotEmpty(parm) && parm.equals(orgId)){
				obj.put("parentId", 0);
			}else{
				obj.put("parentId", upOrg);
			}
			obj.put("parentName", parentName);
			if (StringUtils.isEmpty(upOrg)) {
				orgLevel = 0;
			}
			obj.put("rank", orgLevel);
			this.setOrgDisabled(obj, getMapValue(map, "isDisabled"));
			
			array.add(obj);
			obj.clear();
		}
		return String.valueOf(array);
	}

	@Override
	public String getOrgTreeLevel() {
		JSONArray array = new JSONArray();
		List orgInfo = systemPubDao.queryOrgInfo("", "");
		if (StringUtils.isNull(orgInfo)) {
			return array.toString();
		}
		JSONArray orgArr = JSONArray.fromObject(orgInfo);
		for (int i = 0; i < orgArr.size(); i++) {
			JSONObject obj = orgArr.optJSONObject(i);
			String orgId = obj.optString(KEY_ORG_ID);
			String orgName = obj.optString(KEY_ORG_NAME);
			if("10".equals(orgId)){
				JSONObject item = new JSONObject();
				item.put("id", orgId);
				item.put(KEY_LABEL, orgName);
				JSONArray children = this.getOrgItem(orgArr, orgId);
				if(!children.isEmpty()){
					item.put(KEY_CHILDREN, children);
				}
				array.add(item);
			}
		}
		return array.toString();
	}
	
	public JSONArray getOrgItem(JSONArray orgArr,String topId){
		JSONArray newArr = new JSONArray();
		for (int i = 0; i < orgArr.size(); i++) {
			JSONObject obj = orgArr.optJSONObject(i);
			String orgId = obj.optString(KEY_ORG_ID);
			String orgName = obj.optString(KEY_ORG_NAME);
			String upOrg = obj.optString(KEY_UP_ORG);
			if(upOrg.equals(topId)){
				JSONObject item = new JSONObject();
				item.put("id", orgId);
				item.put(KEY_LABEL, orgName);
				JSONArray children = this.getOrgItem(orgArr, orgId);
				if(!children.isEmpty()){
					item.put(KEY_CHILDREN, children);
				}
				newArr.add(item);
			}
		}
		return newArr;
	}

	@Override
	public String getStaffByOrganId(int orgId,String staffName,String loginName,String sheetId) {
		if(StringUtils.isEmpty(sheetId)){
			return this.systemPubDao.queryStaffByOrgId(orgId, staffName, loginName, null, null);
		}
		String infos = "";
		String flag = "3";
		String sendRevId = "10";
		//flag = 3 派单（不包含协查部门，二级部门organization_type=1）
		//flag = 4 指定部门派单（全渠道指定三级部门、其他指定二级部门）
		//flag = 5 协查派单（派发全部门，包含协查部门，二级部门organization_type in (1,2)）
		//flag = 6 非投诉类工单派单（派发全部门，从江苏省电信公司开始）
		if(StringUtils.isNotEmpty(sheetId) && sheetId.equals("5")){
			flag = "5";
		}else if(StringUtils.isNotEmpty(sheetId)){
			SheetPubInfo sheetPubInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
			if("3".equals(sheetPubInfo.getServiceOrderId().substring(2, 3))){
				if(pubFunc.queryListByOid(sheetPubInfo.getServiceOrderId()) != null){
					flag = "3";
				}else{
					int pdlimite = pubFunc.checkPDTime(sheetId);
					if(pdlimite <= 0){
						infos = "已超过环节时限不允许派单到其他二级部门";
					} else if(sheetPubInfo.getTacheId() == 720130023 || sheetPubInfo.getTacheId() == 700000086){
						if (sheetPubInfo.getSheetType() == 720130014) {
							infos = "部门内处理单不允许派单到其他二级部门";
						}
						if(StringUtils.isEmpty(infos)){
							int result = complaintWorksheetDealImpl.queryWorkSheetAreaBySheetId(sheetId, sheetPubInfo.getSheetType());
							if(sheetPubInfo.getSheetType() == 700000127 && result==2){
								infos = "部门内处理单不允许派单到其他二级部门";
							}else if(result==0){
								infos = "协办单不允许派单到其他二级部门";
							}
						}
						if (StringUtils.isEmpty(infos)) {
							boolean lastDeal = complaintWorksheetDealImpl.checkLastDeal(sheetId, sheetPubInfo.getSheetType(), sheetPubInfo.getMonth());
							if(!lastDeal){
								infos = "存在上级审批单未完成不允许派单到其他二级部门";
							}
						}
						if (StringUtils.isEmpty(infos)) {
							int countSheet = complaintWorksheetDealImpl.countSheetAreaByOrderId(sheetPubInfo.getServiceOrderId(), 1);
							if ((sheetPubInfo.getSheetType() == 720130013 || sheetPubInfo.getSheetType() == 720130015) && countSheet >= 2) {
								infos = "流转达到两次后不允许派单到其他二级部门";
								flag = this.getOrgFlag(countSheet, sheetPubInfo.getRcvOrgId());
							}
						}
					}
				}
				
			}else{
				flag = "6";
			}
			if(StringUtils.isNotEmpty(infos)){
				sendRevId = this.getSendRevId(flag, sheetPubInfo.getRcvOrgId());
				flag = "4";
			}
		}
		log.info("infos: {} flag: {} sendRevId: {}", infos, flag, sendRevId);
		return this.systemPubDao.queryStaffByOrgId(orgId, staffName, loginName, flag, sendRevId);
	}
	
	@Override
	public String channlAskInfoTree() {
		String [] entityIdList = {"707907004","707907005","707907006"};
		
		List<Map<String,String>> channelList = new ArrayList<>();
		String defaultValue = "";
		for (int i = 0; i < entityIdList.length; i++) {
			List tempchannlask = systemPubQury.getAskChannel(entityIdList[i]);
			for (int j = 0; j < tempchannlask.size(); j++) {
				Map map = (Map) tempchannlask.get(j);
				String referId = map.get("REFER_ID").toString();
				if(channelList.isEmpty()){
					defaultValue = referId;
				}
				channelList.add(map);
			}
		}
		
		//根据当前渠道  向上 向下  遍历对应的层级数据
		List result = systemPubDao.queryAskInfoNew(channelList);
		
		JSONArray tree=new JSONArray();
		result.forEach(item->{
			Map map=(Map)item;
			String orgId=map.get("REFER_ID").toString();
			String orgName=map.get("COL_VALUE_NAME").toString();
			String upOrg=map.get("ENTITY_ID").toString();
			if("201112".equals(upOrg)){
				JSONObject itemObj = new JSONObject();
				itemObj.put("value", orgId);
				itemObj.put("label", orgName);
				JSONArray children =StaticUtil.getPubClounm(result, orgId);
				if(!children.isEmpty()){
					itemObj.put(KEY_CHILDREN, children);
				}
				tree.add(itemObj);
			}
		});
		JSONArray defalutTreeValue=new JSONArray();
		JSONArray defalutTreeOne=new JSONArray();
		JSONObject defalut=new JSONObject();
		defalutTreeValue.add(defalutTreeOne);
		defalutTreeValue.add(defalutTreeOne);
		defalut.put("defalut", "");
		defalutTreeValue.add(defalut);
		List<PubColumn> deafalutTree = new ArrayList<>();
		deafalutTree = systemPubDao.queryPubColumnNew(deafalutTree, defaultValue);
		for (int i = 0; i < deafalutTree.size(); i++) {
			PubColumn pubColumn=deafalutTree.get(i);
			if("201112".equals(pubColumn.getEntityId())){
				defalutTreeValue.optJSONArray(0).add(pubColumn.getReferId());
				defalutTreeValue.optJSONArray(1).add(pubColumn.getColValueName());
				//遍历子集：获得层级数据
				defalutTreeValue=StaticUtil.getChannalTree(deafalutTree, pubColumn.getReferId(), defalutTreeValue);
				if(defalutTreeValue.optJSONObject(2).optString("defalut").length()>0){
					defalutTreeValue=StaticUtil.getChannalTree(deafalutTree, defalutTreeValue.optJSONObject(2).optString("defalut"), defalutTreeValue);
				}
				//取四级
				if(defalutTreeValue.optJSONObject(2).optString("defalut").length()>0){
					defalutTreeValue=StaticUtil.getChannalTree(deafalutTree, defalutTreeValue.optJSONObject(2).optString("defalut"), defalutTreeValue);
				}
				
			}
		}
		JSONObject obj=new JSONObject();
		obj.put("channalTree", tree);
		obj.put("defaultValue", defaultValue);
		obj.put("defaultTree", defalutTreeValue);
		return obj.toString();
	}

	@Override
	public String systemOrgTreeNew(String sheetId) {
		return systemOrgTreeNewSunUdate(sheetId);
	}
	
	//新修改部门树
	private String systemOrgTreeNewSunUdate(String sheetId) {
		log.info("orgTreeCascader logonName: {} sheetId: {}", pubFunc.getLogonStaff().getLogonName(), sheetId);
		
		String infos = "";
		String flag = "3";
		String sendRevId = "10";
		
		//flag = 3 派单（不包含协查部门，二级部门organization_type=1）
		//flag = 4 指定部门派单（全渠道指定三级部门、其他指定二级部门）
		//flag = 5 协查派单（派发全部门，包含协查部门，二级部门organization_type in (1,2)）
		//flag = 6 非投诉类工单派单（派发全部门，从江苏省电信公司开始）
		if(StringUtils.isNotEmpty(sheetId) && sheetId.equals("5")){
			flag = "5";
		}else if(StringUtils.isNotEmpty(sheetId)){
			SheetPubInfo sheetPubInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
			if("3".equals(sheetPubInfo.getServiceOrderId().substring(2, 3))){//投诉类工单
				OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(sheetPubInfo.getServiceOrderId(), false);
				if(pubFunc.queryListByOid(sheetPubInfo.getServiceOrderId()) != null || StaticData.getAskLevelId()[0] != orderInfo.getComeCategory()){
					flag = "3";
				}else{
					int pdlimite = pubFunc.checkPDTime(sheetId);
					if(pdlimite <= 0){
						infos = "已超过环节时限不允许派单到其他二级部门";
					}else if(sheetPubInfo.getTacheId() == 720130023 || sheetPubInfo.getTacheId() == 700000086){
						if (sheetPubInfo.getSheetType() == 720130014) {
							infos = "部门内处理单不允许派单到其他二级部门";
						}
						if(StringUtils.isEmpty(infos)){
							int result = complaintWorksheetDealImpl.queryWorkSheetAreaBySheetId(sheetId, sheetPubInfo.getSheetType());
							if(sheetPubInfo.getSheetType() == 700000127 && result==2){
								infos = "部门内处理单不允许派单到其他二级部门";
							}else if(result==0){
								infos = "协办单不允许派单到其他二级部门";
							}
						}
						if (StringUtils.isEmpty(infos)) {
							boolean lastDeal = complaintWorksheetDealImpl.checkLastDeal(sheetId, sheetPubInfo.getSheetType(), sheetPubInfo.getMonth());
							if(!lastDeal){
								infos = "存在上级审批单未完成不允许派单到其他二级部门";
							}
						}
						if (StringUtils.isEmpty(infos)) {
							int countSheet = complaintWorksheetDealImpl.countSheetAreaByOrderId(sheetPubInfo.getServiceOrderId(), 1);
							if ((sheetPubInfo.getSheetType() == 720130013 || sheetPubInfo.getSheetType() == 720130015) && countSheet >= 2) {
								infos = "流转达到两次后不允许派单到其他二级部门";
								flag = this.getOrgFlag(countSheet, sheetPubInfo.getRcvOrgId());
							}
						}
					}
				}
			}else {
				flag = "6";
			}
			if(StringUtils.isNotEmpty(infos)){
				sendRevId = this.getSendRevId(flag, sheetPubInfo.getRcvOrgId());
				flag = "4";
			}
		}
		log.info("infos: {} flag: {} sendRevId: {}", infos, flag, sendRevId);
		
		//分公司或者省直属，发起协查
		boolean xiechaFlag = this.getXiechaFlag(flag);
		log.info("xiechaFlag: {}", xiechaFlag);
		
		String rediKey = StaticData.REDIS_ORG_TREE_INFO+"@"+flag+"@"+sendRevId+"@"+xiechaFlag;
		String result = redisUtils.get(rediKey, RedisType.WORKSHEET);
		if(StringUtils.isNotEmpty(result)) {
			return result;
		}
		
		JSONArray array = new JSONArray();
		List orgInfo = systemPubDao.queryOrgInfo(flag, sendRevId);
		if (StringUtils.isNotNull(orgInfo)) {
			// 3：派单；5：协查派单，包含协查部门
			if("3".equals(flag) || "5".equals(flag)){
				return this.getAssignOrgTree(flag, orgInfo, array, rediKey, xiechaFlag);
			}
			
			//4：起始部门不是10，全渠道从三级部门、其他从二级部门；6：从10-江苏省电信公司开始展示，剩下的部门拼参数
			for (int i = 0; i < orgInfo.size(); i++) {
				Map map = (Map) orgInfo.get(i);
				String orgId = map.get("ORG_ID") == null ? "" : map.get("ORG_ID").toString();
				String orgName = map.get("ORG_NAME") == null ? "" : map.get("ORG_NAME").toString();
				String isDisable = map.get("isDisabled") == null ? "" : map.get("isDisabled").toString();
				if(sendRevId.equals(orgId)){
					JSONObject obj = new JSONObject();
					obj.put("value", orgId);
					obj.put("label", orgName);
					if(StringUtils.isNotEmpty(isDisable)){
						obj.put("disabled", true);
					}
					JSONArray itemArr = StaticUtil.getOrgTreeItem(orgInfo, orgId);
					if(!itemArr.isEmpty()){
						obj.put("children", itemArr);
					}
					array.add(obj);
					obj.clear();
				}
			}
		}
		redisUtils.setex(rediKey,14400,array.toString(),RedisType.WORKSHEET);
		return array.toString();
	}
	
	private boolean getXiechaFlag(String flag) {
		TsmStaff staff = pubFunc.getLogonStaff();//当前操作员工
		String orgId = staff.getOrganizationId();
		boolean subFlag = pubFunc.isAffiliated(orgId, "11");//全渠道下属部门
		//针对需协查给全渠道的工单，若派单部门为分公司或者省直属，则发起协查时，限制其只能协查给”省投诉投诉处理班”
		if(!subFlag && "5".equals(flag)) {
			return true;
		}
		return false;
	}
	
	private String getAssignOrgTree(String flag, List orgInfo, JSONArray array, String rediKey, boolean xiechaFlag) {
		JSONArray secondArray = systemPubDao.queryOrgTypeInfo(("3".equals(flag) ? 1 : 2));
		for (int i = 0; i < secondArray.size(); i++) {
			JSONObject obj = secondArray.optJSONObject(i);
			String topId = obj.optString("ORG_ID");
			JSONArray childArr = null;
			if(xiechaFlag) {//协查限制
				childArr = this.getOrgTreeItemNew(orgInfo, topId);
			} else {
				childArr = StaticUtil.getOrgTreeItem(orgInfo, topId);
			}
			//二级部门
			JSONObject secondObj = new JSONObject();
			secondObj.put("value", topId);
			secondObj.put("label", obj.optString("ORG_NAME"));
			this.setOrgDisabled(secondObj, obj.optString("isDisabled"));
			if(!childArr.isEmpty()){
				secondObj.put("children", childArr);
			}
			array.add(secondObj);
		}
		//最后补充10
		JSONObject maxObj = new JSONObject();
		maxObj.put("value", "10");
		maxObj.put("label", "江苏省电信公司");
		maxObj.put("disabled", true);
		if(!array.isEmpty()){
			maxObj.put("children", array);
		}
		JSONArray maxArr = new JSONArray();
		maxArr.add(maxObj);
		if(!maxArr.isEmpty()){
			redisUtils.setex(rediKey,14400,maxArr.toString(),RedisType.WORKSHEET);
		}
		return maxArr.toString();
	}
	
	private JSONArray getOrgTreeItemNew(List list,String topId){
		JSONArray newArr = new JSONArray();
		for (int i = 0; i < list.size(); i++) {
			Map map = (Map) list.get(i);
			String orgId = getStringByKey(map, "ORG_ID");
			String orgName = getStringByKey(map, "ORG_NAME");
			String upOrg = getStringByKey(map, "UP_ORG");
			String isDisabled = getStringByKey(map, "isDisabled");
			if(topId.equals(upOrg)){
				//派单部门为分公司或者省直属，需协查给全渠道的工单，限制其只能协查给”省投诉投诉处理班”
				if(this.isOrgFlag(topId, orgId)) {
					continue;
				}
				JSONObject obj = new JSONObject();
				obj.put("value", orgId);
				obj.put("label", orgName);
				if(StringUtils.isNotEmpty(isDisabled)){
					obj.put("disabled", true);
				}
				JSONArray itemArr = this.getOrgTreeItemNew(list, orgId);
				if(!itemArr.isEmpty()){
					obj.put("children", itemArr);
				}
				newArr.add(obj);
				obj.clear();
			}
		}
		return newArr;
	}
	
	private boolean isOrgFlag(String topId, String orgId) {
		//11-省全渠道客户运营服务中心；361143-服务管理部（省投诉中心）；363718-省投诉投诉处理班
		if("11".equals(topId) && (!"361143".equals(orgId))) {
			return true;
		}
		if("361143".equals(topId) && (!"363718".equals(orgId))) {
			return true;
		}
		if("363718".equals(topId)) {
			return true;
		}
		return false;
	}

	private static String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	private String getOrgFlag(int countSheet, String rcvOrgId) {
		String flag = "3";//全省派单
		boolean subFlag = pubFunc.isAffiliated(rcvOrgId, "11");//全渠道下属部门
		if(subFlag && countSheet < 7) {//2次以内可以派全省√  3次-7次全渠道可以派全渠道√  8次以上只能派各自部门√
			flag = "7";
		}
		return flag;
	}
	
	private String getSendRevId(String flag, String rcvOrgId) {
		String sendRevId = pubFunc.getAreaOrgId(rcvOrgId);
		if("7".equals(flag)) {//3次-7次全渠道可以派全渠道√
			sendRevId = "11";
			log.info("全渠道下属部门 流转7次以内可以派全渠道");
		}
		log.info("本单收单部门: {} 转派组织机构树父节点: {}", rcvOrgId, sendRevId);
		return sendRevId;
	}
	
	private void setOrgDisabled(JSONObject obj, String isDisabled) {
		if(org.apache.commons.lang3.StringUtils.isNotBlank(isDisabled) && !"null".equals(isDisabled)){
			obj.put("disabled", true);
		}
	}
	
	@Override
	public String reaSonTree() {
		String reasonTree=redisUtils.get(StaticData.REDIS_REASON_TREE_INFO,RedisType.WORKSHEET);
		if(StringUtils.isNotEmpty(reasonTree)){
			log.info("办结原因缓存取出：");
			return reasonTree;
		}
		JSONArray array=new JSONArray();
		List<PubColumn> list=systemPubDao.queryReasonInfo();
		if(!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				PubColumn pubColumn=list.get(i);
				if("202307".equals(pubColumn.getEntityId())){
					JSONObject item = new JSONObject();
					item.put("value", pubColumn.getReferId());
					item.put("label", pubColumn.getColValueName());
					JSONArray children =StaticUtil.getPubClounmNew(list, pubColumn.getReferId());
					if(!children.isEmpty()){
						item.put(KEY_CHILDREN, children);
					}
					array.add(item);
					item.clear();
				}
			}
		}
		if(!array.isEmpty()){
			redisUtils.setex(StaticData.REDIS_REASON_TREE_INFO,86400,array.toString(),RedisType.WORKSHEET);
		}
		return array.toString();
	}

	@Override
	public String getPubColomndefaultTree(int sixGread) {
		JSONArray defalutTreeValue = new JSONArray();
		List<PubColumn> deafalutTree = new ArrayList<>();
		deafalutTree = systemPubDao.queryPubColumnNew(deafalutTree, String.valueOf(sixGread));
		JSONArray defalutTreeOne = new JSONArray();
		defalutTreeValue.add(defalutTreeOne);
		defalutTreeValue.add(defalutTreeOne);
		JSONObject defalut = new JSONObject();
		defalut.put("defalut", "");
		defalutTreeValue.add(defalut);
		for (int i = 0; i < deafalutTree.size(); i++) {
			PubColumn pubColumn = deafalutTree.get(i);
			if(!"202307".equals(pubColumn.getEntityId())){
				continue;
			}
			defalutTreeValue.optJSONArray(0).add(pubColumn.getReferId());
			defalutTreeValue.optJSONArray(1).add(pubColumn.getColValueName());
			//遍历子集：获得层级数据
			defalutTreeValue = StaticUtil.getChannalTree(deafalutTree, pubColumn.getReferId(), defalutTreeValue);
			if(defalutTreeValue.optJSONObject(2).optString("defalut").length() > 0){
				defalutTreeValue = StaticUtil.getChannalTree(deafalutTree, defalutTreeValue.optJSONObject(2).optString("defalut"), defalutTreeValue);
			}
			//取三级
			if(defalutTreeValue.optJSONObject(2).optString("defalut").length() > 0){
				defalutTreeValue = StaticUtil.getChannalTree(deafalutTree, defalutTreeValue.optJSONObject(2).optString("defalut"), defalutTreeValue);
			}
			//取四级
			if(defalutTreeValue.optJSONObject(2).optString("defalut").length() > 0){
				defalutTreeValue = StaticUtil.getChannalTree(deafalutTree, defalutTreeValue.optJSONObject(2).optString("defalut"), defalutTreeValue);
			}
			//取五级
			if(defalutTreeValue.optJSONObject(2).optString("defalut").length() > 0){
				defalutTreeValue = StaticUtil.getChannalTree(deafalutTree, defalutTreeValue.optJSONObject(2).optString("defalut"), defalutTreeValue);
			}
			//取六级
			if(defalutTreeValue.optJSONObject(2).optString("defalut").length() > 0){
				defalutTreeValue = StaticUtil.getChannalTree(deafalutTree, defalutTreeValue.optJSONObject(2).optString("defalut"), defalutTreeValue);
			}
		}
		return defalutTreeValue.toString();
	}

	@Override
	public String auxiliaryToolMuen() {
		TsmStaff tsmStaff=pubFunc.getLogonStaff();
		List menuInfo=this.systemPubDao.auxiliaryToolMuen(tsmStaff.getLogonName());
		if(menuInfo.isEmpty()){
			log.info("工号 {} 未查询到对应的 ：关联平台信息 ",tsmStaff.getLogonName());
			return "[]";
		}
		return JSONArray.fromObject(menuInfo).toString();
	}

	@Override
	public String specificInfoService(String neworgId) {
		String linkId=pubFunc.getOrgLink(neworgId);
		String [] linlStr=linkId.split("-");
		if(linlStr.length == 1) return "[]";
		List specifInfo=systemPubDao.specificOrgInfo(linlStr[1]);
		if(StringUtils.isNull(specifInfo)) return "[]";
		JSONArray array=new JSONArray();
		for (int i = 0; i < specifInfo.size(); i++) {
			Map map = (Map) specifInfo.get(i);
			String orgId = getMapValue(map, "org_id");
			String orgName = getMapValue(map, "org_name");
			int orgLevel = map.get("org_level") == null ? 0 : Integer.parseInt(map.get("org_level").toString());
			String upOrg = getMapValue(map, "up_org");
			String parentName = getMapValue(map, "parentName");
			if(orgLevel==1 && ! linlStr[1].equals(orgId)){
				continue;
			}
			JSONObject obj = new JSONObject();
			obj.put("id", orgId);
			if(StringUtils.isNotEmpty(linlStr[1]) && linlStr[1].equals(orgId)){
				obj.put(KEY_PARENTID, 0);
			}else{
				obj.put(KEY_PARENTID, upOrg);
			}

			if (StringUtils.isEmpty(upOrg)) {
				orgLevel = 0;
			}
			obj.put("name", orgName);
			obj.put("rank", orgLevel);
			obj.put(KEY_PARENTNAME, parentName);
			array.add(obj);
			obj.clear();
		}
		return String.valueOf(array);
	}

	@Override
	public boolean zerenOrgFlag(String referid, String revOrgId) {
		List ccResponsibilityOrg = pubFunc.getDir("CC_RESPONSIBILITY_ORG", "", referid);
		Map map= (Map) ccResponsibilityOrg.get(0);
		String colhandling=map.get("COL_VALUE_HANDLING")==null?"":map.get("COL_VALUE_HANDLING").toString();
		String linkId=pubFunc.getOrgLink(revOrgId);
		if(StringUtils.isNotEmpty(colhandling)  &&  linkId.indexOf("-",3)>-1){
			if(linkId.substring(0,linkId.indexOf("-",3)).equals(colhandling)){
				return true;
			}
		}else if(StringUtils.isNotEmpty(colhandling)  &&  linkId.indexOf("-")>-1  &&  colhandling.equals(linkId)){
			return true;
		}
		return false;
	}
	
	@Override
	public String sendRestUserCaptcha(String loginName,int captchaType) {
		TsmStaff tsmStaff=pubFunc.getAvlStaffByLoginName(loginName);
		if(StringUtils.isNull(tsmStaff)){
			return ResultUtil.error(ResultEnum.PASSWORD_ISNULL);
		}
		if(StringUtils.isEmpty(tsmStaff.getRelaPhone()) || tsmStaff.getRelaPhone().length()!=11){
			return ResultUtil.error(ResultEnum.PASSWORD_NOTBIANDIN);
		}
		CapchaInfo capchaInfo=new CapchaInfo();
		String guid=pubFunc.crtGuid();
		capchaInfo.setCaptchaId(guid);
		capchaInfo.setStaffId(Integer.parseInt(tsmStaff.getId()));
		capchaInfo.setValIdTime(pubFunc.getAddMinutes(5));
		capchaInfo.setCaptchaType(captchaType);
		capchaInfo.setCaptcha(StaticUtil.createRandom(6));
		capchaInfo.setCaptchaNumer(tsmStaff.getRelaPhone());
		String sendCaptchaContent="客服系统工号为 : %s 的工作人员您好，您正在操作江苏电信10000号工单系统的密码找回功能，手机验证码为 : %s ,有效期为: %s ,请妥善保管。";
		if(captchaType == 1) {
			sendCaptchaContent="客服系统工号为 : %s 的工作人员您好，您正在尝试登录江苏电信10000号工单系统，手机验证码为 : %s ,有效期为: %s ,请妥善保管。";
		}
		capchaInfo.setCaptchaContent(String.format(sendCaptchaContent, tsmStaff.getLogonName(),capchaInfo.getCaptcha(),capchaInfo.getValIdTime()));
		this.systemPubDao.saveCaptchaInfo(capchaInfo);
		//短信发送，插入当前表数据
		NoteSeand noteBean = new NoteSeand();
		noteBean.setSheetGuid(guid);
		noteBean.setRegionId(3);
		noteBean.setDestteRmid(tsmStaff.getRelaPhone());
		noteBean.setClientType(1);//发送给个人
		noteBean.setSendContent(capchaInfo.getCaptchaContent());	
		noteBean.setOrgId(tsmStaff.getOrganizationId());
		noteBean.setOrgName(tsmStaff.getOrgName());
		noteBean.setStaffId(Integer.parseInt(tsmStaff.getId()));
		noteBean.setStaffName(tsmStaff.getName());
		noteBean.setBusiId("YZM");
		this.noteSen.saveNoteContent(noteBean);
		return ResultUtil.success(guid);
	}

	@Override
	public String validationUserCaptcha(String guid, String loginName, String cahtcha) {
		TsmStaff staff=pubFunc.getAvlStaffByLoginName(loginName);
		CapchaInfo capchaInfo=this.systemPubDao.captChaByGuid(guid);
		if(!cahtcha.equals(capchaInfo.getCaptcha())){
			return ResultUtil.error(ResultEnum.VALIDATION_INCORRECT);
		}
		if(pubFunc.getBetweenSysDateSec(capchaInfo.getValIdTime())>0){
			return ResultUtil.error(ResultEnum.VALIDATION_GUOQI);
		}
		if(Integer.parseInt(staff.getId())!=capchaInfo.getStaffId()){
			return ResultUtil.error(ResultEnum.VALIDATION_BUPIPEI);
		}
		return ResultUtil.success();
	}

	@Override
	public String createFourMuLuTree() {
		List<PubColumn> list = systemPubDao.queryFourSixMulu();
		if(!list.isEmpty()){
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				PubColumn pubColumn = list.get(i);
				if("202307".equals(pubColumn.getEntityId())){
					JSONObject item = new JSONObject();
					item.put("value", pubColumn.getReferId());
					item.put("label", pubColumn.getColValueName());
					item.put("type", pubColumn.getColValueHandling());
					JSONArray children = StaticUtil.getPubClounmNew(list, pubColumn.getReferId());
					if(!children.isEmpty()){
						item.put(KEY_CHILDREN, children);
					}
					array.add(item);
					item.clear();
				}
			}
			return array.toString();
		}
		return null;
	}

	@Override
	public String loadColumnsByEntity(String table, String colCode, String entity) {
		List<PubColumn> list = systemPubDao.loadColumnsByEntity(table, colCode, entity);
		if(!list.isEmpty()){
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				PubColumn pubColumn = list.get(i);
				if(pubColumn.getTableCode().equals(table) && pubColumn.getColCode().equals(colCode) && pubColumn.getEntityId().equals(entity)){
					JSONObject item = new JSONObject();
					item.put("value", pubColumn.getReferId());
					item.put("label", pubColumn.getColValueName());
					JSONArray children = StaticUtil.getPubClounmNew(list, pubColumn.getReferId());
					if(!children.isEmpty()){
						item.put(KEY_CHILDREN, children);
					}
					array.add(item);
					item.clear();
				}
			}
			return array.toString();
		}
		return null;
	}

	public String getColumnsByCode(String table, String colCode, String entity){
		List<PubColumn> list = systemPubDao.getAllColumnsNew(table, colCode);
		if(!list.isEmpty()) {
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				PubColumn pubColumn = list.get(i);
				if(entity.equals(pubColumn.getEntityId())){
					JSONObject item = new JSONObject();
					item.put("value", pubColumn.getReferId());
					item.put("label", pubColumn.getColValueName());
					if("1".equals(pubColumn.getHavingChildItem())) {//存在子节点
						JSONArray children = StaticUtil.getPubColumnJSON(list, pubColumn.getReferId());
						if(!children.isEmpty()){
							item.put(KEY_CHILDREN, children);
						}
					}
					array.add(item);
					item.clear();
				}
			}
			JSONArray listArray = JSONArray.fromObject(list);
			JSONObject json = new JSONObject();
			json.put("list", listArray);
			json.put("array", array);
			return json.toString();
		}
		return null;
	}

	@Override
	public String skillOrgTree(String flag, String parm) {
		List systemTreeRoot = systemPubQury.getSystemTreeRoot(flag, parm);
		StringBuilder sb = new StringBuilder("");
		JSONArray systemTreeRootArr = JSONArray.fromObject(systemTreeRoot);
		for (int i = 0; i < systemTreeRootArr.size(); i++) {
			String orgId = systemTreeRootArr.getJSONObject(i).optString("orgId");
			if(i==0) {
				sb.append(orgId);
			}else {
				sb.append(","+orgId);
			}
		}
		String strWhere = sb.toString();
		if(StringUtils.isEmpty(strWhere)){
			strWhere="10";
		}
		List skillOrgTreelist = systemPubDao.skillOrgTreelist(strWhere);
		JSONArray array=new JSONArray();
		//遍历查询对应的子集，组合成新的树行结果
		for (int i = 0; i < skillOrgTreelist.size(); i++) {
			Map map = (Map) skillOrgTreelist.get(i);
			String orgId = getMapValue(map, "org_id");
			String orgName = getMapValue(map, "org_name");
			int orgLevel = map.get("org_level") == null ? 0 : Integer.parseInt(map.get("org_level").toString()); 
			String upOrg = getMapValue(map, "up_org");
			String parentName = getMapValue(map, "parentName");
			JSONObject obj = new JSONObject();
			obj.put("id", orgId);
			if(isTopTree(orgId, strWhere)){
				obj.put(KEY_PARENTID, 0);
			}else{
				obj.put(KEY_PARENTID, upOrg);
			}
			
			if (StringUtils.isEmpty(upOrg)) {
				orgLevel = 0;
			}
			obj.put("name", orgName);
			obj.put("rank", orgLevel);
			obj.put(KEY_PARENTNAME, parentName);
			array.add(obj);
			obj.clear();
		}
		return array.toString();
	}
	
	public boolean isTopTree(String orgId,String strWhere){
		String [] toplist=strWhere.split(",");
		boolean isTopFlag=false;
		for (int i = 0; i < toplist.length; i++) {
			if(orgId.equals(toplist[i])){
				isTopFlag=true;
			     break;
			}
		}
		return isTopFlag;
	}

	@Override
	public String getSysttemOrgTreeAuth(String flag, String parm,String staffId,String param) {
		JSONArray array = new JSONArray();
		List orgInfo = systemPubDao.queryOrgInfoAuth(flag,parm,staffId,param);
		if (StringUtils.isNull(orgInfo) || orgInfo.isEmpty()) {
			return array.toString();
		}
		for (int i = 0; i < orgInfo.size(); i++) {
			Map map = (Map) orgInfo.get(i);
			String orgId = getMapValue(map, "org_id");
			String orgName = getMapValue(map, "org_name");
			int orgLevel = map.get("org_level") == null ? 0 : Integer.parseInt(map.get("org_level").toString());
			String upOrg = getMapValue(map, "up_org");
			String parentName = getMapValue(map, "parentName");
			JSONObject obj = new JSONObject();
			obj.put("id", orgId);
			if(StringUtils.isNotEmpty(parm) && parm.equals(orgId)){
				obj.put(KEY_PARENTID, 0);
			}else{
				obj.put(KEY_PARENTID, upOrg);
			}
			
			if (StringUtils.isEmpty(upOrg)) {
				orgLevel = 0;
			}
			obj.put("name", orgName);
			obj.put("rank", orgLevel);
			obj.put(KEY_PARENTNAME, parentName);
			array.add(obj);
			obj.clear();
		}
		return String.valueOf(array);
	}
	
	public String getXcDispatchOrg(String sheetId) {
		log.info("getXcDispatchOrg sheetId: {}", sheetId);
		String orgTreeId = "11";
		XcFlow info = sheetPubInfoDao.getXcFlowByCurXcId(sheetId);
		log.info("XcFlow: {}", info);
		if(info != null) {
			int count = info.getSendCount();//已转派次数
			if(count >= 7) {//协查单，全渠内部转派，流转次数限制为第8次不能转派，只能转派内部
				String rcvOrgId = info.getCurReceiveOrg();//本单收单部门
				orgTreeId = pubFunc.getAreaOrgId(rcvOrgId);
			}
		}
		return orgTreeId;
	}
	
	@Override
    public String loadColumnsByEntityNew(String table, String colCode, String entity) {
        List<PubColumn> list = systemPubDao.loadColumnsByEntityNew(table, colCode, entity);
        if(!list.isEmpty()){
            JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				PubColumn pubColumn = list.get(i);
				JSONObject item = new JSONObject();
				item.put("ReferId", pubColumn.getReferId());
				item.put("ColValueName", pubColumn.getColValueName());
				item.put("ColValue", pubColumn.getColValue());
				item.put("ColCode", pubColumn.getColCode());
				item.put("ColName", pubColumn.getColName());
				item.put("ColOrder", pubColumn.getColOrder());
				item.put("ColValueHandling", pubColumn.getColValueHandling());
				item.put("HavingChildItem", pubColumn.getHavingChildItem());
				item.put("EntityId", pubColumn.getEntityId());
				item.put("TableCode", pubColumn.getTableCode());
				array.add(item);
				item.clear();
			}
            return array.toString();
        }
        return null;
    }

	public int addColumnsReference(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		Map newColumnData = new HashMap<String,String>();
		newColumnData.put("ReferId",json.optString("ReferId"));
		newColumnData.put("ColValueName",json.optString("ColValueName"));
		newColumnData.put("ColCode",json.optString("ColCode"));
		newColumnData.put("ColValue",json.optString("ColValue"));
		newColumnData.put("ColName",json.optString("ColName"));
		newColumnData.put("ColOrder",json.optString("ColOrder"));
		newColumnData.put("ColValueHandling",json.optString("ColValueHandling"));
		newColumnData.put("HavingChildItem",json.optString("HavingChildItem"));
		newColumnData.put("EntityId",json.optString("EntityId"));
		newColumnData.put("TableCode",json.optString("TableCode"));
		return systemPubDao.addColumnsReference(newColumnData);
	}

	public int updateColumnsReference(String parm){
		JSONObject json = JSONObject.fromObject(parm);
		Map updateColumnData = new HashMap<String,String>();
		updateColumnData.put("ReferId",json.optString("ReferId"));
		updateColumnData.put("ColValueName",json.optString("ColValueName"));
		updateColumnData.put("ColCode",json.optString("ColCode"));
		updateColumnData.put("ColValue",json.optString("ColValue"));
		updateColumnData.put("ColName",json.optString("ColName"));
		updateColumnData.put("ColOrder",json.optString("ColOrder"));
		updateColumnData.put("ColValueHandling",json.optString("ColValueHandling"));
		updateColumnData.put("HavingChildItem",json.optString("HavingChildItem"));
		updateColumnData.put("EntityId",json.optString("EntityId"));
		updateColumnData.put("TableCode",json.optString("TableCode"));
		return systemPubDao.updateColumnsReference(updateColumnData);
	}

	public int delColumnsReference(String referId){
		return systemPubDao.delColumnsReference(referId);
	}

	public String getColumnsByCodeBuop(String table, String colCode, String entity){
		List<PubColumn> list = systemPubDao.getAllColumnsNew(table);
		if(!list.isEmpty()) {
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				PubColumn pubColumn = list.get(i);
				if(entity.equals(pubColumn.getEntityId())){
					JSONObject item = new JSONObject();
					item.put("value", pubColumn.getReferId());
					item.put("label", pubColumn.getColValueName());
					JSONArray children = StaticUtil.getPubColumnJSON(list, pubColumn.getReferId());
					if(!children.isEmpty()){
						item.put(KEY_CHILDREN, children);
					}
					array.add(item);
					item.clear();
				}
			}
			JSONArray listArray = JSONArray.fromObject(list);
			JSONObject json = new JSONObject();
			json.put("list", listArray);
			json.put("array", array);
			return json.toString();
		}
		return null;
	}

}
