/**
 * @author 
 */
package com.timesontransfar.labelConfig.service;

import java.util.List;
import java.util.Map;

import com.timesontransfar.labelConfig.ResultObj;
import com.timesontransfar.labelConfig.pojo.LabelClass;
import com.timesontransfar.labelConfig.pojo.LabelDepartment;
import com.timesontransfar.labelConfig.pojo.LabelGroup;
import com.timesontransfar.labelConfig.pojo.LabelInsertPoint;
import com.timesontransfar.labelConfig.pojo.LabelRules;
import com.timesontransfar.labelConfig.pojo.LabelTemplate;
import com.timesontransfar.labelConfig.pojo.LabelWay;

import net.sf.json.JSONArray;

@SuppressWarnings("rawtypes")
public interface ILabelManagerService {

	/**
	 * Description: 新标签树<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年12月28日 下午1:03:11 <br>
	 */
	public JSONArray initDTreeDataNew();

	
	//标签模板相关操作----开始
	/**
	 * 增加标签模板
	 * @param label
	 * @return
	 * @throws Exception
	 */
	public ResultObj addLabelTemplate(LabelTemplate label) throws Exception;
	
	/**
	 * 查询所有的标签模板
	 * @param row 
	 * @param page 
	 * @return
	 */
	public List<LabelTemplate> queryAllLabelTemplate(int page, int row, String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId);

	/**
	 * 查询所有的标签模板数量
	 * @return
	 */
	public int queryLabelTemplateCount(String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId);

	/**查询标签组
	 * 
	 */
	List<LabelGroup> queryLabelGroup();


	/**查询标签类别------静态资源表
	 * 
	 */
	List<LabelClass> queryLabelClass();

	/**查询识别方式------静态资源表
	 * 
	 */
	List<LabelWay> queryLabelWay();

	/**查询规则左边变量值------静态资源表
	 * 
	 */
	List<LabelRules> queryLabelRuleLeftField();

	/**查询适用部门
	 * 
	 */
	List<LabelDepartment> queryLabelDepartment(String regionId);


	

	/**标签主键获取-32随机字符
	 * 
	 */
	
	public String queryLabelId();


	/**
	 * 通过标签模板ID查询标签模板的信息
	 * @param label_id
	 * @return
	 */
	public LabelTemplate queryLabelByLabelId(String labelId);



    /**
     * 查询Label包含的规则
     * @return
     */
	 List<LabelRules> queryLabelRulesByLabelId(String labelId);

	/**
	 * 查询标签规则---关系符号
	 */
	List<Map<String, String>> queryLogicSymbol();

	/**
	 * 查询标签规则---逻辑符
	 */
	List<Map<String, String>> queryNextLogicSymbol();


	/**
	 * 修改标签模板（嵌入点，规则）
	 * @param template
	 * @return
	 * @throws Exception 
	 */
	ResultObj editLabelTemplate(LabelTemplate template) throws Exception;

	/**
	 * 新增标签组
	 * @param lg
	 * @return
	 */
	ResultObj addLabelGroup(LabelGroup lg);


	/**
	 * 查询标签组 详细信息
	 * @param label_group_id
	 * @return
	 */
	public LabelGroup queryLabelGroupByLabelGroupId(String labelGroupId);

	/**
	 * 更新标签组信息
	 * @param lg
	 * @return
	 */
	public ResultObj updateLabelGroup(LabelGroup lg);


	/**
	 * 删除标签组和包含的标签
	 * @param label_group_id
	 * @param list
	 * @return
	 */
	public ResultObj deleteLabelGroup(String labelGroupId,
			List<LabelTemplate> list);
	
	/**
	 * 删除标签
	 * @param label_group_id
	 * @param list
	 * @return
	 */
	public ResultObj deleteLabelTemplate(String labelId);

	public ResultObj batchDeleteLabelTemplate(List list);

	
	public List<LabelInsertPoint> queryLabelInsertPointForTree();

	
	
	//标签模板操作----完
	
	
	
	public Map queryLabelProList();


	/**
	 * 新增标签属性
	 * @param la
	 * @return
	 */
	public ResultObj addLabelpro(String labelProId, String labelProName);
}
