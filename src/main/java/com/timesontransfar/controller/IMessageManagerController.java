package com.timesontransfar.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.pojo.TipsPojo;
import com.timesontransfar.customservice.common.message.service.IMessageManager;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;

import net.sf.json.JSONObject;

@RestController
@RefreshScope
@SuppressWarnings("rawtypes")
public class IMessageManagerController {
	protected Logger log = LoggerFactory.getLogger(IMessageManagerController.class);
	
	@Autowired
	private IMessageManager messageManager;
	
	
	@PostMapping(value = "/workflow/messageManager/getAllMsgSheet")
	public GridDataInfo getAllMsgSheet(@RequestBody(required=true) Map param) {
		int begion = Integer.parseInt(param.get("begion").toString());
		int pageSize = Integer.parseInt(param.get("pageSize").toString());
		return messageManager.getAllMsgSheet(begion, pageSize);
	}

	@PostMapping(value = "/workflow/messageManager/getAllMegCount")
	public int getAllMegCount() {
		return messageManager.getAllMegCount();
	}

	@PostMapping(value = "/workflow/messageManager/getNotifyMsg")
	public String getNotifyMsg() {
		return messageManager.getNotifyMsg();
	}


	@PostMapping(value = "/workflow/messageManager/gotoHisById")
	public int gotoHisById(@RequestParam(value="orgId", required=true) String guid) {
		return messageManager.gotoHisById(guid);
	}


	@PostMapping(value = "/workflow/messageManager/updateReadedFlagByStaff")
	public void updateReadedFlagByStaff() {
		 messageManager.updateReadedFlagByStaff();
	}

	@PostMapping(value = "/workflow/messageManager/updateReadedFlagByGuid")
	public int updateReadedFlagByGuid(@RequestParam(value="guid", required=true) String guid) {
		 return messageManager.updateReadedFlagByGuid(guid);
	}

	@PostMapping(value = "/workflow/messageManager/createMsgPrompt")
	public String createMsgPrompt(@RequestBody String msg) {
		MessagePrompt m = (MessagePrompt)JSONObject.toBean(JSONObject.fromObject(msg),MessagePrompt.class);
		return messageManager.createMsgPrompt(m);
	}

	@PostMapping(value = "/workflow/messageManager/savePersonRemind")
	public int savePersonRemind(@RequestParam(value="staffId", required=true) int staffId,
								@RequestParam(value="remindTitle", required=true) String remindTitle,
								@RequestParam(value="remindContent", required=true) String remindContent,
								@RequestParam(value="remindTime", required=true) String remindTime,
								@RequestParam(value="remind_type", required=true) int remindType) {
		
		TipsPojo t = new TipsPojo();
		t.setStaffId(staffId);
		t.setRemindTime(remindTime);
		t.setRemindContent(remindContent);
		t.setRemindType(remindType);
		t.setRemindTitle(remindTitle);
		return messageManager.savePersonRemind(t);
	}

	@PostMapping(value = "/workflow/messageManager/getPersonRemind")
	public List getPersonRemind(@RequestParam(value="staffId", required=true) int staffId) {
		
		return messageManager.getPersonRemind(staffId);
	}

	@PostMapping(value = "/workflow/messageManager/saveBusinessRemind")
	public int saveBusinessRemind( @RequestParam(value="orgId", required=true) String orgId,
									@RequestParam(value="staffId", required=true) String staffId,
									@RequestParam(value="staffName", required=true) String staffName,
									@RequestParam(value="remindTitle", required=true) String remindTitle,
									@RequestParam(value="remind_type", required=true) String remindType,
									@RequestParam(value="remindContent", required=true) String remindContent,
									@RequestParam(value="selecteOrg", required=true) String selecteOrg) {
		TipsPojo t = new TipsPojo();
		t.setCreateStaffId(Integer.parseInt(staffId));
		t.setCreateStaffName(staffName);
		t.setRemindTitle(remindTitle);
		t.setRemindType(Integer.parseInt(remindType));
		t.setRemindContent(remindContent);

		return messageManager.saveBusinessRemind(t,orgId,selecteOrg);
	}

	@PostMapping(value = "/workflow/messageManager/getBusinessRemind")
	public List getBusinessRemind( @RequestParam(value="orgId", required=true) String orgId) {
		return messageManager.getBusinessRemind(orgId);
	}

	@PostMapping(value = "/workflow/messageManager/removeRemind")
	public int removeRemind( @RequestParam(value="remind_id", required=true) String remindId,
							@RequestParam(value="flag", required=true) String flag) {
		return messageManager.removeRemind(remindId, flag);
	}
}
