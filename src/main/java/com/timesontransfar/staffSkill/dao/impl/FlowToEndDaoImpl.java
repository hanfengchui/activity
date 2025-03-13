package com.timesontransfar.staffSkill.dao.impl;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.staffSkill.FlowToEnd;
import com.timesontransfar.staffSkill.FlowToEndRmp;
import com.timesontransfar.staffSkill.dao.IFlowToEndDao;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Component(value="flowToEndDao")
public class FlowToEndDaoImpl implements IFlowToEndDao {
	protected static Logger log = LoggerFactory.getLogger(FlowToEndDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;

	@Autowired
	private IdbgridDataPub dbgridDataPub;

	@Autowired
	private PubFunc pubFun;
	/*
	 * 根据ID和类型查询一跟到底配置存不存在，keyType：1、生效单位，2、生效渠道，3、不生效的号码
	 */
	public int countFlowToEndConfigByIdType(String keyId, int keyType) {
		String sql = "SELECT COUNT(1)FROM cc_flow_to_end_config WHERE key_state=0 AND key_id=? AND key_type=?";
		return jt.queryForObject(sql, new Object[] { keyId, keyType }, Integer.class);
	}

	/*
	 * 产品号码当前30天
	 */
	public FlowToEnd selectFlowToEndByProdNum(int regionId, String prodNum, String orgPlace) {
		String sql =
"SELECT 0 increment_id,\n" +
"       '' create_date,\n" + 
"       '' cur_order_id,\n" + 
"       '' cur_sheet_id,\n" + 
"       a.region_id,\n" + 
"       a.prod_num,\n" + 
"       a.rela_info,\n" + 
"       a.service_order_id old_order_id,\n" + 
"       DATE_FORMAT(a.accept_date, '%Y-%m-%d %H:%i:%s') old_accept_date,\n" + 
"       a.old_sheet_id,\n" + 
"       a.deal_staff deal_staff_id,\n" + 
"       b.org_id deal_org_id,\n" + 
"       d.org_id deal_org,\n" + 
"       1 flow_type,\n" + 
"       '' count_workload_guid,\n" + 
"       '' count_workload_date,\n" + 
"       '' force_staff_id,\n" + 
"       '' force_date\n" + 
"  FROM (SELECT x.region_id,\n" + 
"               x.prod_num,\n" + 
"               x.rela_info,\n" + 
"               x.service_order_id,\n" + 
"               x.accept_date,\n" + 
"               (SELECT y.work_sheet_id\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NULL\n" + 
"                   AND y.lock_flag = 1\n" + 
"                 ORDER BY y.creat_date DESC, y.main_sheet_flag DESC LIMIT 1) old_sheet_id,\n" + 
"               (SELECT y.deal_staff\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NULL\n" + 
"                   AND y.lock_flag = 1\n" + 
"                 ORDER BY y.creat_date DESC, y.main_sheet_flag DESC LIMIT 1) deal_staff\n" + 
"          FROM cc_service_order_ask x\n" + 
"         WHERE x.service_type IN (720130000, 720200003)\n" + 
"           AND IF(x.channel_detail_id > 0, x.channel_detail_id, x.accept_channel_id) IN\n" + 
"               (SELECT key_id\n" + 
"                  FROM cc_flow_to_end_config\n" + 
"                 WHERE key_state = 0\n" + 
"                   AND key_type = 2)\n" + 
"           AND x.accept_date > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND x.region_id = ?\n" + 
"           AND x.prod_num = ?) a,\n" + 
"       tsm_staff b,\n" + 
"       tsm_organization c,\n" + 
"       tsm_organization d\n" + 
" WHERE a.deal_staff = b.staff_id\n" + 
"   AND b.state = 8\n" + 
"   AND b.org_id = c.org_id\n" + 
"   AND IF(SUBSTRING_INDEX(c.linkid, '-', 2) = '10-11', SUBSTRING_INDEX(c.linkid, '-', 3), SUBSTRING_INDEX(c.linkid, '-', 2)) = d.linkid\n" + 
"   AND a.deal_staff NOT IN (SELECT auto_zp_staff_id FROM cc_worksheet_allot_config_staff_map WHERE auto_zp_level = 2)\n" + 
"   AND a.deal_staff NOT IN (SELECT rest_staff_id\n" + 
"                              FROM cc_flow_to_end_rest_config\n" + 
"                             WHERE rest_state = 0\n" + 
"                               AND NOW() BETWEEN rest_start_time AND rest_end_time)\n" + 
"   AND d.org_id = ?\n" + 
" ORDER BY a.accept_date DESC";
		List list = jt.query(sql, new Object[] { regionId, prodNum, orgPlace }, new FlowToEndRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (FlowToEnd) list.get(0);
	}

	/*
	 * 产品号码定性回访30天
	 */
	public FlowToEnd selectFlowToEndDxhfByProdNum(int regionId, String prodNum, String orgPlace) {
		String sql =
"SELECT 0 increment_id,\n" +
"       '' create_date,\n" + 
"       '' cur_order_id,\n" + 
"       '' cur_sheet_id,\n" + 
"       a.region_id,\n" + 
"       a.prod_num,\n" + 
"       a.rela_info,\n" + 
"       a.service_order_id old_order_id,\n" + 
"       DATE_FORMAT(a.accept_date, '%Y-%m-%d %H:%i:%s') old_accept_date,\n" + 
"       a.old_sheet_id,\n" + 
"       a.deal_staff deal_staff_id,\n" + 
"       b.org_id deal_org_id,\n" + 
"       d.org_id deal_org,\n" + 
"       2 flow_type,\n" + 
"       '' count_workload_guid,\n" + 
"       '' count_workload_date,\n" + 
"       '' force_staff_id,\n" + 
"       '' force_date\n" + 
"  FROM (SELECT x.region_id,\n" + 
"               x.prod_num,\n" + 
"               x.rela_info,\n" + 
"               x.service_order_id,\n" + 
"               x.accept_date,\n" + 
"               (SELECT y.work_sheet_id\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) old_sheet_id,\n" + 
"               (SELECT y.deal_staff\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) deal_staff\n" + 
"          FROM cc_service_order_ask x\n" + 
"         WHERE x.service_type IN (720130000, 720200003)\n" + 
"           AND x.order_statu IN (700000101, 720130006, 720130007, 720130008)\n" + 
"           AND IF(x.channel_detail_id > 0, x.channel_detail_id, x.accept_channel_id) IN\n" + 
"               (SELECT key_id\n" + 
"                  FROM cc_flow_to_end_config\n" + 
"                 WHERE key_state = 0\n" + 
"                   AND key_type = 2)\n" + 
"           AND x.accept_date > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND x.region_id = ?\n" + 
"           AND x.prod_num = ?) a,\n" + 
"       tsm_staff b,\n" + 
"       tsm_organization c,\n" + 
"       tsm_organization d\n" + 
" WHERE a.deal_staff = b.staff_id\n" + 
"   AND b.state = 8\n" + 
"   AND b.org_id = c.org_id\n" + 
"   AND IF(SUBSTRING_INDEX(c.linkid, '-', 2) = '10-11', SUBSTRING_INDEX(c.linkid, '-', 3), SUBSTRING_INDEX(c.linkid, '-', 2)) = d.linkid\n" + 
"   AND a.deal_staff NOT IN (SELECT auto_zp_staff_id FROM cc_worksheet_allot_config_staff_map WHERE auto_zp_level = 2)\n" + 
"   AND a.deal_staff NOT IN (SELECT rest_staff_id\n" + 
"                              FROM cc_flow_to_end_rest_config\n" + 
"                             WHERE rest_state = 0\n" + 
"                               AND NOW() BETWEEN rest_start_time AND rest_end_time)\n" + 
"   AND d.org_id = ?\n" + 
" ORDER BY a.accept_date DESC";
		List list = jt.query(sql, new Object[] { regionId, prodNum, orgPlace }, new FlowToEndRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (FlowToEnd) list.get(0);
	}

	/*
	 * 产品号码历史30天
	 */
	public FlowToEnd selectFlowToEndHisByProdNum(int regionId, String prodNum, String orgPlace) {
		String sql =
"SELECT 0 increment_id,\n" +
"       '' create_date,\n" + 
"       '' cur_order_id,\n" + 
"       '' cur_sheet_id,\n" + 
"       a.region_id,\n" + 
"       a.prod_num,\n" + 
"       a.rela_info,\n" + 
"       a.service_order_id old_order_id,\n" + 
"       DATE_FORMAT(a.accept_date, '%Y-%m-%d %H:%i:%s') old_accept_date,\n" + 
"       a.old_sheet_id,\n" + 
"       a.deal_staff deal_staff_id,\n" + 
"       b.org_id deal_org_id,\n" + 
"       d.org_id deal_org,\n" + 
"       3 flow_type,\n" + 
"       '' count_workload_guid,\n" + 
"       '' count_workload_date,\n" + 
"       '' force_staff_id,\n" + 
"       '' force_date\n" + 
"  FROM (SELECT x.region_id,\n" + 
"               x.prod_num,\n" + 
"               x.rela_info,\n" + 
"               x.service_order_id,\n" + 
"               x.accept_date,\n" + 
"               (SELECT y.work_sheet_id\n" + 
"                  FROM cc_work_sheet_his y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) old_sheet_id,\n" + 
"               (SELECT y.deal_staff\n" + 
"                  FROM cc_work_sheet_his y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) deal_staff\n" + 
"          FROM cc_service_order_ask_his x\n" + 
"         WHERE x.service_type IN (720130000, 720200003)\n" + 
"           AND x.order_statu IN (700000103, 720130010)\n" + 
"           AND IF(x.channel_detail_id > 0, x.channel_detail_id, x.accept_channel_id) IN\n" + 
"               (SELECT key_id\n" + 
"                  FROM cc_flow_to_end_config\n" + 
"                 WHERE key_state = 0\n" + 
"                   AND key_type = 2)\n" + 
"           AND x.accept_date > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND x.region_id = ?\n" + 
"           AND x.prod_num = ?) a,\n" + 
"       tsm_staff b,\n" + 
"       tsm_organization c,\n" + 
"       tsm_organization d\n" + 
" WHERE a.deal_staff = b.staff_id\n" + 
"   AND b.state = 8\n" + 
"   AND b.org_id = c.org_id\n" + 
"   AND IF(SUBSTRING_INDEX(c.linkid, '-', 2) = '10-11', SUBSTRING_INDEX(c.linkid, '-', 3), SUBSTRING_INDEX(c.linkid, '-', 2)) = d.linkid\n" + 
"   AND a.deal_staff NOT IN (SELECT auto_zp_staff_id FROM cc_worksheet_allot_config_staff_map WHERE auto_zp_level = 2)\n" + 
"   AND a.deal_staff NOT IN (SELECT rest_staff_id\n" + 
"                              FROM cc_flow_to_end_rest_config\n" + 
"                             WHERE rest_state = 0\n" + 
"                               AND NOW() BETWEEN rest_start_time AND rest_end_time)\n" + 
"   AND d.org_id = ?\n" + 
" ORDER BY a.accept_date DESC";
		List list = jt.query(sql, new Object[] { regionId, prodNum, orgPlace }, new FlowToEndRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (FlowToEnd) list.get(0);
	}

	/*
	 * 联系号码当前30天
	 */
	public FlowToEnd selectFlowToEndByRelaInfo(int regionId, String relaInfo, String orgPlace) {
		String sql =
"SELECT 0 increment_id,\n" +
"       '' create_date,\n" + 
"       '' cur_order_id,\n" + 
"       '' cur_sheet_id,\n" + 
"       a.region_id,\n" + 
"       a.prod_num,\n" + 
"       a.rela_info,\n" + 
"       a.service_order_id old_order_id,\n" + 
"       DATE_FORMAT(a.accept_date, '%Y-%m-%d %H:%i:%s') old_accept_date,\n" + 
"       a.old_sheet_id,\n" + 
"       a.deal_staff deal_staff_id,\n" + 
"       b.org_id deal_org_id,\n" + 
"       d.org_id deal_org,\n" + 
"       4 flow_type,\n" + 
"       '' count_workload_guid,\n" + 
"       '' count_workload_date,\n" + 
"       '' force_staff_id,\n" + 
"       '' force_date\n" + 
"  FROM (SELECT x.region_id,\n" + 
"               x.prod_num,\n" + 
"               x.rela_info,\n" + 
"               x.service_order_id,\n" + 
"               x.accept_date,\n" + 
"               (SELECT y.work_sheet_id\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NULL\n" + 
"                   AND y.lock_flag = 1\n" + 
"                 ORDER BY y.creat_date DESC, y.main_sheet_flag DESC LIMIT 1) old_sheet_id,\n" + 
"               (SELECT y.deal_staff\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NULL\n" + 
"                   AND y.lock_flag = 1\n" + 
"                 ORDER BY y.creat_date DESC, y.main_sheet_flag DESC LIMIT 1) deal_staff\n" + 
"          FROM cc_service_order_ask x\n" + 
"         WHERE x.service_type IN (720130000, 720200003)\n" + 
"           AND IF(x.channel_detail_id > 0, x.channel_detail_id, x.accept_channel_id) IN\n" + 
"               (SELECT key_id\n" + 
"                  FROM cc_flow_to_end_config\n" + 
"                 WHERE key_state = 0\n" + 
"                   AND key_type = 2)\n" + 
"           AND x.accept_date > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND x.region_id = ?\n" + 
"           AND x.rela_info = ?) a,\n" + 
"       tsm_staff b,\n" + 
"       tsm_organization c,\n" + 
"       tsm_organization d\n" + 
" WHERE a.deal_staff = b.staff_id\n" + 
"   AND b.state = 8\n" + 
"   AND b.org_id = c.org_id\n" + 
"   AND IF(SUBSTRING_INDEX(c.linkid, '-', 2) = '10-11', SUBSTRING_INDEX(c.linkid, '-', 3), SUBSTRING_INDEX(c.linkid, '-', 2)) = d.linkid\n" + 
"   AND a.deal_staff NOT IN (SELECT auto_zp_staff_id FROM cc_worksheet_allot_config_staff_map WHERE auto_zp_level = 2)\n" + 
"   AND a.deal_staff NOT IN (SELECT rest_staff_id\n" + 
"                              FROM cc_flow_to_end_rest_config\n" + 
"                             WHERE rest_state = 0\n" + 
"                               AND NOW() BETWEEN rest_start_time AND rest_end_time)\n" + 
"   AND d.org_id = ?\n" + 
" ORDER BY a.accept_date DESC";
		List list = jt.query(sql, new Object[] { regionId, relaInfo, orgPlace }, new FlowToEndRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (FlowToEnd) list.get(0);
	}

	/*
	 * 联系号码定性回访30天
	 */
	public FlowToEnd selectFlowToEndDxhfByRelaInfo(int regionId, String relaInfo, String orgPlace) {
		String sql =
"SELECT 0 increment_id,\n" +
"       '' create_date,\n" + 
"       '' cur_order_id,\n" + 
"       '' cur_sheet_id,\n" + 
"       a.region_id,\n" + 
"       a.prod_num,\n" + 
"       a.rela_info,\n" + 
"       a.service_order_id old_order_id,\n" + 
"       DATE_FORMAT(a.accept_date, '%Y-%m-%d %H:%i:%s') old_accept_date,\n" + 
"       a.old_sheet_id,\n" + 
"       a.deal_staff deal_staff_id,\n" + 
"       b.org_id deal_org_id,\n" + 
"       d.org_id deal_org,\n" + 
"       5 flow_type,\n" + 
"       '' count_workload_guid,\n" + 
"       '' count_workload_date,\n" + 
"       '' force_staff_id,\n" + 
"       '' force_date\n" + 
"  FROM (SELECT x.region_id,\n" + 
"               x.prod_num,\n" + 
"               x.rela_info,\n" + 
"               x.service_order_id,\n" + 
"               x.accept_date,\n" + 
"               (SELECT y.work_sheet_id\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) old_sheet_id,\n" + 
"               (SELECT y.deal_staff\n" + 
"                  FROM cc_work_sheet y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) deal_staff\n" + 
"          FROM cc_service_order_ask x\n" + 
"         WHERE x.service_type IN (720130000, 720200003)\n" + 
"           AND x.order_statu IN (700000101, 720130006, 720130007, 720130008)\n" + 
"           AND IF(x.channel_detail_id > 0, x.channel_detail_id, x.accept_channel_id) IN\n" + 
"               (SELECT key_id\n" + 
"                  FROM cc_flow_to_end_config\n" + 
"                 WHERE key_state = 0\n" + 
"                   AND key_type = 2)\n" + 
"           AND x.accept_date > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND x.region_id = ?\n" + 
"           AND x.rela_info = ?) a,\n" + 
"       tsm_staff b,\n" + 
"       tsm_organization c,\n" + 
"       tsm_organization d\n" + 
" WHERE a.deal_staff = b.staff_id\n" + 
"   AND b.state = 8\n" + 
"   AND b.org_id = c.org_id\n" + 
"   AND IF(SUBSTRING_INDEX(c.linkid, '-', 2) = '10-11', SUBSTRING_INDEX(c.linkid, '-', 3), SUBSTRING_INDEX(c.linkid, '-', 2)) = d.linkid\n" + 
"   AND a.deal_staff NOT IN (SELECT auto_zp_staff_id FROM cc_worksheet_allot_config_staff_map WHERE auto_zp_level = 2)\n" + 
"   AND a.deal_staff NOT IN (SELECT rest_staff_id\n" + 
"                              FROM cc_flow_to_end_rest_config\n" + 
"                             WHERE rest_state = 0\n" + 
"                               AND NOW() BETWEEN rest_start_time AND rest_end_time)\n" + 
"   AND d.org_id = ?\n" + 
" ORDER BY a.accept_date DESC";
		List list = jt.query(sql, new Object[] { regionId, relaInfo, orgPlace }, new FlowToEndRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (FlowToEnd) list.get(0);
	}

	/*
	 * 联系号码历史30天
	 */
	public FlowToEnd selectFlowToEndHisByRelaInfo(int regionId, String relaInfo, String orgPlace) {
		String sql =
"SELECT 0 increment_id,\n" +
"       '' create_date,\n" + 
"       '' cur_order_id,\n" + 
"       '' cur_sheet_id,\n" + 
"       a.region_id,\n" + 
"       a.prod_num,\n" + 
"       a.rela_info,\n" + 
"       a.service_order_id old_order_id,\n" + 
"       DATE_FORMAT(a.accept_date, '%Y-%m-%d %H:%i:%s') old_accept_date,\n" + 
"       a.old_sheet_id,\n" + 
"       a.deal_staff deal_staff_id,\n" + 
"       b.org_id deal_org_id,\n" + 
"       d.org_id deal_org,\n" + 
"       6 flow_type,\n" + 
"       '' count_workload_guid,\n" + 
"       '' count_workload_date,\n" + 
"       '' force_staff_id,\n" + 
"       '' force_date\n" + 
"  FROM (SELECT x.region_id,\n" + 
"               x.prod_num,\n" + 
"               x.rela_info,\n" + 
"               x.service_order_id,\n" + 
"               x.accept_date,\n" + 
"               (SELECT y.work_sheet_id\n" + 
"                  FROM cc_work_sheet_his y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) old_sheet_id,\n" + 
"               (SELECT y.deal_staff\n" + 
"                  FROM cc_work_sheet_his y\n" + 
"                 WHERE y.service_order_id = x.service_order_id\n" + 
"                   AND y.sheet_type IN (720130011, 720130013, 720130014, 700000126, 700000127, 700001002, 720130015)\n" + 
"                   AND y.respond_date IS NOT NULL\n" + 
"                   AND y.deal_content <> '系统自动处理'\n" + 
"                 ORDER BY y.respond_date DESC LIMIT 1) deal_staff\n" + 
"          FROM cc_service_order_ask_his x\n" + 
"         WHERE x.service_type IN (720130000, 720200003)\n" + 
"           AND x.order_statu IN (700000103, 720130010)\n" + 
"           AND IF(x.channel_detail_id > 0, x.channel_detail_id, x.accept_channel_id) IN\n" + 
"               (SELECT key_id\n" + 
"                  FROM cc_flow_to_end_config\n" + 
"                 WHERE key_state = 0\n" + 
"                   AND key_type = 2)\n" + 
"           AND x.accept_date > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND x.region_id = ?\n" + 
"           AND x.rela_info = ?) a,\n" + 
"       tsm_staff b,\n" + 
"       tsm_organization c,\n" + 
"       tsm_organization d\n" + 
" WHERE a.deal_staff = b.staff_id\n" + 
"   AND b.state = 8\n" + 
"   AND b.org_id = c.org_id\n" + 
"   AND IF(SUBSTRING_INDEX(c.linkid, '-', 2) = '10-11', SUBSTRING_INDEX(c.linkid, '-', 3), SUBSTRING_INDEX(c.linkid, '-', 2)) = d.linkid\n" + 
"   AND a.deal_staff NOT IN (SELECT auto_zp_staff_id FROM cc_worksheet_allot_config_staff_map WHERE auto_zp_level = 2)\n" + 
"   AND a.deal_staff NOT IN (SELECT rest_staff_id\n" + 
"                              FROM cc_flow_to_end_rest_config\n" + 
"                             WHERE rest_state = 0\n" + 
"                               AND NOW() BETWEEN rest_start_time AND rest_end_time)\n" + 
"   AND d.org_id = ?\n" + 
" ORDER BY a.accept_date DESC";
		List list = jt.query(sql, new Object[] { regionId, relaInfo, orgPlace }, new FlowToEndRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (FlowToEnd) list.get(0);
	}

	/*
	 * 判断之前处理员工是否长休假
	 */
	public int countFlowToEndRestConfigByDealStaffId(int dealStaffId) {
		String sql = "SELECT COUNT(1)FROM cc_flow_to_end_rest_config "
				+ "WHERE rest_state=0 AND NOW()BETWEEN rest_start_time AND rest_end_time AND rest_staff_id=?";
		return jt.queryForObject(sql, new Object[] { dealStaffId }, Integer.class);
	}

	/*
	 * 由30天匹配后初始插入记录，未关联到工作量情况
	 */
	public int insertFlowToEndEmptyWorkload(FlowToEnd fte) {
		String sql = "INSERT INTO cc_flow_to_end(cur_order_id,cur_sheet_id,region_id,prod_num,rela_info,old_order_id,old_accept_date,old_sheet_id,"
				+ "deal_staff_id,deal_org_id,deal_org,flow_type)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
		return jt.update(sql, fte.getCurOrderId(), fte.getCurSheetId(), fte.getRegionId(), fte.getProdNum(),
				fte.getRelaInfo(), fte.getOldOrderId(), fte.getOldAcceptDate(), fte.getOldSheetId(),
				fte.getDealStaffId(), fte.getDealOrgId(), fte.getDealOrg(), fte.getFlowType());
	}

	/*
	 * 由30天匹配后初始插入记录，关联到工作量情况
	 */
	public int insertFlowToEndWithWorkload(FlowToEnd fte) {
		String sql = "INSERT INTO cc_flow_to_end(cur_order_id,cur_sheet_id,region_id,prod_num,rela_info,old_order_id,old_accept_date,old_sheet_id,"
				+ "deal_staff_id,deal_org_id,deal_org,flow_type,count_workload_guid,count_workload_date)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,NOW())";
		return jt.update(sql, fte.getCurOrderId(), fte.getCurSheetId(), fte.getRegionId(), fte.getProdNum(),
				fte.getRelaInfo(), fte.getOldOrderId(), fte.getOldAcceptDate(), fte.getOldSheetId(),
				fte.getDealStaffId(), fte.getDealOrgId(), fte.getDealOrg(), fte.getFlowType(),
				fte.getCountWorkloadGuid());
	}

	/*
	 * 根据30天内员工Id查询未更新工作量情况表的记录
	 */
	public List<FlowToEnd> selectFlowToEndEmptyWorkloadByDealStaffId(int dealStaffId) {
		String sql = "SELECT increment_id,DATE_FORMAT(create_date,'%Y-%m-%d %H:%i:%s')create_date,cur_order_id,cur_sheet_id,region_id,prod_num,rela_info,"
				+ "old_order_id,DATE_FORMAT(old_accept_date,'%Y-%m-%d %H:%i:%s')old_accept_date,old_sheet_id,deal_staff_id,deal_org_id,deal_org,"
				+ "flow_type,count_workload_guid,DATE_FORMAT(count_workload_date,'%Y-%m-%d %H:%i:%s')count_workload_date,force_staff_id,"
				+ "DATE_FORMAT(force_date,'%Y-%m-%d %H:%i:%s')force_date FROM cc_flow_to_end WHERE create_date>DATE_SUB(NOW(),INTERVAL 30 DAY)AND "
				+ "count_workload_date IS NULL AND force_staff_id IS NULL AND deal_staff_id=? ORDER BY create_date";
		return jt.query(sql, new Object[] { dealStaffId }, new FlowToEndRmp());
	}

	/*
	 * 根据单号查询一跟到底前单和后单信息
	 */
	public List selectFlowToEndByOrderId(String orderId) {
		String sql =
"SELECT *\n" +
"  FROM (SELECT '前单' fte_type, b.service_order_id, DATE_FORMAT(b.accept_date, '%Y-%m-%d %H:%i:%s') accept_date, b.prod_num, b.rela_info, '在途' fte_statu, b.order_statu_desc\n" + 
"          FROM cc_flow_to_end a, cc_service_order_ask b\n" + 
"         WHERE a.old_order_id = b.service_order_id\n" + 
"           AND a.cur_order_id = ?\n" + 
"        UNION ALL\n" + 
"        SELECT '前单', b.service_order_id, DATE_FORMAT(b.accept_date, '%Y-%m-%d %H:%i:%s'), b.prod_num, b.rela_info, '归档', b.order_statu_desc\n" + 
"          FROM cc_flow_to_end a, cc_service_order_ask_his b\n" + 
"         WHERE b.order_statu IN (700000103, 3000047, 720130002, 720130010)\n" + 
"           AND a.old_order_id = b.service_order_id\n" + 
"           AND a.cur_order_id = ?\n" + 
"        UNION ALL\n" + 
"        SELECT '后单', b.service_order_id, DATE_FORMAT(b.accept_date, '%Y-%m-%d %H:%i:%s'), b.prod_num, b.rela_info, '在途', b.order_statu_desc\n" + 
"          FROM cc_flow_to_end a, cc_service_order_ask b\n" + 
"         WHERE a.cur_order_id = b.service_order_id\n" + 
"           AND a.old_order_id = ?\n" + 
"        UNION ALL\n" + 
"        SELECT '后单', b.service_order_id, DATE_FORMAT(b.accept_date, '%Y-%m-%d %H:%i:%s'), b.prod_num, b.rela_info, '归档', b.order_statu_desc\n" + 
"          FROM cc_flow_to_end a, cc_service_order_ask_his b\n" + 
"         WHERE b.order_statu IN (700000103, 3000047, 720130002, 720130010)\n" + 
"           AND a.cur_order_id = b.service_order_id\n" + 
"           AND a.old_order_id = ?) c\n" + 
" ORDER BY accept_date";
		return jt.queryForList(sql, orderId, orderId, orderId, orderId);
	}

	/*
	 * 更新工作量情况表的记录
	 */
	public int updateFlowToEndWorkloadByIncrementId(String countWorkloadGuid, int incrementId) {
		String sql = "UPDATE cc_flow_to_end SET count_workload_guid=?,count_workload_date=NOW()WHERE count_workload_guid IS NULL AND increment_id=?";
		return jt.update(sql, countWorkloadGuid, incrementId);
	}

	/*
	 * 查询一跟到底没有被提取且还在处理中的信息
	 */
	public GridDataInfo selectFlowToEndCouldForce(String dealOrg, String serviceType, String hours, int begin, String incrementId) {
		StringBuilder sb = new StringBuilder();
		if (!"".equals(dealOrg)) {
			sb.append(" AND D.DEAL_ORG='");
			sb.append(dealOrg);
			sb.append("'");
		}
		if (!"".equals(serviceType)) {
			sb.append(" AND A.SERVICE_TYPE=");
			sb.append(serviceType);
		}
		if (!"".equals(hours)) {
			sb.append(" AND TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW())>=");
			sb.append(Integer.parseInt(hours) * 60);
		}
		if (!"".equals(incrementId)) {
			sb.append(" AND D.INCREMENT_ID=").append(incrementId);
		}
		String sql = 
"SELECT A.SERVICE_ORDER_ID,\n" +
"       A.REGION_NAME,\n" + 
"       A.PROD_NUM,\n" + 
"       A.SERVICE_TYPE_DESC,\n" + 
"       A.ACCEPT_COME_FROM_DESC,\n" + 
"       A.ACCEPT_CHANNEL_DESC,\n" + 
"       B.BEST_ORDER_DESC,\n" + 
"       DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\n" + 
"       CONCAT_WS('',\n" + 
"                 CASE\n" + 
"                   WHEN TRUNCATE(TIMESTAMPDIFF(MINUTE, A.ACCEPT_DATE, NOW()) / 60, 0) > 0 THEN\n" + 
"                    CONCAT(TRUNCATE(TIMESTAMPDIFF(MINUTE, A.ACCEPT_DATE, NOW()) / 60, 0), '时')\n" + 
"                 END,\n" + 
"                 CASE\n" + 
"                   WHEN TRUNCATE(MOD(TIMESTAMPDIFF(MINUTE, A.ACCEPT_DATE, NOW()), 60), 0) >= 0 THEN\n" + 
"                    TRUNCATE(MOD(TIMESTAMPDIFF(MINUTE, A.ACCEPT_DATE, NOW()), 60), 0)\n" + 
"                 END,\n" + 
"                 '分') DEAL_TIME,\n" + 
"       A.ORDER_STATU,\n" + 
"       A.REGION_ID,\n" + 
"       A.MONTH_FLAG,\n" + 
"       C.WORK_SHEET_ID SHEET_ID,\n" + 
"       C.SHEET_STATU_DESC SHEET_STATU,\n" + 
"       C.DEAL_ORG_NAME,\n" + 
"       C.DEAL_STAFF_NAME,\n" + 
"       D.INCREMENT_ID\n" + 
"  FROM CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK B, CC_WORK_SHEET C, CC_FLOW_TO_END D\n" + 
" WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" + 
"   AND A.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n" + 
"   AND C.WORK_SHEET_ID = D.CUR_SHEET_ID\n" + 
"   AND C.DEAL_STAFF = D.DEAL_STAFF_ID\n" + 
"   AND C.LOCK_FLAG = 1\n" + 
"   AND D.FORCE_STAFF_ID IS NULL";
		return dbgridDataPub.getResult(sql + sb.toString(), begin, " ORDER BY A.ACCEPT_DATE", "selectFlowToEndCouldForce");
	}

	/*
	 * 查询豁免人员列表
	 */
	public GridDataInfo getExemptionData(String createStaffId, String restStaffId, int begin) {
		log.info("getExemptionData createStaffId: {},restStaffId: {}",createStaffId,restStaffId);
		GridDataInfo result = new GridDataInfo();
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(createStaffId)) {
			sb.append(" AND CREATE_STAFF_ID = '" + createStaffId + "'");
		}
		if (StringUtils.isNotBlank(restStaffId)) {
			sb.append(" AND REST_STAFF_ID = '" + restStaffId + "'");
		}
		String sql = "SELECT INCREMENT_ID,(SELECT LOGONNAME FROM TSM_STAFF A WHERE A.STAFF_ID = CREATE_STAFF_ID) LOGON_NAME," +
				"CREATE_STAFF_ID,DATE_FORMAT(CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE,REST_STAFF_ID," +
				"(SELECT LOGONNAME FROM TSM_STAFF A WHERE A.STAFF_ID = REST_STAFF_ID) REST_LOGON_NAME," +
				"DATE_FORMAT(REST_START_TIME, '%Y-%m-%d %H:%i:%s') REST_START_TIME,DATE_FORMAT(REST_END_TIME, '%Y-%m-%d %H:%i:%s') REST_END_TIME," +
				"REST_REMARK,REST_STATE,MODIFY_STAFF_ID,DATE_FORMAT(MODIFY_DATE, '%Y-%m-%d %H:%i:%s') MODIFY_DATE FROM CC_FLOW_TO_END_REST_CONFIG WHERE 1=1 AND REST_STATE = 0";
		try {
			result = dbgridDataPub.getResult(sql + sb, begin, " ORDER BY CREATE_DATE",null);
		}catch (Exception e){
			log.error("getExemptionData error: {}",e.getMessage(),e);
		}
		return result;
	}

	/*
	 * 查询一跟到底配置列表
	 */
	public GridDataInfo getConfigData(String createStaffId, String keyId, String keyType, String keyRemark, int begin) {
		log.info("getConfigData createStaffId: {},keyId: {},keyType: {},keyRemark: {}",createStaffId,keyId,keyType,keyRemark);
		GridDataInfo result = new GridDataInfo();
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(createStaffId)) {
			sb.append(" AND CREATE_STAFF_ID = '" + createStaffId + "'");
		}
		if (StringUtils.isNotBlank(keyId)) {
			sb.append(" AND KEY_ID = '" + keyId + "'");
		}
		if (StringUtils.isNotBlank(keyType)) {
			sb.append(" AND KEY_TYPE = '" + keyType + "'");
		}
		if (StringUtils.isNotBlank(keyRemark)) {
			sb.append(" AND KEY_REMARK = '" + keyRemark + "'");
		}
		String sql = "SELECT INCREMENT_ID,CREATE_STAFF_ID,(SELECT LOGONNAME FROM TSM_STAFF A WHERE A.STAFF_ID = CREATE_STAFF_ID) LOGON_NAME," +
				"DATE_FORMAT(CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE,KEY_ID," +
				"( SELECT CONCAT_WS(' > ', four.col_value_name, three.col_value_name, two.col_value_name, one.col_value_name) AS hierarchy FROM pub_column_reference AS one " +
				"LEFT JOIN pub_column_reference AS two ON one.entity_id = two.refer_id LEFT JOIN pub_column_reference AS three ON two.entity_id = three.refer_id " +
				"LEFT JOIN pub_column_reference AS four ON three.entity_id = four.refer_id WHERE one.refer_id = KEY_ID ) AS KEY_ID_DESC,"+
				"KEY_TYPE,KEY_REMARK,KEY_STATE,MODIFY_STAFF_ID,DATE_FORMAT(MODIFY_DATE, '%Y-%m-%d %H:%i:%s') MODIFY_DATE FROM CC_FLOW_TO_END_CONFIG WHERE 1=1 AND KEY_STATE = 0 AND KEY_TYPE = 2";
		try {
			result = dbgridDataPub.getResult(sql + sb, begin, " ORDER BY CREATE_DATE",null);
		}catch (Exception e){
			log.error("getConfigData error: {}",e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 逻辑删除豁免人员数据
	 */
	public int delExemptionData(String ids) {
		log.info("delExemptionData id: {}",ids);
		int result = 0 ;
		try {
			TsmStaff staff = pubFun.getLogonStaff();
			JSONArray array = JSON.parseArray(ids);
			for (int i = 0; i < array.size(); i++) {
				String key = array.getString(i);
				String sql = "UPDATE CC_FLOW_TO_END_REST_CONFIG SET REST_STATE = 1,MODIFY_STAFF_ID=? WHERE INCREMENT_ID = ?";
				result += this.jt.update(sql,staff.getId(),key);
			}
		}catch (Exception e){
			log.error("delExemptionData error: {}",e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 逻辑删除一跟到底配置
	 */
	public int delConfigData(String ids) {
		log.info("delConfigData id: {}",ids);
		int result = 0 ;
		try {
			TsmStaff staff = pubFun.getLogonStaff();
			JSONArray array = JSON.parseArray(ids);
			for (int i = 0; i < array.size(); i++) {
				String key = array.getString(i);
				String sql = "UPDATE CC_FLOW_TO_END_CONFIG SET KEY_STATE=1,MODIFY_STAFF_ID=?,MODIFY_DATE=NOW() WHERE INCREMENT_ID=?";
				result += this.jt.update(sql,staff.getId(),key);
			}
		}catch (Exception e){
			log.error("delConfigData error: {}",e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 新增休息员工数据
	 * */
	public int addExemptionData(String param){
		log.info("addExemptionData param: {}",param);
		int result = 0;
		try{
			JSONObject jsonObject = JSON.parseObject(param);
			String createStaffId = jsonObject.getString("createStaffId");
			String restStaffId = jsonObject.getString("restStaffId");
			String restStartTime = jsonObject.getString("restStartTime");
			String restEndTime = jsonObject.getString("restEndTime");
			String restRemark = jsonObject.getString("restRemark");
			String sql = "INSERT INTO CC_FLOW_TO_END_REST_CONFIG (CREATE_STAFF_ID, CREATE_DATE, REST_STAFF_ID, REST_START_TIME, REST_END_TIME,REST_REMARK,REST_STATE)" +
					"VALUES (?,now(),?,STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'),STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'),?,0)";
			result = this.jt.update(sql, createStaffId,restStaffId,restStartTime,restEndTime,restRemark);
		}catch (Exception e){
			log.error("addExemptionData error: {}",e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 新增一跟到底配置数据
	 * */
	public int addConfigData(String param){
		log.info("addConfigData param: {}",param);
		int result = 0;
		try{
			JSONObject jsonObject = JSON.parseObject(param);
			String createStaffId = jsonObject.getString("createStaffId");
			String keyId = jsonObject.getString("keyId");
			String keyType = jsonObject.getString("keyType");
			String keyRemark = jsonObject.getString("keyRemark");
			String sql = "INSERT INTO CC_FLOW_TO_END_CONFIG (CREATE_STAFF_ID, CREATE_DATE, KEY_ID, KEY_TYPE, KEY_REMARK,KEY_STATE)" +
					"VALUES (?,now(),?,?,?,0)";
			result = this.jt.update(sql, createStaffId,keyId,keyType,keyRemark);
		}catch (Exception e){
			log.error("addConfigData error: {}",e.getMessage(),e);
		}
		return result;
	}

	/*
	 * 更新提取信息
	 */
	public int updateFlowToEndForceByIncrementId(String forceStaffId, int incrementId) {
		String sql = "UPDATE cc_flow_to_end SET force_staff_id=?,force_date=NOW()WHERE force_staff_id IS NULL AND increment_id=?";
		return jt.update(sql, forceStaffId, incrementId);
	}
}