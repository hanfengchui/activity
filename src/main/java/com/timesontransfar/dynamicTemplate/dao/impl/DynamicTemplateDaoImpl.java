package com.timesontransfar.dynamicTemplate.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.redis.pojo.dynamicTemplate.AttrEle;
import com.redis.pojo.dynamicTemplate.AttrEleRmp;
import com.redis.pojo.dynamicTemplate.TemplateEle;
import com.redis.pojo.dynamicTemplate.TemplateEleRmp;
import com.templet.pojo.TemplateAttrPojo;
import com.templet.pojo.TemplateElementAttrPojo;
import com.templet.pojo.TemplateElementPojo;
import com.templet.pojo.TemplateRmp;
import com.timesontransfar.baseDao.IBaseDao;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.dynamicTemplate.dao.IDynamicTemplateDao;

import net.sf.json.JSONArray;

@Component(value="dynamicTemplateDao")
@SuppressWarnings({ "rawtypes", "unchecked", "static-access", "deprecation" })
public class DynamicTemplateDaoImpl implements IDynamicTemplateDao {
	protected Logger log = LoggerFactory.getLogger(DynamicTemplateDaoImpl.class);
	
	@Autowired
	private IBaseDao baseDao;
	@Autowired
	private PubFunc pubFunc;
    @Resource
    private JdbcTemplate jdbcTemplate;

	@Override
	public List loadDirOne2Two() {
		String sql = "select CONVERT(a.REFER_ID, CHAR) REFER_ID, a.COL_VALUE_NAME from pub_column_reference a where a.table_code = 'CC_SERVICE_CONTENT_ASK' and a.col_code = 'APPEAL_PROD_ID_TS' and a.entity_id = '202307' order by col_order" ;
		return baseDao.getJdbcTemplate().queryForList(sql);
	}
	
	@Override
	public List queryFirstTwoObj(String id) {
		String sql = 
				"select CONVERT(REFER_ID,CHAR) as REFER_ID, COL_VALUE_NAME  from pub_column_reference where refer_id = ( " +
						"  select entity_id from pub_column_reference where refer_id = ? )";
		List<Map<String, Object>> list = baseDao.getJdbcTemplate().queryForList(sql,id);
		return list;
	}

	@Override
	public List loadSubDirByEntityId(String id) {
		String sql = "select a.REFER_ID,a.COL_VALUE_NAME from pub_column_reference a where a.entity_id = ? order by col_order " ;
	    List<Map<String, Object>> list = baseDao.getJdbcTemplate().queryForList(sql,id);
		return list;
	}

	@Override
	public List loadBanjieDirOne2Two() {
		String sql = "select CONVERT(a.REFER_ID, CHAR) as REFER_ID, a.COL_VALUE_NAME from pub_column_reference a where a.table_code = 'CC_SHEET_QUALITATIVE' and a.col_code = 'TS_KEY_WORD_TS' and a.entity_id = '202307' order by col_order " ;
		List<Map<String, Object>> list = baseDao.getJdbcTemplate().queryForList(sql);
		return list;
	}
	
	@Override
	public List loadSubDir(String id,boolean boo) {
		String sql = "select a.REFER_ID ,a.COL_VALUE_NAME from pub_column_reference a where a.entity_id = ? order by col_order " ;
	    List<Map<String, Object>> list = baseDao.getJdbcTemplate().queryForList(sql,id);
	    List ls = new ArrayList();
	    for(int i=0;i<list.size();i++) {
	    	Map m = list.get(i);
	    	Map tmp = new HashMap();
	    	tmp.put("id", m.get("REFER_ID").toString());
	    	tmp.put("name", m.get("COL_VALUE_NAME").toString());
	    	tmp.put("parentId", boo ? null : id);
	    	ls.add(tmp);
	    }
		return ls;
	}
	
