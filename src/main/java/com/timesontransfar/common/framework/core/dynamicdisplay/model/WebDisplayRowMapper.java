package com.timesontransfar.common.framework.core.dynamicdisplay.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.timesontransfar.common.database.ISqlUtil;
import com.timesontransfar.common.database.impl.CommonSqlUtilImpl;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubConstraint;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubDisplayCtrl;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubEvent;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubFunction;
import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;

public class WebDisplayRowMapper extends AbstractRowMapper {
	private ISqlUtil sqlUtil = new CommonSqlUtilImpl();

	public Object mapRow(ResultSet rs, int index) throws SQLException {
		// 自动生成方法存根
		Object obj = null;
		switch (this.getIntClassType()) {
			case 0:
				obj = this.mappingFunction(rs);
				break;
			case 1:
				obj = this.mappingDisplayCtrl(rs);
				break;
			case 2:
				obj = this.mappingEvent(rs);
				break;
			case 3:
				obj = this.mappingConstraint(rs);
				break;
			default:
				break;
		}
		return obj;
	}

	/*
	 * CREATE TABLE `pub_function` ( `FUN_ID` varchar(39) NOT NULL defaut '',
	 * `FUN_NAME` varchar(20) default NULL, `FUN_TYPE` int(6) default NULL,
	 * `ENITITY_ID` varchar(20) default NULL, `QUERY_FLAG` int(2) default NULL,
	 * `QUERY_BEAN` varchar(32) default NULL, `QUERY_METHOD` varchar(64) default
	 * NULL, `QUERY_CLASS` varchar(255) default NULL, `QUERY_SQL` varchar(255)
	 * default NULL, `ADD_FLAG` int(2) default NULL, `ADD_BEAN` varchar(32)
	 * default NULL, `ADD_CLASS` varchar(255) default NULL, `ADD_METHOD`
	 * varchar(64) default NULL, `ADD_SQL` varchar(255) default NULL,
	 * `MODIFY_FLAG` int(2) default NULL, `MODIFY_BEAN` varchar(32) default
	 * NULL, `MODIFY_CLASS` varchar(255) default NULL, `MODIFY_METHOD`
	 * varchar(64) default NULL, `MODIFY_SQL` varchar(255) default NULL,
	 * `DEL_FLAG` int(2) default NULL, `DEL_BEAN` varchar(32) default NULL,
	 * `DEL_CLASS` varchar(255) default NULL, `DEL_METHOD` varchar(64) default
	 * NULL, `DEL_SQL` varchar(255) default NULL, `DISPLAYWIDTH` smallint(6)
	 * default NULL, `DISPLAYHEIGHT` smallint(6) default NULL, PRIMARY KEY
	 * (`FUN_ID`) ) TYPE=InnoDB
	 */

