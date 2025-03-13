package com.timesontransfar.customservice.dbgridData.impl;

import com.timesontransfar.customservice.dbgridData.CaseDao;
import com.timesontransfar.customservice.worksheet.pojo.CaseDataEntity;
import com.timesontransfar.sheetCase.entity.CaseEntity;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component(value="caseDaoImpl")
public class CaseDaoImpl implements CaseDao {
	
    public static final String SQL1 = " and c.order_id = ";
    public static final String SQL2 = " and c.update_time between  " ;
    public static final String SQL3 = " and c.service_type = " ;
    public static final String SQL4 = " and c.case_id = " ;
    public static final String SQL5 = " and c.prod_num = " ;
    public static final String SQL6 = " and c.case_level = " ;
    public static final String SQL7 = " and c.case_state = " ;
    public static final String SQL8 = "level";
    public static final String SQL9 = "select c.case_id caseId,c.order_id orderId,c.creator,case when c.`case_level` = '1' THEN '个人' when c.`case_level` = '2' THEN '地市' when c.`case_level` = '3' THEN '省份' ELSE ' '  end as level,a.name encode,";
    public static final String SQL10 =  "c.case_detail caseDetail,case when c.`case_state` = '0' THEN '劣质' when c.`case_state` = '1' THEN '优质' ELSE ' '  end as caseState,c.creator_department creatorDepartment,DATE_FORMAT(c.update_time,'%Y-%m-%d %H:%i:%s') updateTime,c.prod_num sheetId,";
    public static final String SQL11 =  "','%Y-%m-%d %H:%i:%s') ";
    public static final String SQL12 =  "and str_to_date('";
    public static final String SQL13 =  "10-11";
    public static final String SQL14 =  " and c.update_time between str_to_date('";
    public static final String SQL15 =  "case when c.pass_status = '0' then '不通过' when c.pass_status = '1' then '通过' else '未审核' end as passStatus,c.not_pass_cause notPassCause,";
    public static final String SQL16 =  "case when c.`status` = '1' THEN '草稿' when c.`status` = '2' THEN '审批' when c.`status` = '3' THEN '发布' when c.`status` = '4' THEN '停用' ELSE ' '  end as STATUS,DATE_FORMAT(c.create_time,'%Y-%m-%d %H:%i:%s') createTime,str_to_date(c.lapse_time,'%Y-%m-%d %H:%i:%s') lapseTime,";
    public static final String SQL17 =  " UNION ALL select c.case_id caseId,c.order_id orderId,c.creator,case when c.`case_level` = '1' THEN '个人' when c.`case_level` = '2' THEN '地市' when c.`case_level` = '3' THEN '省份' ELSE ' '  end as level,a.name encode,";
    public static final String SQL18 = "and c.encode = ";
    public static final String SQL19 = "and c.creator = ";
    public static final String SQL20 = " limit ";
    public static final String SQL21 = " and str_to_date('";
    public static final String SQL22 = "','%Y-%m-%d %H:%i:%s')";
    public static final String SQL23 = " and c.encode = ";
    public static final String SQL24 = " and c.status = ";
    public static final String SQL25 = "ELSE ' ' ";
    public static final String SORT = " order by updateTime desc" ;
    
    @Autowired
    public JdbcTemplate jdbcTemplate;

    /*
    查询对应的地市和部门名称
     */
    @Override
    public Map<String, Object> getCreator(String creator) {
        String sql="select LINKID as city,ORG_NAME as orgName from tsm_organization where ORG_ID = (select ORG_ID from tsm_staff where LOGONNAME ="+creator+" )";
        Map<String, Object> stringObjectMap = jdbcTemplate.queryForMap(sql);
        String city = (String) stringObjectMap.get("city");
        String citEnd = city.substring(0, city.indexOf("-", city.indexOf("-") + 1));
        stringObjectMap.replace("city",citEnd);
        return stringObjectMap;
    }

