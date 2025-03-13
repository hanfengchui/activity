package com.timesontransfar.refund.dao.impl;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.refund.dao.Irefund;
import com.timesontransfar.refund.pojo.RefundPojo;

@Component(value="refundDao")
@SuppressWarnings({ "rawtypes" })
public class RefundDao implements Irefund {
	
	@Autowired
	private JdbcTemplate jt;
	@Autowired
	private PubFunc pubFunc;
	
   public int checkMonth(int region,String month){
	   String sql = "SELECT * FROM CC_REFUND where REFUND_MONTH = ? and REGION_ID = ?";
	   List ls = jt.queryForList(sql, month, region);
	   return ls.size();
   }

	/**
	 * 插入
	 */
	public int insertRefund(RefundPojo refund) {		
		TsmStaff staff = pubFunc.getLogonStaff();		
		refund.setCreateStaff(Integer.parseInt(staff.getId()));
		
		String insertSql = 
			"INSERT INTO CC_REFUND(" +
			" REFUND_MONTH," + 
			" REGION_ID," + 
			" REFUND," + 
			" REFUND_NUMBER," + 
			" REFUND_DESC," + 
			" CREATE_DATE," + 
			" CREATE_STAFF," + 
			" MODI_DATE," + 
			" MODI_STAFF" + 
			") VALUES (?,?,?,?,?,NOW(),?,?,?)";
		return this.jt.update(insertSql, 
				StringUtils.defaultIfEmpty(refund.getRefundMonth(),null),
				refund.getRegionId(),				
				new DecimalFormat("#########.##").format(refund.getRefund()),	
				refund.getRefundNumber(),			
			    StringUtils.defaultIfEmpty(refund.getRefundDesc(),null),
			    refund.getCreateStaff(),
			    StringUtils.defaultIfEmpty(refund.getModiDate(),null),
			    refund.getModiStaff()
		); 
	}	
	
	/**
	 * 修改
	 */
	public int updateRefund(RefundPojo refund) {
		String updateSql = 
			"UPDATE CC_REFUND SET\n" +
			" REFUND = ?,\n" + 
			" REFUND_NUMBER = ?,\n" + 
			" REFUND_DESC = ?,\n" + 
			" MODI_DATE = NOW(),\n" +
			" MODI_STAFF = "+ pubFunc.getLogonStaff().getId()+"\n" + 
			"WHERE REFUND_MONTH = ? AND REGION_ID = ?";		
		return this.jt.update(updateSql,
				new DecimalFormat("#########.##").format(refund.getRefund()),
				refund.getRefundNumber(),
				StringUtils.defaultIfEmpty(refund.getRefundDesc(),null),
				StringUtils.defaultIfEmpty(refund.getRefundMonth(),null),
				refund.getRegionId()
		);
	}
}