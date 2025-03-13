package com.timesontransfar.controller;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.web.system.ILoginDAO;
import com.timesontransfar.common.web.system.IRemarkPassword;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.systemPub.ISystemPubService;
import com.timesontransfar.systemPub.SystemPubQury;
import com.transfar.common.security.LoginBody;
import com.transfar.common.utils.AESCoder;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class SystemPubController {
	private static Logger log = LoggerFactory.getLogger(SystemPubController.class);
	
	@Autowired
	private ISystemPubService systemPubService;
	@Autowired
	private ILoginDAO isLogin;
	@Autowired
	private IRemarkPassword againLogin;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private SystemPubQury systemPubQury;
	
	@PostMapping(value = "/workflow/common/orgTree")
	public String orgTree(@RequestParam(name="flag",required=false) String flag,
			@RequestParam(name="parm",required=false) String parm) {
		String result=systemPubService.getSysttemOrgTree(flag,parm);
		return ResultUtil.success(result);
	}
	
	@PostMapping(value = "/workflow/common/orgTreeAuth")
	public String orgTreeAuth(@RequestParam(name="flag",required=false) String flag,
			@RequestParam(name="parm",required=false) String parm,
			@RequestParam(name="staffId",required=false) String staffId,
			@RequestParam(name="param",required=false) String param) {
		String result=systemPubService.getSysttemOrgTreeAuth(flag,parm,staffId,param);
		return ResultUtil.success(result);
	}
	
	@PostMapping(value = "/workflow/common/orgTreeCascader")
	public String orgTreeCascader(@RequestParam(name="sheetId",required=false) String sheetId) {
		String result = systemPubService.systemOrgTreeNew(sheetId);
		return ResultUtil.success(result);
	}
	
	@PostMapping(value = "/workflow/common/orgTreeLevel")
	public String orgTreeLevel() {
		String result=systemPubService.getOrgTreeLevel();
		return ResultUtil.success(result);
	}
	
	@PostMapping(value = "/workflow/common/getStaffByOrganId")
	public String getStaffByOrganId(@RequestParam(name="orgId",required=false) int orgId,
			@RequestParam(name="staffName",required=false) String staffName,
			@RequestParam(name="loginName",required=false) String loginName) {
		String result=systemPubService.getStaffByOrganId(orgId, staffName, loginName, null);
		return ResultUtil.success(result);
	}
	
	@PostMapping(value = "/workflow/common/getStaffByOrganIdNew")
	public String getStaffByOrganIdNew(@RequestParam(name="orgId",required=false) int orgId,
			@RequestParam(name="staffName",required=false) String staffName,
			@RequestParam(name="loginName",required=false) String loginName,
			@RequestParam(name="sheetId",required=false) String sheetId) {
		String result=systemPubService.getStaffByOrganId(orgId, staffName, loginName, sheetId);
		return ResultUtil.success(result);
	}
	
	@PostMapping(value = "/workflow/common/channlAskInfoTree")
	public String channlAskInfoTree() {
		String result=systemPubService.channlAskInfoTree();
		return ResultUtil.success(result);
	}
	
	@PostMapping(value = "/workflow/common/reasonTree")
	public String reasonTree() {
		return systemPubService.reaSonTree();
	}
	
	@PostMapping(value = "/workflow/common/pubColomndefaultTree")
	public String pubColomndefaultTree(@RequestParam(name="referId",required=false) int referId) {
		return systemPubService.getPubColomndefaultTree(referId);
	}
	
	@PostMapping(value = "/workflow/common/auxiliaryToolMuen")
	public String pubColomndefaultTree() {
		return systemPubService.auxiliaryToolMuen();
	}
	
	@PostMapping(value = "/workflow/common/specificOrgInfo")
	public String specificOrgInfo(@RequestParam(name="orgId",required=false) String orgId) {
		return systemPubService.specificInfoService(orgId);
	}
	
	@PostMapping(value = "/workflow/common/zeRenOrgFlag")
	public boolean zeRenOrgFlag(@RequestParam(name="referId",required=false) String referId,
			@RequestParam(name="revOrgId",required=false) String revOrgId) {
		return systemPubService.zerenOrgFlag(referId,revOrgId);
	}
	
	@PostMapping(value = "/workflow/common/sendRestUserCaptcha")
	public String sendRestUserCaptcha(@RequestBody String parm) {
		log.info("sendRestUserCaptcha: {}",parm);
		JSONObject json=JSONObject.fromObject(parm);
		return systemPubService.sendRestUserCaptcha(json.optString("loginName"),json.optInt("captchaType"));
	}
	
	@PostMapping(value = "/workflow/common/validationUserCaptcha")
	public String validationUserCaptcha(@RequestBody String parm) {
		log.info("validationUserCaptcha: {}", parm);
		JSONObject json=JSONObject.fromObject(parm);
		String oldCode=json.optString("oldCode");
		String sb = AESCoder.encrypt(json.optString("newCode"));
		if (!sb.equals(oldCode)) {
			return ResultUtil.error("验证码错误!");
		}
		return systemPubService.validationUserCaptcha(json.optString("guid"), json.optString("loginName"), json.optString("cahtcha"));
	}
	
	@PostMapping(value = "/workflow/common/restPasswd")
	public String restPasswd(@RequestBody LoginBody loginBody){
		String checkPass=isLogin.checkPassWithGroup(loginBody.getUsername(), loginBody.getPassword());
		String result="";
		if(checkPass.equals("14")) {
			result="密码必须为8~16位字符";
		} else if (checkPass.equals("15")) {
			result="密码中需同时包括字母、数字、特殊字符";
		}else if (checkPass.equals("16")) {
			result="密码中不能连续出现3个或3个以上相邻/同字符";
		} else if (checkPass.equals("17")) {
			result="密码中不能连续出现3个或3个以上相邻/同字符";
		} else if (checkPass.equals("18")) {
			result="密码中不能连续出现3个或3个以上相邻/同字符";
		} else if (checkPass.equals("19")) {
			result="密码中不能包含个人信息（姓名首字母缩写、联系电话、登录名）";
		} else if (checkPass.equals("20")) {
			result="密码中不能包含空格";
		}
		if(StringUtils.isEmpty(result)){
			boolean isFlag=againLogin.isBeforePassword(loginBody.getUsername(), loginBody.getPassword());
			if(!isFlag){
				againLogin.updateStaffPs(loginBody.getUsername(), loginBody.getPassword());
				return ResultUtil.success();
			}
			result="新旧密码不允许相同请重新输入";
		}
		return ResultUtil.error(result);
	}
	
	@PostMapping(value = "/workflow/common/chanagePasswd")
	public String chanagePasswd(@RequestBody String json){
		JSONObject obj=JSONObject.fromObject(json);
		String oldPassword=obj.optString("oldPassword");
		String newPassword=obj.optString("newPassword");
		String loginName=obj.optString("loginName");
		if(com.transfar.common.utils.StringUtils.isEmpty(loginName)){
			TsmStaff tsmStaff=pubFunc.getLogonStaff();
			loginName=tsmStaff.getLogonName();
		}
		boolean isOldFlag=againLogin.isBeforePassword(loginName, oldPassword);
		if(!isOldFlag){
			return ResultUtil.error("输入的旧密码不正确");
		}
		String checkPass=isLogin.checkPassWithGroup(loginName, newPassword);
		String result="";
		if(checkPass.equals("14")) {
			result="密码必须为8~16位字符";
		} else if (checkPass.equals("15")) {
			result="密码中需同时包括字母、数字、特殊字符";
		}else if (checkPass.equals("16")) {
			result="密码中不能连续出现3个或3个以上相邻/同字符";
		} else if (checkPass.equals("17")) {
			result="密码中不能连续出现3个或3个以上相邻/同字符";
		} else if (checkPass.equals("18")) {
			result="密码中不能连续出现3个或3个以上相邻/同字符";
		} else if (checkPass.equals("19")) {
			result="密码中不能包含个人信息（姓名首字母缩写、联系电话、登录名）";
		} else if (checkPass.equals("20")) {
			result="密码中不能包含空格";
		}
		if(StringUtils.isEmpty(result)){
			boolean isFlag=againLogin.isBeforePassword(loginName, newPassword);
			if(!isFlag){
				againLogin.updateStaffPs(loginName, newPassword);
				return ResultUtil.success();
			}
			result="新旧密码不允许相同请重新输入";
		}
		return ResultUtil.error(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getStaffByOrgId")
	public Object getStaffByOrgId(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		List staffByOrgId = systemPubQury.getStaffByOrgId(json.optString("orgId"));
		return ResultUtil.success(staffByOrgId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/loadFourMuLuTree")
	public Object loadFourMuLuTree(@RequestBody(required=false) String parm) {
		String fourTree = systemPubService.createFourMuLuTree();
		return ResultUtil.success(fourTree);
	}
	
	@RequestMapping(value = "/workflow/dynamic/loadColumnsByEntity")
	public Object loadColumnsByEntity(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String systemTree = systemPubService.loadColumnsByEntity(json.optString("tableCode"),json.optString("colCode"),json.optString("entity"));
		return ResultUtil.success(systemTree);
	}

	@RequestMapping(value = "/workflow/dynamic/getColumnsByCode")
	public Object getColumnsByCode(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String systemTree = systemPubService.getColumnsByCode(json.optString("tableCode"),json.optString("colCode"),json.optString("entity"));
		return ResultUtil.success(systemTree);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getSystemTreeRootNew")
	public Object getSystemTreeRootNew(@RequestBody(required = false)String param) {
		JSONObject obj=JSONObject.fromObject(param);
		String flag=obj.optString("flag");
		String parm=obj.optString("parm");
		String skillOrgTree = systemPubService.skillOrgTree(flag,parm);
		return ResultUtil.success(skillOrgTree);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getXcDispatchOrg")
	public Object getXcDispatchOrg(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String sheetId = json.optString("sheetId");
		String orgId = systemPubService.getXcDispatchOrg(sheetId);
		return ResultUtil.success(orgId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/loadColumnsByEntityNew")
	public Object loadColumnsByEntityNew(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String systemTree = systemPubService.loadColumnsByEntityNew(json.optString("tableCode"),json.optString("colCode"),json.optString("entity"));
		return ResultUtil.success(systemTree);
	}

	@RequestMapping(value = "/workflow/dynamic/addColumnsReference")
	public int addColumnsReference( @RequestBody String parm) {
		return systemPubService.addColumnsReference(parm);
	}

	@RequestMapping(value = "/workflow/dynamic/updateColumnsReference")
	public int updateColumnsReference( @RequestBody String parm) {
		return systemPubService.updateColumnsReference(parm);
	}

	@RequestMapping(value = "/workflow/dynamic/delColumnsReference")
	public int delColumnsReference( @RequestBody String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String referId = json.optString("ReferId");
		return systemPubService.delColumnsReference(referId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getColumnsByCodeBuop")
	public Object getColumnsByCodeBuop(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String systemTree = systemPubService.getColumnsByCodeBuop(json.optString("tableCode"),json.optString("colCode"),json.optString("entity"));
		return ResultUtil.success(systemTree);
	}

	@RequestMapping(value = "/workflow/dynamic/getCompletionReason")
	public Object getCompletionReason(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String reason = pubFunc.getCompletionReason(json.optString("N"),json.optString("N_ID"),json.optString("ID"));
		return ResultUtil.success(reason);
	}

	@RequestMapping(value = "/workflow/dynamic/addCompletionReason")
	public int addCompletionReason( @RequestBody String parm) {
		return pubFunc.addCompletionReason(parm);
	}

	@RequestMapping(value = "/workflow/dynamic/updateCompletionReason")
	public int updateCompletionReason( @RequestBody String parm) {
		return pubFunc.updateCompletionReason(parm);
	}

	@RequestMapping(value = "/workflow/dynamic/delCompletionReason")
	public int delCompletionReason( @RequestBody String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String  id= json.optString("N_ID");
		return pubFunc.delCompletionReason(id);
	}
	
}