	/**
	 * @param ResultSet
	 *            rs
	 */
	public Object mappingFunction(ResultSet rs) throws SQLException {
		PubFunction function = new PubFunction();
		function.setFunId(rs.getString("FUN_ID"));
		function.setFunName(rs.getString("FUN_NAME"));
		function.setFunType(rs.getInt("FUN_TYPE"));
		function.setEnitityId(rs.getString("ENITITY_ID"));
		function.setQueryFlag(rs.getInt("QUERY_FLAG"));
		function.setQueryBean(rs.getString("QUERY_BEAN"));
		function.setQueryMethod(rs.getString("QUERY_METHOD"));
		function.setQueryClass(rs.getString("QUERY_CLASS"));
		String clobString = this.sqlUtil.getClob(rs, "QUERY_SQL");
		function.setQuerySql(clobString);
		function.setAddFlag(rs.getInt("ADD_FLAG"));
		function.setAddBean(rs.getString("ADD_BEAN"));
		function.setAddClass(rs.getString("ADD_CLASS"));
		function.setAddMethod(rs.getString("ADD_METHOD"));
		clobString = this.sqlUtil.getClob(rs, "ADD_SQL");
		function.setAddSql(clobString);
		function.setModifyFlag(rs.getInt("MODIFY_FLAG"));
		function.setModifyBean(rs.getString("MODIFY_BEAN"));
		function.setModifyClass(rs.getString("MODIFY_CLASS"));
		function.setModifyMethod(rs.getString("MODIFY_METHOD"));
		clobString = this.sqlUtil.getClob(rs, "MODIFY_SQL");
		function.setModifySql(clobString);
		function.setDelFlag(rs.getInt("DEL_FLAG"));
		function.setDelBean(rs.getString("DEL_BEAN"));
		function.setDelClass(rs.getString("DEL_CLASS"));
		function.setDelMethod(rs.getString("DEL_METHOD"));
		clobString = this.sqlUtil.getClob(rs, "DEL_SQL");
		function.setDelSql(clobString);
		function.setDisplayWidth(rs.getInt("DISPLAYWIDTH"));
		function.setDisplayHeight(rs.getInt("DISPLAYHEIGHT"));
		function.setRecordCount(rs.getInt("RECORDCOUNT"));
		function.setAutoAdapt(rs.getBoolean("AUTO_ADAPT"));
		function.setConfigQueryFlag(rs.getInt("CONFIG_QUERY_FLAG"));

		// 添加DBGrid新属性,是否展示查询框，是否展示选择框，是否展示打印按钮
		// add by 王景周
		function.setShowSearchBtn(rs.getInt("SHOW_SEARCHBTN") == 1);
		function.setShowPrintBtn(rs.getInt("SHOW_PRINTBTN") == 1);
		function.setSelectBoxType(rs.getInt("SHOW_SELBOX"));
		return function;
	}

	/*
	 * CREATE TABLE `pub_display_ctrl` ( `CTRL_ID` varchar(39) NOT NULL default
	 * '', `FUN_ID` varchar(20) default NULL, `POS_X` varchar(20) default NULL,
	 * `POS_Y` varchar(20) default NULL, `WIDTH` int(9) default NULL, `HEIGHT`
	 * int(9) default NULL, `IS_VIRSUAL` int(2) default NULL, `IS_WRITE` int(2)
	 * default NULL, `REFER_TYPE` varchar(50) default NULL, `ATTRIBUTE_ID`
	 * int(9) default NULL, `TABLE_NAME` varchar(64) default NULL, `COLUMN_NAME`
	 * varchar(64) default NULL, `CTRL_TYPE` varchar(50) default NULL,
	 * `IS_STATIC` int(2) default NULL, `CONTENT` varchar(255) default NULL,
	 * PRIMARY KEY (`CTRL_ID`), KEY `FUN_ID` (`FUN_ID`), CONSTRAINT
	 * `pub_display_ctrl_ibfk_1` FOREIGN KEY (`FUN_ID`) REFERENCES
	 * `pub_function` (`FUN_ID`) ) TYPE=InnoDB
	 */
	/**
	 * @param rs
	 */
	public Object mappingDisplayCtrl(ResultSet rs) throws SQLException {
		PubDisplayCtrl displayCtrl = new PubDisplayCtrl();
		displayCtrl.setCtrlId(rs.getString("CTRL_ID"));
		displayCtrl.setPosX(rs.getInt("POS_X"));
		displayCtrl.setPosY(rs.getInt("POS_Y"));
		displayCtrl.setWidth(rs.getInt("WIDTH"));
		displayCtrl.setHeight(rs.getInt("HEIGHT"));
		displayCtrl.setIsVirsual(rs.getInt("IS_VIRSUAL"));
		displayCtrl.setIsWrite(rs.getInt("IS_WRITE"));
		displayCtrl.setReferType(rs.getString("REFER_TYPE"));
		displayCtrl.setAttributeId(rs.getString("ATTRIBUTE_ID"));
		displayCtrl.setWebId(rs.getString("WEB_ID"));
		displayCtrl.setCtrlType(rs.getString("CTRL_TYPE"));
		displayCtrl.setIsStatic(rs.getInt("IS_STATIC"));
		displayCtrl.setContent(rs.getString("CONTENT")==null?"":rs.getString("CONTENT"));
		displayCtrl.setForward(rs.getBoolean("IS_FORWORD"));
		displayCtrl.setMatchCtrlType(rs.getInt("MATCHCTRLTYPE"));
		displayCtrl.setMatchValue(rs.getString("MATCHVALUE"));
		displayCtrl.setDefaultValue(rs.getString("DEFAULT_VALUE"));
		displayCtrl.setAltString(rs.getString("CTRLCLEW")==null?"":rs.getString("CTRLCLEW"));
		try {
			displayCtrl.setOutFlag(rs.getInt("OUTFLAG") != 0);
		} catch (Exception ex) {
		//异常处理逻辑
		}
		return displayCtrl;
	}

