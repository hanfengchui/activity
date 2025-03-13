package com.timesontransfar.common.authorization.model;

import java.io.Serializable;


public class ConstraintFactory implements IConstraintFactory,Serializable{

	public IConstraint generateConstraint(int constraintType){
		switch(constraintType){
		case 0:
			//为零则为控件约束
			return new CtrlConstraint();
		case 1:
			//为一则为菜单约束
			return new MenuConstraint();
		case 2:
			//为一则为菜单约束
			return new PopMenuConstraint();
		default:
			return new CtrlConstraint();
		}
	}

}
