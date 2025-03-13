package com.timesontransfar.common.web.system.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.web.system.IRemarkPassword;

import cn.hutool.crypto.symmetric.AES;

public class RemarkPasswordImpl implements IRemarkPassword {
	protected static Logger log = LoggerFactory.getLogger(RemarkPasswordImpl.class);
	
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("pubJdbcTemplate")
	private JdbcTemplate pubjt;

	/**
	 * 将密码转换成ASSIIC码
	 * @param pass
	 * @return
	 */
	private String encodePwd(String pass) {
		char[] strchar = pass.toCharArray();
		StringBuilder newOPwd = new StringBuilder();
		int charLength = strchar.length;
		for(int i=0;i<charLength;i++) {
			int intChar =  strchar[i];
			if(i == 0) {
				newOPwd.append(String.valueOf(intChar));
			} else {
				newOPwd.append("-"+intChar);
			}
		}
		return newOPwd.toString();
	}
	
	//算法
    private static final String KEY = "wxwshi1ai2ndzdi3";

    /**
    * @Description:  AES加密
    * @Param: [content] 加密内容
    */
    private String enCode(String content) {
        try {
            AES aes = new AES(KEY.getBytes());
            return aes.encryptHex(content);
        } catch (Exception e) {
            log.error("门户密码加密异常：{}", e.getMessage(), e);
            return "";
        }
    }

	// 修改密码
	public boolean updateStaffPs(String logonname, String newPs) {
		String pwd = encodePwd(newPs);
		String psSql = "UPDATE TSM_STAFF SET PASSWORD=?, PWD_UPDATE_DATE=now(), LOGIN_COUNT=1 WHERE LOGONNAME = ?";
		int i = this.jdbcTemplate.update(psSql, pwd, logonname);
		
		String pubPwd = enCode(newPs);//门户密码
		String upSql = "update jscsc_ct_pub.t_pub_login t set t.password=?, t.create_time=now() where t.login_name=?";
		this.pubjt.update(upSql, pubPwd, logonname);
		
		return i > 0;
	}

	/**
	 * 判断输入密码和使用密码是否相同
	 * 
	 * @param logonname
	 * @param password
	 * @return
	 */
	public boolean isBeforePassword(String logonname, String password) {
		StringBuilder str = new StringBuilder();
  	  	for(int i=0; i < password.length(); i++) {
  	  		char c = password.charAt(i);
  	  		String s = String.valueOf((int)c);
  	  		str.append(s + "-");
  	  	}
  	  	String outPwd = str.substring(0, str.length()-1);
  	  	
  	  	String validSql = "select count(staff_id) from tsm_staff where logonname=? and password=?";
  		int validCount = this.jdbcTemplate.queryForObject(validSql, new Object[] { logonname, outPwd }, Integer.class);
  		return validCount > 0;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
