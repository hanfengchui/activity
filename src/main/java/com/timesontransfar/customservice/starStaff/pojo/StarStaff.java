package com.timesontransfar.customservice.starStaff.pojo;

public class StarStaff {

	 private  int  staffId = 0 ; // 员工号
	 private  String  staffname = ""; // 员工姓名
	 private  String  starClass = ""; // 星级
	 private  int  starClassId = 0 ; // 星级ID
	 private  String  groupType = ""; // 班级类型
	 private  String  importDate = ""; // 导入时间
	 private  int  importStaff = 0 ; // 导入员工ID

	/** 
	 * @param staffId  设置  员工号
	 */
	public void setStaffId( int staffId){
	 this.staffId = staffId; 
	}

	/** 
	 * @param staffId  获取  员工号
	 */
	public  int  getStaffId(){
	 return this.staffId; 
	}
	/** 
	 * @param staffname  设置  员工姓名
	 */
	public void setStaffname( String staffname){
	 this.staffname = staffname; 
	}

	/** 
	 * @param staffname  获取  员工姓名
	 */
	public  String  getStaffname(){
	 return this.staffname; 
	}
	/** 
	 * @param starClass  设置  星级
	 */
	public void setStarClass( String starClass){
	 this.starClass = starClass; 
	}

	/** 
	 * @param starClass  获取  星级
	 */
	public  String  getStarClass(){
	 return this.starClass; 
	}
	/** 
	 * @param starClassId  设置  星级ID
	 */
	public void setStarClassId( int starClassId){
	 this.starClassId = starClassId; 
	}

	/** 
	 * @param starClassId  获取  星级ID
	 */
	public  int  getStarClassId(){
	 return this.starClassId; 
	}
	/** 
	 * @param groupType  设置  班级类型
	 */
	public void setGroupType( String groupType){
	 this.groupType = groupType; 
	}

	/** 
	 * @param groupType  获取  班级类型
	 */
	public  String  getGroupType(){
	 return this.groupType; 
	}
	/** 
	 * @param importDate  设置  导入时间
	 */
	public void setImportDate( String importDate){
	 this.importDate = importDate; 
	}

	/** 
	 * @param importDate  获取  导入时间
	 */
	public  String  getImportDate(){
	 return this.importDate; 
	}
	/** 
	 * @param importStaff  设置  导入员工ID
	 */
	public void setImportStaff( int importStaff){
	 this.importStaff = importStaff; 
	}

	/** 
	 * @param importStaff  获取  导入员工ID
	 */
	public  int  getImportStaff(){
	 return this.importStaff; 
	}
}
