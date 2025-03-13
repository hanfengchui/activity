/**
 * @author 万荣伟
 */
package com.timesontransfar.pubWebservice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.orderask.pojo.OrderRelation;
import com.timesontransfar.systemPub.entity.PubColumn;
import com.timesontransfar.systemPub.entity.PubColumnRmp;

/**
 * @author 万荣伟
 */
@SuppressWarnings("rawtypes")
@Component(value = "pubwebServiceDaoImpl")
public class PubwebServiceDaoImpl implements IpubwebServiceDao {
    protected Logger log = LoggerFactory.getLogger(PubwebServiceDaoImpl.class);
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 查询定单当天状态,先进行当天表查询,在当前表不存在进行历史查询
     *
     * @param orderId
     * @param boo
     * @return
     */
    public List getOrderStatu(String orderId) {
        String strSql =
                "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       A.PROD_NUM,\n" +
                        "        if(A.ACCEPT_STAFF_ID='1000', B.ACCEPT_CONTENT,'') ACCEPT_CONTENT,\n" +
                        "       '1' ORDER_STATU,\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s') FINISH_DATE,\n" +
                        "       (SELECT UNIFIED_COMPLAINT_CODE FROM CC_CMP_UNIFIED_RETURN WHERE COMPLAINT_WORKSHEET_ID = A.SERVICE_ORDER_ID) UNIFIED_COMPLAINT_CODE\n" +
                        "  FROM CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK B\n" +
                        " WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" +
                        "   AND A.SERVICE_ORDER_ID = ?\n" +
                        "UNION\n" +
                        "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       A.PROD_NUM,\n" +
                        "       if(A.ACCEPT_STAFF_ID='1000', B.ACCEPT_CONTENT,''),\n" +
                        "       if(A.ORDER_STATU=700000103, '2',if(A.ORDER_STATU=720130010,'2', if(A.ORDER_STATU=3000047, '3', if(A.ORDER_STATU=720130002, '3','')))),\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s'),\n" +
                        "       (SELECT UNIFIED_COMPLAINT_CODE FROM CC_CMP_UNIFIED_RETURN_HIS WHERE COMPLAINT_WORKSHEET_ID = A.SERVICE_ORDER_ID)\n" +
                        "  FROM CC_SERVICE_ORDER_ASK_HIS A, CC_SERVICE_CONTENT_ASK_HIS B\n" +
                        " WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" +
                        "   AND A.ORDER_VESION = B.ORDER_VESION\n" +
                        "   AND A.ORDER_STATU IN (700000103, 3000047, 720130002, 720130010)\n" +
                        "   AND A.SERVICE_ORDER_ID = ?";
        List tmp = null;
        tmp = this.jdbcTemplate.queryForList(strSql, orderId, orderId);
        return tmp;
    }

    /**
     * 查询定单当天状态,先进行当天表查询,在当前表不存在进行历史查询
     *
     * @param unifiedComplaintCode集团统一编码
     * @param boo
     * @return
     */
    public List getUccStatu(String unifiedComplaintCode) {
        String strSql =
                "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       A.PROD_NUM,\n" +
                        "       if(A.ACCEPT_STAFF_ID='1000', B.ACCEPT_CONTENT,'') ACCEPT_CONTENT,\n" +
                        "       '1' ORDER_STATU,\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s') FINISH_DATE,\n" +
                        "       UNIFIED_COMPLAINT_CODE\n" +
                        "  FROM CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK B, CC_CMP_UNIFIED_RETURN\n" +
                        " WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" +
                        "   AND A.SERVICE_ORDER_ID = COMPLAINT_WORKSHEET_ID\n" +
                        "   AND UNIFIED_COMPLAINT_CODE = ?\n" +
                        "UNION\n" +
                        "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       A.PROD_NUM,\n" +
                        "       if(A.ACCEPT_STAFF_ID='1000', B.ACCEPT_CONTENT,''),\n" +
                        "       if(A.ORDER_STATU=700000103, '2',if(A.ORDER_STATU=720130010,'2', if(A.ORDER_STATU=3000047, '3', if(A.ORDER_STATU=720130002, '3','')))),\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s'),\n" +
                        "       UNIFIED_COMPLAINT_CODE\n" +
                        "  FROM CC_SERVICE_ORDER_ASK_HIS A, CC_SERVICE_CONTENT_ASK_HIS B, CC_CMP_UNIFIED_RETURN_HIS\n" +
                        " WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" +
                        "   AND A.ORDER_VESION = B.ORDER_VESION\n" +
                        "   AND A.SERVICE_ORDER_ID = COMPLAINT_WORKSHEET_ID\n" +
                        "   AND A.ORDER_STATU IN (700000103, 3000047, 720130002, 720130010)\n" +
                        "   AND UNIFIED_COMPLAINT_CODE = ?";
        List tmp = null;
        tmp = this.jdbcTemplate.queryForList(strSql, unifiedComplaintCode, unifiedComplaintCode);
        return tmp;
    }

