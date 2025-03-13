package com.timesontransfar.customservice.orderask.dao;

import java.util.List;

import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;
@SuppressWarnings("rawtypes")
public interface IpersonaDao {
	public int savePersona(CustomerPersona persona);

	public int savePersonaHis(String orderId);

	public List queryPersonaByOrderId(String orderId);

	public List queryPersonaHisByOrderId(String orderId);
}