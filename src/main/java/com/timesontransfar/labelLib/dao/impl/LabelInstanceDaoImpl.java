package com.timesontransfar.labelLib.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.timesontransfar.labelLib.dao.ILabelInstanceDao;

import edu.emory.mathcs.backport.java.util.Collections;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component(value="labelInstanceDaoImpl")
public class LabelInstanceDaoImpl implements ILabelInstanceDao {
	
	protected Logger log = LoggerFactory.getLogger(LabelInstanceDaoImpl.class);
	@Resource
    private JdbcTemplate jt;
	@Autowired
    private IdbgridDataPub dbgridDataPub;
	@Autowired
	private PubFunc pubFun;
	
	public static final String CH1 = "WHERE 条件中没有AND,请检查WHERE条件";
    private String saveInstanceSql="INSERT INTO cc_label_instance " + 
    		"(label_instance_id," + 
    		"label_id," + 
    		"instance_type," + 
    		"instance_id," + 
    		"prod_num," + 
    		"update_time," + 
    		"staff_id," + 
    		"label_prop_id," + 
    		"label_prop_name," + 
    		"region_id," + 
    		"service_type," + 
    		"come_category," + 
    		"accept_come_from," + 
    		"accept_channel_id,service_order_id,label_state) " + 
    		"VALUES " + 
    		"(replace(UUID(), '-', ''), ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?,1)" ;
    private String gridCancelLableSql="SELECT b.LABEL_NAME," + 
    		"a.PROD_NUM," + 
    		"a.INSTANCE_ID," + 
    		"a.SERVICE_ORDER_ID," + 
    		"a.LABEL_INSTANCE_ID," + 
    		"a.INSTANCE_TYPE," + 
    		"DATE_FORMAT(a.update_time, '%Y-%m-%d %H:%i:%s') UPDATE_TIME," + 
    		"(SELECT label_group_name " + 
    		"FROM cc_label_group " + 
    		"WHERE label_group_id = b.label_group_id) GROUPNAME," + 
    		"a.LABEL_ID " + 
    		"FROM cc_label_instance a, cc_label_template b " + 
    		"WHERE a.label_state=1 AND a.label_id = b.label_id";
    private String rightGridSql=" SELECT A.LABEL_ID," + 
    		" A.LABEL_NAME," + 
    		" A.PROP_FLAG," + 
    		" (SELECT LABEL_GROUP_NAME" + 
    		" FROM CC_LABEL_GROUP" + 
    		" WHERE LABEL_GROUP_ID = A.LABEL_GROUP_ID) AS GROUP_NAME " + 
    		"FROM CC_LABEL_TEMPLATE A " + 
    		"WHERE A.LABEL_WAY_ID = 8000000008 " + 
    		"AND A.STATE = 1 " + 
    		"AND NOW() >= A.EFF_DATE " + 
    		"AND A.EXP_DATE >= NOW() ";
    
	@Override
    public GridDataInfo lableRightGrid(int begion, String strWhere) {
    	StringBuilder sb = new StringBuilder();
        sb.append(rightGridSql);
        if(!"".equals(strWhere)) {
            sb.append(strWhere);
        }
        String sql = "SELECT B.LABEL_PROP_ID,B.LABEL_PROP_NAME,C.LABEL_PRO_ID   " +
        		" FROM CC_LABEL_PRO_REF C,CC_LABEL B " +
        		" WHERE C.LABEL_PRO_ID = B.LABEL_PROP_ID " +
        		" AND C.LABEL_ID = ?";
        GridDataInfo d = dbgridDataPub.getResult(sb.toString(), begion, " ", DbgridStatic.GRID_FUNID_LABLE_TEMPLATE);
        List ls = d.getList();
        for(int i=0;i<ls.size();i++){
           Map m = (Map)ls.get(i);
           JSONObject attrJson=new JSONObject();
           JSONArray attrArr=new JSONArray();
           Map attrMap=new HashMap();
			if (m.get("PROP_FLAG") != null) {
				String f = m.get("PROP_FLAG").toString();
				String labelId = m.get("LABEL_ID").toString();
				if (f.equals("1")) {// 带属性标签
					List list = jt.queryForList(sql, labelId );
					for (int j = 0; j < list.size(); j++) {
						Map mm = (Map) list.get(j);
						attrJson.put("LABEL_PROP_ID", mm.get("LABEL_PROP_ID").toString());
						attrJson.put("LABEL_PROP_NAME", mm.get("LABEL_PROP_NAME").toString());
						attrArr.add(attrJson);
					}
					attrMap.putAll(m);
					attrMap.put("attrArr", attrArr);
					ls.set(i, attrMap);
				}
			}
        }
        return d;
    }
    