	@Override
	public String queryTemplateId(String sixId, String prodId) {
		List<Object> args = new ArrayList<>();
		args.add(sixId);
		String sql = "select a.template_id from CC_DYNAMIC_TEMPLATE_SIX a where a.six_id = ? and a.prod_id is null";
		if(StringUtils.isNotEmpty(prodId)) {
			args.add(prodId);
			sql = "select a.template_id from CC_DYNAMIC_TEMPLATE_SIX a where a.six_id = ? and a.prod_id = ?";
		}
		String templateId = baseDao.queryForString(sql, args.toArray(), "template_id");
		log.info("建单模板id: {}", templateId);
		return templateId;
	}
	
	@Override
	public List<String> loadRowListByOrderId(String orderId, boolean hisFlag) {
		String sql = "select t.ROW_ID from CC_DYNAMIC_TEMPLATE_ROW t " +
				" where t.template_id = (select b.complaints_id from cc_service_content_save b where b.service_order_id = ? limit 1) order by t.row_sort";
		if(hisFlag) {
			sql = "select t.ROW_ID from CC_DYNAMIC_TEMPLATE_ROW t " +
					" where t.template_id = (select b.complaints_id from cc_service_content_save_his b where b.service_order_id = ? limit 1) order by t.row_sort";
		}
		List<Map<String, Object>> list = baseDao.getJdbcTemplate().queryForList(sql,orderId);
		if(list.isEmpty())return Collections.emptyList();
		
		List<String> ls = new ArrayList<String>();
		for(int i=0;i<list.size();i++) {
			Map<String, Object> m = list.get(i);
			String id = m.get("row_id").toString();
			ls.add(id);
		}
		return ls;
		
	}

	@Override
	public List<TemplateEle> loadEleByRowId(String rowId) {
		String sql =
				"select E.ELE_ID,E.ALIAS_NAME,E.ELE_NAME,E.ELE_TYPE,E.INPUT_TYPE,E.ELE_BTN,E.ELE_EVENT,E.ELE_DESC,S.COL_SPAN,E.HEIGHT,S.IS_NECESSARY,S.IS_HIDDEN,S.IS_DISABLED from CC_DYNAMIC_TEMPLATE_ELE E, " +
				"( " + 
				"   SELECT T.ELE_ID,T.ELE_SORT,T.COL_SPAN,T.IS_NECESSARY,T.IS_HIDDEN,T.IS_DISABLED FROM CC_DYNAMIC_TEMPLATE_SORT T WHERE T.ROW_ID = ? ORDER BY T.ELE_SORT " + 
				") S " + 
				"where S.ELE_ID = E.ELE_ID ORDER BY S.ELE_SORT";

		return baseDao.getJdbcTemplate().query(sql,new Object[]{rowId},new TemplateEleRmp());
	}

	@Override
	public List<AttrEle> loadAttrListByRowId(String eleId) {
		String sql = "select t.ATTR_ID,t.ATTR_NAME,t.ATTR_DESC,a.PARENT_ATTR from CC_DYNAMIC_TEMPLATE_ATTR_SORT a,CC_DYNAMIC_TEMPLATE_ATTR t where a.ele_id = ? and a.attr_id = t.attr_id order by a.attr_sort";
		return baseDao.getJdbcTemplate().query(sql,new Object[]{eleId},new AttrEleRmp());
	}