    /*
    新增案例
     */
    @Override
    public int addCase(CaseEntity sheetCase) {
        String sql = "INSERT INTO tsm_order_case(case_id, order_id," +
                " creator, case_level, encode, status, " +
                " case_detail, case_state, creator_department," +
                " update_time, prod_num, audit_status,pass_status,service_type)" +
                " VALUES (?, ?, ?, ?, ?, ?,  ?, ?, ?, NOW(), ?, ?,?,?) ";
        return  jdbcTemplate.update(sql,
                sheetCase.getCaseId(),
                sheetCase.getOrderId(),
                sheetCase.getCreator(),
                sheetCase.getLevel(),
                sheetCase.getEncode(),
                sheetCase.getStatus(),
                sheetCase.getCaseDetail(),
                sheetCase.getCaseState(),
                sheetCase.getCreatorDepartment(),
                sheetCase.getSheetId(),
                sheetCase.getAuditStatus(),
                "2",
                sheetCase.getOrderType());
    }


    /*
    修改案例
     */
    @Override
    public int updateCase(String status, JSONObject jsonObject, String orgName, String caseDetail, String level, String notPassCause, String caseReview) {
        String caseId = (String) jsonObject.get("caseId");  //案例编号
        String approver = (String) jsonObject.get("approver");//审核人
        //修改案例审核状态
        String sql = "update tsm_order_case set create_time = NOW(), audit_status = 1,status = ?,approver = ?,audit_department = ? , case_detail = ? , case_level = ? , not_pass_cause = ?,pass_status = ? where case_id = ?";
        Object[] args = {status,approver,orgName,caseDetail,level,notPassCause,caseReview,caseId};
        return jdbcTemplate.update(sql, args);
    }

    /*
   修改案例状态/亮点/差错点
    */
    @Override
    public int updateSheetCase(String level,String caseId, String caseDetail, String status) {
        List<Object> param= new LinkedList<>();

        String sql = "UPDATE tsm_order_case  SET ";

        if (status != null && !status.equals("") ) {
            sql += "status= ? ";
            param.add(status);
        }

        if (caseDetail != null && !caseDetail.equals("")) {
            sql += ",case_detail= ? ";
            param.add(caseDetail);
        }

        if (level != null && !level.equals("")){
            sql += ",case_level= ?";
            param.add(level);
        }

        sql += " where case_id=?";
        param.add(caseId);
        return jdbcTemplate.update(sql, param.toArray());
    }

    public List<Map<String, Object>> getCaseData(CaseEntity caseEntity){

        List<Map<String, Object>> caseEntityList = null;

        StringBuilder  sql1 = new StringBuilder ();

        if (StringUtils.isNotEmpty(caseEntity.getOrderId())){
            sql1.append(SQL1+"'"+caseEntity.getOrderId()+"'");
        }
        if (caseEntity.getOrderType() != 0){
            sql1.append(SQL3+"'"+caseEntity.getOrderType()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseId())){
            sql1.append(SQL4+"'"+caseEntity.getCaseId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getSheetId())){
            sql1.append(SQL5+"'"+caseEntity.getSheetId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getLevel())){
            sql1.append(SQL6+"'"+caseEntity.getLevel()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseState())){
            sql1.append(SQL7+"'"+caseEntity.getCaseState()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getEncode())){
            sql1.append(SQL23+"'"+caseEntity.getEncode()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getApprover())){
            sql1.append(" and c.approver = "+"'"+caseEntity.getApprover()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getAuditDepartment())){
            sql1.append(" and c.audit_department = "+"'"+caseEntity.getAuditDepartment()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getStatus())){
            sql1.append(SQL24+"'"+caseEntity.getStatus()+"'");
        }

        String sql = "select c.case_id caseId,c.order_id orderId,c.creator," +
                "case when c.case_level = '1' THEN '个人'" +
                "when c.`case_level` = '2' THEN '地市'" +
                "when c.`case_level` = '3' THEN '省份'"+
                "ELSE '' " +
                "end as level," +
                "b.name as encode," +
                "case when c.STATUS = '1' THEN '草稿'" +
                "when c.STATUS = '2' THEN '审批'" +
                "when c.STATUS = '3' THEN '发布'" +
                "when c.STATUS = '4' THEN '停用'" +
                SQL25 +
                "end as STATUS," +
                SQL15+
                "DATE_FORMAT(c.create_time,'%Y-%m-%d %H:%i:%s') createTime,DATE_FORMAT(c.lapse_time,'%Y-%m-%d %H:%i:%s') lapseTime,c.case_detail caseDetail," +
                "case  when c.`case_state` = '0' THEN '劣质' when c.`case_state` = '1' THEN '优质'" +
                SQL25 +
                "end as caseState," +
                "c.creator_department creatorDepartment,DATE_FORMAT(c.update_time,'%Y-%m-%d %H:%i:%s') updateTime,c.prod_num sheetId,c.audit_status," +
                "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatusDesc," +
                "case when c.service_type = '720130000' then '投诉' when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType," +
                "c.approver,c.audit_department auditDepartment from tsm_order_case c LEFT JOIN tsm_order_case_citycode b on c.encode = b.encode where 1=1 and  " +
                "c.update_time between DATE_FORMAT('"+caseEntity.getBeginTime()+SQL22 +
                SQL21+caseEntity.getEndTime()+"','%Y-%m-%d %H:%i:%s') and c.creator = "+caseEntity.getCreator();
        String sql2 = SQL20+caseEntity.getCurrentPage()+","+caseEntity.getPageSize();
        String getDataSql = sql.concat(sql1.toString()).concat(SORT).concat(sql2);
        caseEntityList = jdbcTemplate.queryForList(getDataSql);
        return caseEntityList;
    }



