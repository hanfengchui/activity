package com.timesontransfar.autoAccept.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.timesontransfar.autoAccept.dao.IAutoAcceptDao;
import com.timesontransfar.autoAccept.pojo.AutoAcceptOrder;
import com.timesontransfar.autoAccept.pojo.AutoAcceptOrderRmp;
import com.timesontransfar.autoAccept.pojo.ZQCustInfoData;

@Component("autoAcceptDao")
@SuppressWarnings("rawtypes")
public class AutoAcceptDaoImpl implements IAutoAcceptDao {
	private static final Logger log = LoggerFactory.getLogger(AutoAcceptDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	
	private String saveAutoAcceptOrderSql="insert into CC_AUTO_ACCEPT_ORDER (GUID, ACCEPT_CHANNEL_ID, \n" + 
			"ACCEPT_CHANNEL_DESC, REGION_ID, REGION_NAME, PROD_NUM, PROD_TYPE, \n" + 
			"PROD_TYPE_DESC, CUST_NAME, RELA_MAN, RELA_INFO, SS_FLOW, \n" + 
			"ACCEPT_CONTENT, CREATE_DATE, CREATE_STAFF, STATU, STATU_DESC)\n" + 
			"values (UPPER(replace(UUID(), '-', '')), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?)";
	private String deleteAutoAcceptOrderSql="delete c from CC_AUTO_ACCEPT_ORDER c where c.STATU != 4";
	private String queryAutoAcceptOrderSql="SELECT GUID,\n" + 
			"SERVICE_TYPE,\n" + 
			"SERVICE_TYPE_DESC,\n" + 
			"COME_CATEGORY,\n" + 
			"COME_CATEGORY_NAME,\n" + 
			"ACCEPT_COME_FROM,\n" + 
			"ACCEPT_COME_FROM_DESC,\n" + 
			"ACCEPT_CHANNEL_ID,\n" + 
			"ACCEPT_CHANNEL_DESC,\n" + 
			"CHANNEL_DETAIL_ID,\n" + 
			"CHANNEL_DETAIL_DESC,\n" + 
			"REGION_ID,\n" + 
			"REGION_NAME,\n" + 
			"PROD_NUM,\n" + 
			"PROD_TYPE,\n" + 
			"PROD_TYPE_DESC,\n" + 
			"CUST_NAME,\n" + 
			"RELA_MAN,\n" + 
			"RELA_INFO,\n" + 
			"SS_FLOW,\n" + 
			"ACCEPT_CONTENT,\n" + 
			"STATU,\n" + 
			"STATU_DESC\n" + 
			" FROM CC_AUTO_ACCEPT_ORDER\n" + 
			"WHERE 1 = 1";
	private String updateAutoAcceptOrderSql="update CC_AUTO_ACCEPT_ORDER c \n" + 
			"set c.service_type=?,c.service_type_desc=?,\n" + 
			"c.come_category=?,c.come_category_name=?,\n" + 
			"c.accept_come_from=?,c.accept_come_from_desc=?,\n" + 
			"c.channel_detail_id=?,c.channel_detail_desc=?,\n" + 
			"c.region_id=?,c.region_name=?,\n" + 
			"c.accept_content=?,\n" + 
			"c.statu=?,c.statu_desc=?,\n" + 
			"c.modify_date=now(),c.modify_staff=? \n" +
			"where c.guid=?";
	private String updateAutoAcceptOrderStatuSql="update CC_AUTO_ACCEPT_ORDER c \n" + 
			"set c.statu=?,c.statu_desc=?,\n" + 
			"c.modify_date=now(),c.modify_staff=? \n" + 
			"where c.guid=?";
	
	public int saveAutoAcceptOrderPatch(final List list){
		int[] i = this.jt.batchUpdate(this.saveAutoAcceptOrderSql, new BatchPreparedStatementSetter(){
            public int getBatchSize() {
                return list.size();
            }
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                AutoAcceptOrder bean = (AutoAcceptOrder)list.get(j);
                ps.setInt(1, bean.getAskChannelId());
                ps.setString(2, StringUtils.defaultIfEmpty(bean.getAskChannelDesc(),null));
                ps.setInt(3, bean.getRegionId());
                ps.setString(4, StringUtils.defaultIfEmpty(bean.getRegionName(),null));
                ps.setString(5, StringUtils.defaultIfEmpty(bean.getProdNum(),null));
                ps.setString(6, StringUtils.defaultIfEmpty(bean.getProdType(),null));
                ps.setString(7, StringUtils.defaultIfEmpty(bean.getProdTypeDesc(),null));
                ps.setString(8, StringUtils.defaultIfEmpty(bean.getCustName(),null));
                ps.setString(9, StringUtils.defaultIfEmpty(bean.getRelaMan(),null));
                ps.setString(10, StringUtils.defaultIfEmpty(bean.getRelaInfo(),null));
                ps.setString(11, StringUtils.defaultIfEmpty(bean.getSsFlow(),null));
                ps.setString(12, StringUtils.defaultIfEmpty(bean.getAcceptContent(),null));
                ps.setString(13, StringUtils.defaultIfEmpty(bean.getCreateStaff(),null));
                ps.setInt(14, bean.getStatu());
                ps.setString(15, StringUtils.defaultIfEmpty(bean.getStatuDesc(),null));
            }           
        });
        
        if(log.isDebugEnabled()) {
            log.debug("导入申诉单"+i.length+"条");
        }
        return i.length;
	}
	