	@Override
	public List<TemplateRmp> queryAllTemplate(String name,String dir) {
		StringBuffer sb = new StringBuffer();
		sb.append("select a.TEMPLATE_ID, a.TEMPLATE_NAME,a.TEMPLATE_TYPE,a.ACCEPT_TYPE_DETAIL,a.FORMAT_CONTENT, a.TEMPLATE_DESC,( " +
				"     select n from Ccs_St_Mapping_All_New where v_num = 2 AND n_id = b.six_id " + 
				"   ) as DIR_NAME " +
				"  from cc_dynamic_template a, cc_dynamic_template_six b " + 
				" where a.template_id = b.template_id ");
		List<TemplateRmp> ls = null ;
		if(StringUtils.isEmpty(name) && StringUtils.isEmpty(dir)) {
			ls = this.jdbcTemplate.query(sb.toString(), new BeanPropertyRowMapper<>(TemplateRmp.class));
		} else {
			if(!StringUtils.isEmpty(name) && !StringUtils.isEmpty(dir)) {
				sb.append(" and a.template_name like ? ");
				sb.append(" and b.six_id = ? ");
				log.info("sql = \n {}",sb.toString());
				ls = this.jdbcTemplate.query(sb.toString(), new Object[]{ "'%"+name+"%'",  dir }, new BeanPropertyRowMapper<>(TemplateRmp.class));
			}else if(!StringUtils.isEmpty(name) && StringUtils.isEmpty(dir)) {
				sb.append(" and a.template_name like ? ");
				String ts =  "%"+name+"%";
				log.info("sql = \n {}",sb.toString());
				ls = this.jdbcTemplate.query(sb.toString(), new Object[]{ ts }, new BeanPropertyRowMapper<>(TemplateRmp.class));
			}else {
				sb.append(" and b.six_id = ? ");
				log.info("sql = \n {}",sb.toString());
				ls = this.jdbcTemplate.query(sb.toString(), new Object[]{ dir }, new BeanPropertyRowMapper<>(TemplateRmp.class));
			}
		}
		return ls;
	}

	@Override
	public int saveContent(String id, String content) {
		String sql = "update cc_dynamic_template set template_desc = ? where template_id = ?";
		return baseDao.getJdbcTemplate().update(sql,content,id);
	}
   
	@Override
	public List queryRowInfo(String templateId) {
		//查询模板绑定的每行关联的元素列表
		String sql = "select a.ROW_ID, a.ROW_SORT," +
				"       (select GROUP_CONCAT(CONCAT('【', c.ele_name ,'】')) " +
				"          from cc_dynamic_template_ele c " + 
				"         where c.ele_id in (select b.ele_id " + 
				"                              from cc_dynamic_template_sort b " + 
				"                             where b.row_id = a.row_id)) as ELE_NAME " +
				"  from cc_dynamic_template_row a\n" + 
				" where a.template_id =?" + 
				" order by a.row_sort ";
		return baseDao.getJdbcTemplate().queryForList(sql,templateId);
	}

	@Override
	public List queryAllEle(String rowId,String eleName) {
		String qeruyAllEle = "select a.ELE_ID,a.ELE_NAME,a.ELE_TYPE from cc_dynamic_template_ele a";//查询所有元素
		List<String> argList = new ArrayList<>();
		if(StringUtils.isNotEmpty(eleName)) {
			qeruyAllEle = "select a.ELE_ID,a.ELE_NAME,a.ELE_TYPE from cc_dynamic_template_ele a where a.ELE_NAME like concat('%',?,'%')";
			argList.add(eleName);
		}
		//查询行绑定的元素
		String queryRowEle="select a.ELE_ID, a.ELE_NAME, a.ELE_TYPE\n" +
				"  from cc_dynamic_template_ele a\n" + 
				" where a.ele_id in\n" + 
				"       (select c.ele_id\n" + 
				"          from cc_dynamic_template_sort c\n" + 
				"         where c.row_id =?)";
		List rowList = baseDao.getJdbcTemplate().queryForList(queryRowEle,rowId);
		List allEleList = baseDao.getJdbcTemplate().queryForList(qeruyAllEle,argList.toArray());
		List list=new ArrayList();
		list.add(rowList);
		list.add(allEleList);
		return list;
	}

	@Override
	public int[] deleteEle(String rowId, JSONArray eleArr) {
		String sql="delete c from cc_dynamic_template_sort c where c.row_id=? and c.ele_id=?";//删除指定元素
		List<Object[]> batchArgs=new ArrayList<Object[]>();
		if(!eleArr.isEmpty()) {
			for(int i=0;i<eleArr.size();i++) {
				 batchArgs.add(new Object[]{rowId, eleArr.getString(i)});
			}
		}
		return baseDao.getJdbcTemplate().batchUpdate(sql, batchArgs);
	}