    public List<Map<String, Object>> getCaseDataByAudit(CaseEntity caseEntity){

        Map<String,Object> cityDepartment = getCreator(caseEntity.getCreator());

        String city = (String) cityDepartment.get("city");  //城市编码

        StringBuilder sql1 = new StringBuilder();
        StringBuilder sql4 = new StringBuilder();

        if (StringUtils.isNotEmpty(caseEntity.getOrderId())){
            sql1.append(SQL1+"'"+caseEntity.getOrderId()+"'");
        }

        if (caseEntity.getOrderType() != 0){
            sql1.append(SQL3+"'"+caseEntity.getOrderType()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseId())){
            sql1.append(SQL4+"'"+caseEntity.getCaseId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getSheetId())){
            sql1.append(SQL5+"'"+caseEntity.getSheetId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getLevel())){
            if (caseEntity.getLevel().equals("1")){
                sql1.append(SQL6+"'"+caseEntity.getLevel()+"'"+SQL19+"'"+caseEntity.getCreator()+"'");
            }else if (caseEntity.getLevel().equals("2")){
                sql1.append(SQL6+"'"+caseEntity.getLevel()+"'"+SQL18+"'"+city+"'");
            }else if (caseEntity.getLevel().equals("3")){
                sql1.append(SQL6+"'"+caseEntity.getLevel()+"'");
            }
        }
       return this.getCaseDataByAudit2(caseEntity,sql1,sql4,city);
    }

    public List<Map<String, Object>> getCaseDataByAudit2(CaseEntity caseEntity,StringBuilder sql1,StringBuilder sql4,String city){
        List<Map<String, Object>> caseEntityList = null;
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotEmpty(caseEntity.getCaseState())){
            sql1.append(SQL7+"?");
            params.add(caseEntity.getCaseState());
        }
        if (StringUtils.isNotEmpty(caseEntity.getStatus())){
            sql1.append(SQL24+"?");
            params.add(caseEntity.getStatus());
        }
        if (StringUtils.isNotEmpty(caseEntity.getEncode())){
            sql1.append(SQL23+"?");
            params.add(caseEntity.getEncode());
        }
        if (StringUtils.isNotEmpty(caseEntity.getPassStatus())){
            sql1.append(" and c.pass_status = "+"?");
            params.add(caseEntity.getPassStatus());
        }
        if (getEncode(city)){
            sql1.append(SQL17 +
                    SQL16 +
                    SQL10 +
                    SQL15+
                    "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType, c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode  where 1=1  " +
                    " and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s')" +
                    " and str_to_date(?,'%Y-%m-%d %H:%i:%s') and c.case_level =3"+sql1 );
            params.add(caseEntity.getBeginTime());
            params.add(caseEntity.getEndTime());
        }
        if (city.contains(SQL13)){
            sql4.append(" ");
        }else {
            sql4.append(SQL23+"?");
            params.add(city);
        }

