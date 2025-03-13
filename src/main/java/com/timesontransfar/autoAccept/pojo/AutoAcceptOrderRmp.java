package com.timesontransfar.autoAccept.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
@SuppressWarnings("rawtypes")
public class AutoAcceptOrderRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		AutoAcceptOrder bean = new AutoAcceptOrder();
		bean.setGuid(rs.getString("GUID")==null?"":rs.getString("GUID"));
		bean.setServType(rs.getInt("SERVICE_TYPE"));
		bean.setServTypeDesc(rs.getString("SERVICE_TYPE_DESC")==null?"":rs.getString("SERVICE_TYPE_DESC"));
		bean.setComeCategory(rs.getInt("COME_CATEGORY"));
		bean.setCategoryName(rs.getString("COME_CATEGORY_NAME")==null?"":rs.getString("COME_CATEGORY_NAME"));
		bean.setAskSource(rs.getInt("ACCEPT_COME_FROM"));
		bean.setAskSourceDesc(rs.getString("ACCEPT_COME_FROM_DESC")==null?"":rs.getString("ACCEPT_COME_FROM_DESC"));
		bean.setAskChannelId(rs.getInt("ACCEPT_CHANNEL_ID"));
		bean.setAskChannelDesc(rs.getString("ACCEPT_CHANNEL_DESC")==null?"":rs.getString("ACCEPT_CHANNEL_DESC"));
		bean.setChannelDetailId(rs.getInt("CHANNEL_DETAIL_ID"));
		bean.setChannelDetailDesc(rs.getString("CHANNEL_DETAIL_DESC")==null?"":rs.getString("CHANNEL_DETAIL_DESC"));
		bean.setRegionId(rs.getInt("REGION_ID"));
		bean.setRegionName(rs.getString("REGION_NAME")==null?"":rs.getString("REGION_NAME"));
		bean.setProdNum(rs.getString("PROD_NUM"));
		bean.setProdType(rs.getString("PROD_TYPE"));
		bean.setProdTypeDesc(rs.getString("PROD_TYPE_DESC"));
		bean.setCustName(rs.getString("CUST_NAME"));
		bean.setRelaMan(rs.getString("RELA_MAN"));
		bean.setRelaInfo(rs.getString("RELA_INFO"));
		bean.setSsFlow(rs.getString("SS_FLOW"));
		bean.setAcceptContent(rs.getString("ACCEPT_CONTENT"));
		bean.setStatu(rs.getInt("STATU"));
		bean.setStatuDesc(rs.getString("STATU_DESC"));
		return bean;
	}

}