	@Override
	public int[] addEle(String rowId,JSONArray eleArr) {
		String qrySort=" select max(b.ele_sort)as sort from cc_dynamic_template_sort b where b.row_id=? ";//查询行对应的最后一个元素的序号
		String inSql="insert into cc_dynamic_template_sort (row_id,ele_id,ele_sort) values(?,?,?)";//新增行与元素绑定关系
		List<Map<String, Object>> getSort=baseDao.getJdbcTemplate().queryForList(qrySort,rowId);
		int sort=getSort.get(0).get("SORT")==null? 0 :Integer.parseInt(getSort.get(0).get("SORT").toString());
		
		List<Object[]> batchArgs=new ArrayList<Object[]>();
		if(!eleArr.isEmpty()) {
			for(int i=0;i<eleArr.size();i++) {
				 batchArgs.add(new Object[]{rowId, eleArr.getString(i), (sort+i+1)});
			}
		}
		
		return baseDao.getJdbcTemplate().batchUpdate(inSql, batchArgs);
	}

	@Override
	public int deleteRow(String rowId) {
		String qryCount="select count(1)as count  from cc_dynamic_template_sort a where a.row_id=?";//查询行与元素绑定关系
		String deleteEle="delete a from cc_dynamic_template_sort a where a.row_id=?";//删除元素与行绑定关系
		String deleteRow="delete t from cc_dynamic_template_row t where t.row_id=?";//删除模板与行绑定关系
		List<Map<String, Object>> getSort=baseDao.getJdbcTemplate().queryForList(qryCount,rowId);
		int count=Integer.parseInt(getSort.get(0).get("COUNT").toString());
		if(count>0) {
			 baseDao.getJdbcTemplate().update(deleteEle,rowId);
		}
		return baseDao.getJdbcTemplate().update(deleteRow,rowId);
	}

	@Override
	public int createRow(String templateId) {
		String qrySortSql="select max(a.row_sort)as sort from cc_dynamic_template_row a where a.template_id=?";//查询模板绑定的最后一行的序号
		String newRowId=pubFunc.crtGuid();
		String sql="insert into cc_dynamic_template_row(template_id,row_id,row_sort) values(?,?,?)";//新增模板与行绑定关系
		List<Map<String, Object>> getSort=baseDao.getJdbcTemplate().queryForList(qrySortSql,templateId);
		int sort=getSort.get(0).get("SORT")==null? 0 :Integer.parseInt(getSort.get(0).get("SORT").toString());
		return baseDao.getJdbcTemplate().update(sql,templateId,newRowId,(sort+1));
	}

	@Override
	public int updateRow(String rowId, int sort) {
		String sql="update cc_dynamic_template_row t set  t.row_sort=?  where t.row_id=?";
		
		return baseDao.getJdbcTemplate().update(sql, sort, rowId);
	}

	@Override
	public int saveTemplate(String templateName, int sixId, String content) {
		String insertTemp = "insert into cc_dynamic_template (template_id, template_name, template_desc) values(?, ?, ?)";
		String insertTempSix = "insert into cc_dynamic_template_six (template_id, six_id) values(?, ?)";
		String tempId = pubFunc.crtGuid();
		int num = baseDao.getJdbcTemplate().update(insertTemp, tempId, templateName, content);
		if(num > 0) {
			num = baseDao.getJdbcTemplate().update(insertTempSix, tempId, sixId);
		}
		return num;
	}

	@Override
	public int removeTemplate(String templateId) {
		String getRow = "select b.row_id from cc_dynamic_template_row b where b.template_id=?";//查询行与元素绑定
		String deleteRowEle = "delete a from cc_dynamic_template_sort a where a.row_id in(:rowId)";//删除行与元素绑定
		String deleteRowTemp = "delete c from cc_dynamic_template_row c where c.template_id=?";//删除行与模板绑定
		String deleteSixTemp = "delete b from cc_dynamic_template_six b where b.template_id=?";//删除六级目录与模板绑定
		String deleteTemp = "delete a from cc_dynamic_template a where a.template_id=?";//删除模板本身
		List<Map<String, Object>> rowList = baseDao.getJdbcTemplate().queryForList(getRow, templateId);
		if(!rowList.isEmpty()) {//删除行与元素绑定
			StringBuilder str = new StringBuilder("");
			for(int i=0; i<rowList.size(); i++) {
				String id = rowList.get(i).get("ROW_ID").toString();
				str.append(id+",");
			}
			String ids = str.toString().substring(0, str.length()-1);
			String[] s = ids.split(",");
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("rowId", Arrays.asList(s));
			new NamedParameterJdbcTemplate(baseDao.getJdbcTemplate()).update(deleteRowEle,params);
		}
		int num = baseDao.getJdbcTemplate().update(deleteTemp, templateId);
		num += baseDao.getJdbcTemplate().update(deleteSixTemp, templateId);
		num += baseDao.getJdbcTemplate().update(deleteRowTemp, templateId);
		return num;
	}

