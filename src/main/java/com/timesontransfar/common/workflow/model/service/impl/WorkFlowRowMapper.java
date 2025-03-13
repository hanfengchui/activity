package com.timesontransfar.common.workflow.model.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;

import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;
import com.timesontransfar.common.workflow.model.pojo.*;
import com.timesontransfar.common.database.ISqlUtil;
@SuppressWarnings("rawtypes")
public class WorkFlowRowMapper extends AbstractRowMapper {
	private ISqlUtil sqlUtil;

	public ISqlUtil getSqlUtil() {
		return sqlUtil;
	}

	public void setSqlUtil(ISqlUtil sqlUtil) {
		this.sqlUtil = sqlUtil;
	}

	public static final String WFL_ID = "WFL_ID";
	public static final String WFGUID = "WFGUID";
	public static final String CREATE_STAFF = "CREATE_STAFF";
	public static final String CREATE_DATE = "CREATE_DATE";
	public static final String MODIFY_STAFF = "MODIFY_STAFF";
	public static final String MODIFY_DATE = "MODIFY_DATE";
	public static final String TACHE_ID = "TACHE_ID";
	public static final String REMARK = "REMARK";
	public static final String METHODCODE = "METHODCODE";
	public static final String REGION_ID = "REGION_ID";

	public WorkFlowRowMapper() {
		super();
	}

	public Object mapRow(ResultSet arg0, int arg1) throws SQLException {
		Object object = null;

		switch (this.getIntClassType()) {
		case 0:// 转换工作流
			object = this.mapWorkFlowSchema(arg0);
			break;
		case 1:// 转换工作流节点
			object = this.mapWorkFlowNode(arg0);
			break;
		case 2:// 转换路由
			object = this.mapWorkFlowRoute(arg0);
			break;
		case 3:// 转换条件
			object = this.mapWorkFlowCondition(arg0);
			break;
		case 4:// 转换工作流实例
			object = this.mapWorkFlowInstance(arg0);
			break;
		case 5:// 转换流程节点实例
			object = this.mapWorkFlowNodeIntance(arg0);
			break;
		case 6:// 转换方法
			object = this.mapExecMethod(arg0);
			break;
		case 7:// 转换外部系统
			object = this.mapSystemInfo(arg0);
			break;
		case 8:// 转换工作流地区信息
			object = this.mapSchemaRegion(arg0);
			break;
		case 9:// 转换元数据配置信息
			object = this.mapMetaDataConfig(arg0);
			break;
		case 10:// 转换外系统消息
			object = this.mapEsysMsg(arg0);
			break;
		case 11:// 转换参数对象
			object = this.mapNodeOParamConfig(arg0);
			break;
		case 12:// 转换产品流程授权表
			object = this.mapInterfaceInfo(arg0);
			break;
		case 13:// 转换地区代码
			object = this.mapWorkSheetFlow(arg0);
			break;
		case 14:// 取出节点拥有的所有条件
			object = this.mapConditionForTache(arg0);
			break;
		default:
		}

		return object;
	}

	private Object mapWorkFlowSchema(ResultSet rs) throws SQLException {
		WorkFlowSchema bean = new WorkFlowSchema();

		bean.setWflId(rs.getLong(WFL_ID));
		bean.setWflType(rs.getLong("WFL_TYPE"));
		bean.setWflName(rs.getString("WFL_NAME"));
		bean.setWflDesc(rs.getString("WFL_DESC"));
		bean.setWflFile(rs.getString("WFL_FILE"));
		bean.setWflXmlSchema(rs.getString("WFL_XML_SCHEMA"));
		bean.setWflEffDate(rs.getTimestamp("WFL_EFF_DATE"));
		bean.setWflExpDate(rs.getTimestamp("WFL_EXP_DATE"));
		bean.setWfguid(rs.getString(WFGUID));
		bean.setWfstate(rs.getLong("WFSTATE"));
		bean.setSpecFlag(rs.getLong("SPEC_FLAG"));
		bean.setWfStartNode(rs.getString("WF_START_NODE"));
		bean.setWfEndNode(rs.getString("WF_END_NODE"));
		bean.setCreateStaff(rs.getLong(CREATE_STAFF));
		bean.setCreateDate(rs.getTimestamp(CREATE_DATE));
		bean.setModifyStaff(rs.getLong(MODIFY_STAFF));
		bean.setModifyDate(rs.getTimestamp(MODIFY_DATE));
		bean.setWflTypeEx(rs.getLong("WFL_TYPE_EX"));

		return bean;
	}

