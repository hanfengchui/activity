package com.timesontransfar.common.exception;

import java.io.Serializable;

public class BusinessExceptionObject implements Serializable{
	private String id;
	private String name;
	private String type;
	private boolean system;
	private String description;
	private String possibleCause;

	public BusinessExceptionObject() {
		super();
		// Auto-generated constructor stub
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPossibleCause() {
		return possibleCause;
	}

	public void setPossibleCause(String possibleCause) {
		this.possibleCause = possibleCause;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isSystem() {
		return system;
	}

	public void setSystem(boolean system) {
		this.system = system;
	}


}