    // 2014-11-11：现有投诉建议接口仅支持查询一条记录，为方便用户通过网掌厅查询投诉建议所有相关记录，特提出此接口改造需求：用户通过网掌厅前台输入投诉建议号码，能够查询最近三个月内所有与该号码相关的记录，包括处理中和处理完毕的记录
    // 恳请支持!省电子渠道运营中心联系人：叶枰15301582985
    public List getProdOrder(String prodNum) {
        String strSql =
                "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       A.PROD_NUM,\n" +
                        "       if(A.ACCEPT_STAFF_ID='1000', B.ACCEPT_CONTENT,'') ACCEPT_CONTENT,\n" +
                        "       '1' ORDER_STATU,\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s') FINISH_DATE,\n" +
                        "       (SELECT UNIFIED_COMPLAINT_CODE FROM CC_CMP_UNIFIED_RETURN WHERE COMPLAINT_WORKSHEET_ID = A.SERVICE_ORDER_ID) UNIFIED_COMPLAINT_CODE\n" +
                        "  FROM CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK B\n" +
                        " WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" +
                        "   AND A.PROD_NUM = ?\n" +
                        "   AND A.ACCEPT_DATE >= date_sub(str_to_date(NOW(),'%Y-%m-%d %H:%i:%s'),interval 90 day)\n" +
                        "UNION\n" +
                        "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       A.PROD_NUM,\n" +
                        "       if(A.ACCEPT_STAFF_ID='1000', B.ACCEPT_CONTENT,''),\n" +
                        "       if(A.ORDER_STATU=700000103, '2',if(A.ORDER_STATU=720130010,'2', if(A.ORDER_STATU=3000047, '3', if(A.ORDER_STATU=720130002, '3','')))),\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s'),\n" +
                        "       (SELECT UNIFIED_COMPLAINT_CODE FROM CC_CMP_UNIFIED_RETURN_HIS WHERE COMPLAINT_WORKSHEET_ID = A.SERVICE_ORDER_ID)\n" +
                        "  FROM CC_SERVICE_ORDER_ASK_HIS A, CC_SERVICE_CONTENT_ASK_HIS B\n" +
                        " WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" +
                        "   AND A.ORDER_VESION = B.ORDER_VESION\n" +
                        "   AND A.ORDER_STATU IN (700000103, 3000047, 720130002, 720130010)\n" +
                        "   AND A.PROD_NUM = ?\n" +
                        "   AND A.ACCEPT_DATE >= date_sub(str_to_date(NOW(),'%Y-%m-%d %H:%i:%s'),interval 90 day)";
        List tmp = null;
        tmp = this.jdbcTemplate.queryForList(strSql, prodNum, prodNum);
        return tmp;
    }

    /**
     * 根据CRM的客户ID得到某时间段
     *
     * @param crmCustId CRM客户ID
     * @param startData 开始时间
     * @param endData   结束时间
     * @return
     */
    public List getCrmCstOrder(String crmCustId, String startData, String endData) {
        String strSql =
                "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       '1' ORDER_STATU,\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s') FINISH_DATE,\n" +
                        "       (SELECT UNIFIED_COMPLAINT_CODE FROM CC_CMP_UNIFIED_RETURN WHERE COMPLAINT_WORKSHEET_ID = A.SERVICE_ORDER_ID) UNIFIED_COMPLAINT_CODE\n" +
                        "  FROM CC_SERVICE_ORDER_ASK A, CC_ORDER_CUST_INFO C\n" +
                        " WHERE A.CUST_GUID = C.CUST_GUID\n" +
                        "   AND C.CRM_CUST_ID = ?\n" +
                        "   AND A.ACCEPT_DATE BETWEEN str_to_date(?, '%Y-%m-%d %H:%i:%s') AND str_to_date(?, '%Y-%m-%d %H:%i:%s')\n" +
                        "UNION\n" +
                        "SELECT A.SERVICE_ORDER_ID,\n" +
                        "       if(A.ORDER_STATU=700000103, '2',if(A.ORDER_STATU=720130010,'2', if(A.ORDER_STATU=3000047, '3', if(A.ORDER_STATU=720130002, '3', '')))),\n" +
                        "       DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s'),\n" +
                        "       (SELECT UNIFIED_COMPLAINT_CODE FROM CC_CMP_UNIFIED_RETURN_HIS WHERE COMPLAINT_WORKSHEET_ID = A.SERVICE_ORDER_ID)\n" +
                        "  FROM CC_SERVICE_ORDER_ASK_HIS A, CC_ORDER_CUST_INFO_HIS C\n" +
                        " WHERE A.CUST_GUID = C.CUST_GUID\n" +
                        "   AND C.CRM_CUST_ID = ?\n" +
                        "   AND A.ORDER_STATU IN (700000103, 3000047, 720130002, 720130010)\n" +
                        "   AND A.ACCEPT_DATE BETWEEN str_to_date(?,'%Y-%m-%d %H:%i:%s') AND str_to_date(?,'%Y-%m-%d %H:%i:%s')";
        List tmp = null;
        tmp = this.jdbcTemplate.queryForList(strSql, crmCustId, startData, endData, crmCustId, startData, endData);
        return tmp;
    }