	/*
	 * 
	 * SELECT
	 * PUB_DISPLAY_CTRL.CTRL_ID,PUB_EVENT.EVENT_ID,PUB_EVENT.EVENT_TYPE,PUB_EVENT.FUN_ID,PUB_EVENT.JAVACLASS,
	 * PUB_EVENT.JAVA_BEAN,PUB_EVENT.METHOD,PUB_EVENT.JAVASCRIPT,PUB_EVENT.DISPLAYTYPE,PUB_EVENT.PAGE_ID
	 * FROM PUB_EVENT,PUB_DISPLAY_CTRL,PUB_CTRL_EVENT_RELA WHERE
	 * PUB_DISPLAY_CTRL.FUN_ID=? AND PUB_DISPLAY_CTRL.CTRL_ID =
	 * PUB_CTRL_EVENT_RELA.CTRL_ID AND PUB_EVENT.EVENT_ID =
	 * PUB_CTRL_EVENT_RELA.EVENT_ID
	 */
	/**
	 * @param rs
	 * @return Objet
	 * @throws SQLException;
	 */
	public Object mappingEvent(ResultSet rs) throws SQLException {
		PubEvent event = new PubEvent();
		event.setCtrlId(rs.getString("CTRL_ID"));
		event.setEventId(rs.getString("EVENT_ID"));
		event.setEventType(rs.getString("EVENT_TYPE"));
		event.setFunId(rs.getString("FUN_ID"));
		event.setJavaClass(rs.getString("JAVACLASS"));
		event.setJavaBean(rs.getString("JAVA_BEAN"));
		event.setMethod(rs.getString("METHOD"));
		String clobString = this.sqlUtil.getClob(rs, "Javascript");
		event.setJavascript(clobString);
		event.setDisplayType(rs.getInt("DisplayType"));
		event.setPageId(rs.getString("PAGE_ID"));
		event.setPerformType(rs.getInt("PERFORMTYPE"));
		return event;
	}

	/*
	 * SELECT
	 * PUB_DISPLAY_CTRL.CTRL_ID,PUB_CONSTRAINT.CONST_ID,PUB_CONSTRAINT.CONST_TYPE,PUB_CONSTRAINT.NAME,
	 * PUB_CONSTRAINT.JAVACLASS,PUB_CONSTRAINT.DESCRIPTION FROM
	 * PUB_CONSTRAINT,PUB_DISPLAY_CTRL,PUB_CTRL_CONSTRAINT_RELA WHERE
	 * PUB_DISPLAY_CTRL.FUN_ID=? AND PUB_DISPLAY_CTRL.CTRL_ID =
	 * PUB_CTRL_CONSTRAINT_RELA.CTRL_ID AND PUB_CONSTRAINT.CONST_ID =
	 * PUB_CTRL_CONSTRAINT_RELA.CONST_ID
	 */
	/**
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	public Object mappingConstraint(ResultSet rs) throws SQLException {
		PubConstraint constraint = new PubConstraint();
		constraint.setCtrlId(rs.getString("CTRL_ID"));
		constraint.setConstId(rs.getString("CONST_ID"));
		constraint.setConstType(rs.getInt("CONST_TYPE"));
		constraint.setConstName(rs.getString("CONST_NAME"));
		constraint.setJavaClass(rs.getString("JAVACLASS"));
		constraint.setJavaBean(rs.getString("JAVABEAN"));
		constraint.setMethod(rs.getString("METHOD"));
		constraint.setDescription(rs.getString("CONST_DESC"));
		return constraint;
	}

	public ISqlUtil getSqlUtil() {
		return sqlUtil;
	}

	public void setSqlUtil(ISqlUtil sqlUtil) {
		this.sqlUtil = sqlUtil;
	}

}