	private Object mapWorkFlowNode(ResultSet rs) throws SQLException {
		WorkFlowNode bean = new WorkFlowNode();

		bean.setWflId(rs.getLong(WFL_ID));
		bean.setWflSeqNbr(rs.getLong("WFL_SEQ_NBR"));
		bean.setWorksheetType(rs.getLong("WORKSHEET_TYPE"));
		bean.setTacheId(rs.getLong(TACHE_ID));
		bean.setNdguid(rs.getString("NDGUID"));
		bean.setWfguid(rs.getString(WFGUID));
		bean.setNdname(rs.getString("NDNAME"));
		bean.setNdtype(rs.getShort("NDTYPE"));
		bean.setNdstate(rs.getShort("NDSTATE"));
		WorkFlowExecMethod outMethod = new WorkFlowExecMethod();
		outMethod.setMethodcode(rs.getString("OUTMETHOD"));
		bean.setOutmethod(outMethod);

		WorkFlowExecMethod inMethod = new WorkFlowExecMethod();
		inMethod.setMethodcode(rs.getString("INMETHOD"));
		bean.setInmethod(inMethod);

		bean.setAutoFlag(mapBoolean(rs.getShort("AUTO_FLAG")));
		bean.setSpecFlag(rs.getLong("SPEC_FLAG"));
		bean.setForceCtrlFlag(rs.getLong("FORCE_CTRL_FLAG"));
		bean.setRollbackFlag(rs.getLong("ROLLBACK_FLAG"));
		bean.setDrawbackFlag(rs.getLong("DRAWBACK_FLAG"));
		bean.setOverruntime(rs.getLong("OVERRUNTIME"));
		bean.setOverstepttime(rs.getLong("OVERSTEPTTIME"));
		bean.setOutsystemid(rs.getString("OUTSYSTEMID"));
		bean.setOutmsgtype(rs.getString("OUTMSGTYPE"));
		bean.setSubwfid(rs.getString("SUBWFID"));
		bean.setAltInfo(rs.getString("ALT_INFO"));
		bean.setRemark(rs.getString(REMARK));
		bean.setCreateStaff(rs.getLong(CREATE_STAFF));
		bean.setCreateDate(rs.getTimestamp(CREATE_DATE));
		bean.setModifyStaff(rs.getLong(MODIFY_STAFF));
		bean.setModifyDate(rs.getTimestamp(MODIFY_DATE));
		return bean;
	}

	private Object mapWorkFlowRoute(ResultSet rs) throws SQLException {
		WorkFlowRoute bean = new WorkFlowRoute();

		bean.setRtno(rs.getString("RTNO"));
		bean.setTacheId(rs.getLong(TACHE_ID));
		bean.setRtguid(rs.getString("RTGUID"));
		bean.setRtname(rs.getString("RTNAME"));
		bean.setRttype(rs.getLong("RTTYPE"));
		bean.setRtstate(rs.getLong("RTSTATE"));
		bean.setNextNode(rs.getString("NEXT_NODE"));
		bean.setPriLevel(rs.getLong("PRI_LEVEL"));
		bean.setRemark(rs.getString(REMARK));
		bean.setCreatestaff(rs.getLong(CREATE_STAFF));
		bean.setCreatetime(rs.getDate(CREATE_DATE));
		bean.setModifystaff(rs.getLong(MODIFY_STAFF));
		bean.setModifytime(rs.getDate(MODIFY_DATE));

		return bean;

	}

	private Object mapWorkFlowCondition(ResultSet rs) throws SQLException {
		WorkFlowCondition bean = new WorkFlowCondition();

		bean.setCondno(rs.getLong("CONDNO"));
		bean.setSeq(rs.getLong("SEQ"));
		bean.setRtno(rs.getString("RTNO"));
		bean.setCondguid(rs.getString("CONDGUID"));
		bean.setCondname(rs.getString("CONDNAME"));
		bean.setCondtype(rs.getLong("CONDTYPE"));
		bean.setRtstate(rs.getLong("RTSTATE"));
		WorkFlowExecMethod eMethod = new WorkFlowExecMethod();
		eMethod.setMethodcode(rs.getString(METHODCODE));
		bean.setExecMethod(eMethod);
		bean.setOperateId(rs.getLong("OPERATE_ID"));
		bean.setCondValue(rs.getString("COND_VALUE"));
		bean.setRelation(rs.getShort("RELATION"));
		bean.setRemark(rs.getString(REMARK));
		bean.setCreatestaff(rs.getLong(CREATE_STAFF));
		bean.setCreatetime(rs.getDate(CREATE_DATE));
		bean.setModifystaff(rs.getLong(MODIFY_STAFF));
		bean.setModifytime(rs.getDate(MODIFY_DATE));

		return bean;
	}