	@Override
	public List queryTemplateldServiceContent(String rowId, String orderId, boolean hisFlag) {
		String sql="SELECT b.ELE_ID, b.ELE_NAME, b.ELE_TYPE, b.INPUT_TYPE, d.ANSWER_ID, d.ANSWER_NAME, a.IS_HIDDEN FROM cc_dynamic_template_sort a,\n" +
						" cc_dynamic_template_ele b,cc_service_content_save d\n" + 
						" where a.ele_id = b.ele_id and b.ele_id = d.element_id and a.row_id = ? and d.service_order_id = ?";
		if(hisFlag) {
			sql="SELECT b.ELE_ID, b.ELE_NAME, b.ELE_TYPE, b.INPUT_TYPE, d.ANSWER_ID, d.ANSWER_NAME, a.IS_HIDDEN FROM cc_dynamic_template_sort a,\n" +
					" cc_dynamic_template_ele b,cc_service_content_save_his d\n" + 
					" where a.ele_id = b.ele_id and b.ele_id = d.element_id and a.row_id = ? and d.service_order_id = ?";
		}
		List<Map<String, Object>> rowList = baseDao.getJdbcTemplate().queryForList(sql, rowId, orderId);
		return rowList;
	}
	
	@Override
	public List queryAllTemplateAnswer(String orderId, boolean hisFlag) {
		String sql = "SELECT B.ELE_ID, B.ELE_NAME, B.ELE_TYPE, D.ANSWER_ID, D.ANSWER_NAME FROM " +
				"CC_DYNAMIC_TEMPLATE_ELE B, CC_SERVICE_CONTENT_SAVE D " + 
				"WHERE B.ELE_ID = D.ELEMENT_ID AND D.SERVICE_ORDER_ID = ?";
		if(hisFlag) {
			sql = "SELECT B.ELE_ID, B.ELE_NAME, B.ELE_TYPE, D.ANSWER_ID, D.ANSWER_NAME FROM " +
					"CC_DYNAMIC_TEMPLATE_ELE B, CC_SERVICE_CONTENT_SAVE_HIS D " + 
					"WHERE B.ELE_ID = D.ELEMENT_ID AND D.SERVICE_ORDER_ID = ?";
		}
		return baseDao.getJdbcTemplate().queryForList(sql, orderId);
	}
	
	public int queryTemplateType(String templateId) {
		String sql = "select ifnull(c.TEMPLATE_TYPE, 0) from cc_dynamic_template c where c.template_id=?";
		List<Integer> tmp = baseDao.getJdbcTemplate().queryForList(sql,new Object[]{templateId},Integer.class);
		int type = 0;
		if(!tmp.isEmpty()) {
			type = tmp.get(0);
		}
		return type;
	}
	
	public String queryFinishTemplateId(String sixId) {
		String sql = "select a.FINISH_TEMPLATE_ID from CC_DYNAMIC_TEMPLATE_SIX a where a.six_id = ?";
		String templateId = baseDao.queryForString(sql, new Object[] {sixId}, "finish_template_id");
		log.info("结案模板id: {}", templateId);
		return templateId;
	}
	
