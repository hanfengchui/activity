package com.timesontransfar.controller;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.web.system.ILoginDAO;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.menu.IMenuService;
import com.transfar.common.security.LoginBody;

@RestController
@RefreshScope
public class LoginController {
	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@Autowired
	private ILoginDAO  isLogin;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
    private HttpServletRequest request;
	@Autowired
	private IMenuService menuService;
	
	@PostMapping(value = "/workflow/login/loadLeftMenu")
	public String loadLeftMenu(){
		TsmStaff staff = pubFunc.getLogonStaff();
		String res = "";
		if(null != staff) {
			res = menuService.loadLeftMenu(staff.getLogonName());
			if(null != res) {
				log.debug("loadRedisMenu: \n{}", res);
			}
		}
		return res;
	}
	
	@PostMapping(value = "/workflow/login/refreshMenu")
	public String refreshMenu(){
		TsmStaff staff = pubFunc.getLogonStaff();
		String res = "";
		if(null != staff) {
			res = menuService.refreshMenu(staff.getLogonName());
			if(null != res) {
				log.info("deleteRedisMenu: \n{}",res);
			}
		}
		return res;
	}
	
	@PostMapping(value = "/workflow/login/loadNewAuthorization")
	public String loadNewAuthorization(){
		TsmStaff staff = pubFunc.getLogonStaff();
		String res = "";
		if(null != staff) {
			log.info("loadNewAuthorization logonName：{}", staff.getLogonName());
			res = isLogin.loadNewAuthorization(staff.getLogonName());
			if(null != res) {
				log.info("loadNewAuthorization: {}",res);
			}
		}
		return res;
	}
	
	/**
	 * 密码验证功能
	 * Description: <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param username
	 * @param password
	 * @return <br>
	 * @CreateDate 2020年8月12日 上午11:29:36 <br>
	 */
	@RequestMapping("/workflow/login/validateUserNew")
	public String validateUserNew(@RequestBody LoginBody loginBody) {
		//记录登录日志
		this.pubFunc.saveLoginLog(request, loginBody.getUsername());
		//用户密码校验,返回登录信息
		return isLogin.validateUserNew(loginBody.getUsername(), loginBody.getPassword(), request);
	}
	
	@RequestMapping("/workflow/login/saveLoginLog")
	public int saveLoginLog(@RequestParam(value="logonName", required=true) String logonName) {
		//记录登录日志
		return this.pubFunc.saveLoginLog(request, logonName);
	}
}