	private Object mapWorkFlowInstance(ResultSet rs) throws SQLException {
		WorkFlowInstance bean = new WorkFlowInstance();

		bean.setInstanceid(rs.getString("INSTANCEID"));
		WorkFlowSchema schema = new WorkFlowSchema();
		schema.setWflId(rs.getLong(WFL_ID));
		bean.setWorkFlowSchema(schema);
		bean.setWfguid(rs.getString(WFGUID));
		WorkFlowNodeInstance curNode = new WorkFlowNodeInstance();
		curNode.setInstanceid(rs.getString("CUR_NODE"));
		bean.setRegionId(rs.getLong(REGION_ID));
		short endFlag = rs.getShort("END_FLAG");
		bean.setEnd(endFlag == 1);
		return bean;
	}

	private Object mapWorkFlowNodeIntance(ResultSet rs) throws SQLException {
		WorkFlowNodeInstance bean = new WorkFlowNodeInstance();
		String strParentNodes = "";
		String strChildrenNodes = "";
		List parents = null;
		List childrens = null;

		bean.setInstanceid(rs.getString("INSTANCEID"));
		bean.setTacheId(rs.getLong(TACHE_ID));
		bean.setWfguid(rs.getString(WFGUID));
		bean.setIndatetime(rs.getTimestamp("INDATETIME"));
		bean.setCosttime(rs.getLong("COSTTIME"));
		bean.setOutdatetime(rs.getTimestamp("OUTDATETIME"));
		bean.setRegionId(rs.getLong(REGION_ID));
		strParentNodes = rs.getString("PARENTID");
		if (strParentNodes != null && !strParentNodes.equals("")) {
			StringTokenizer nizer = new StringTokenizer(strParentNodes, "|");
			parents = new ArrayList();
			while (nizer.hasMoreTokens())
				parents.add(nizer.nextToken());
			bean.setParentid(parents);
		}

		strChildrenNodes = rs.getString("CHILDRENID");
		if (strChildrenNodes != null && !strChildrenNodes.equals("")) {
			StringTokenizer nizer = new StringTokenizer(strChildrenNodes, "|");
			childrens = new ArrayList();
			while (nizer.hasMoreTokens())
				childrens.add(nizer.nextToken());
			bean.setChildrenid(childrens);
		}

		bean.setWfinstanceid(rs.getString("WFINSTANCEID"));
		bean.setNodeType(rs.getLong("NODE_TYPE"));

		return bean;
	}

	private boolean mapBoolean(short iBoolean) {
		return iBoolean == 1 ;
	}


	private WorkFlowExecMethod mapExecMethod(ResultSet rs) throws SQLException {
		WorkFlowExecMethod bean = new WorkFlowExecMethod();

		bean.setMethodcode(rs.getString(METHODCODE));
		bean.setMethodguid(rs.getString("METHODGUID"));
		bean.setMethodname(rs.getString("METHODNAME"));
		bean.setUrl(rs.getString("URL"));
		bean.setJavaclass(rs.getString("JAVACLASS"));
		bean.setJavamethod(rs.getString("JAVAMETHOD"));

		return bean;
	}

	private WorkFlowSchemaRegion mapSchemaRegion(ResultSet rs)
			throws SQLException {
		WorkFlowSchemaRegion bean = new WorkFlowSchemaRegion();

		bean.setWfguid(rs.getString(WFGUID));
		bean.setRegionid(rs.getLong("REGIONID"));

		return bean;
	}

	private OuterSystemInfo mapSystemInfo(ResultSet rs) throws SQLException {
		OuterSystemInfo bean = new OuterSystemInfo();

		bean.setSystemid(rs.getString("SYSTEMID"));
		bean.setSystemname(rs.getString("SYSTEMNAME"));
		bean.setJmsurl(rs.getString("JMSURL"));
		bean.setJmsqueue(rs.getString("JMSQUEUE"));

		return bean;
	}