	public List<String> loadRowListByTemplateId(String templateId) {
		String sql = "select t.ROW_ID from CC_DYNAMIC_TEMPLATE_ROW t where t.template_id = ? order by t.row_sort";
		List<Map<String, Object>> list = baseDao.getJdbcTemplate().queryForList(sql, templateId);
		if(list.isEmpty())return Collections.emptyList();
		
		List<String> ls = new ArrayList<>();
		for(int i=0;i<list.size();i++) {
			Map<String, Object> m = list.get(i);
			String id = m.get("row_id").toString();
			ls.add(id);
		}
		return ls;
	}
	
	public List queryAllFinishTemplateAnswer(String orderId) {
		String sql = "SELECT B.ELE_ID, B.ELE_NAME, B.ELE_TYPE, D.ANSWER_ID, D.ANSWER_NAME FROM " +
						"cc_dynamic_template_ele b, cc_deal_content_save d " + 
						"where b.ele_id = d.element_id and d.service_order_id = ?";
		List<Map<String, Object>> rowList = baseDao.getJdbcTemplate().queryForList(sql,orderId);
		return rowList;
	}
	
	public String queryFormatContent(String templateId) {
		String sql = "select ifnull(c.FORMAT_CONTENT, '') FORMAT_CONTENT from cc_dynamic_template c where c.template_id = ?";
		String formatContent = baseDao.queryForString(sql, new Object[] {templateId}, "FORMAT_CONTENT");
		log.info("模板格式: {}", formatContent);
		return formatContent;
	}
	
	public String queryJudgeIdByOrderId(String orderId) {
		String sql = "select s.SIX_ID ID from cc_deal_content_save c "
				+ "LEFT JOIN cc_dynamic_template_six s on c.COMPLAINTS_ID = s.FINISH_TEMPLATE_ID "
				+ "where c.SERVICE_ORDER_ID = ? limit 1";
		String judgeId = baseDao.queryForString(sql, new Object[] {orderId}, "ID");
		log.info("结案模板关联ID: {}", judgeId);
		return judgeId;
	}
	
	public List<TemplateAttrPojo> queryTemplateAttr(String attrId, String attrName) {
		StringBuffer sb = new StringBuffer();
		sb.append("select a.ATTR_ID, a.ATTR_NAME,a.ATTR_DESC" +
				"  from cc_dynamic_template_attr a " );
		List<TemplateAttrPojo> result = null ;
		if(StringUtils.isEmpty(attrId) && StringUtils.isEmpty(attrName)) {
			result = this.jdbcTemplate.query(sb.toString(), new BeanPropertyRowMapper<>(TemplateAttrPojo.class));
		} else {
			if(!StringUtils.isEmpty(attrId) && !StringUtils.isEmpty(attrName)) {
				sb.append(" where a.ATTR_NAME like ? ");
				sb.append(" and a.ATTR_ID = ? ");
				log.info("sql = \n {}",sb.toString());
				result = this.jdbcTemplate.query(sb.toString(), new Object[]{ "'%"+attrName+"%'", attrId }, new BeanPropertyRowMapper<>(TemplateAttrPojo.class));
			}else if(!StringUtils.isEmpty(attrName) && StringUtils.isEmpty(attrId)) {
				sb.append(" where a.ATTR_NAME like ? ");
				String ts =  "%"+attrName+"%";
				log.info("sql = \n {}",sb.toString());
				result = this.jdbcTemplate.query(sb.toString(), new Object[]{ ts }, new BeanPropertyRowMapper<>(TemplateAttrPojo.class));
			}else {
				sb.append(" where a.ATTR_ID = ? ");
				log.info("sql = \n {}",sb.toString());
				result = this.jdbcTemplate.query(sb.toString(), new Object[]{ attrId }, new BeanPropertyRowMapper<>(TemplateAttrPojo.class));
			}
		}
		return result;
	}

	public int addTemplateAttr(TemplateAttrPojo attrPojo){
		String sql="insert into cc_dynamic_template_attr(ATTR_ID, ATTR_NAME,ATTR_DESC) values(?,?,?)";
		return baseDao.getJdbcTemplate().update(sql,attrPojo.getAttrId(),attrPojo.getAttrName(),attrPojo.getAttrDesc());
	}

