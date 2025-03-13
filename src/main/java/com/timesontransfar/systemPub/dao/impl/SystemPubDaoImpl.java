package com.timesontransfar.systemPub.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.common.authorization.service.impl.SystemAuthorizationWas;
import com.timesontransfar.systemPub.dao.SystemPubDao;
import com.timesontransfar.systemPub.entity.CapchaInfo;
import com.timesontransfar.systemPub.entity.CapchaInfoRmp;
import com.timesontransfar.systemPub.entity.PubColumn;
import com.timesontransfar.systemPub.entity.PubColumnRmp;
import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONArray;

@SuppressWarnings({ "unchecked", "rawtypes" })
@Component(value = "systemPubDao")
public class SystemPubDaoImpl implements SystemPubDao {
	private static final Logger log = LoggerFactory.getLogger(SystemPubDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;

	@Autowired
	private SystemAuthorizationWas systemAuthorization;
	
	@Autowired
	private RedisUtils redisUtils;
	
	class KeyRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int arg1) throws SQLException {
            return rs.getString(1);
        }
    }
    
	private String getLinkOrgBySql(String orgSql) {
		List linkIdList = this.jt.query(orgSql, new KeyRowMapper());
		StringBuilder strWhere = new StringBuilder();
		for(int i = 0; i < linkIdList.size(); i++) {
			String linkId = linkIdList.get(i).toString();
			strWhere.append(" t.LINKID='" + linkId + "' or t.LINKID like '" + linkId + "-%' or");
		}
		String orgWhere = strWhere.substring(0, strWhere.length()-2);
		orgWhere = " and (" + orgWhere + ")";
		return orgWhere;
	}
	
	private String getLinkOrgByOrgId(String orgId) {
		String strWhere = "";
		String sql = "select t.LINKID from tsm_organization t where t.ORG_ID=?";
		List org = this.jt.query(sql, new Object[] {orgId}, new KeyRowMapper());
		if(!org.isEmpty() && null != org.get(0)) {
			String linkId = org.get(0).toString();
			strWhere = " and (t.LINKID='" + linkId + "' or t.LINKID like '" + linkId + "-%')";
		}
		return strWhere;
	}
	
	private String getLinkOrgByOrgIdArray(String orgIdArray) {
		String sql = "select t.LINKID from tsm_organization t where t.ORG_ID in (" + orgIdArray + ")";
		List orgList = this.jt.query(sql, new KeyRowMapper());
		StringBuilder strWhere = new StringBuilder();
		for(int i = 0; i < orgList.size(); i++) {
			String linkId = orgList.get(i).toString();
			strWhere.append(" t.LINKID='" + linkId + "' or t.LINKID like '" + linkId + "-%' or");
		}
		String orgWhere = strWhere.substring(0, strWhere.length()-2);
		orgWhere = " and (" + orgWhere + ")";
		return orgWhere;
	}
	
	@Override
	public List queryOrgInfo(String flag, String parm) {
		List tmpList = null;
		try {
			String tmpString = this.redisUtils.get("tsm_organization:queryOrgInfo?flag="+flag+"&parm="+parm, RedisType.WORKSHEET);
			tmpList = JSON.parseObject(tmpString, List.class);
		}
		catch(Exception e) {
			log.error("redisUtils异常：queryOrgInfo {}", e.getMessage());
    	}
		if(tmpList == null) {
			String querySql = "SELECT t.ORG_ID,t.ORG_NAME,t.ORG_LEVEL,t.UP_ORG,a.org_name parentName,b.org_id isDisabled "
						+ "FROM tsm_organization t left join tsm_organization a on t.up_org = a.org_id "
						+ "left join cc_empty_org b on t.org_id=b.org_id where t.state!=3";
			if("3".equals(flag)){
				querySql += this.getLinkOrgBySql("SELECT t.LINKID FROM tsm_organization t where t.organization_type=1");
			}else if("5".equals(flag)){
				querySql += this.getLinkOrgBySql("SELECT t.LINKID FROM tsm_organization t where t.organization_type in(1,2)");
			}else if("4".equals(flag)){
				querySql += this.getLinkOrgByOrgId(parm);
			}else if("6".equals(flag)){
				querySql += "  and t.LINKID like '10%'";
			}
			List tempList = this.jt.queryForList(querySql);//CodeSec未验证的SQL注入；CodeSec误报：2
			if (!tempList.isEmpty()) {
				JSONArray jsonArray = JSONArray.fromObject(tempList);
				this.redisUtils.setex("tsm_organization:queryOrgInfo?flag="+flag+"&parm="+parm,86400,jsonArray.toString(),RedisType.WORKSHEET);
				return tempList;
			}
		}
		return tmpList;
	}

	@Override
	public String queryStaffByOrgId(int orgId,String staffName,String loginName,String flag,String parm) {
		StringBuilder querySql = new StringBuilder("SELECT t.STAFF_ID,t.STAFFNAME,t.LOGONNAME FROM tsm_staff t where 1=1 and t.state=8");
		List<Object> params = new ArrayList<>();
		if(orgId != 0){
			querySql.append(" AND t.org_id = ?");
			params.add(orgId);
		}
		if(StringUtils.isNotEmpty(staffName)){
			querySql.append(" AND t.staffname LIKE ?");
			params.add("%" + staffName + "%");
		}
		if(StringUtils.isNotEmpty(loginName)){
			querySql.append(" AND t.logonname LIKE ?");
			params.add("%" + loginName + "%");
		}
		if(StringUtils.isNotEmpty(flag) && StringUtils.isNotEmpty(parm)){
			//限制部门，不能选择
			querySql.append(" AND t.org_id NOT IN (SELECT org_id FROM cc_empty_org)");
			if("3".equals(flag)){
				String orgWhere = this.getLinkOrgBySql("SELECT t.LINKID FROM tsm_organization t where t.organization_type=1");
				querySql.append(" and t.org_id in( SELECT t.org_id FROM tsm_organization t where 1=1 " + orgWhere + ")");
			}else if("5".equals(flag)){
				String orgWhere = this.getLinkOrgBySql("SELECT t.LINKID FROM tsm_organization t where t.organization_type in(1,2)");
				querySql.append(" and t.org_id in( SELECT t.org_id FROM tsm_organization t where 1=1 " + orgWhere + ")");
			}else if("4".equals(flag)){
				String orgWhere = this.getLinkOrgByOrgId(parm);
				querySql.append(" and t.org_id in( SELECT t.org_id FROM tsm_organization t where 1=1 " + orgWhere + ")");
			}
		}
		//限制条件部门 不能选择
		List tempList=this.jt.queryForList(querySql.toString(),params.toArray());//CodeSec未验证的SQL注入；CodeSec误报：2
		if(!tempList.isEmpty()){
			JSONArray array=JSONArray.fromObject(tempList);
			return array.toString();
		}
		return null;
	}

	public List queryAskInfoNew(List list) {
		List result = new ArrayList<>();
		result.addAll(list);
		List<String> entityIdList = new ArrayList<>();
		for(Object obj : list) {
			Map<String,String> map = (Map<String,String>)obj;
			String referId = String.valueOf(map.get("REFER_ID"));
			String sql = "select a.REFER_ID,a.COL_VALUE_NAME,a.ENTITY_ID from pub_column_reference a where a.ENTITY_ID=?";
			List tempList = jt.queryForList(sql, referId);//子类
			result.addAll(tempList);
			
			String entityId = String.valueOf(map.get("ENTITY_ID"));
			if(!entityIdList.contains(entityId)) {
				entityIdList.add(entityId);
			}
		}
		
		String[] entityIdArr = entityIdList.stream().toArray(String[]::new);
		String entityIdStr = StringUtils.join(entityIdArr,",");
		String sql = "select a.REFER_ID,a.COL_VALUE_NAME,a.ENTITY_ID from pub_column_reference a where a.REFER_ID in (" + entityIdStr + ")";
		List parentList = jt.queryForList(sql);//父类
		result.addAll(parentList);
		entityIdList = new ArrayList<>();
		for(Object obj : parentList) {
			Map<String,String> map = (Map<String,String>)obj;
			String entityId = String.valueOf(map.get("ENTITY_ID"));
			if(!entityIdList.contains(entityId)) {
				entityIdList.add(entityId);
			}
		}
		
		entityIdArr = entityIdList.stream().toArray(String[]::new);
		entityIdStr = StringUtils.join(entityIdArr,",");
		sql = "select a.REFER_ID,a.COL_VALUE_NAME,a.ENTITY_ID from pub_column_reference a where a.REFER_ID in (" + entityIdStr + ")";
		parentList = jt.queryForList(sql);//父类
		result.addAll(parentList);
		return result;
	}

	@Override
	public List<PubColumn> queryReasonInfo() {
		String sql="SELECT A.REFER_ID,A.TABLE_CODE,A.COL_CODE,A.COL_NAME,A.COL_VALUE,A.COL_VALUE_NAME,A.ENTITY_ID,A.COL_ORDER,A.COL_VALUE_HANDLING,A.HAVING_CHILD_ITEM\n" +
						"  FROM PUB_COLUMN_REFERENCE A\n" + 
						" WHERE A.TABLE_CODE = 'CC_SHEET_QUALITATIVE'\n" + 
						"   AND A.COL_CODE IN ('TS_KEY_WORD_TS','SUB_KEY_WORD','THREE_GRADE_CATALOG','FOUR_GRADE_CATALOG','FIVE_GRADE_CATALOG','SIX_GRADE_CATALOG')\n" + 
						"   ORDER BY A.COL_ORDER ASC";
		return jt.query(sql, new PubColumnRmp());
	}

	public List<PubColumn> queryPubColumnNew(List<PubColumn> list, String referId) {
		String sql = "SELECT A.REFER_ID,A.TABLE_CODE,A.COL_CODE,A.COL_NAME,A.COL_VALUE,A.COL_VALUE_NAME,A.ENTITY_ID,A.COL_ORDER,A.COL_VALUE_HANDLING,A.HAVING_CHILD_ITEM " +
				"FROM PUB_COLUMN_REFERENCE A WHERE A.REFER_ID = ?";
		List<PubColumn> parentList = jt.query(sql, new Object[]{referId}, new PubColumnRmp());
		if (!parentList.isEmpty()) {
			list.addAll(parentList);
			String entityId = parentList.get(0).getEntityId();
			if(StringUtils.isNoneEmpty(entityId)) {
				return this.queryPubColumnNew(list, entityId);
			}
		}
		return list;
	}
	
	public List auxiliaryToolMuen(String logName){
		String menuSql="select F.MENU_ID, F.MENU_NAME, F.Url, F.MENU_SEQ\n" +
						"  from tsm_staff_role_rela a,\n" +
						"       tsm_staff           b,\n" +
						"       tsm_role            c,\n" +
						"       TSM_ROLE_FUNC_RELA  d,\n" +
						"       TSM_MAINMENU        f,\n" +
						"       tsm_menu_rela       e\n" +
						" where a.role_id = c.role_id\n" +
						"   and a.staff_id = b.staff_id\n" +
						"   and a.role_id = d.role_id\n" +
						"   and f.menu_id = d.func_id\n" +
						"   and b.logonname =?\n" +
						"   and f.menu_id = e.menu_id\n" +
						"   and e.top_menu_id = '122675a3275b4082a37c6509bea371c9'\n" +
						" order by f.menu_seq";
		return this.jt.queryForList(menuSql,logName);
	}

	@Override
	public List specificOrgInfo(String orgId) {
		String specificSql="SELECT t.org_id,t.org_name,t.org_level,t.org_level,t.up_org,a.org_name parentName,b.org_id isDisabled\n" +
				"  FROM tsm_organization t left join tsm_organization a on t.up_org = a.org_id\n" +
				"  left join cc_empty_org b on t.org_id=b.org_id where 1=1 ";
		String whereStr = this.getLinkOrgByOrgId(orgId);
		List specificInfo=this.jt.queryForList(specificSql + whereStr);//CodeSec未验证的SQL注入；CodeSec误报：1
		if(specificInfo.isEmpty()){
			return Collections.emptyList();
		}
		return specificInfo;
	}

	@Override
	public JSONArray queryOrgTypeInfo(int type) {
		String sql="SELECT t.ORG_ID,t.ORG_NAME,b.org_id isDisabled FROM tsm_organization t left join cc_empty_org b on t.org_id=b.org_id ";
		if(type==1){
			sql+="where t.organization_type = 1";
		}else if(type==2){
			sql+="where t.organization_type in (1,2)";
		}
		List specificInfo=this.jt.queryForList(sql);
		return JSONArray.fromObject(specificInfo);
	}

	@Override
	public int saveCaptchaInfo(CapchaInfo capchaInfo) {
		String saveSql="insert into CC_CAPTCHA_INFO( CAPTCHA_ID, SEND_TIME, VALID_TIME, STAFF_ID, CAPTCHA, CAPTCHA_TYPE, CAPTCHA_NUMBER, CAPTCHA_CONTENT )\n" +
						"values(?, NOW(), str_to_date(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?, ?, ?)";
		return jt.update(saveSql, capchaInfo.getCaptchaId(), capchaInfo.getValIdTime(), capchaInfo.getStaffId(), capchaInfo.getCaptcha(),
				capchaInfo.getCaptchaType(), capchaInfo.getCaptchaNumer(), capchaInfo.getCaptchaContent());
	}

	@Override
	public CapchaInfo captChaByGuid(String guid) {
		String sql="SELECT CAPTCHA_ID, SEND_TIME, VALID_TIME, STAFF_ID, CAPTCHA, CAPTCHA_TYPE, CAPTCHA_NUMBER, CAPTCHA_CONTENT FROM CC_CAPTCHA_INFO T WHERE T.CAPTCHA_ID=?";
		List<CapchaInfo> list=jt.query(sql,new Object[]{guid}, new CapchaInfoRmp());
		if (list.isEmpty()) {
			log.warn("没有查询到默认得受理渠道");
			return null;
		}
		return list.get(0);
	}

	@Override
	public List<PubColumn> queryFourSixMulu() {
		String sql="SELECT T.REFER_ID,T.TABLE_CODE,T.COL_CODE,T.COL_NAME,T.COL_VALUE,T.COL_VALUE_NAME,IF(T.REFER_ID IN(720130000,720200003),'202307',"
				+ "IF(T.ENTITY_ID='202307',IF(T.COL_VALUE_HANDLING='APPEAL_PROD_ID_TS',720130000,720200003),T.ENTITY_ID))ENTITY_ID,T.COL_ORDER,"
				+ "T.COL_VALUE_HANDLING,T.HAVING_CHILD_ITEM FROM PUB_COLUMN_REFERENCE T WHERE T.REFER_ID IN(720130000,720200003)OR(T.TABLE_CODE='CC_SERVICE_CONTENT_ASK'"
				+ "AND T.COL_VALUE_HANDLING IN('APPEAL_PROD_ID_TS','APPEAL_PROD_ID_CX'))ORDER BY T.COL_ORDER";
		return jt.query(sql, new PubColumnRmp());
	}

	@Override
	public List<PubColumn> loadColumnsByEntity(String table, String colCode, String entity) {
		String sql = "SELECT * FROM (SELECT T.REFER_ID,T.TABLE_CODE,T.COL_CODE,T.COL_NAME,T.COL_VALUE,T.COL_VALUE_NAME,T.ENTITY_ID,T.COL_ORDER,T.COL_VALUE_HANDLING,T.HAVING_CHILD_ITEM "
				+ "FROM PUB_COLUMN_REFERENCE T LEFT JOIN PUB_COLUMN_REFERENCE_STATUS S ON T.REFER_ID=S.REFER_ID "
				+ "WHERE (S.STATUS IS NULL OR S.STATUS <> '1') AND T.ENTITY_ID IN "
				+ "(SELECT T.REFER_ID FROM PUB_COLUMN_REFERENCE T LEFT JOIN PUB_COLUMN_REFERENCE_STATUS S ON T.REFER_ID=S.REFER_ID "
				+ "WHERE (S.STATUS IS NULL OR S.STATUS <> '1') AND T.TABLE_CODE = ? AND T.COL_CODE = ? AND T.ENTITY_ID = ?) "
				+ "UNION "
				+ "SELECT T.REFER_ID,T.TABLE_CODE,T.COL_CODE,T.COL_NAME,T.COL_VALUE,T.COL_VALUE_NAME,T.ENTITY_ID,T.COL_ORDER,T.COL_VALUE_HANDLING,T.HAVING_CHILD_ITEM"
				+ " FROM PUB_COLUMN_REFERENCE T LEFT JOIN PUB_COLUMN_REFERENCE_STATUS S ON T.REFER_ID=S.REFER_ID "
				+ "WHERE (S.STATUS IS NULL OR S.STATUS <> '1') AND T.TABLE_CODE = ? AND T.COL_CODE = ? AND T.ENTITY_ID = ?) RT ORDER BY COL_ORDER";
		return jt.query(sql,new Object[]{table,colCode,entity,table,colCode,entity}, new PubColumnRmp());
	}

	@Override
	public List<PubColumn> getAllColumnsNew(String table, String colValueHandling) {
		String sql = "SELECT T.REFER_ID,T.TABLE_CODE,T.COL_CODE,T.COL_NAME,T.COL_VALUE,T.COL_VALUE_NAME,T.ENTITY_ID,T.COL_ORDER,T.COL_VALUE_HANDLING,T.HAVING_CHILD_ITEM "
				+ "FROM PUB_COLUMN_REFERENCE T WHERE T.TABLE_CODE = ? AND T.COL_VALUE_HANDLING = ? ORDER BY T.COL_ORDER";
		return jt.query(sql, new Object[]{table, colValueHandling}, new PubColumnRmp());
	}

	@Override
	public List skillOrgTreelist(String strWhere) {
		String querySql = "SELECT distinct t.org_id,t.org_name,t.org_level,t.org_level,t.up_org,a.org_name parentName " +
						"FROM tsm_organization t left join tsm_organization a on t.up_org = a.org_id where 1=1 ";
		querySql += this.getLinkOrgByOrgIdArray(strWhere);
		return this.jt.queryForList(querySql);//CodeSec未验证的SQL注入；CodeSec误报：1
	}

	//按权限查询组织机构
	@Override
	public List queryOrgInfoAuth(String flag, String orgId,String staffId,String param) {
		String querySql = "";
		boolean other = false;
		//初始化查询 查出根节点
		if("1".equals(flag)){
            String pa = "";
            String rootsql = "";
            if (StringUtils.indexOf(param, "||") > 0) {
                String paramString = param.replace('|', ':');
                String[] par = paramString.split("::");
                for (int i = 0; i < par.length; i++) {
                    pa += par[i] + ",";
                }
                if (par.length < 15) {
                    pa = pa.substring(0, pa.length() - 1);
                } else {
                    pa = "";
                }
            }
            rootsql = "SELECT UP_ORG, ORG_ID, ORG_NAME FROM TSM_ORGANIZATION WHERE state != 3 ";
            querySql = systemAuthorization.getAuthedSql(null, rootsql, "666666");
            if (querySql.equals(rootsql)) {
                if (pa.equals("1")) {
                	querySql = "SELECT UP_ORG,ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE state != 3 and UP_ORG IS NULL";
                } else if (pa.equals("")) {
                	querySql = "SELECT UP_ORG,ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE state != 3 and UP_ORG IS NULL";
                } else {
                	querySql = "SELECT UP_ORG,ORG_ID ,ORG_NAME FROM TSM_ORGANIZATION WHERE state != 3 and UP_ORG = "
                            + "(SELECT ORG_ID FROM TSM_ORGANIZATION WHERE org_id ="
                            + " (SELECT UP_ORG FROM TSM_ORGANIZATION"
                            + " WHERE REGION_ID IN (" + pa + ")"
                            + " ORDER BY LINKID ASC LIMIT 1))"
                            + "AND REGION_ID IN (" + pa + ")";
                }
            }else {
            	other=true;
            }
		}
		if("5".equals(flag)){
			querySql ="select t.org_id," + 
					"       t.org_name," + 
					"       t.org_level," + 
					"       a.org_name  parentName," + 
					"       b.org_id    isDisabled," + 
					"       t.up_org," + 
					"       t.state" + 
					"  FROM tsm_organization t" + 
					"  left join tsm_organization a" + 
					"    on t.up_org = a.org_id" + 
					"  left join cc_empty_org b" + 
					"    on t.org_id = b.org_id" + 
					" WHERE t.ORG_ID IN (SELECT DISTINCT C.COND_VALUE" + 
					"                    FROM TSM_STAFF_ROLE_RELA A, TSM_ROLE B, TSM_CONDITION C" + 
					"                   WHERE A.ROLE_ID = B.ROLE_ID" + 
					"                     AND B.ROLE_ID = C.ROLE_ID" + 
					"                     AND C.OBJ_ID = '888888'" + 
					"                     AND A.STAFF_ID = '"+staffId+"')" + 
					"   and t.state != 3 " + 
					"ORDER BY t.org_level ";
		}
		if("6".equals(flag)){
			String rootsql = "SELECT ORG_ID ,ORG_NAME,UP_ORG  FROM TSM_ORGANIZATION WHERE STATE != 3 ";
			String pa = "";
			if (StringUtils.indexOf(param, "||") > 0) {
				String paramString = param.replace('|', ':');
				String[] par = paramString.split("::");
				for (int i = 0; i < par.length; i++) {
					pa += par[i] + ",";
				}
				if (par.length < 15){
					pa = pa.substring(0, pa.length() - 1);
				}else{
					pa = "";
				}
			}
			querySql = systemAuthorization.getAuthedSql(null, rootsql, "900018402");
			if (querySql.equals(rootsql)){
				if(pa.equals("1")){
					querySql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE UP_ORG IS NULL AND STATE != 3";
				}else if(pa.equals("")){
					querySql = "SELECT ORG_ID ,ORG_NAME  FROM TSM_ORGANIZATION WHERE UP_ORG IS NULL AND STATE != 3";
				}else{
					querySql="SELECT ORG_ID ,ORG_NAME FROM TSM_ORGANIZATION WHERE STATE != 3 AND UP_ORG = "+"(SELECT ORG_ID FROM TSM_ORGANIZATION WHERE org_id ="
							+" (SELECT UP_ORG FROM TSM_ORGANIZATION"
							+ " WHERE REGION_ID IN ("+pa+")"
							+" ORDER BY LINKID ASC LIMIT 1))"+ "AND REGION_ID IN ("+pa+")";
				}
			}
		}
		//根据根节点查出子节点
		if("111".equals(flag)) {
			return queryOrgNode(orgId);
		}
		List<Map<String, Object>> tempList = this.jt.queryForList(querySql);//CodeSec未验证的SQL注入；CodeSec误报：4
		//查出根节点
		if(tempList.isEmpty()) return null;
		if(!other) {
			int min=0;
			for(int i=0;i<tempList.size();i++){
				if(tempList.get(i).get("UP_ORG")!=null) {
					min=Integer.parseInt(tempList.get(i).get("UP_ORG").toString());
					break;
				}
			}
			for(int i=0;i<tempList.size();i++){
				if(tempList.get(i).get("UP_ORG")==null) {
					tempList.get(i).put("UP_ORG", 0);
				}
				int upId=Integer.parseInt(tempList.get(i).get("UP_ORG").toString());
				min=min<upId?min:upId;
			}
			List resultList=new ArrayList();
			for(int y=0;y<tempList.size();y++) {
				  int upId=Integer.parseInt(tempList.get(y).get("UP_ORG").toString());
				  if(min==upId) {
					  resultList.add(tempList.get(y));
					  break;
				  }
			}
			return resultList;
		}
		return tempList;
	}
	
	public List queryOrgNode(String upOrg) {
		String childsql = "select t.org_id,\r\n" + 
				"       t.org_name,\r\n" + 
				"       t.org_level,\r\n" + 
				"       a.org_name  parentName,\r\n" + 
				"       b.org_id    isDisabled,\r\n" + 
				"       t.up_org\r\n" + 
				"  FROM TSM_ORGANIZATION t\r\n" + 
				"  left join tsm_organization a\r\n" + 
				"    on t.up_org = a.org_id\r\n" + 
				"  left join cc_empty_org b\r\n" + 
				"    on t.org_id = b.org_id\r\n" + 
				" WHERE t.up_org = ?";
		return this.jt.queryForList(childsql,upOrg);
	}

	@Override
	public List<PubColumn> loadColumnsByEntityNew(String table, String colCode, String entity) {

		//String sql = "SELECT T.REFER_ID ,T.COL_VALUE_NAME ,ifnull(T.COL_NAME,' ') as COL_NAME,ifnull(T.COL_VALUE,' ') as COL_VALUE FROM PUB_COLUMN_REFERENCE T " +
				///" where T.TABLE_CODE = ?  AND T.ENTITY_ID = ?  ORDER BY T.COL_ORDER " ;
		String sql = "SELECT T.REFER_ID,T.TABLE_CODE,T.COL_CODE,T.COL_NAME,T.COL_VALUE,T.COL_VALUE_NAME,T.ENTITY_ID,T.COL_ORDER,T.COL_VALUE_HANDLING,T.HAVING_CHILD_ITEM "
				+ "FROM PUB_COLUMN_REFERENCE T WHERE T.TABLE_CODE = ? AND T.ENTITY_ID = ? ORDER BY T.COL_ORDER";
		return jt.query(sql, new Object[]{table, entity}, new PubColumnRmp());
	}

	public int addColumnsReference(Map<String, String> newColumnData){
		String referId = newColumnData.get("ReferId");
		String tableCode = newColumnData.get("TableCode");
		String colCode = newColumnData.get("ColCode");
		String colValue = newColumnData.get("ColValue");
		String colValueName = newColumnData.get("ColValueName");
		String colName = newColumnData.get("ColName");
		String colOrder = newColumnData.get("ColOrder");
		String colValueHandling = newColumnData.get("ColValueHandling");
		String havingChildItem = newColumnData.get("HavingChildItem");
		String entityId = newColumnData.get("EntityId");
		//新增该条数据
		StringBuffer sb = new StringBuffer().append("INSERT INTO PUB_COLUMN_REFERENCE (REFER_ID,TABLE_CODE,COL_CODE,COL_NAME,COL_VALUE,COL_VALUE_NAME,COL_VALUE_HANDLING,ENTITY_ID,COL_ORDER,HAVING_CHILD_ITEM) VALUES ('")
				.append(referId).append("','").append(tableCode).append("','").append(colCode).append("','")
				.append(colName).append("','").append(colValue).append("','").append(colValueName).append("','")
				.append(colValueHandling).append("','").append(entityId).append("','").append(colOrder).append("','")
				.append(havingChildItem).append("')");
		return this.jt.update(sb.toString());
	}

	public int updateColumnsReference(Map<String, String> updateColumnData){
		String referId = updateColumnData.get("ReferId");
		String colValue = updateColumnData.get("ColValue");
		String colValueName = updateColumnData.get("ColValueName");
		String colOrder = updateColumnData.get("ColOrder");
		String havingChildItem = updateColumnData.get("HavingChildItem");
		String sql ="UPDATE PUB_COLUMN_REFERENCE SET COL_VALUE=?,COL_VALUE_NAME=?,COL_ORDER=?,HAVING_CHILD_ITEM=?  where REFER_ID = ?";
		return jt.update(sql, colValue,colValueName,colOrder,havingChildItem,referId);
	}

	public int delColumnsReference(String referId){
		String sql ="delete from PUB_COLUMN_REFERENCE  where REFER_ID = ?";
		return jt.update(sql,referId);
	}

	@Override
	public List<PubColumn> getAllColumnsNew(String table) {
		String sql = "SELECT T.REFER_ID,T.TABLE_CODE,T.COL_CODE,T.COL_NAME,T.COL_VALUE,T.COL_VALUE_NAME,T.ENTITY_ID,T.COL_ORDER,T.COL_VALUE_HANDLING,T.HAVING_CHILD_ITEM "
					+ "FROM PUB_COLUMN_REFERENCE T WHERE T.TABLE_CODE = ?  ORDER BY T.COL_ORDER";
        if("CC_SERVICE_CONTENT_ASK".equals(table)){
            sql = "SELECT T.REFER_ID,T.TABLE_CODE,T.COL_CODE,T.COL_NAME,T.COL_VALUE,T.COL_VALUE_NAME,T.ENTITY_ID,T.COL_ORDER,T.COL_VALUE_HANDLING,T.HAVING_CHILD_ITEM "
                    + "FROM PUB_COLUMN_REFERENCE T WHERE T.TABLE_CODE = ? and (T.COL_CODE='PROD_ONE' or T.COL_CODE='PROD_TWO') ORDER BY T.COL_ORDER";
        }
		return jt.query(sql, new Object[]{table}, new PubColumnRmp());
	}

}
