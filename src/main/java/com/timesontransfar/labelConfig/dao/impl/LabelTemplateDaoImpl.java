package com.timesontransfar.labelConfig.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.labelConfig.dao.ILabelTemplateDao;
import com.timesontransfar.labelConfig.pojo.LabelGroup;
import com.timesontransfar.labelConfig.pojo.LabelInsertPointreference;
import com.timesontransfar.labelConfig.pojo.LabelProRef;
import com.timesontransfar.labelConfig.pojo.LabelTemplate;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Component(value="labelTemplateDao")
public class LabelTemplateDaoImpl implements ILabelTemplateDao {
    @Resource
    private JdbcTemplate jt;
    
    private String saveSql = "INSERT INTO CC_LABEL_TEMPLATE(" +
    		"LABEL_ID," +
    		"LABEL_WAY_ID," +
    		"LABEL_NAME," +
    		"LABEL_NAME_SPY," +
    		"LABEL_CLASS_ID," +
    		"LABEL_GROUP_ID," +
    		"LABEL_GLOBAL_MARK_ID," +
    		"RULE_DESC," +
    		"REGION_ID," +
    		"STATE," +
    		"STAFF_ID," +
    		"STATE_DATE," +
    		"EFF_DATE," +
    		"EXP_DATE," +
    		"NOTE," +
    		"label_rules,  "+
    		"PROP_FLAG  "+
    		")VALUES(?,?,?,?,?,?,?,?,?,?,?, STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'), STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'), STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,?,?)";

	
	//标签树-----开始
	/**查询标签组（标签树节点）
    * 
    * @return
    */
	@Override
	public List queryLabelTemplate() {
		List list=new ArrayList();
		String sql="SELECT t.LABEL_ID,"+
		      " t.LABEL_WAY_ID,"+
		      " t.LABEL_NAME,"+
		      " t.LABEL_NAME_SPY,"+
		      " t.LABEL_CLASS_ID,"+
		      " t.LABEL_GROUP_ID,"+
		      " t.LABEL_GLOBAL_MARK_ID,"+
		      " t.RULE_DESC,"+
		      " t.REGION_ID,"+
		      " DATE_FORMAT(t.state_date, '%Y-%m-%d %H:%i:%s') as  STATE_DATE,  "+
		      " DATE_FORMAT(t.eff_date, '%Y-%m-%d %H:%i:%s') as  EFF_DATE , "+
		      " DATE_FORMAT(t.exp_date, '%Y-%m-%d %H:%i:%s') as  EXP_DATE,  "+
		      " t.STAFF_ID,"+
		      " t.NOTE, "+
		      " t.STATE "+
		  "FROM CC_LABEL_TEMPLATE t "+
		  " where 1 = 1 order by t.STATE desc ";
		try{
	    	list=this.jt.queryForList(sql);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}

	/**查询标签组（标签树节点）
    * 
    * @return
    */
	//REGION_ID 适用部门ID 根据用户部门是否有权限显示
	@Override
	public List queryLabelGroupNode() {
		List list=new ArrayList();
		String sql="SELECT T.LABEL_GROUP_ID,T.LABEL_GROUP_NAME ,T.LABEL_GROUP_DESC,T.LABEL_CLASS_ID,T.REGION_ID,T.STATE  FROM CC_LABEL_GROUP T order by t.state desc";
	    try{
	    	list=this.jt.queryForList(sql);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}
	//标签树-----结束
	
	
	//标签模板-----开始
	/**
	 * 增加标签模板
	 */
	   public int saveLabelTemplate(LabelTemplate t) {
	        return this.jt.update(saveSql,
	                t.getLabelId(),
	                t.getLabelWayId(),
	                t.getLabelName(),
	                t.getLabelNameSpy(),
	                t.getLabelClassId(),
	                t.getLabelGroupId(),
	                t.getLabelGlobalMarkId(),
	                t.getRuleDesc(),
	                t.getRegionId(),
	                t.getState(),
	                t.getStaffId(),
	                t.getStateDate(),
	                t.getEffDate(),
	                t.getExpDate(),
	                t.getNote(),
	                t.getRulesStr(),
	                t.getPropFlag()
	        );
	    }

	   /**
	    * 删除标签模板
	    */
	    @Override
	    public int deleteLabelTemplate(String labelId) {
	        String delSql = "update CC_LABEL_TEMPLATE t set t.state=0,t.state_date=now()   WHERE t.LABEL_ID = ?";
	        return this.jt.update(delSql,labelId);
	    }
	    
		public int batchDeleteLabelTemplate(final List list) {
	        String delSql = "update CC_LABEL_TEMPLATE t set t.state=0,t.state_date=now()   WHERE t.LABEL_ID = ?";
			int[] i = this.jt.batchUpdate(delSql, new BatchPreparedStatementSetter() {
				public void setValues(PreparedStatement ps, int j) throws SQLException {
					ps.setString(1, list.get(j).toString());
				}
				
				public int getBatchSize() {
					return list.size();
				}
			});
			return i.length;
		}

	    /**
	     * 查询标签模板是否已经存在
	     */
	    @Override
		public boolean isLabelTemplateCreate(String labelName,String labelId) {
			String sql="SELECT COUNT(*) FROM CC_LABEL_TEMPLATE T WHERE T.LABEL_NAME =? and t.label_id!=?";
			int result=0;
			try{
				result=this.jt.queryForObject(sql, new Object[]{labelName,labelId},Integer.class);
			}catch(Exception e){
				e.printStackTrace();
			}
			return result>0;
		}
	 /**
	  * 查询所有的标签模板信息
	  */
	//REGION_ID 适用部门ID 根据用户部门是否有权限显示
	@Override
	public List queryAllLabelTemplate(int page,int rows,String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId) {
		List list=new ArrayList();
		String sql="SELECT t.label_id, "+
			       " t.label_way_id, "+
			       " (select a.col_value_name from pub_column_reference a "+
			       " where a.table_code = 'CC_LABEL_TEMPLATE' and a.col_code = 'LABEL_WAY_ID' and a.refer_id = t.label_way_id) as label_way_name, "+
			       " t.label_name, "+
			       " t.label_name_spy, "+
			       " t.label_class_id, "+
			       " (select a.col_value_name from pub_column_reference a where a.col_code = 'LABEL_CLASS_ID' and a.table_code = 'CC_LABEL_GROUP'  and a.refer_id = t.label_class_id) as label_class_name, "+
			       " t.label_group_id, "+
			       " g.label_group_name, "+
			       " t.label_global_mark_id, "+
			       " t.rule_desc, "+
			       " t.region_id, "+
			       //增加查询部门名称
			       "(SELECT b.org_name FROM tsm_organization b where t.region_id=b.org_id) as org_name, "+
			       " date_format(t.state_date, '%Y-%m-%d %H:%i:%s') as state_date, "+
			       " date_format(t.eff_date, '%Y-%m-%d %H:%i:%s') as eff_date, "+
			       " date_format(t.exp_date, '%Y-%m-%d %H:%i:%s') as exp_date, "+
			       " t.staff_id, "+
			       " t.note, "+
			       " t.state "+
			       " FROM CC_LABEL_TEMPLATE t "+
			       " left join CC_LABEL_GROUP g on t.label_group_id = g.label_group_id "+
			       " where  1 = 1 ";
		if(null!=labelName&&!"".equals(labelName)){
			sql+=" and t.label_name like '%"+labelName+"%'";
			
		}
		if(null!=labelWayId&&!"".equals(labelWayId)){
			sql+=" and t.label_way_id = CONVERT('"+labelWayId+"' , SIGNED)";
			
		}
		if(null!=labelClassId&&!"".equals(labelClassId)){
			sql+=" and t.label_class_id = CONVERT('"+labelClassId+"' , SIGNED)";
			
		}
		if(null!=labelGroupId&&!"".equals(labelGroupId)){
			sql+=" and t.label_group_id = '"+labelGroupId+"'";
			
		}
		if(null!=labelDepartmentId&&!"".equals(labelDepartmentId)){
			String [] departmentList=labelDepartmentId.split(",");
			String temp="and (";
			for(int i=0;i<departmentList.length;i++){
				temp+=" t.region_id like '%"+departmentList[i]+"%' or";
			}
			if(temp.lastIndexOf("r")==temp.length()-1){
				temp=temp.substring(0,temp.lastIndexOf("or")-1);
			}
			temp+=" )";
			sql+=temp;
		}
		
		sql+=" order by t.state desc limit " + (page-1)*rows + ", " + rows;
		try{
	    	list=this.jt.queryForList(sql);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}

	@Override
	public int queryLabelTemplateCount(String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId) {
		String sql="SELECT count(*) "+
		  " FROM CC_LABEL_TEMPLATE t "+
		  " where 1 = 1 ";
		  if(null!=labelName&&!"".equals(labelName)){
				sql+=" and t.label_name like '%"+labelName+"%'";
				
			}
			if(null!=labelWayId&&!"".equals(labelWayId)){
				sql+=" and t.label_way_id = CONVERT('"+labelWayId+"' , SIGNED)";
				
			}
			if(null!=labelClassId&&!"".equals(labelClassId)){
				sql+=" and t.label_class_id = CONVERT('"+labelClassId+"' , SIGNED)";
				
			}
			if(null!=labelGroupId&&!"".equals(labelGroupId)){
				sql+=" and t.label_group_id = '"+labelGroupId+"'";
				
			}
			if(null!=labelDepartmentId&&!"".equals(labelDepartmentId)){
				String [] departmentList=labelDepartmentId.split(",");
				String temp="and (";
				for(int i=0;i<departmentList.length;i++){
					temp+=" t.region_id like '%"+departmentList[i]+"%' or";
				}
				if(temp.lastIndexOf("r")==temp.length()-1){
					temp=temp.substring(0,temp.lastIndexOf("or")-1);
				}
				temp+=" )";
				sql+=temp;
			}
		int result=0;
		try{
			result=this.jt.queryForObject(sql,Integer.class);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	/**查询标签组
	 * 
	 */
	//REGION_ID 适用部门ID 根据用户部门是否有权限显示
	@Override
	public List queryLabelGroup() {
		List list=new ArrayList();
		String sql="SELECT T.LABEL_GROUP_ID,T.LABEL_GROUP_NAME,T.LABEL_GROUP_DESC,T.LABEL_CLASS_ID,T.REGION_ID,T.STATE FROM CC_LABEL_GROUP T WHERE 1=1 order by t.state desc";
	    try{
	    	list=this.jt.queryForList(sql);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}
	
	
	/**查询标签类别------静态资源表
	 * 
	 */
	@Override
	public List queryLabelClass() {
		List list=new ArrayList();
		String sql="SELECT D.REFER_ID,D.COL_VALUE_NAME,D.COL_VALUE FROM PUB_COLUMN_REFERENCE D WHERE D.COL_CODE = ? AND D.TABLE_CODE=?";
	    try{
	    	list=this.jt.queryForList(sql,"LABEL_CLASS_ID","CC_LABEL_GROUP");
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}
	/**查询识别方式------静态资源表
	 * 
	 */
	@Override
	public List queryLabelWay() {
		List list=new ArrayList();
		String sql="SELECT D.REFER_ID,D.COL_VALUE_NAME,D.COL_VALUE FROM PUB_COLUMN_REFERENCE D WHERE D.COL_CODE = ? AND D.TABLE_CODE=?";
	    try{
	    	list=this.jt.queryForList(sql,"LABEL_WAY_ID","CC_LABEL_TEMPLATE");
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}
	/**查询嵌入点------静态资源表
	 * 
	 */
	@Override
	public List queryLabelInsertPoint() {
		List list=new ArrayList();
		String sql="SELECT d.refer_id,d.col_value_name,d.col_name FROM PUB_COLUMN_REFERENCE d where d.table_code = 'CC_LABEL_INSERT_POINT' and d.col_code='LABEL_INSERT_POINTS_ID'";
	    try{
	    	list=this.jt.queryForList(sql);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}
	
	
	/**查询规则左边变量值------静态资源表
	 * 
	 */
	@Override
	public List queryLabelRuleLeftField() {
		List list=new ArrayList();
		String sql="SELECT d.refer_id,d.col_value_name FROM PUB_COLUMN_REFERENCE d where d.table_code = 'CC_LABEL_RULES' and d.col_code='LEFT_FIELD_ID'";
	    try{
	    	list=this.jt.queryForList(sql);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}
	
	/**查询适用部门
	 * 
	 */
	@Override
	public List queryLabelDepartment(String regionId) {
		List list=new ArrayList();
		String sql="SELECT t.ORG_ID,t.ORG_NAME FROM tsm_organization t WHERE   (instr(linkid,'-')=0 or (substr(linkid,3,1)='-' and if(LENGTH(linkid)-LENGTH(REPLACE(linkid,'-',''))>=2, LENGTH(SUBSTRING_INDEX(linkid, '-', 2))+1, 0) = 0)) and (t.up_org =? or t.org_id=?)";
	    try{
	    	list=this.jt.queryForList(sql,regionId,regionId);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}
	//标签模板-----结束
	
	/**
	 * 保存标签模板--标签嵌入点关系
	 * @param i
	 * @return
	 */
   @Override
    public int saveLabelInsertPointreference(LabelInsertPointreference i) {  
	    String sql="insert into CC_LABEL_INSERT_POINT "+
	    			" (LABEL_INSERT_ID, LABEL_ID, LABEL_INSERT_POINTS_ID) "+
	    			"  values "+
	    			" (?,?,?) ";
        return this.jt.update(sql,
              i.getLabelInsertId(),
              i.getLabelId(),
              i.getLabelInsertPointsId()
        );
    }
   
   
   /**
	 * 通过标签模板ID查询标签模板的信息
	 * @param label_id
	 * @return
	 */
	public Map queryLabelByLabelId(String labelId){
		Map label=new HashMap();
		String sql="SELECT t.LABEL_ID, "+
			       " t.LABEL_WAY_ID, "+
			       " (select a.col_value_name "+
			       " from pub_column_reference a "+
			       " where a.table_code = 'CC_LABEL_TEMPLATE' "+
			       "  and a.col_code = 'LABEL_WAY_ID' "+
			       "  and a.refer_id = t.label_way_id) as LABEL_WAY_NAME, "+
			       " t.LABEL_NAME, "+
			       " t.LABEL_NAME_SPY, "+
			       " t.LABEL_CLASS_ID, "+
			       " (select a.col_value_name "+
			       " from pub_column_reference a "+
			       " where a.col_code = 'LABEL_CLASS_ID' "+
			       " and a.table_code = 'CC_LABEL_GROUP' "+
			       " and a.refer_id = t.label_class_id) as LABEL_CLASS_NAME, "+
			       " t.LABEL_GROUP_ID, "+
			       " g.LABEL_GROUP_NAME, "+
			       " t.LABEL_GLOBAL_MARK_ID, "+
			       " t.RULE_DESC, "+
			       " t.LABEL_RULES, "+
			       " t.REGION_ID, "+
			       //增加查询部门名称
			       "(SELECT b.org_name FROM tsm_organization b where t.region_id=b.org_id) as ORG_NAME, "+
			       " DATE_FORMAT(t.state_date, '%Y-%m-%d') as  STATE_DATE, "+
			       " DATE_FORMAT(t.eff_date, '%Y-%m-%d') as  EFF_DATE,  "+
			       " DATE_FORMAT(t.exp_date, '%Y-%m-%d') as  EXP_DATE,  "+
			       " t.staff_id, "+
			       " t.note, "+
			       " t.state "+
			       "  FROM CC_LABEL_TEMPLATE t "+
			       " left join CC_LABEL_GROUP g on t.label_group_id = g.label_group_id "+
			       " where t.label_id = ? ";
		try{
			label=this.jt.queryForMap(sql,
	    			labelId
	    	);
	    	
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return label;
	}
	/**根据标签ID查询嵌入点
	 * 
	 */
	@Override
	public List queryLabelInsertPoints(String labelId) {
		List list = new ArrayList();
		String sql = "select t.LABEL_INSERT_POINTS_ID from CC_LABEL_INSERT_POINT t where t.label_id=?";
	    try {
	    	list = this.jt.queryForList(sql, labelId);
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
		return list;
	}
	
	/**
     * 查询Label包含的规则
     * @return
     */
	public List queryLabelRulesByLabelId(String labelId){
		List list = new ArrayList();
		String sql = "select t.LABEL_RULES_ID, "+
				      " t.LABEL_ID, "+
				      " t.LEFT_FIELD_ID, "+
				      " (SELECT d.col_value_name "+
				      "     FROM PUB_COLUMN_REFERENCE d "+
				      "    where d.refer_id = t.left_field_id) as LEFT_FIELD_VALUE, "+
				      "  t.LOGIC_SYMBOL, "+
				      "  t.RIGHT_CONTENT, "+
				      "  t.NEXT_LOGICSYMBOL, "+
				      "  t.RULES_SORT, "+
				      "  t.RULES_REMARK, "+
				      "  t.SIXTH_DIR_ID "+
				      " from CC_LABEL_RULES t "+
				      " where t.label_id = ? order by t.rules_sort";
	    try {
	    	list = this.jt.queryForList(sql, labelId);
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
		return list;
	}

	@Override
	public int deleteLabelInsertPointsByLabelId(String labelId) {
		String delSql = "delete t from CC_LABEL_INSERT_POINT t where t.label_id=?";
	    return this.jt.update(delSql, labelId);
	}

	@Override
	public int deleteLabelRelusByLabelId(String labelId) {
		String delSql = "delete t from CC_LABEL_RULES t where t.label_id=?";
	    return this.jt.update(delSql, labelId);
	}

	@Override
	public int updateLabelTemplate(LabelTemplate label) {
		String sql=" update CC_LABEL_TEMPLATE t "+
				   " set t.label_way_id          = ?, "+
				   "      t.label_name           = ?, "+
				   "      t.label_name_spy       = ?, "+
				   "      t.label_class_id       = ?, "+
				   "      t.label_group_id       = ?, "+
				   "      t.label_global_mark_id = ?, "+
				   "      t.rule_desc            = ?, "+
				   "      t.region_id            = ?, "+
				   "      t.state_date           = str_to_date(?, '%Y-%m-%d %H:%i:%s'), "+
				   "      t.eff_date             = str_to_date(?, '%Y-%m-%d %H:%i:%s'), "+
				   "      t.exp_date             = str_to_date(?, '%Y-%m-%d %H:%i:%s'), "+
				   "      t.staff_id             = ?, "+
				   "      t.note                 = ?, "+
				   "      t.state                = ?, "+
				   "      t.label_rules            = ? "+
				   "  where t.label_id = ?";
		return this.jt.update(sql,
				label.getLabelWayId(),
				label.getLabelName(),
				label.getLabelNameSpy(),
				label.getLabelClassId(),
				label.getLabelGroupId(),
				label.getLabelGlobalMarkId(),
				label.getRuleDesc(),
				label.getRegionId(),
				label.getStateDate(),
				label.getEffDate(),
				label.getExpDate(),
				label.getStaffId(),
				label.getNote(),
				label.getState(),
				label.getRulesStr(),
				label.getLabelId()
			);
	}

	@Override
	public boolean isLabelGroupCreate(String labelGroupName,String labelGroupId) {
		String sql = "SELECT COUNT(*) FROM CC_LABEL_GROUP T WHERE T.LABEL_GROUP_NAME=? AND T.LABEL_GROUP_ID!=?";
		int result = 0;
		try{
			result = this.jt.queryForObject(sql, new Object[]{labelGroupName,labelGroupId}, Integer.class);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result > 0;
	}

	@Override
	public int saveLabelGroup(LabelGroup lg) {
		String sql="insert into CC_LABEL_GROUP "+
						"  (LABEL_GROUP_ID, "+
						"  LABEL_GROUP_NAME, "+
						" LABEL_GROUP_DESC, "+
						" LABEL_CLASS_ID, "+
						" REGION_ID, "+
						" STATE) "+
						" values "+
						" (?, ?, ?, ?, ?, ?) ";
		return this.jt.update(sql, 
	                lg.getLabelGroupId(),
	                lg.getLabelGroupName(),
	                lg.getLabelGroupDesc(),
	                lg.getLabelClassId(),
	                lg.getRegionId(),
	                lg.getState()
	        );
	}

	@Override
	public Map queryLabelGroupByGroupId(String labelGroupId) {
		Map m=new HashMap();
		String sql="SELECT T.LABEL_GROUP_ID,T.LABEL_GROUP_NAME,T.LABEL_GROUP_DESC,T.LABEL_CLASS_ID,T.REGION_ID,T.STATE FROM CC_LABEL_GROUP T WHERE 1=1 and T.LABEL_GROUP_ID=?";
	    try{
	    	m=this.jt.queryForMap(sql,labelGroupId);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return m;
	}
	
	/**
	 * 更新标签组信息
	 * @param lg
	 * @return
	 */
	public int updateLabelGroup(LabelGroup lg){
		String sql="   update CC_LABEL_GROUP t "+
				   "   set t.label_group_name = ?, "+
				   "       t.label_group_desc = ?, "+
				   "       t.label_class_id   = ?, "+
				   "       t.region_id        = ?, "+
				   "       t.state        = ? "+
				   "  where t.label_group_id = ? ";
		return this.jt.update(sql,
				lg.getLabelGroupName(),
				lg.getLabelGroupDesc(),
				lg.getLabelClassId(),
				lg.getRegionId(),
				lg.getState(),
				lg.getLabelGroupId()
		);
	}
	
	/**
    * 删除标签组
    */
    @Override
    public int deleteLabelGroup(String labelGroupId) {
        String delSql = "update CC_LABEL_GROUP t set t.state=0 WHERE t.label_group_id =?";
        return this.jt.update(delSql,labelGroupId);
    }

	@Override
	public int queryLabelConfigureValue(String string) {
		String sql = "SELECT d.col_value FROM PUB_COLUMN_REFERENCE d where d.table_code = 'CC_ORG_ADDLABELNUM_REF' and d.col_code = 'ORG_ID' and d.col_value_name = ?";
		int result = 0;
		try {
			List<Integer> tmp = this.jt.queryForList(sql,new Object[]{string},Integer.class);
			if(!tmp.isEmpty()) {
				result = tmp.get(0);
			}
		} catch(Exception e) {
			result = 0;
		}
		return result;
	}

	@Override
	public int queryLabelConfiguredCount(String string) {
		String sql="select count(*) from CC_LABEL_TEMPLATE t where t.region_id like '%"+string+"%'";
		int result=0;
		try{
			result=this.jt.queryForObject(sql,Integer.class);
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public boolean labelGroupState(String labelId,String labelGroupId) {
		String sql="select t.state+0 from CC_LABEL_GROUP t where t.label_group_id=(select t.label_group_id from CC_LABEL_TEMPLATE t where t.label_id=?)";
		int result=0;
		try{
			result=this.jt.queryForObject(sql, new Object[]{labelId},Integer.class);
		}catch(Exception e){
			e.printStackTrace();
			return true;
		}
		return result > 0;
	}
	@Override
	public List queryLabelProList(){
		List list=new ArrayList();
		String sql="select t.LABEL_PROP_ID,t.LABEL_PROP_NAME  from cc_label t";
	    try{
	    	list=this.jt.queryForList(sql);
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
		return list;
	}

	@Override
	public int saveLabelProRef(LabelProRef labelProRef) {
		String sql="insert into cc_label_pro_ref(id,label_id,label_pro_id) values (?,?,?)";
	    return this.jt.update(sql,
				labelProRef.getId(),
				labelProRef.getLabelId(),
				labelProRef.getLabelProId()
        );
	}

	@Override
	public void deleteLabelProRefByLabelId(String labelId) {
		String delSql = "delete t from cc_label_pro_ref t where t.label_id=?";
        this.jt.update(delSql, labelId);
	}

	@Override
	public List queryLabelProRef(String labelId) {
		List list = new ArrayList();
		String sql = 
				"select t.LABEL_PRO_ID,t.LABEL_ID,"+
				   "(select l.label_prop_name "+
				   "from cc_label l "+
				   "where t.label_pro_id = l.label_prop_id) as LABEL_PROP_NAME "+
				   "from cc_label_pro_ref t "+
				   "where t.label_id = ?";
		try {
	    	list = this.jt.queryForList(sql, labelId);
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
		return list;
	}

	@Override
	public int addLabelpro(String labelProId, String labelProName) {
		String sql="insert into cc_label(label_prop_id,label_prop_name) values (?,?)";
	    return this.jt.update(sql,
	    		labelProId,
	    		labelProName
        );
	}
}