	public int updateTemplateAttr(TemplateAttrPojo attrPojo){
		String sql = "update cc_dynamic_template_attr set ATTR_NAME = ?, ATTR_DESC = ? where ATTR_ID = ?";
		return baseDao.getJdbcTemplate().update(sql,attrPojo.getAttrName(),attrPojo.getAttrDesc(),attrPojo.getAttrId());
	}
	public int delTemplateAttr(TemplateAttrPojo attrPojo){
		String sql="delete from cc_dynamic_template_attr  where attr_id=?";//删除模板与行绑定关系
		return baseDao.getJdbcTemplate().update(sql,attrPojo.getAttrId());
	}

	public List<TemplateElementPojo> queryTemplateElement(String eleId, String eleName){
		StringBuffer sb = new StringBuffer();
		sb.append("select a.ELE_ID, a.ELE_NAME,a.ELE_TYPE,a.ELE_BTN,a.ELE_EVENT,a.ELE_DESC,a.HEIGHT,a.ALIAS_NAME,a.INPUT_TYPE" +
				"  from cc_dynamic_template_ele a " );
		List<TemplateElementPojo> result = null ;
		if(StringUtils.isEmpty(eleId) && StringUtils.isEmpty(eleName)) {
			result = baseDao.query(sb.toString(),new Object[]{},TemplateElementPojo.class);
		} else {
			if(!StringUtils.isEmpty(eleId) && !StringUtils.isEmpty(eleName)) {
				sb.append(" where a.ELE_NAME like ? ");
				sb.append(" and a.ELE_ID = ? ");
				log.info("sql = \n {}",sb.toString());
				result = baseDao.query(sb.toString(),new Object[]{ "'%"+eleName+"%'",  eleId },TemplateElementPojo.class);
			}else if(!StringUtils.isEmpty(eleName) && StringUtils.isEmpty(eleId)) {
				sb.append(" where a.ELE_NAME like ? ");
				String ts =  "%"+eleName+"%";
				log.info("sql = \n {}",sb.toString());
				result = baseDao.query(sb.toString(),new Object[]{ ts },TemplateElementPojo.class);
			}else {
				sb.append(" where a.ELE_ID = ? ");
				log.info("sql = \n {}",sb.toString());
				result = baseDao.query(sb.toString(),new Object[]{ eleId },TemplateElementPojo.class);
			}
		}
		return result;
	}

	public int addTemplateElement(TemplateElementPojo elePojo){
		String guid = this.pubFunc.crtGuid();
		String sql="insert into cc_dynamic_template_ele(ELE_ID, ELE_NAME,ELE_TYPE,ELE_BTN,ELE_EVENT,ELE_DESC,HEIGHT,ALIAS_NAME,INPUT_TYPE) "+
				" values(?,?,?,?,?,?,?,?,?)";
		return baseDao.getJdbcTemplate().update(sql,guid,elePojo.getEleName(),elePojo.getEleType(),elePojo.getEleBtn(),elePojo.getEleEvent(),
				elePojo.getEleDesc(),elePojo.getHeight(),elePojo.getAliasName(),elePojo.getInputType());
	}
	
	public int updateTemplateElement(TemplateElementPojo elePojo){
		String sql = "update cc_dynamic_template_ele set ELE_NAME= ? ,ELE_TYPE= ? ,ELE_BTN= ? ,ELE_EVENT= ? ,ELE_DESC= ? ,HEIGHT= ? ,ALIAS_NAME= ? ,INPUT_TYPE= ?  "+
				" where ELE_ID = ?";
		return baseDao.getJdbcTemplate().update(sql,elePojo.getEleName(),elePojo.getEleType(),elePojo.getEleBtn(),elePojo.getEleEvent(),
				elePojo.getEleDesc(),elePojo.getHeight(),elePojo.getAliasName(),elePojo.getInputType(),elePojo.getEleId());
	}
	
	public int delTemplateElement(TemplateElementPojo elePojo){
		String sql="delete from cc_dynamic_template_ele  where ele_id=?";//删除模板与行绑定关系
		return baseDao.getJdbcTemplate().update(sql,elePojo.getEleId());
	}
	