        String sql = SQL9 +
                SQL16 +
                SQL10 +
                SQL15+
                "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType, c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode " +
                "where 1=1 and c.case_level = 2 and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s')" +
                "and str_to_date(?,'%Y-%m-%d %H:%i:%s') ";
        params.add(caseEntity.getBeginTime());
        params.add(caseEntity.getEndTime());
        String sql2 = SQL20+"?,?";
        params.add(caseEntity.getCurrentPage());
        params.add(caseEntity.getPageSize());
        String getDataSql = sql.concat(sql4.toString()).concat(sql1.toString()).concat(SORT).concat(sql2);
        caseEntityList = jdbcTemplate.queryForList(getDataSql,params.toArray());
        return caseEntityList;
    }
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public List getCityCode() {
        String sql = "select c.encode,c.case_level,c.name from tsm_order_case_cityCode c ";
        return jdbcTemplate.queryForList(sql);
    }
    @Override
    public int getCaseCount(CaseEntity caseEntity){

        List<Map<String, Object>> caseEntityList = null;

        StringBuilder  sql1 = new StringBuilder ();

        if (StringUtils.isNotEmpty(caseEntity.getOrderId())){
            sql1.append(SQL1+"'"+caseEntity.getOrderId()+"'");
        }
        if (caseEntity.getOrderType() != 0){
            sql1.append(SQL3+"'"+caseEntity.getOrderType()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseId())){
            sql1.append(SQL4+"'"+caseEntity.getCaseId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getSheetId())){
            sql1.append(SQL5+"'"+caseEntity.getSheetId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getLevel())){
            sql1.append(SQL6+"'"+caseEntity.getLevel()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseState())){
            sql1.append(SQL7+"'"+caseEntity.getCaseState()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getEncode())){
            sql1.append(SQL23+"'"+caseEntity.getEncode()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getApprover())){
            sql1.append(" and c.approver = "+"'"+caseEntity.getApprover()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getAuditDepartment())){
            sql1.append(" and c.audit_department = "+"'"+caseEntity.getAuditDepartment()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getStatus())){
            sql1.append(SQL24+"'"+caseEntity.getStatus()+"'");
        }

        String sql = "select c.case_id caseId,c.order_id orderId,c.creator," +
                "case when c.case_level = '1' THEN '个人'" +
                "when c.`case_level` = '2' THEN '地市'" +
                "when c.`case_level` = '3' THEN '省份'"+
                "ELSE '' " +
                "end as level," +
                "b.name as encode," +
                "case when c.STATUS = '1' THEN '草稿'" +
                "when c.STATUS = '2' THEN '审批'" +
                "when c.STATUS = '3' THEN '发布'" +
                "when c.STATUS = '4' THEN '停用'" +
                SQL25 +
                "end as STATUS," +
                SQL15+
                "c.create_time createTime,c.lapse_time lapseTime,c.case_detail caseDetail," +
                "case  when c.`case_state` = '0' THEN '劣质' when c.`case_state` = '1' THEN '优质'" +
                SQL25 +
                "end as caseState," +
                "c.creator_department creatorDepartment,c.update_time updateTime,c.prod_num sheetId,c.audit_status," +
                "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatusDesc," +
                "case when c.service_type = '720130000' then '投诉' when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType," +
                "c.approver,c.audit_department auditDepartment from tsm_order_case c LEFT JOIN tsm_order_case_citycode b on c.encode = b.encode where 1=1 and  " +
                "c.update_time between str_to_date('"+caseEntity.getBeginTime()+SQL22 +
                SQL21+caseEntity.getEndTime()+"','%Y-%m-%d %H:%i:%s') and c.creator = "+caseEntity.getCreator();
        String getDataSql = sql.concat(sql1.toString()).concat(SORT);
        caseEntityList = jdbcTemplate.queryForList(getDataSql);
        return caseEntityList.size();
    }
    @Override
    public int getCaseByAuditCount(CaseEntity caseEntity){

        Map<String,Object> cityDepartment = getCreator(caseEntity.getCreator());

        String city = (String) cityDepartment.get("city");  //城市编码

        StringBuilder sql1 = new StringBuilder();

        StringBuilder sql4 = new StringBuilder();

        if (StringUtils.isNotEmpty(caseEntity.getOrderId())){
            sql1.append(SQL1+"'"+caseEntity.getOrderId()+"'");
        }
        if (caseEntity.getOrderType() != 0){
            sql1.append(SQL3+"'"+caseEntity.getOrderType()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseId())){
            sql1.append(SQL4+"'"+caseEntity.getCaseId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getSheetId())){
            sql1.append(SQL5+"'"+caseEntity.getSheetId()+"'");
        }
        if (StringUtils.isNotEmpty(caseEntity.getLevel())){
            if (caseEntity.getLevel().equals("1")){
                sql1.append(SQL6+"'"+caseEntity.getLevel()+"'"+SQL19+"'"+caseEntity.getCreator()+"'");
            }else if (caseEntity.getLevel().equals("2")){
                sql1.append(SQL6+"'"+caseEntity.getLevel()+"'"+SQL18+"'"+city+"'");
            }else if (caseEntity.getLevel().equals("3")){
                sql1.append(SQL6+"'"+caseEntity.getLevel()+"'");
            }
        }
       return this.getCaseByAuditCount2(caseEntity,sql1,sql4,city);
    }
    public int getCaseByAuditCount2(CaseEntity caseEntity,StringBuilder sql1,StringBuilder sql4,String city){
        List<Map<String, Object>> caseEntityList = null;
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotEmpty(caseEntity.getCaseState())){
            sql1.append(SQL7+"?");
            params.add(caseEntity.getCaseState());
        }
        if (StringUtils.isNotEmpty(caseEntity.getStatus())){
            sql1.append(SQL24+"?");
            params.add(caseEntity.getStatus());
        }
        if (StringUtils.isNotEmpty(caseEntity.getEncode())){
            sql1.append(SQL23+"?");
            params.add(caseEntity.getEncode());
        }
        if (StringUtils.isNotEmpty(caseEntity.getPassStatus())){
            sql1.append(" and c.pass_status = "+"?");
            params.add(caseEntity.getPassStatus());
        }
        if (getEncode(city)){
            sql1.append(SQL17 +
                    SQL16 +
                    SQL10 +
                    SQL15+
                    "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType, c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode  where 1=1  " +
                    "and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s') " +
                    "and str_to_date(?,'%Y-%m-%d %H:%i:%s')  and c.case_level =3"+sql1 );
            params.add(caseEntity.getBeginTime());
            params.add(caseEntity.getEndTime());
        }
        if (city.contains(SQL13)){
            sql4.append(" ");
        }else {
            sql4.append(SQL23+"?");
            params.add(city);
        }

        String sql = SQL9 +
                SQL16 +
                SQL10 +
                SQL15+
                "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType, c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode " +
                "where 1=1 and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s') "+
                "and str_to_date(?,'%Y-%m-%d %H:%i:%s') and c.case_level = 2 ";
        params.add(caseEntity.getBeginTime());
        params.add(caseEntity.getEndTime());
        String getDataSql = sql.concat(sql4.toString()).concat(sql1.toString());
        caseEntityList = jdbcTemplate.queryForList(getDataSql,params.toArray());
        return caseEntityList.size();
    }
    @Override
    public CaseDataEntity getCase(CaseEntity caseEntity) {

        CaseDataEntity caseDataEntity = new CaseDataEntity();

        Map<String,Object> cityDepartment = getCreator(caseEntity.getCreator());

        String city = (String) cityDepartment.get("city");  //城市编码

        StringBuilder sql1 = new StringBuilder();
        StringBuilder sql3 = new StringBuilder();
        StringBuilder sql4 = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (StringUtils.isNotEmpty(caseEntity.getOrderId())){
            sql1.append(SQL1+"?");
            params.add(caseEntity.getOrderId());
        }
        if (caseEntity.getOrderType() != 0){
            sql1.append(SQL3+"?");
            params.add(caseEntity.getOrderType());
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseId())){
            sql1.append(SQL4+"?");
            params.add(caseEntity.getCaseId());
        }
        if (StringUtils.isNotEmpty(caseEntity.getSheetId())){
            sql1.append(SQL5+"?");
            params.add(caseEntity.getSheetId());
        }
        if (StringUtils.isNotEmpty(caseEntity.getLevel())){
            if (caseEntity.getLevel().equals("1")){
                sql1.append(SQL6+"?"+SQL19+"?");
                params.add(caseEntity.getLevel());
                params.add(caseEntity.getCreator());
            }else if (caseEntity.getLevel().equals("2")){
                sql1.append(SQL6+"?"+SQL18+"?");
                params.add(caseEntity.getLevel());
                params.add(city);
            }else if (caseEntity.getLevel().equals("3")){
                sql1.append(SQL6+"?");
                params.add(caseEntity.getLevel());
            }
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseState())){
            sql1.append(SQL7+"?");
            params.add(caseEntity.getCaseState());
        }
        if (StringUtils.isNotEmpty(caseEntity.getStatus())){
            sql1.append(SQL24+"?");
            params.add(caseEntity.getStatus());
        }
        if (StringUtils.isNotEmpty(caseEntity.getEncode())){
            sql1.append(SQL23+"?");
            params.add(caseEntity.getEncode());
        }
        if (city.contains(SQL13)){
            sql4.append(" ");
        }else {
            sql4.append(SQL23+"?");
            params.add(city);
        }
        if (StringUtils.isEmpty(caseEntity.getLevel())){
            sql1.append(" and c.creator = ?"+
                    SQL17 +
                    "case when c.`status` = '1' THEN '草稿' when c.`status` = '2' THEN '审批' when c.`status` = '3' THEN '发布' when c.`status` = '4' THEN '停用' ELSE ' '  end as STATUS,DATE_FORMAT(c.create_time,'%Y-%m-%d %H:%i:%s') createTime,DATE_FORMAT(c.lapse_time,'%Y-%m-%d %H:%i:%s') lapseTime,c.case_detail caseDetail," +
                    "case when c.`case_state` = '0' THEN '劣质' when c.`case_state` = '1' THEN '优质' ELSE ' '  end as caseState,c.creator_department creatorDepartment,DATE_FORMAT(c.update_time,'%Y-%m-%d %H:%i:%s') updateTime,c.prod_num sheetId," +
                    "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType,c.approver,c.audit_department auditDepartment from tsm_order_case c  left join tsm_order_case_citycode a on a.encode = c.encode" +
                    " where 1=1 and c.audit_status = 1 and c.status = 3 and c.pass_status = 1 AND c.update_time BETWEEN str_to_date(?,'%Y-%m-%d %H:%i:%s')"+
                    "and str_to_date(?,'%Y-%m-%d %H:%i:%s')  and c.case_level =2 "+sql4+sql1+
                    SQL17 +
                    SQL16 +
                    SQL10 +
                    "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType,c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode  where 1=1 " +
                    " and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s')"+
                    "and str_to_date(?,'%Y-%m-%d %H:%i:%s') and c.audit_status = 1 and c.pass_status = 1 and c.status = 3 and c.case_level =3"+sql1);
            sql3.append(" and c.case_level = 1 ");
            params.add(caseEntity.getCreator());
            params.add(caseEntity.getBeginTime());
            params.add(caseEntity.getEndTime());
            params.add(caseEntity.getBeginTime());
            params.add(caseEntity.getEndTime());
        }

        String sql = SQL9 +
                SQL16 +
                SQL10 +
                "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType,c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode where 1=1 " +
                " and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s')" +
                "and str_to_date(?,'%Y-%m-%d %H:%i:%s') and c.audit_status = 1 and c.pass_status = 1 and c.status = 3 ";
        params.add(caseEntity.getBeginTime());
        params.add(caseEntity.getEndTime());
        String sql2 = SQL20+"?,?";
        params.add(caseEntity.getCurrentPage());
        params.add(caseEntity.getPageSize());
        String getDataSql = sql.concat(sql3.toString()).concat(sql1.toString()).concat(SORT).concat(sql2);
        List<Map<String,Object>>  caseEntityList = jdbcTemplate.queryForList(getDataSql,params.toArray());
        caseDataEntity.setList(caseEntityList);
        caseDataEntity.setCount(getCaseCountS(caseEntity));
        return caseDataEntity;
    }

    public List<Map<String,Object>> searchCase(String caseId){

        String sql = "select c.case_id caseId,c.order_id orderId,c.creator,c.case_level level,c.encode,c.status,c.create_time createTime,c.lapse_time lapseTime," +
                "c.case_detail caseDetail,c.case_state caseState,c.creator_department creatorDepartment,c.update_time updateTime,c.prod_num sheetId," +
                "c.audit_status auditStatus,c.not_pass_cause notPassCause,c.pass_status passStatus from tsm_order_case c where c.order_id = ?";
        return jdbcTemplate.queryForList(sql,caseId);
    }

    /*
    删除案例
     */
    @Override
    public int deleteCase(String caseId) {
        String sql = "DELETE FROM tsm_order_case WHERE case_id = ? and case_level = 1";
        return jdbcTemplate.update(sql, caseId);
    }

    @Override
    public int endCase(String caseId,String logonName) {
        String sql = "update tsm_order_case set status = 4 , lapse_time = now() where case_id = ? and pass_status = 1  ";
        return jdbcTemplate.update(sql,caseId);
    }

    /*
        查询案例级别和案例状态
         */
    @Override
    public Map<String, Object> getLevelStatus(String caseId) {
        String sql="select case_level level,status from tsm_order_case where case_id = ?";
        return jdbcTemplate.queryForMap(sql,caseId);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public String getTsReasonName(String orderId, boolean hisFlag) {
        String tsReasonName = null;
        try {
        	String hisStr = hisFlag ? "_his" : "";
            String sql = "select t.ts_reason_name as tsReasonName from cc_sheet_qualitative" + hisStr + " t where t.service_order_id = ? order by t.creat_data desc limit 1";
            List tmpList = jdbcTemplate.queryForList(sql, orderId);
            if(!tmpList.isEmpty()) {
            	return ((Map)tmpList.get(0)).get("tsReasonName").toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tsReasonName;
    }

    @Override
    public int getCaseNum(String orderId) {
        int num = 0;
        try {
            String sql = "select count(1) from tsm_order_case where order_id = ?";
            num = jdbcTemplate.queryForObject(sql, new Object[] { orderId }, Integer.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return num;
    }

    @Override
    public int updatePass(String caseId, String approver, String orgName, String caseDetail, String level,String caseReview) {
        //修改案例审核状态
        String sql = "update tsm_order_case set audit_status = 1,status = 3, create_time = NOW(),approver = ?,audit_department = ? , case_detail = ? , case_level = ?,pass_status = ?  where case_id = ?";
        Object[] args = {approver,orgName,caseDetail,level,caseReview,caseId};
        return jdbcTemplate.update(sql, args);
    }

    @Override
    public int getCaseCountS(CaseEntity caseEntity) {
        Map<String,Object> cityDepartment = getCreator(caseEntity.getCreator());

        String city = (String) cityDepartment.get("city");  //城市编码

        StringBuilder sql1 = new StringBuilder();
        StringBuilder sql3 = new StringBuilder();
        StringBuilder sql4 = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (StringUtils.isNotEmpty(caseEntity.getOrderId())){
            sql1.append(SQL1+"?");
            params.add(caseEntity.getOrderId());
        }
        if (caseEntity.getOrderType() != 0){
            sql1.append(SQL3+"?");
            params.add(caseEntity.getOrderType());
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseId())){
            sql1.append(SQL4+"?");
            params.add(caseEntity.getCaseId());
        }
        if (StringUtils.isNotEmpty(caseEntity.getSheetId())){
            sql1.append(SQL5+"?");
            params.add(caseEntity.getSheetId());
        }
        if (StringUtils.isNotEmpty(caseEntity.getLevel())){
            if (caseEntity.getLevel().equals("1")){
                sql1.append(SQL6+"?"+SQL19+"?");
                params.add(caseEntity.getLevel());
                params.add(caseEntity.getCreator());
            }else if (caseEntity.getLevel().equals("2")){
                sql1.append(SQL6+"?"+SQL18+"?");
                params.add(caseEntity.getLevel());
                params.add(city);
            }else if (caseEntity.getLevel().equals("3")){
                sql1.append(SQL6+"?");
                params.add(caseEntity.getLevel());
            }
        }
        if (StringUtils.isNotEmpty(caseEntity.getCaseState())){
            sql1.append(SQL7+"?");
            params.add(caseEntity.getCaseState());
        }
        if (StringUtils.isNotEmpty(caseEntity.getStatus())){
            sql1.append(SQL24+"?");
            params.add(caseEntity.getStatus());
        }
        if (StringUtils.isNotEmpty(caseEntity.getEncode())){
            sql1.append(SQL23+"?");
            params.add(caseEntity.getEncode());
        }
        if (city.contains(SQL13)){
            sql4.append(" ");
        }else {
            sql4.append(SQL23+"?");
            params.add(city);
        }
        if (StringUtils.isEmpty(caseEntity.getLevel())){
            sql1.append(" and c.creator = ?"+
                    SQL17 +
                    "case when c.`status` = '1' THEN '草稿' when c.`status` = '2' THEN '审批' when c.`status` = '3' THEN '发布' when c.`status` = '4' THEN '停用' ELSE ' '  end as STATUS,c.create_time createTime,c.lapse_time lapseTime,c.case_detail caseDetail," +
                    "case when c.`case_state` = '0' THEN '劣质' when c.`case_state` = '1' THEN '优质' ELSE ' '  end as caseState,c.creator_department creatorDepartment,c.update_time updateTime,c.prod_num sheetId," +
                    "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType,c.approver,c.audit_department auditDepartment from tsm_order_case c  left join tsm_order_case_citycode a on a.encode = c.encode" +
                    " where 1=1 and c.audit_status = 1 and c.status = 3 and c.pass_status = 1 AND c.update_time BETWEEN str_to_date(?,'%Y-%m-%d %H:%i:%s')"+
                    "and str_to_date(?,'%Y-%m-%d %H:%i:%s')  and c.case_level =2 "+ sql4 +sql1+
                    SQL17 +
                    SQL16 +
                    SQL10 +
                    "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType,c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode  where 1=1 " +
                    " and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s')"+
                    "and str_to_date(?,'%Y-%m-%d %H:%i:%s') and c.audit_status = 1 and c.pass_status = 1 and c.status = 3 and c.case_level =3"+sql1);
            sql3.append(" and c.case_level = 1 ");
            params.add(caseEntity.getCreator());
            params.add(caseEntity.getBeginTime());
            params.add(caseEntity.getEndTime());
            params.add(caseEntity.getBeginTime());
            params.add(caseEntity.getEndTime());
        }
        String sql = SQL9 +
                SQL16 +
                SQL10 +
                "case when c.audit_status = '1' then '已审核' else '未审核' end as auditStatus,case when c.service_type = '720130000' then '投诉'  when c.service_type = '700006312' then '咨询' when c.service_type = '720200003' then '查询' when c.service_type = '720200000' then '协同' else '' end as  orderType,c.approver,c.audit_department auditDepartment from tsm_order_case c left join tsm_order_case_citycode a on a.encode = c.encode where 1=1 " +
                " and c.update_time between str_to_date(?,'%Y-%m-%d %H:%i:%s')" +
                "and str_to_date(?,'%Y-%m-%d %H:%i:%s') and c.audit_status = 1 and c.pass_status = 1 and c.status = 3 ";
        params.add(caseEntity.getBeginTime());
        params.add(caseEntity.getEndTime());
        String getDataSql = sql.concat(sql3.toString()).concat(sql1.toString());
        List<Map<String,Object>>  caseEntityList = jdbcTemplate.queryForList(getDataSql,params.toArray());
        return caseEntityList.size();
    }

    public boolean getLevel(String caseId, String logonName) {
        boolean levelAudit = false;
        Map<String, Object> cityDepartment = getCreator(logonName);
        String city = (String) cityDepartment.get("city");
        String level = null;
        String myLevel = null;

        String sql = "select case_level level from tsm_order_case_cityCode where encode = ?";
        List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(sql, city);
        if (!tmpList.isEmpty()) {
            level = (tmpList.get(0)).get(SQL8).toString();
        }
        String sql1 = "select case_level level from tsm_order_case where case_id = ?";
        List<Map<String, Object>> levelList = jdbcTemplate.queryForList(sql1, caseId);
        if (!levelList.isEmpty()) {
            myLevel = (levelList.get(0)).get(SQL8).toString();
        }
        if ((StringUtils.isNotEmpty(level) && StringUtils.isNotEmpty(myLevel)) && Integer.valueOf(level) <= Integer.valueOf(myLevel) ) {
                levelAudit = true;
        }
        return levelAudit;
    }

    public boolean getEncode(String city){
        boolean encode = false;
        String level = null;
        String sql = "select case_level level from tsm_order_case_cityCode where encode = ?";
        List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(sql, city);
        if(!tmpList.isEmpty()) {
            level = (tmpList.get(0)).get(SQL8).toString();
        }
        if (Integer.valueOf(level) == 1){
            encode = true;
        }

        return encode;
    }
}
