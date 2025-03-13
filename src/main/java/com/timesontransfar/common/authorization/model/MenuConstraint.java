package com.timesontransfar.common.authorization.model;

public class MenuConstraint implements IConstraint,java.io.Serializable{
	private String id;

	public String doConstraint() {
		// Auto-generated method stub
		return "MENU__"+this.getId();
	}

	public String getId() {
		// Auto-generated method stub
		return this.id;
	}

	public void setId(String constraintId) {
		// Auto-generated method stub
		this.id=constraintId;
	}
}