	public List<TemplateRmp> queryTemplateRmp(String tempId, String tempName){
		StringBuffer sb = new StringBuffer();
		sb.append("select a.TEMPLATE_ID, a.TEMPLATE_NAME,a.TEMPLATE_DESC,a.ACCEPT_TYPE,a.ACCEPT_TYPE_DETAIL,a.ELE_DESC,a.TEMPLATE_TYPE,a.FORMAT_CONTENT" +
				"  from cc_dynamic_template a " );
		List<TemplateRmp> result = null ;
		if(StringUtils.isEmpty(tempId) && StringUtils.isEmpty(tempName)) {
			result = baseDao.query(sb.toString(),new Object[]{},TemplateRmp.class);
		} else {
			if(!StringUtils.isEmpty(tempId) && !StringUtils.isEmpty(tempName)) {
				sb.append(" where a.TEMPLATE_NAME like ? ");
				sb.append(" and a.TEMPLATE_ID = ? ");
				log.info("sql = \n {}",sb.toString());
				result = baseDao.query(sb.toString(),new Object[]{ "'%"+tempName+"%'",  tempId },TemplateRmp.class);
			}else if(!StringUtils.isEmpty(tempName) && StringUtils.isEmpty(tempId)) {
				sb.append(" where a.TEMPLATE_NAME like ? ");
				String ts =  "%"+tempName+"%";
				log.info("sql = \n {}",sb.toString());
				result = baseDao.query(sb.toString(),new Object[]{ ts },TemplateRmp.class);
			}else {
				sb.append(" where a.TEMPLATE_ID = ? ");
				log.info("sql = \n {}",sb.toString());
				result = baseDao.query(sb.toString(),new Object[]{ tempId },TemplateRmp.class);
			}
		}
		return result;
	}

	public List<TemplateAttrPojo> queryTemplateAttrByEle(String eleId){
		StringBuffer sb = new StringBuffer();
		sb.append("select a.ATTR_ID, a.ATTR_NAME,a.ATTR_DESC" +
				"  from cc_dynamic_template_attr a ,cc_dynamic_template_attr_sort b" );
		List<TemplateAttrPojo> result = null ;

		sb.append(" where b.ELE_ID = ? ");
		sb.append(" and  b.ATTR_ID = a.ATTR_ID ");
		sb.append(" order by  b.ATTR_SORT ");
		log.info("sql = \n {}",sb.toString());
		result = baseDao.query(sb.toString(),new Object[]{ eleId },TemplateAttrPojo.class);
		return result;
	}

	public int updateTemplateEleRelaAttr(TemplateElementAttrPojo pojo){
		String attrIds = pojo.getAttrId();
		JSONArray json = new JSONArray();
		JSONArray arr = json.fromObject(attrIds);

		if("right".contentEquals(pojo.getFlag())) {
			StringBuffer sb = new StringBuffer();
			sb.append("insert into cc_dynamic_template_attr_sort (ELE_ID, ATTR_ID,ATTR_SORT)").
					append(" values(?,?,?) ");
			List<Object[]> batchArgs=new ArrayList<Object[]>();
			if(!arr.isEmpty()) {
				for(int i=0;i<arr.size();i++) {
					batchArgs.add(new Object[]{pojo.getEleId(), arr.getString(i),i});
				}
			}
			return baseDao.getJdbcTemplate().batchUpdate(sb.toString(), batchArgs).length;
		}else if("left".contentEquals(pojo.getFlag())) {
			StringBuffer sb = new StringBuffer();
			sb.append("delete from cc_dynamic_template_attr_sort ").
					append(" where ELE_ID=? and ATTR_ID=? ");
			List<Object[]> batchArgs=new ArrayList<Object[]>();
			if(!arr.isEmpty()) {
				for(int i=0;i<arr.size();i++) {
					batchArgs.add(new Object[]{pojo.getEleId(), arr.getString(i)});
				}
			}
			return baseDao.getJdbcTemplate().batchUpdate(sb.toString(), batchArgs).length;
		}else {
			return 0;
		}
	}
	
}
