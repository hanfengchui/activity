package com.timesontransfar.common.authorization.model;

public interface IConstraint {
	/**
	 *
	 * @param Id
	 * @return
	 */
	public String getId();
	/**
	 *
	 * @param Id 
	 * @return
	 */
	public void setId(String constraintId);
	
	/**
	 *
	 * @param 
	 * @return
	 */
	public String doConstraint();	

}