	private MetaData4WorkFlow mapMetaDataConfig(ResultSet rs)
			throws SQLException {
		MetaData4WorkFlow bean = new MetaData4WorkFlow();

		bean.setId(rs.getString("MDM_ID"));
		bean.setEntityId(rs.getLong("ENITITY_ID"));
		bean.setQuerySql(sqlUtil.getClob(rs, "QUERYSQL"));
		return bean;
	}

	private EsysMsgTmp mapEsysMsg(ResultSet rs) throws SQLException {
		EsysMsgTmp bean = new EsysMsgTmp();

		bean.setTempId(rs.getString("TEMP_ID"));
		bean.setInstanceId(rs.getString("INSTANCE_ID"));
		bean.setNodeInstanceId(rs.getString("NODE_INSTANCE_ID"));
		bean.setMsgType(rs.getString("MSG_TYPE"));

		return bean;

	}

	private NodeOParamConfig mapNodeOParamConfig(ResultSet rs)
			throws SQLException {
		NodeOParamConfig bean = new NodeOParamConfig();

		bean.setParamId(rs.getString("PARAM_ID"));
		bean.setTacheId(rs.getLong(TACHE_ID));
		bean.setParamNo(rs.getLong("PARAM_NO"));
		bean.setParamName(rs.getString("PARAM_NAME"));
		bean.setParamKey(rs.getString("PARAM_KEY"));
		bean.setParamCom(rs.getShort("PARAM_COM"));
		bean.setParamJoin(rs.getShort("PARAM_JOIN"));
		bean.setParamValue(rs.getString("PARAM_VALUE"));
		bean.setParamRemark(rs.getString("PARAM_REMARK"));
		bean.setParamType(rs.getShort("PARAM_TYPE"));
		bean.setCondno(rs.getLong("CONDNO"));
		bean.setNodemethodtype(rs.getLong("NODEMETHODTYPE"));
		bean.setMethodcode(rs.getLong(METHODCODE));

		return bean;

	}

	private InterfaceInfo mapInterfaceInfo(ResultSet rs) throws SQLException {
		InterfaceInfo bean = new InterfaceInfo();

		bean.setWfno(rs.getString("WFNO"));
		bean.setWfname(rs.getString("WFNAME"));
		bean.setRemark(rs.getString(REMARK));
		bean.setRegionId(rs.getLong(REGION_ID));
		bean.setProductId(rs.getLong("PRODUCT_ID"));
		bean.setActionId(rs.getLong("ACTION_ID"));
		bean.setEntityId(rs.getLong("ENTITY_ID"));
		bean.setInterfaceGuid(rs.getString("INTERFACE_GUID"));

		return bean;
	}

	private WorkSheetFlow mapWorkSheetFlow(ResultSet rs) throws SQLException {
		WorkSheetFlow bean = new WorkSheetFlow();

		bean.setWsFlowRuleId(rs.getLong("WS_FLOW_RULE_ID"));
		bean.setWsNbr(rs.getLong("WS_NBR"));
		bean.setWorksheetSchemaId(rs.getLong("WORKSHEET_SCHEMA_ID"));
		bean.setRegionId(rs.getLong(REGION_ID));
		bean.setRuleId(rs.getLong("RULE_ID"));
		bean.setItemId(rs.getLong("ITEM_ID"));
		bean.setItemValue(rs.getString("ITEM_VALUE"));
		bean.setFlowOrgId(rs.getString("FLOW_ORG_ID"));
		bean.setTacheId(rs.getLong(TACHE_ID));

		return bean;
	}

	private WorkFlowNodeCondition mapConditionForTache(ResultSet rs) throws SQLException {
		WorkFlowNodeCondition cond = new WorkFlowNodeCondition();
		cond.setCondNo(rs.getLong("condNo"));
		cond.setCondType(rs.getLong("condType"));
		cond.setMethodCode(rs.getLong("methodCode"));
		cond.setCondName(rs.getString("condName"));
		cond.setSeq(rs.getLong("seq"));
		cond.setCondName(rs.getString("condName"));
		cond.setCondGuid(rs.getLong("condGuid"));
		cond.setParamKey(rs.getString("paramKey"));
		cond.setParamCom(rs.getLong("paramCom"));
		cond.setParamValue(rs.getString("paramValue"));
		cond.setParamType(rs.getString("paramType"));

		return cond;
	}
}
