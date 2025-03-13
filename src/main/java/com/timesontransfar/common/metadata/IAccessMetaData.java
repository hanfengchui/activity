/**
 *
 */
package com.timesontransfar.common.metadata;

import java.util.List;

import com.timesontransfar.common.metadata.model.MetaAttribute;

/**
 * @author Administrator
 *
 */
@SuppressWarnings("rawtypes")
public interface IAccessMetaData {
	
	/**
	 * 根据属性ID取属性对象
	 * @param id
	 * @return
	 */
	public MetaAttribute getAttribute(String id);

	/**
	 * 取所有属性对象
	 * @return
	 */
	public List getAllAttribute();

	/**
	 * 取到所有的某实体的属性列表
	 * @param objId
	 */
	public List getAllAttributeByObjId(Long objId);
	
	/**
	 * 取得所有的被权限因子使用的实体属性
	 * @return
	 */
	public List getAllAttributeByCondition();

	/**
	 * 取给定的实体、表名、列名对应的元数据对象
	 */
	public Object getAttributeIdIn(Long objId,String tableCode,String colCode);

}
