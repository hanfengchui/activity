package com.timesontransfar.customservice.orderask.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.orderask.dao.IpersonaDao;
import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;

@Component(value = "personaDao")
@SuppressWarnings("rawtypes")
public class PersonaDaoImpl implements IpersonaDao {
	protected Logger log = LoggerFactory.getLogger(PersonaDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;

	private String insertPersonaSql = "INSERT INTO cc_customer_persona "
			+ "(service_order_id, create_date, prefer_appeal, prefer_complaint, consume_type, final_option_order_id, "
			+ "up_tendency_num, refund_num, satisfy_num, unsatisfy_num, is_key_customer, is_key_person, city_label, sensitive_type, FINAL_OPTION_ORDER_NUM) "
			+ "VALUES (?, now(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

	private String deletePersonaByOrderIdSql = "DELETE FROM cc_customer_persona WHERE service_order_id = ?";

	private String selectPersonaByOrderIdSql =
"SELECT a.PREFER_APPEAL,\n" +
"       a.PREFER_COMPLAINT,\n" + 
"       a.FINAL_OPTION_ORDER_ID,ifnull(c.CRM_CUST_ID, '') CRM_CUST_ID,\n" +
"       CASE\n" + 
"         WHEN c.cust_sex = 0 OR c.cust_sex IS NULL THEN\n" + 
"          '0'\n" + 
"         WHEN c.custage = 0 OR c.custage IS NULL THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 2)\n" + 
"         WHEN c.custage BETWEEN 0 AND 6 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 0)\n" + 
"         WHEN c.custage BETWEEN 7 AND 17 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 1)\n" + 
"         WHEN c.custage BETWEEN 18 AND 40 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 2)\n" + 
"         WHEN c.custage BETWEEN 41 AND 65 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 3)\n" + 
"         ELSE\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 4)\n" + 
"       END CUST_FACE,\n" + 
"       if((substr(replace(b.cust_serv_grade_desc, '级', ''), 1 ,1) REGEXP '[^0-9]')=0, substr(b.cust_serv_grade_desc, 1 ,1), 0) CUST_STAR,\n" + 
"       TIMESTAMPDIFF(MONTH, str_to_date(c.install_date, '%Y-%m-%d %H:%i:%s'), NOW()) + 1 MONTHS_BT,\n" + 
"       ifnull(d.repeat_new_flag, 0) REPEAT_NEW_FLAG,ifnull(d.up_tendency_flag, 0) UP_TENDENCY_FLAG,\n" + 
"       c.cust_name CUST_NAME,IF((c.custage is null or c.custage=0), '', CONCAT(c.custage, '岁')) CUSTAGE,\n" + 
"       CASE WHEN (b.cust_group_desc is null or b.cust_group_desc='') THEN '未知'\n" + 
"            WHEN INSTR(b.cust_group_desc, '客户')=0 THEN CONCAT(b.cust_group_desc,'客户')\n" + 
"            ELSE b.cust_group_desc END CUST_GROUP_DESC,ifnull(c.cust_sex, 0) CUST_SEX,\n" + 
"       ifnull(a.is_key_customer, 0) IS_KEY_CUSTOMER,ifnull(a.is_key_person, 0) IS_KEY_PERSON,\n" +
"       ifnull(a.city_label,'未知') CITY_LABEL,ifnull(a.satisfy_num, 0) SATISFY_NUM,ifnull(a.unsatisfy_num, 0) UNSATISFY_NUM,\n" +
"       ifnull(a.up_tendency_num, 0) UP_TENDENCY_NUM,ifnull(a.refund_num, 0) REFUND_NUM,ifnull(d.c_call_flag, 0) CALL_FLAG,ifnull(a.sensitive_type, '') SENSITIVE_TYPE,\n" +
"       ifnull(a.FINAL_OPTION_ORDER_NUM, 0) FINAL_OPTION_ORDER_NUM, b.RELA_INFO\n" +
"  FROM cc_customer_persona a, cc_service_order_ask b, cc_order_cust_info c, cc_service_label d\n" + 
" WHERE a.service_order_id = b.service_order_id\n" + 
"   AND b.cust_guid = c.cust_guid\n" + 
"   AND a.service_order_id = d.service_order_id\n" + 
"   AND a.service_order_id = ?";

	private String insertPersonaHisByOrderIdSql =
"INSERT INTO cc_customer_persona_his\n" +
"  (service_order_id, create_date, prefer_appeal, prefer_complaint, consume_type, final_option_order_id, "
+ "up_tendency_num, refund_num, satisfy_num, unsatisfy_num, is_key_customer, is_key_person, city_label, sensitive_type, FINAL_OPTION_ORDER_NUM)\n" + 
"  SELECT service_order_id, create_date, prefer_appeal, prefer_complaint, consume_type, final_option_order_id, "
+ "up_tendency_num, refund_num, satisfy_num, unsatisfy_num, is_key_customer, is_key_person, city_label, sensitive_type, FINAL_OPTION_ORDER_NUM FROM cc_customer_persona WHERE service_order_id = ?";

	private String selectPersonaHisByOrderIdSql =
"SELECT a.PREFER_APPEAL,\n" +
"       a.PREFER_COMPLAINT,\n" + 
"       a.FINAL_OPTION_ORDER_ID,ifnull(c.CRM_CUST_ID, '') CRM_CUST_ID,\n" +
"       CASE\n" + 
"         WHEN c.cust_sex = 0 OR c.cust_sex IS NULL THEN\n" + 
"          '0'\n" + 
"         WHEN c.custage = 0 OR c.custage IS NULL THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 2)\n" + 
"         WHEN c.custage BETWEEN 0 AND 6 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 0)\n" + 
"         WHEN c.custage BETWEEN 7 AND 17 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 1)\n" + 
"         WHEN c.custage BETWEEN 18 AND 40 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 2)\n" + 
"         WHEN c.custage BETWEEN 41 AND 65 THEN\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 3)\n" + 
"         ELSE\n" + 
"           CONCAT(MOD(c.cust_sex, 2), '_', 4)\n" + 
"       END CUST_FACE,\n" + 
"       if((substr(replace(b.cust_serv_grade_desc, '级', ''), 1 ,1) REGEXP '[^0-9]')=0, substr(b.cust_serv_grade_desc, 1 ,1), 0) CUST_STAR,\n" + 
"       TIMESTAMPDIFF(MONTH, str_to_date(c.install_date, '%Y-%m-%d %H:%i:%s'), NOW()) + 1 MONTHS_BT,\n" + 
"       ifnull(d.repeat_new_flag, 0) REPEAT_NEW_FLAG,ifnull(d.up_tendency_flag, 0) UP_TENDENCY_FLAG,\n" + 
"       c.cust_name CUST_NAME,IF((c.custage is null or c.custage=0), '', CONCAT(c.custage, '岁')) CUSTAGE,\n" + 
"       CASE WHEN (b.cust_group_desc is null or b.cust_group_desc='') THEN '未知'\n" + 
"            WHEN INSTR(b.cust_group_desc, '客户')=0 THEN CONCAT(b.cust_group_desc,'客户')\n" + 
"            ELSE b.cust_group_desc END CUST_GROUP_DESC,ifnull(c.cust_sex, 0) CUST_SEX,\n" + 
"       ifnull(a.is_key_customer, 0) IS_KEY_CUSTOMER,ifnull(a.is_key_person, 0) IS_KEY_PERSON,\n" +
"       ifnull(a.city_label,'未知') CITY_LABEL,ifnull(a.satisfy_num, 0) SATISFY_NUM,ifnull(a.unsatisfy_num, 0) UNSATISFY_NUM,\n" +
"       ifnull(a.up_tendency_num, 0) UP_TENDENCY_NUM,ifnull(a.refund_num, 0) REFUND_NUM,ifnull(d.c_call_flag, 0) CALL_FLAG,ifnull(a.sensitive_type, '') SENSITIVE_TYPE,\n" +
"       ifnull(a.FINAL_OPTION_ORDER_NUM, 0) FINAL_OPTION_ORDER_NUM, b.RELA_INFO\n" +
"  FROM cc_customer_persona_his a, cc_service_order_ask_his b, cc_order_cust_info_his c, cc_service_label_his d\n" + 
" WHERE a.service_order_id = b.service_order_id\n" + 
"   AND b.order_statu IN (700000103, 720130010)\n" + 
"   AND b.cust_guid = c.cust_guid\n" + 
"   AND a.service_order_id = d.service_order_id\n" + 
"   AND a.service_order_id = ?";

	@Override
	public int savePersona(CustomerPersona persona) {
		return jt.update(insertPersonaSql, persona.getOrderId(), persona.getPreferAppeal(), persona.getPreferComplaint(), 
				persona.getConsumeType(), persona.getFinalOptionOrderId(), persona.getUpTendencyNum(), 
				persona.getRefundNum(), persona.getSatisfyNum(), persona.getUnsatisfyNum(),
				persona.getIsKeyCustomer(), persona.getIsKeyPerson(), StringUtils.defaultIfEmpty(persona.getCityLabel(), null),
				StringUtils.defaultIfEmpty(persona.getSensitiveType(), null), persona.getFinalOptionOrderNum());
	}

	@Override
	public int savePersonaHis(String orderId) {
		int res = 0;
		res = jt.update(insertPersonaHisByOrderIdSql, orderId);
		if (res > 0) {
			return jt.update(deletePersonaByOrderIdSql, orderId);
		}
		return res;
	}

	@Override
	public List queryPersonaByOrderId(String orderId) {
		return this.jt.queryForList(selectPersonaByOrderIdSql, orderId);
	}

	@Override
	public List queryPersonaHisByOrderId(String orderId) {
		return this.jt.queryForList(selectPersonaHisByOrderIdSql, orderId);
	}
}