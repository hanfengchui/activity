package com.timesontransfar.customservice.orderask.service.impl;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import com.timesontransfar.customservice.orderask.service.IcompWorksheetFullWebService;

@Component("compFull")
public class CompWorksheetFullWebService implements IcompWorksheetFullWebService {
	@Resource
	private JdbcTemplate jdbcTemplate;

	/**
	 * 插入需要归档的数据
	 */
	@PostMapping(value = "/workflow/ChiefOperatorDeskAction/insertSupplement")
	public int insertSupplement(String serviceOrderId) {
		String sql = "INSERT INTO cc_cmp_supplement(service_order_id,oper_flag,update_date,fail_count,exe_time)VALUES(?,0,NOW(),0,NOW())";
		return jdbcTemplate.update(sql, serviceOrderId);
	}

	// 更新需要赔付归档的数据
	public int updateFullPayDateByNewOrderId(String serviceOrderId) {
		String sql = "UPDATE cc_cmp_full_pay_list SET pay_date=NOW()WHERE pay_date IS NULL AND new_order_id=?";
		return jdbcTemplate.update(sql, serviceOrderId);
	}
}