    /**
     * 查询静态数名
     *
     * @param referId
     * @return 静态数名
     */
    @SuppressWarnings("unchecked")
	public String getStaticName(int referId) {
        String strsql = "SELECT T.COL_VALUE_NAME FROM PUB_COLUMN_REFERENCE T WHERE T.REFER_ID = ?";

        List tmpList = jdbcTemplate.query(strsql, new Object[]{ referId },
                new KeyRowMapper());

        if (tmpList.isEmpty()) {
            log.warn("没有查询到referId为{}的静态数据值!", referId);
            return "";
        }

        return tmpList.get(0).toString();
    }

    class KeyRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int arg1) throws SQLException {
            return rs.getString(1);
        }
    }

    /**
     * 得到97D级(县市)的上级本地网地域id
     *
     * @param regionId 地域id
     * @return 97D级(县市)的上级本地网地域id
     */
    @SuppressWarnings("unchecked")
	public int getUpRegionId(int regionId) {
        String strsql = "SELECT B.REGION_ID " +
                "FROM TRM_REGION A, TRM_REGION B WHERE A.REGION_ID = ? " +
                "AND A.SUPER_ID = B.REGION_ID AND B.REGION_LEVEL='97C'";
        List tmpList = jdbcTemplate.query(strsql, new Object[]{regionId},
                new KeyRowMapper());

        if (tmpList.isEmpty()) {
            log.warn("没有查询到regionId为{}的上级地域", regionId);
            return regionId;
        }

        return Integer.parseInt(tmpList.get(0).toString());
    }

    /**
     * 根据地域得到地域名
     *
     * @param regionId 地域ID
     * @return
     */
    @SuppressWarnings("unchecked")
	public String getRegionName(int regionId) {
        String strSql = "SELECT R.REGION_NAME FROM TRM_REGION R WHERE R.REGION_ID=? ";
        List tmpList = jdbcTemplate.query(strSql, new Object[]{regionId},
                new KeyRowMapper());

        if (tmpList.isEmpty()) {
            log.warn("没有查询到regionId为{}的地域", regionId);
            return " ";
        }
        
        return tmpList.get(0).toString();
    }
	
	public int saveOrderRelation(OrderRelation r) {
		String sql = "INSERT INTO CC_ORDER_RELATION(SERVICE_ORDER_ID, OTHER_ORDER_ID, SOURCE_NAME, ADDRESS_NAME, STATUS, PUSH_FLAG) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";
		try {
			return jdbcTemplate.update(sql, 
					r.getServiceOrderId(),
					r.getOtherOrderId(),
					r.getSourceName(),
					StringUtils.defaultIfBlank(r.getAddressName(), null),
					r.getStatus(),
					r.getPushFlag());
		}
		catch(Exception e) {
			log.error("saveOrderRelation {} mysql异常: {}", r, e.getMessage(), e);
		}
		return 0;
	}
	
	@SuppressWarnings("unchecked")
	public PubColumn getPubColumn(String referId) {
		String sql = "SELECT A.REFER_ID,A.TABLE_CODE,A.COL_CODE,A.COL_NAME,A.COL_VALUE,A.COL_VALUE_NAME,A.ENTITY_ID,A.COL_ORDER,A.COL_VALUE_HANDLING,A.HAVING_CHILD_ITEM " +
				"FROM PUB_COLUMN_REFERENCE A WHERE A.REFER_ID = ?";
		List<PubColumn> list = jdbcTemplate.query(sql, new Object[]{referId}, new PubColumnRmp());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

}