    @Override
    public int cancelLableById(String labelInstanceId){
        String sql = "UPDATE cc_label_instance SET label_state = 0 WHERE LABEL_INSTANCE_ID = ?";
        return jt.update(sql, labelInstanceId);
    }
    
    @Override
    public GridDataInfo lableCancel(int begion, String strWhere) {
    	StringBuilder sb = new StringBuilder();
        sb.append(gridCancelLableSql);
        if(!"".equals(strWhere)) {
            sb.append(strWhere);
        }
        return dbgridDataPub.getResult(sb.toString(), begion, " ", DbgridStatic.GRID_FUNID_LABLE_CANCEL);
    }
    
	@Override
	public int saveLabelInstanceBatch(final LabelInstance[] ls) {
		int[] i = jt.batchUpdate(saveInstanceSql, new BatchPreparedStatementSetter() {
			public int getBatchSize() {
				return ls.length;
			}
			public void setValues(PreparedStatement ps, int j) throws SQLException {
				LabelInstance o = ls[j];
				ps.setString(1, o.getLabelId());
				ps.setString(2, o.getInstanceType());
				ps.setString(3, StringUtils.defaultIfEmpty(o.getInstanceId(),"0"));
				ps.setString(4, StringUtils.defaultIfEmpty(o.getProdNum(), "0"));
				ps.setString(5, StringUtils.defaultIfEmpty(o.getStaffId(), null));
				ps.setString(6, StringUtils.defaultIfEmpty(o.getPropertyId(), null));
				ps.setString(7, StringUtils.defaultIfEmpty(o.getPerportyName(), null));
				ps.setInt(8, o.getRegionId());
				ps.setInt(9, o.getServiceType());
				ps.setInt(10, o.getComeCategory());
				ps.setInt(11, o.getAcceptComeFrom());
				ps.setInt(12, o.getAcceptChannelId());
				ps.setString(13, StringUtils.defaultIfEmpty(o.getServiceOrderId(), null));
			}
		});
		return i.length;
	}

	/**
	 * 得到工单标签数量
	 * 
	 * @param orderId 服务单号
	 * @return
	 */
	public int getLabelCountByOrderId(String orderId) {
		String sql = 
"SELECT COUNT(1)FROM CC_LABEL_INSTANCE A,CC_LABEL_TEMPLATE B,CC_LABEL_GROUP C,TSM_STAFF D WHERE A.LABEL_ID=B.LABEL_ID AND B.LABEL_GROUP_ID="
+ "C.LABEL_GROUP_ID AND A.STAFF_ID=D.STAFF_ID AND A.LABEL_STATE=1 AND A.SERVICE_ORDER_ID=?";
		return jt.queryForObject(sql, new Object[] { orderId }, Integer.class);
	}

	/**
	 * 得到工单标签列表
	 * 
	 * @param orderId 服务单号
	 * @return
	 */
	public List getLabelListByOrderId(String orderId) {
		String strSql = 
"SELECT A.SERVICE_ORDER_ID,IF(A.INSTANCE_TYPE='10','',A.INSTANCE_ID)INSTANCE_ID,C.LABEL_GROUP_NAME,B.LABEL_NAME,A.LABEL_PROP_NAME,D.ORG_ID,D.LOGONNAME,"
+ "DATE_FORMAT(A.UPDATE_TIME,'%Y-%m-%d %H:%i:%s')UPDATE_TIME FROM CC_LABEL_INSTANCE A,CC_LABEL_TEMPLATE B,CC_LABEL_GROUP C,TSM_STAFF D WHERE A.LABEL_ID="
+ "B.LABEL_ID AND B.LABEL_GROUP_ID=C.LABEL_GROUP_ID AND A.STAFF_ID=D.STAFF_ID AND A.LABEL_STATE=1 AND A.SERVICE_ORDER_ID=?ORDER BY A.UPDATE_TIME";
		List list = jt.queryForList(strSql, orderId);
		if (list.isEmpty()) {
			return Collections.emptyList();
		} else {
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				map.put("ORG_NAME", map.get("ORG_ID") == null ? "" : pubFun.getOrgWater(map.get("ORG_ID").toString()));
				list.set(i, map);
			}
		}
		return list;
	}

	public List getLabelInstanceByLabelId(String orderId, String labelId) {
		String sql = "SELECT*FROM cc_label_instance WHERE LABEL_STATE=1 AND LABEL_ID=?AND SERVICE_ORDER_ID=?";
		return jt.queryForList(sql, labelId, orderId);
	}
}