package com.timesontransfar.refund.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.refund.dao.impl.RefundDao;
import com.timesontransfar.refund.pojo.RefundPojo;
import com.timesontransfar.refund.service.IrefundService;

import net.sf.json.JSONObject;

@Component(value="refundService")
public class RefundServiceImpl implements IrefundService {
	
	@Autowired
	private RefundDao refundDao;
	
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	
	public GridDataInfo getRefund(String parm){
		JSONObject json = JSONObject.fromObject(parm);
		StringBuilder sb = new StringBuilder();
		String begion = json.optString("begion");
		if(StringUtils.isNotEmpty(json.optString("regionId"))){
			sb.append(" and region_id = " + json.optString("regionId"));
		}
		if(StringUtils.isNotEmpty(json.optString("createDate"))){
			sb.append(" and create_date > str_to_date ('"+ json.optJSONArray("createDate").optString(0)+"' , '%Y-%m-%d %H:%i:%s')");
			sb.append(" and create_date < str_to_date ('"+ json.optJSONArray("createDate").optString(1)+"' , '%Y-%m-%d %H:%i:%s')");
		}
		
		String sql = 
				"SELECT REFUND_MONTH,\n" +			
				" (select region_name from trm_region where region_id = CC_REFUND.REGION_ID ) as REGION_NAME ,\n"+
				"       REGION_ID,\n" + 
				"       REFUND,\n" + 
				"       REFUND_NUMBER,\n" + 
				"       REFUND_DESC,\n" + 
				"       date_format(CREATE_DATE, '%Y%m') as CREATE_DATE ,\n" +
				"       CREATE_STAFF,\n" + 
				"       date_format(MODI_DATE, '%Y%m') as MODI_DATE,\n" +
				"       MODI_STAFF\n" + 
				"  FROM CC_REFUND WHERE 1=1 " + sb.toString();
		return dbgridDataPub.getResult(sql, Integer.parseInt(begion), "", "");
	}

	public String insertRefund(RefundPojo refund){
		String str = "";
		if(refundDao.checkMonth(refund.getRegionId(),refund.getRefundMonth()) > 0){
			str = "more";
		}else{
			if(this.refundDao.insertRefund(refund) == 1){
				str = "success";
			}else{
				str = "err";
			}
		}
		return str;
	}

	public int updateRefund(RefundPojo refund){
		return this.refundDao.updateRefund(refund);
	}
}