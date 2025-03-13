package com.timesontransfar.labelLib.dao.impl;

import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.labelLib.dao.ILabelTemplateDao;
import java.util.Collections;
import com.labelLib.pojo.LabelPoint;
import com.labelLib.pojo.LabelPointRmp;

@Component(value="labelTemplateDaoImpl")
@SuppressWarnings({"rawtypes", "unchecked"})
public class LabelTemplateDaoImpl implements ILabelTemplateDao {
	protected Logger log = LoggerFactory.getLogger(LabelTemplateDaoImpl.class);
	
	@Resource
    private JdbcTemplate jt;

    private String queryInsertPointsByUrl="SELECT  \r\n"
    		+ "       D.LABEL_ID, \r\n"
    		+ "       D.LABEL_NAME, \r\n"
    		+ "       C.LABEL_INSERT_POINTS_ID, \r\n"
    		+ "       C.COL_NAME, \r\n"
    		+ "       C.COL_VALUE_NAME , \r\n"
    		+ "       D.PROP_FLAG, \r\n"
    		+ "       NULL AS LABEL_PROP_NAME, \r\n"
    		+ "       D.LABEL_WAY_ID \r\n"
    		+ "     FROM CC_LABEL_TEMPLATE D,( \r\n"
    		+ "      SELECT A.LABEL_ID,A.LABEL_INSERT_POINTS_ID,B.COL_NAME ,B.COL_VALUE_NAME \r\n"
    		+ "      FROM CC_LABEL_INSERT_POINT A ,PUB_COLUMN_REFERENCE B  \r\n"
    		+ "          WHERE A.LABEL_INSERT_POINTS_ID = B.REFER_ID \r\n"
    		+ "          AND B.TABLE_CODE = 'CC_LABEL_INSERT_POINT' \r\n"
    		+ "          AND B.COL_CODE = 'LABEL_INSERT_POINTS_ID' \r\n"
    		+ "          AND B.COL_NAME LIKE ?\r\n"
    		+ "    ) C \r\n"
    		+ "  WHERE D.LABEL_ID = C.LABEL_ID \r\n"
    		+ "  AND D.STATE = 1 \r\n"
    		+ "  AND NOW() >= D.EFF_DATE \r\n"
    		+ "          AND D.EXP_DATE>=NOW()         \r\n"
    		+ "  AND D.LABEL_WAY_ID = ?         \r\n"
    		+ "  AND INSTR(CONCAT(',', D.REGION_ID, ','), \r\n"
    		+ "           (SELECT CONCAT( ',',Z.ORG_ID,',')   \r\n"
    		+ "            FROM TSM_ORGANIZATION Y, TSM_ORGANIZATION Z \r\n"
    		+ "           WHERE Y.ORG_ID =  ?  \r\n"
    		+ "             AND if(if(LENGTH(Y.LINKID)-LENGTH(REPLACE(Y.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(Y.LINKID,'-',2))+1, 0)=0, Y.LINKID, SUBSTR(Y.LINKID, 1, if(LENGTH(Y.LINKID)-LENGTH(REPLACE(Y.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(Y.LINKID,'-',2))+1, 0)-1)) \r\n"
    		+ " = Z.LINKID)) > 0";
   
    private String queryLabelByOrderIdSQL=" SELECT W.*,A.LABEL_PROP_NAME FROM CC_LABEL_INSTANCE  A ,\r\n" + 
    		"(\r\n" + 
    		"SELECT D.LABEL_ID,\r\n" + 
    		"D.LABEL_NAME,\r\n" + 
    		"C.LABEL_INSERT_POINTS_ID,\r\n" + 
    		"C.COL_NAME,\r\n" + 
    		"C.COL_VALUE_NAME,\r\n" + 
    		"D.PROP_FLAG,\r\n" + 
    		"D.LABEL_WAY_ID\r\n" + 
    		"FROM CC_LABEL_TEMPLATE D,\r\n" + 
    		"(SELECT A.LABEL_ID,\r\n" + 
    		"A.LABEL_INSERT_POINTS_ID,\r\n" + 
    		"B.COL_NAME,\r\n" + 
    		"B.COL_VALUE_NAME\r\n" + 
    		"FROM CC_LABEL_INSERT_POINT A, PUB_COLUMN_REFERENCE B\r\n" + 
    		"  WHERE A.LABEL_INSERT_POINTS_ID = B.REFER_ID\r\n" + 
    		"    AND B.TABLE_CODE = 'CC_LABEL_INSERT_POINT'\r\n" + 
    		"    AND B.COL_CODE = 'LABEL_INSERT_POINTS_ID'\r\n" + 
    		"AND B.COL_NAME LIKE ? ) C\r\n" + 
    		"WHERE D.LABEL_ID = C.LABEL_ID\r\n" + 
    		"AND D.STATE = 1\r\n" + 
    		"AND D.LABEL_WAY_ID = ?\r\n" + 
    		") W\r\n" + 
    		"WHERE A.LABEL_STATE=1 AND W.LABEL_ID = A.LABEL_ID\r\n" + 
    		"AND A.INSTANCE_ID = ?";
    
