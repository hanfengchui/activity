package com.timesontransfar.labelConfig.dao;

import java.util.List;
import java.util.Map;

import com.timesontransfar.labelConfig.pojo.LabelGroup;
import com.timesontransfar.labelConfig.pojo.LabelInsertPointreference;
import com.timesontransfar.labelConfig.pojo.LabelProRef;
import com.timesontransfar.labelConfig.pojo.LabelTemplate;

@SuppressWarnings("rawtypes")
public interface ILabelTemplateDao {
   
   
   
   //标签树---开始
   /**查询标签（标签树子节点）
    * 
    */
   List<Map> queryLabelTemplate();

   /**查询标签组（标签树节点）
    * 
    * @return
    */
   List<Map> queryLabelGroupNode();
 //标签树---结束
   
   
   //标签模板---开始
   /**
    * 查询所有的标签模板
    * @return
    */
   List<Map> queryAllLabelTemplate(int page,int rows,String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId);
   
   /**
    * 保存标签模板信息
    * @param template
    * @return
    */
   int saveLabelTemplate(LabelTemplate template);
   
   /**
    * 查询标签模板是否已经存在
    * @param labelName
    * @return
    */
   boolean isLabelTemplateCreate(String labelName,String labelId);
   
   /**
    * 删除标签模板
    * @param labelId
    * @return
    */
   int deleteLabelTemplate(String labelId);

   int batchDeleteLabelTemplate(final List list);

   /**
    * 查询所有标签模板数量
    */
   int queryLabelTemplateCount(String labelName,String labelWayId,String labelClassId, String labelGroupId,String labelDepartmentId);

   /**查询标签组
	 * 
	 */
   List<Map> queryLabelGroup();
   
   /**查询标签类别------静态资源表
	 * 
	 */
   public List<Map> queryLabelClass();
   
   /**查询识别方式------静态资源表
	 * 
	 */
	public List<Map> queryLabelWay();
	
	/**查询嵌入点------静态资源表
	 * 
	 */
	List<Map> queryLabelInsertPoint();

	/**查询规则左边变量值------静态资源表
	 * 
	 */
	List<Map> queryLabelRuleLeftField();
	
	/**查询适用部门
	 * 
	 */
	List<Map> queryLabelDepartment(String regionId);
	
	/**
	 * 保存标签模板--标签嵌入点关系
	 * @param i
	 * @return
	 */
	int saveLabelInsertPointreference(LabelInsertPointreference i);

	

	/**
	 * 通过标签模板ID查询标签模板的信息
	 * @param label_id
	 * @return
	 */
	 Map queryLabelByLabelId(String labelId);

	 /**根据标签ID查询嵌入点
	 * 
	 */
	List<Map> queryLabelInsertPoints(String labelId);


	 /**
     * 查询Label包含的规则
     * @return
     */
	List<Map> queryLabelRulesByLabelId(String labelId);

	/**
	 * 更新标签模板
	 * @param label
	 * @return
	 */
	int updateLabelTemplate(LabelTemplate label);
	
	/**
	 * 删除标签规则
	 * @param label_id
	 * @return
	 */
	int deleteLabelRelusByLabelId(String labelId);
	
	/**
	 * 删除标签嵌入点
	 * @param label_id
	 * @return
	 */
	int deleteLabelInsertPointsByLabelId(String labelId);


	
   
   //标签模板---结束
	
	/**
	 * 查询标签组名是否存在
	 */
	boolean isLabelGroupCreate(String labelGroupName,String labelGroupId);

	/**
	 * 保存标签组
	 * @param lg
	 */
	int saveLabelGroup(LabelGroup lg);

	/**
	 * 查询标签组详细信息
	 * @param label_group_id 
	 * @return
	 */
	Map queryLabelGroupByGroupId(String labelGroupId);

	/**
	 * 更新标签组信息
	 * @param lg
	 * @return
	 */
	int updateLabelGroup(LabelGroup lg);

	/**
	 * 删除标签组
	 * @param label_group_id
	 * @return
	 */
	int deleteLabelGroup(String labelGroupId);


	/**
	 * 查询部门已经配置的标签数量
	 * @param string
	 * @return
	 */
	int queryLabelConfiguredCount(String string);


	/**
	 * 查询部门可以配置的标签数量
	 * @param string
	 * @return
	 */
	int queryLabelConfigureValue(String string);


	/**
	 * 查询标签组状态
	 * @param label_group_id
	 * @param label_id 
	 * @return
	 */
	boolean labelGroupState(String labelGroupId, String labelId);


	List queryLabelProList();


	int saveLabelProRef(LabelProRef labelProRef);


	void deleteLabelProRefByLabelId(String labelId);


	List<Map> queryLabelProRef(String labelId);


	int addLabelpro(String labelProId, String labelProName);
}