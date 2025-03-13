package com.timesontransfar.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.NoteSendList;

import net.sf.json.JSONObject;

@RestController
@RefreshScope
public class InoteSenListController {
	protected Logger log = LoggerFactory.getLogger(InoteSenListController.class);
	
	@Autowired
	private InoteSenList noteSenListDao;
	
	@PostMapping(value = "/workflow/noteSenListDao/saveNoteContent")
	public String saveNoteContent(@RequestParam(value="msg", required=true) String msg) {
		NoteSeand bean = (NoteSeand)JSONObject.toBean(JSONObject.fromObject(msg),NoteSeand.class);
		return noteSenListDao.saveNoteContent(bean) > 0 ? "success" : "fail";
	}
	
	@PostMapping(value = "/workflow/noteSenListDao/getNoteSendNum")
	public String getNoteSendNum(
			@RequestParam(value="orgId", required=true) String orgId,
			@RequestParam(value="relaId", required=true) String relaId,
			@RequestParam(value="tachId", required=true) int tachId,
			@RequestParam(value="queryType", required=true) int queryType) {
		return JSON.toJSONString(noteSenListDao.getNoteSendNum(orgId, relaId, tachId, queryType));
	}
	
	@RequestMapping(value = "/workflow/noteSenListDao/updateNoteList", method = RequestMethod.POST)
	public int updateNoteList(@RequestBody NoteSendList bean) {
		return noteSenListDao.updateNoteList(bean);
	}
	
	@RequestMapping(value = "/workflow/noteSenListDao/deleteNoteList", method = RequestMethod.POST)
	public int deleteNoteList(@RequestParam(value="guid", required=true)  String guid) {
		return noteSenListDao.deleteNoteList(guid);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/workflow/noteSenListDao/getNumInfo", method = RequestMethod.POST)
	public Map getNumInfo(@RequestParam(value="sourcenum", required=true) String sourcenum) {
		return noteSenListDao.getNumInfo(sourcenum);
	}
}