    public List queryAutoRuleLable(){
        String sql = 
                "SELECT W.LABEL_ID, W.LABEL_NAME, W.LABEL_RULES, W.LABEL_WAY_ID" +
                "  FROM CC_LABEL_TEMPLATE W" + 
                " WHERE W.LABEL_WAY_ID IN(8000000007,8000000009)";
        return jt.queryForList(sql);
    }
    
    public List queryPointByTacheId(int tacheId){
        String sql = "SELECT INSERT_POINTS_ID FROM CC_LABEL_TACHEID_POINT WHERE TACHE_ID = ?";
        return jt.queryForList(sql,tacheId);
    }

	public List<LabelPoint> queryLabelBySheetId(String url, String labelWayId, String condition, String orgId) {
        if (null == url || url.length() == 0) {
            return Collections.emptyList();
        }
        url = "%" + url + "%";
        String sql = "SELECT D.LABEL_ID," +
                "       D.LABEL_NAME," + 
                "       C.LABEL_INSERT_POINTS_ID," + 
                "       C.COL_NAME," + 
                "       C.COL_VALUE_NAME," + 
                "       D.PROP_FLAG," + 
                "       NULL AS LABEL_PROP_NAME," + 
                "       D.LABEL_WAY_ID" + 
                "  FROM CC_LABEL_TEMPLATE D," + 
                "       (SELECT A.LABEL_ID," + 
                "               A.LABEL_INSERT_POINTS_ID," + 
                "               B.COL_NAME," + 
                "               B.COL_VALUE_NAME" + 
                "          FROM CC_LABEL_INSERT_POINT A, PUB_COLUMN_REFERENCE B" + 
                "         WHERE A.LABEL_INSERT_POINTS_ID = B.REFER_ID" + 
                "           AND B.TABLE_CODE = 'CC_LABEL_INSERT_POINT'" + 
                "           AND B.COL_CODE = 'LABEL_INSERT_POINTS_ID'" + 
                "           AND B.COL_NAME LIKE '"+url+"'" + 
                "           AND A.LABEL_INSERT_POINTS_ID IN ("+condition+")) C" + 
                " WHERE D.LABEL_ID = C.LABEL_ID" + 
                "   AND D.LABEL_WAY_ID = 8000000008" +
                "   AND NOW() >= D.EFF_DATE" +
                "   AND D.EXP_DATE >= NOW()" +
                "   AND D.STATE = '1'" +
                "  AND INSTR(CONCAT(',', D.REGION_ID, ','), (SELECT CONCAT(',', Z.ORG_ID, ',') FROM TSM_ORGANIZATION Y, TSM_ORGANIZATION Z WHERE Y.ORG_ID = ? " + 
                "                   AND IF(if(LENGTH(Y.LINKID)-LENGTH(REPLACE(Y.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(Y.LINKID,'-',2))+1, 0)=0, Y.LINKID, SUBSTR(Y.LINKID, 1, if(LENGTH(Y.LINKID)-LENGTH(REPLACE(Y.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(Y.LINKID,'-',2))+1, 0) - 1)) = Z.LINKID)) > 0";

                
        log.info("查处理页面的标签的SQL: {} orgId: {}", sql, orgId);
        return jt.query(sql, new Object[] {orgId}, new LabelPointRmp());//CodeSec未验证的SQL注入；CodeSec误报：2
    }
    
	@Override
    public List<LabelPoint> queryLabelInsertPoint(String url,String labelPoint,String orgId) {
        if (null == url || url.length() == 0) {
        	return Collections.emptyList();
        }
        url = "%" + url + "%";
        return jt.query(queryInsertPointsByUrl, new Object[] {url, labelPoint, orgId}, new LabelPointRmp());
    }
    
    @Override
    public List<LabelPoint>queryLabelHandByOrderId(String url,String labelPoint,String orderId){
        if (null == url || url.length() == 0) {
        	return Collections.emptyList();
        }
        url = "%" + url + "%";
        return jt.query(queryLabelByOrderIdSQL, new Object[] {url, labelPoint, orderId}, new LabelPointRmp());
    }
    
    @Override
    public List queryLabelProperty(String labelId){
        String sql = "SELECT B.LABEL_ID, A.LABEL_PROP_ID, A.LABEL_PROP_NAME" +
                "  FROM CC_LABEL A ,CC_LABEL_PRO_REF B" + 
                " WHERE  A.LABEL_PROP_ID = B.LABEL_PRO_ID" + 
                " AND B.LABEL_ID = ?";
        return jt.queryForList(sql, labelId);
    }
}