	public int deleteAutoAcceptOrder(){
		int size = this.jt.update(deleteAutoAcceptOrderSql);
		if(log.isDebugEnabled()) {
			log.debug("删除申诉列表"+size+"条");
		}
		return size;
	}
	
	@SuppressWarnings("unchecked")
	public List getAutoAcceptOrderListByCondition(String condition){
		List tmpList = this.jt.query(queryAutoAcceptOrderSql+condition,new AutoAcceptOrderRmp());
		if(tmpList.isEmpty()){
			tmpList.clear();
			tmpList=null;
        }
		return tmpList;
	}
	
	public int updateAutoAcceptOrderList(final List list) {
		int[] i = this.jt.batchUpdate(this.updateAutoAcceptOrderSql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int j) throws SQLException {
				AutoAcceptOrder o = (AutoAcceptOrder)list.get(j);
				ps.setInt(1, o.getServType());
				ps.setString(2, StringUtils.defaultIfEmpty(o.getServTypeDesc(),null));
				ps.setInt(3, o.getComeCategory());
				ps.setString(4, StringUtils.defaultIfEmpty(o.getCategoryName(),null));
				ps.setInt(5, o.getAskSource());
				ps.setString(6, StringUtils.defaultIfEmpty(o.getAskSourceDesc(),null));
				ps.setInt(7, o.getChannelDetailId());
				ps.setString(8, StringUtils.defaultIfEmpty(o.getChannelDetailDesc(),null));
				ps.setInt(9, o.getRegionId());
				ps.setString(10, StringUtils.defaultIfEmpty(o.getRegionName(),null));
				ps.setString(11, StringUtils.defaultIfEmpty(o.getAcceptContent(),null));
				ps.setInt(12, o.getStatu());
				ps.setString(13, StringUtils.defaultIfEmpty(o.getStatuDesc(),null));
				ps.setString(14, StringUtils.defaultIfEmpty(o.getModifyStaff(),null));
				ps.setString(15, StringUtils.defaultIfEmpty(o.getGuid(),null));
			}
			
			public int getBatchSize() {
				return list.size();
			}
		});
		if(i.length == 0){
			return -1;
		}
		return i.length;
	}
	
	public int updateAutoAcceptOrderListStatu(final List list){
		int[] i = this.jt.batchUpdate(this.updateAutoAcceptOrderStatuSql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int j) throws SQLException {
				AutoAcceptOrder o = (AutoAcceptOrder)list.get(j);
				ps.setInt(1, o.getStatu());
				ps.setString(2, StringUtils.defaultIfEmpty(o.getStatuDesc(),null));
				ps.setString(3, StringUtils.defaultIfEmpty(o.getModifyStaff(),null));
				ps.setString(4, StringUtils.defaultIfEmpty(o.getGuid(),null));
			}
			
			public int getBatchSize() {
				return list.size();
			}
		});
		if(i.length == 0){
			return -1;
		}
		return i.length;
	}
	
	
	private String updateCustInfoDataSql = "UPDATE CC_RECEIVE_CUSTINFO C SET " +
			"C.DEALSTAFFCODE=?,C.DEALSTAFFNAME=?," +
			"C.DEALORGID=?,C.DEALORGNAME=?," +
			"C.DEALCONTENT=?,C.DEALSTATUS='1'," +
			"C.DEALDATE=NOW(),C.REGIONID=?,C.REGIONNAME=?,C.BUSINESSDESC=?,C.PRODNUM=? WHERE C.FLOWNO=? AND C.DEALSTATUS='0'";

	public int updateZQCustInfoData(ZQCustInfoData data){
		int size = this.jt.update(this.updateCustInfoDataSql, 
				data.getDealStaffCode(),
				data.getDealStaffName(),
				data.getDealOrgId(),
				data.getDealOrgName(),
				data.getDealContent(),
				data.getRegionId(),
				data.getRegionName(),
				data.getBusinessDesc(),
				data.getProdNum(),
				data.getFlowNo()
		);
		if(log.isDebugEnabled()) {
			log.debug("更新政企客户鉴权资料"+size+"条");
		}
		return size;
	}
	
	public int getComplaint(String mitCode) {
		String sql = "select count(1) from cc_complaint_list where MIIT_CODE = ?";
		return jt.queryForObject(sql, new Object[]{mitCode}, Integer.class);
	}
	
	private String saveComplaintListSql = "INSERT INTO CC_COMPLAINT_LIST " +
			"(MIIT_CODE, COMPLAINT_SOURCE, COMPLAINT_DATE, FIRST_LEVEL, SECOND_LEVEL, THIRD_LEVEL, IP_ADDRESS, RELATION_STATUS, MODIFY_DATE, STATE, OPERATOR)" +
			" VALUES (?, ?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?, ?, 0, now(), 0, ?)";
	
	public int saveComplaintListPatch(final List list, String operator){
		int[] i = this.jt.batchUpdate(this.saveComplaintListSql, new BatchPreparedStatementSetter(){
            public int getBatchSize() {
                return list.size();
            }
            public void setValues(PreparedStatement ps, int j) throws SQLException {
            	ComplaintInfo bean = (ComplaintInfo)list.get(j);
                ps.setString(1, bean.getMiitCode());
                ps.setString(2, StringUtils.defaultIfBlank(bean.getComplaintSource(), null));
                ps.setString(3, StringUtils.defaultIfBlank(bean.getComplaintDate(), null));
                ps.setString(4, StringUtils.defaultIfBlank(bean.getFirstLevel(), null));
                ps.setString(5, StringUtils.defaultIfBlank(bean.getSecondLevel(), null));
                ps.setString(6, StringUtils.defaultIfBlank(bean.getThirdLevel(), null));
                ps.setString(7, StringUtils.defaultIfBlank(bean.getIpAddress(), null));
                ps.setString(8, StringUtils.defaultIfBlank(operator, null));
            }           
        });
        log.info("导入申诉信息"+i.length+"条");
        return i.length;
	}
	
	private String updateComplaintListSql = "UPDATE CC_COMPLAINT_LIST SET COMPLAINT_SOURCE = ?, COMPLAINT_DATE = str_to_date(?, '%Y-%m-%d %H:%i:%s'), "
			+ "FIRST_LEVEL = ?, SECOND_LEVEL = ?, THIRD_LEVEL = ?, IP_ADDRESS = ?, CREATE_DATE = now(), RELATION_STATUS = 0, STATE = 0, OPERATOR = ? WHERE MIIT_CODE = ?";
	
	public int updateComplaintListPatch(final List list, String operator){
		int[] i = this.jt.batchUpdate(this.updateComplaintListSql, new BatchPreparedStatementSetter(){
            public int getBatchSize() {
                return list.size();
            }
            public void setValues(PreparedStatement ps, int j) throws SQLException {
            	ComplaintInfo bean = (ComplaintInfo)list.get(j);
                ps.setString(1, StringUtils.defaultIfBlank(bean.getComplaintSource(), null));
                ps.setString(2, StringUtils.defaultIfBlank(bean.getComplaintDate(), null));
                ps.setString(3, StringUtils.defaultIfBlank(bean.getFirstLevel(), null));
                ps.setString(4, StringUtils.defaultIfBlank(bean.getSecondLevel(), null));
                ps.setString(5, StringUtils.defaultIfBlank(bean.getThirdLevel(), null));
                ps.setString(6, StringUtils.defaultIfBlank(bean.getIpAddress(), null));
                ps.setString(7, StringUtils.defaultIfBlank(operator, null));
                ps.setString(8, bean.getMiitCode());
            }           
        });
        log.info("更新申诉信息"+i.length+"条");
        return i.length;
	}
	
	public int updateComplaintList(int status, int state, String operator, String mitCode) {
		String strSql = "UPDATE cc_complaint_list set relation_status = ?, state = ?, operator = ?, MODIFY_DATE = now() where MIIT_CODE = ?";
		return this.jt.update(strSql, status, state, operator, mitCode);
	}
}
