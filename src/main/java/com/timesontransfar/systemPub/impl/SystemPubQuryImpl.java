/**
 * @author 万荣伟
 */
package com.timesontransfar.systemPub.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.IStaffPermit;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.util.newtree.pojo.SystemOrganizationTreeBean;
import com.timesontransfar.common.util.newtree.pojo.SystemTreeBean;
import com.timesontransfar.systemPub.SystemPubQury;

/**
 * @author 万荣伟
 *
 */
@Component(value="systemPubQury")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SystemPubQuryImpl implements SystemPubQury {
	
	@Autowired
	private JdbcTemplate jt;
	@Autowired
	private ISystemAuthorization systemAuthorization;
	
	/**
	 * 根据部门ID获取员工
	 * @return
	 */
	public List getStaffByOrgId(String orgId){
		String strSql = "SELECT A.STAFF_ID,A.STAFFNAME,A.LOGONNAME, CONCAT(A.STAFFNAME , '(' , A.LOGONNAME , ')') AS STAFF_LOGIN " +
				" FROM TSM_STAFF A WHERE A.STATE=8 AND A.ORG_ID = '"+orgId+"'";
		return this.jt.queryForList(strSql);
	}
	
	public List getSystemTreeRoot(String flag, String prama) {
		List rootTreeList = new ArrayList();
		String rootsql = "";
		int flg = Integer.parseInt(flag);
		if (flg == 0){//展示到县市
			rootsql = "SELECT REGION_ID,REGION_NAME FROM TRM_REGION WHERE REGION_ID='2' ";
			List treeList = this.jt.queryForList(rootsql);
			Iterator iterator = treeList.iterator();
			while (iterator.hasNext()) {
				SystemTreeBean systemtree = new SystemTreeBean();
				Map item = (Map) iterator.next();
				systemtree.setRegionId(item.get("REGION_ID").toString());
				systemtree.setRegionName(item.get("REGION_NAME").toString());
				rootTreeList.add(systemtree);
			}
		}else if (flg == 2){//不带权限显示本地网
			rootsql = "SELECT REGION_ID,REGION_NAME FROM TRM_REGION WHERE REGION_LEVEL='97C' ";
			List treeList = this.jt.queryForList(rootsql);
			Iterator iterator = treeList.iterator();
			while (iterator.hasNext()) {
				SystemTreeBean systemtree = new SystemTreeBean();
				Map item = (Map) iterator.next();
				systemtree.setRegionId(item.get("REGION_ID").toString());
				systemtree.setRegionName(item.get("REGION_NAME").toString());
				rootTreeList.add(systemtree);
			}
		}else if (flg == 3){//带权限显示本地网
			rootsql = "SELECT REGION_ID,REGION_NAME FROM TRM_REGION WHERE REGION_LEVEL='97C' ";
			String newSql = systemAuthorization.getAuthedSql(null, rootsql, "900018303");
			List treeList = this.jt.queryForList(newSql);//CodeSec未验证的SQL注入；CodeSec误报：2
			Iterator iterator = treeList.iterator();
			while (iterator.hasNext()) {
				SystemTreeBean systemtree = new SystemTreeBean();
				Map item = (Map) iterator.next();
				systemtree.setRegionId(item.get("REGION_ID").toString());
				systemtree.setRegionName(item.get("REGION_NAME").toString());
				rootTreeList.add(systemtree);
			}
		}else if(flg == 4){//带权限显示本地网增加了省公司地域
		   rootsql = "SELECT REGION_ID,REGION_NAME FROM TRM_REGION WHERE REGION_LEVEL IN ('97C','97B') ";
		   String newSql = systemAuthorization.getAuthedSql(null, rootsql, "900018303");
		   List treeList = this.jt.queryForList(newSql);//CodeSec未验证的SQL注入；CodeSec误报：2
		   Iterator iterator = treeList.iterator();
		   while (iterator.hasNext()) {
		    SystemTreeBean systemtree = new SystemTreeBean();
		    Map item = (Map) iterator.next();
		    systemtree.setRegionId(item.get("REGION_ID").toString());
		    systemtree.setRegionName(item.get("REGION_NAME").toString());
		    rootTreeList.add(systemtree);
		   }
		}else if(flg == 1){
			String[] parStr = {};
			String newSubNodeSql = "";
			String pa = "";
			if (prama != null) {
				String paramString = "";
				if (prama.indexOf("||") > -1 && !"||".equals(prama)) {
					paramString = prama.replace('|', ':');
					String[]  par = paramString.split("::");
					for (int i = 0; i < par.length; i++) {
						pa += par[i] + ",";
					}
					if (pa != null &&par.length<15){
						pa = pa.substring(0, pa.length() - 1);
					}else{
						pa="";
					}
				}else{
					pa = "";
				}
			}
			rootsql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE STATE != 3 ";
			newSubNodeSql = systemAuthorization.getAuthedSql(null, rootsql, "900018302");
			if (newSubNodeSql.equals(rootsql)){
				if(pa == null || pa.equals("1")){
					newSubNodeSql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE UP_ORG IS NULL AND STATE != 3";
				}else if(pa.equals("")){
					newSubNodeSql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE UP_ORG IS NULL AND STATE != 3";
				}else{
					newSubNodeSql="SELECT ORG_ID ,ORG_NAME FROM TSM_ORGANIZATION WHERE STATE != 3 AND UP_ORG = "+"(SELECT ORG_ID FROM TSM_ORGANIZATION WHERE org_id ="
							+" (SELECT UP_ORG FROM TSM_ORGANIZATION"
							+ " WHERE REGION_ID IN ("+pa+")"
							+" ORDER BY LINKID ASC LIMIT 1))"+ "AND REGION_ID IN ("+pa+")";
				}
				List treeList = this.jt.queryForList(newSubNodeSql);
				Iterator iterator = treeList.iterator();
				while (iterator.hasNext()) {
					Map item = (Map) iterator.next();
					SystemOrganizationTreeBean systemtree = new SystemOrganizationTreeBean();
					systemtree.setOrgId(item.get("ORG_ID").toString());
					systemtree.setOrgName(item.get("ORG_NAME").toString());
					rootTreeList.add(systemtree);
				}
			} else {
				String[] par = newSubNodeSql.split(" AND ");
				parStr = par[1].split(" OR ");
				for (int j = 0; j < parStr.length; j++) {
					if (parStr[j].indexOf(" = ")>=0) {
						if (parStr[j].indexOf(".") >= 0) {
							int posPoint = parStr[j].indexOf(".");
							int posEqual = parStr[j].indexOf("=");
							String tmpOrgName = parStr[j].substring(posPoint+1, posEqual).trim();
							tmpOrgName = tmpOrgName.toUpperCase();
							if (tmpOrgName.equals("ORG_ID")) {
								int pos1 = parStr[j].indexOf("'");
								int pos2 = parStr[j].lastIndexOf("'");
								String orgId = parStr[j].substring(pos1 + 1, pos2);
                                if(this.getSystemOrganization(orgId, prama)!=null){
                                	rootTreeList.add(this.getSystemOrganization(orgId, prama));
                                }
							}else if(tmpOrgName.equals("REGION_ID")){
								int pos1 = parStr[j].indexOf("'");
								int pos2 = parStr[j].lastIndexOf("'");
								String regionId = parStr[j].substring(pos1 + 1, pos2);
								if(this.getSystemOrgan(regionId)!=null &&!regionId.equals("0") ){
									rootTreeList.add(this.getSystemOrgan(regionId));
								}
							}
						}
					}
					if (parStr[j].toUpperCase().indexOf(" LIKE ")>=0) {
						int posFirst = parStr[j].indexOf("'");
						int posSecond = parStr[j].lastIndexOf("'");
						String linkId = parStr[j].substring(posFirst + 1, posSecond - 1);
						String[] orgIdLst = linkId.split("-");
						String lastOrgId = orgIdLst[orgIdLst.length - 1];
						if(this.getSystemOrganization(lastOrgId, prama)!=null){
                        	rootTreeList.add(this.getSystemOrganization(lastOrgId, prama));
                        }
					}
				}
			}
		}else if(flg == 6){//短信发送列表配置
			String[] parStr = {};
			String newSubNodeSql = "";
			String pa = "";
			if (prama != null) {
				String paramString = "";
				if (prama.indexOf("||") > -1 && !"||".equals(prama)) {
					paramString = prama.replace('|', ':');
					String[]  par = paramString.split("::");
					for (int i = 0; i < par.length; i++) {
						pa += par[i] + ",";
					}
					if (pa != null &&par.length<15){
						pa = pa.substring(0, pa.length() - 1);
					}else{
						pa="";
					}
				}else{
					pa = "";
				}
			}
			rootsql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE STATE != 3 ";
			newSubNodeSql = systemAuthorization.getAuthedSql(null, rootsql, "900018402");
			if (newSubNodeSql.equals(rootsql)){
				if(pa == null || pa.equals("1")){
					newSubNodeSql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE UP_ORG IS NULL AND STATE != 3";
				}else if(pa.equals("")){
					newSubNodeSql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE UP_ORG IS NULL AND STATE != 3";
				}else{
					newSubNodeSql="SELECT ORG_ID ,ORG_NAME FROM TSM_ORGANIZATION WHERE STATE != 3 AND UP_ORG = "+"(SELECT ORG_ID FROM TSM_ORGANIZATION WHERE org_id ="
							+" (SELECT UP_ORG FROM TSM_ORGANIZATION"
							+ " WHERE REGION_ID IN ("+pa+")"
							+" ORDER BY LINKID ASC LIMIT 1))"+ "AND REGION_ID IN ("+pa+")";
				}
				List treeList = this.jt.queryForList(newSubNodeSql);
				Iterator iterator = treeList.iterator();
				while (iterator.hasNext()) {
					Map item = (Map) iterator.next();
					SystemOrganizationTreeBean systemtree = new SystemOrganizationTreeBean();
					systemtree.setOrgId(item.get("ORG_ID").toString());
					systemtree.setOrgName(item.get("ORG_NAME").toString());
					rootTreeList.add(systemtree);
				}
			} else {
				String[] par = newSubNodeSql.split(" AND ");
				parStr = par[1].split(" OR ");
				for (int j = 0; j < parStr.length; j++) {
					if (parStr[j].indexOf(" = ")>=0) {
						if (parStr[j].indexOf(".") >= 0) {
							int posPoint = parStr[j].indexOf(".");
							int posEqual = parStr[j].indexOf("=");
							String tmpOrgName = parStr[j].substring(posPoint+1, posEqual).trim();
							tmpOrgName = tmpOrgName.toUpperCase();
							if (tmpOrgName.equals("ORG_ID")) {
								int pos1 = parStr[j].indexOf("'");
								int pos2 = parStr[j].lastIndexOf("'");
								String orgId = parStr[j].substring(pos1 + 1, pos2);
                                if(this.getSystemOrganization(orgId, prama)!=null){
                                	rootTreeList.add(this.getSystemOrganization(orgId, prama));
                                }
							}else if(tmpOrgName.equals("REGION_ID")){
								int pos1 = parStr[j].indexOf("'");
								int pos2 = parStr[j].lastIndexOf("'");
								String regionId = parStr[j].substring(pos1 + 1, pos2);
								if(this.getSystemOrgan(regionId)!=null &&!regionId.equals("0") ){
									rootTreeList.add(this.getSystemOrgan(regionId));
								}
							}
						}
					}
					if (parStr[j].toUpperCase().indexOf(" LIKE ")>=0) {
						int posFirst = parStr[j].indexOf("'");
						int posSecond = parStr[j].lastIndexOf("'");
						String linkId = parStr[j].substring(posFirst + 1, posSecond - 1);
						String[] orgIdLst = linkId.split("-");
						String lastOrgId = orgIdLst[orgIdLst.length - 1];
						if(this.getSystemOrganization(lastOrgId, prama)!=null){
                        	rootTreeList.add(this.getSystemOrganization(lastOrgId, prama));
                        }
					}
				}
			}
		}else if(flg == 5){//使用Object 888888，即，话务座席中使用的实体
	        String loginName = (String) systemAuthorization.getHttpSession().getAttribute("ACEGI__USERNAME");
	        IStaffPermit staffPermit = systemAuthorization.loadAuthorization(loginName, systemAuthorization.getHttpSession());
	        String newSubNodeSql = "SELECT ORG_ID, ORG_NAME, UP_ORG, LINKID FROM TSM_ORGANIZATION WHERE ORG_ID IN "
	        		+ "(SELECT DISTINCT C.COND_VALUE FROM TSM_STAFF_ROLE_RELA A, TSM_ROLE B, TSM_CONDITION C"
					+ "  WHERE A.ROLE_ID = B.ROLE_ID AND B.ROLE_ID = C.ROLE_ID AND C.OBJ_ID = '888888' AND A.STAFF_ID = '" + staffPermit.getStaff().getId() + "')"
					+ "  ORDER BY LINKID DESC";
	        List treeList = this.jt.queryForList(newSubNodeSql);
			Iterator iterator = treeList.iterator();
			Map tmpMap = new HashMap();
			Map item = null;
			while (iterator.hasNext()){
				item = (Map) iterator.next();
				tmpMap.put(item.get("ORG_ID").toString(), item.get("ORG_NAME").toString());
			}
			iterator = null;
			item = null;
			
			iterator = treeList.iterator();
			String uporg = null;
			while (iterator.hasNext()){
				item = (Map) iterator.next();
				uporg = item.get("UP_ORG").toString();
				if(tmpMap.containsKey(uporg)){
					tmpMap.remove(item.get("ORG_ID").toString());
				}
			}
			iterator = null;
			item = null;
			uporg = null;
			
			iterator = tmpMap.keySet().iterator();
			String orgid = null;
			while (iterator.hasNext()){
				orgid = iterator.next().toString();
				SystemOrganizationTreeBean systemtree = new SystemOrganizationTreeBean();
				systemtree.setOrgId(orgid);
				systemtree.setOrgName(tmpMap.get(orgid).toString());
				rootTreeList.add(systemtree);
			}
		}
		return rootTreeList;
	}
	
	private SystemOrganizationTreeBean getSystemOrgan(String regionId){
		String sql="SELECT ORG_ID ,ORG_NAME FROM TSM_ORGANIZATION WHERE UP_ORG = "+"(SELECT ORG_ID FROM TSM_ORGANIZATION WHERE org_id ="
				+" (SELECT UP_ORG FROM TSM_ORGANIZATION"
				+ " WHERE REGION_ID IN (?)"
				+" ORDER BY LINKID ASC LIMIT 1))"+ "AND REGION_ID IN (?)";
		List treeList = this.jt.queryForList(sql, regionId, regionId);
		SystemOrganizationTreeBean systemtree = null;
		if(!treeList.isEmpty()){
			systemtree = new SystemOrganizationTreeBean();
			Map item = (Map) treeList.get(0);
			systemtree.setOrgId(item.get("ORG_ID").toString());
			systemtree.setOrgName(item.get("ORG_NAME").toString());
		}
		return systemtree;
	}
	
	/**
	 *  取子部门接点
	 * @param ID
	 * @param Prama
	 * @return
	 */
	private SystemOrganizationTreeBean getSystemOrganization(String orgId, String prama) {
		String parStr = "";
		String querySql = "";
		if (prama != null) {
			String paramString = "";
			if (prama.indexOf("||") > -1 && !"||".equals(prama)) {
				paramString = prama.replace('|', ':');
				String[] par = paramString.split("::");
				for (int i = 0; i < par.length; i++) {
					parStr += par[i] + ",";
				}
				if (parStr != null)
					parStr = parStr.substring(0, parStr.length() - 1);
			} else
				parStr = "";
		}
		if (parStr == null) {
			querySql = "SELECT * FROM TSM_ORGANIZATION WHERE ORG_ID = '"
					+ orgId + "'";
		} else if (parStr.equals("")) {
			querySql = "SELECT * FROM TSM_ORGANIZATION WHERE ORG_ID = '"
					+ orgId + "'";
		} else {
			querySql = "SELECT * FROM TSM_ORGANIZATION WHERE ORG_ID = '"
					+ orgId + "'" + " AND REGION_ID IN (" + parStr + ")";
		}
		List treeList = this.jt.queryForList(querySql);//CodeSec未验证的SQL注入；CodeSec误报：2
		SystemOrganizationTreeBean systemtree = null;
		if(!treeList.isEmpty()){
			systemtree = new SystemOrganizationTreeBean();
			Map item = (Map) treeList.get(0);
			systemtree.setOrgId(item.get("ORG_ID").toString());
			systemtree.setOrgName(item.get("ORG_NAME").toString());
		}
		return systemtree;
	}

	public List getAskChannel(String entity){
		String strSql = "SELECT REFER_ID,COL_VALUE_NAME,ENTITY_ID FROM PUB_COLUMN_REFERENCE " +
		        "WHERE TABLE_CODE = 'CC_SERVICE_ORDER_ASK' AND COL_CODE = 'ACCEPT_CHANNEL_ID' AND ENTITY_ID = '" +
		        entity +
		        "' ORDER BY COL_ORDER ";
	    String newSql = systemAuthorization.getAuthedSql(null, strSql, "900018304");
	    return this.jt.queryForList(newSql);//CodeSec未验证的SQL注入；CodeSec误报：1
	}

}
