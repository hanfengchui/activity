package com.timesontransfar.common.web.system.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.web.system.ILoginDAO;
@SuppressWarnings("rawtypes")
public class LoginImpl implements ILoginDAO {

	private JdbcTemplate jdbcTemplate;

	private ISystemAuthorization systemAuthorization;

	@Autowired
	private RedisUtils redisUtils;
	
    
	/**
	 * 判断用户登陆信息
	 */
	public String validateUserNew(String userName, String password, HttpServletRequest request) {
		String info = " ";
		try {
			boolean isExist = isUserExist(userName);// 判断员工是否存在
			if (isExist) {
				// 密码校验
				info = isUserPas(userName, password);
				if (info.equals("true")) {// 作判断
					String cpwg = checkPassWithGroup(userName, password);
					if (cpwg.equals("0")) {
						HttpSession session = request.getSession(true);
						session.setAttribute("TokenId", info);// 保存密码验证后返回的字符串
						
						// 装载staffPermit对象
						systemAuthorization.loadAuthorization(userName, request.getSession());
					} else {
						return cpwg;
					}
				} else {// 密码错误
					return "12";
				}
			} else {
				info = "11";// 用户不存在，请用正确的用户名登陆系统
			}
			return info;
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	public String checkPassWithGroup(String userName, String password) {
		if (password.length() > 16 || password.length() < 8) {// 密码长度大于等于8位小于 16位
			return "14";
		} else {
			if (password.indexOf(" ") >= 0) {
				return "20";
			}
			boolean haveDigit = false;
			boolean haveLetter = false;
			boolean haveOther = false;
			boolean sameSymbol = false;// 密码中不能包含连续两个以上相同的字符/数字
			boolean increaseSymbol = false;// 密码中不能包含连续两个以上递增的字符/数字
			boolean discreaseSymbol = false;// 密码中不能包含连续两个以上递减的字符/数字
			char[] chars = password.toCharArray();
			for (int i = 0; i < chars.length; i++) {
				int c = chars[i];
				if (isDigit(c)) {// 数字
					haveDigit = true;
				}
				if (isLetter(c)) {// 字母
					haveLetter = true;
				}
				if (isOther(c)) {// 特殊字符
					haveOther = true;
				}
				if (i < chars.length - 2) {
					if ( chars[i] == chars[i + 1] && chars[i + 1] == chars[i + 2]) {
						sameSymbol = true;
					}
					if ( chars[i] + 1 == chars[i + 1] && chars[i + 1] + 1 == chars[i + 2]) {
						increaseSymbol = true;
					}
					if ( chars[i] - 1 == chars[i + 1] && chars[i + 1] - 1 == chars[i + 2]) {
						discreaseSymbol = true;
					}
				}
			}
			if (!haveDigit || !haveLetter || !haveOther) {// 密码中需同时包含字母(不限制大小写)、数字、特殊字符
				return "15";
			} else if (sameSymbol) {
				return "16";
			} else if (increaseSymbol) {
				return "17";
			} else if (discreaseSymbol) {
				return "18";
			} else if (isInclude(userName, password.toUpperCase())) {// 密码中不能包含登录名、联系电话、姓名拼音
				return "19";
			} else if (!isPwdValid(userName)) {// 密码已经超过有效期
				return "21";
			} else {
				return "0";
			}
		}
	}

	// 0~9 ASCII：48～57
	private boolean isDigit(int ascii) {
		if (ascii > 47 && ascii < 58) {
			return true;
		}
		return false;
	}

	// a~z 97~122 A~Z 65~90
	private boolean isLetter(int ascii) {
		boolean isLower = (ascii > 96 && ascii < 123);
		boolean isUpper = (ascii > 64 && ascii < 91);
		if (isLower || isUpper) {
			return true;
		}
		return false;
	}

	private boolean isOther(int ascii) {
		boolean isOther = (ascii > 47 && ascii < 58);
		boolean isLower = (ascii > 96 && ascii < 123);
		boolean isUpper = (ascii > 64 && ascii < 91);
		if (!isOther && !isLower && !isUpper) {
			return true;
		}
		return false;
	}

	private boolean isInclude(String userName, String password) {
		String sql = "SELECT COUNT(1) FROM tsm_staff WHERE logonname = '" + userName + "' AND (('" + password + "' LIKE CONCAT('%',UPPER(nickname),'%') AND nickname IS NOT NULL AND nickname != '') " +
				"OR '" + password + "' LIKE CONCAT('%',UPPER(logonname),'%') OR ('" + password + "' LIKE CONCAT('%',UPPER(relaphone),'%') AND LENGTH(relaphone) > 7))";
		return this.jdbcTemplate.queryForObject(sql,Integer.class) == 1;
	}

	/**
	 * 判断用户名密码是否正确
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	public String isUserPas(String userName, String password) {
		StringBuilder str = new StringBuilder();
  	  	for(int i=0; i < password.length(); i++) {
  	  		char c = password.charAt(i);
  	  		String s = String.valueOf((int)c);
  	  		str.append(s + "-");
  	  	}
  	  	String outPwd = str.substring(0, str.length()-1);
  	  	
  	  	String validSql = "select count(staff_id) from tsm_staff where logonname=? and password=?";
  		int validCount = this.jdbcTemplate.queryForObject(validSql, new Object[] { userName, outPwd }, Integer.class);
  		return validCount > 0 ? "true" : "false";
	}

	/**
	 * 判断密码是否有效
	 * 
	 * @param logonname
	 * @return
	 */
	public boolean isPwdValid(String logonname) {
		String sql = "SELECT TIMESTAMPDIFF(DAY, S.PWD_UPDATE_DATE, NOW()) LIMIT_DAY FROM TSM_STAFF S WHERE S.LOGONNAME = ? AND S.STATE=8";
		List tmp = this.jdbcTemplate.queryForList(sql, logonname);
		Map map = (Map)tmp.get(0);
		if(null==map.get("LIMIT_DAY")){
			return false;
		}
		String limitDay = map.get("LIMIT_DAY").toString();
		
		String sqlFlag = "select case when(t.value1-?)>0 then 1 else 0 end flag from CCS_ST_APPSETTINGS t where t.key1='PWD_EXP_DATE' and t.switch=1";
		List listFlag = this.jdbcTemplate.queryForList(sqlFlag, limitDay);
		if(listFlag.isEmpty()){
			return true;
		}
		
		String flag = ((Map)listFlag.get(0)).get("FLAG").toString();
		
		return "1".equals(flag);
	}

	/**
	 * 判断员工是否存在
	 * 
	 * @param logonname
	 * @return
	 */
	public boolean isUserExist(String logonname) {
		String staffExistSql = "SELECT COUNT(*) FROM TSM_STAFF WHERE LOGONNAME = ? AND STATE='8'";
		int staffExistCount = this.jdbcTemplate.queryForObject(staffExistSql, new Object[] { logonname },Integer.class);
		return staffExistCount > 0;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public ISystemAuthorization getSystemAuthorization() {
		return systemAuthorization;
	}

	public void setSystemAuthorization(ISystemAuthorization systemAuthorization) {
		this.systemAuthorization = systemAuthorization;
	}

	public boolean isStaffState(String logonname) {
		if (logonname == null) {
			return false;
		}
		String sqlCon = "SELECT COUNT(*) FROM TSM_STAFF WHERE LOGONNAME = ? AND STATE = 8";
		int count = this.jdbcTemplate.queryForObject(sqlCon, new Object[] { logonname },Integer.class);
		return count > 0;
	}
	
	public String loadNewAuthorization(String logonname) {
		try {
			this.redisUtils.del(RedisType.WORKSHEET,"LOGONNAME__"+logonname);//删除登录工号缓存
			this.redisUtils.del(RedisType.WORKSHEET,"TsmStaff__loginName_"+logonname);//删除员工缓存
			this.redisUtils.batchDelByPreKey("ROLEPERMIT__",RedisType.WORKSHEET);//删除角色权限
			this.redisUtils.batchDelByPreKey("MENU__",RedisType.WORKSHEET);//删除菜单
			this.redisUtils.batchDelByPreKey("URL__",RedisType.WORKSHEET);
			this.redisUtils.batchDelByPreKey("FUNC__",RedisType.WORKSHEET);
			this.redisUtils.batchDelByPreKey("ATTR__",RedisType.WORKSHEET);
			this.redisUtils.batchDelByPreKey("CTRL__",RedisType.WORKSHEET);
			this.redisUtils.batchDelByPreKey("POP__",RedisType.WORKSHEET);
		} catch (Exception e) {
			e.printStackTrace();
			return "fail";
		}
    	
		systemAuthorization.loadNewAuthorization(logonname, null);
		return "success";
	}

}
