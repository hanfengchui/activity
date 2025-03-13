package com.timesontransfar.controller;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.labelLib.service.ILabelService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RefreshScope
@SuppressWarnings("rawtypes")
public class LabelServiceController {
	protected Logger log = LoggerFactory.getLogger(LabelServiceController.class);
	
	@Autowired
	private ILabelService labelService;
	
	@PostMapping(value = "/workflow/labelService/queryLabelHandByUrl")
	public Map queryLabelHandByUrl(@RequestParam(value="url", required=true) String url) {
		return labelService.queryLabelHandByUrl(url);
	}
	
	@PostMapping(value = "/workflow/labelService/queryAllLabel")
	public Map queryAllLabel(
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="sheetId", required=true) String sheetId) {
		return labelService.queryAllLabel(orderId, sheetId);
	}

	/**
	 * 根据工单号查询所标签
	 * @param url
	 * @param orderId
	 * @return
	 */
	@PostMapping(value = "/workflow/labelService/queryLabelBySheetId")
	public Map queryLabelBySheetId(
			@RequestParam(value="url", required=true)String url,
			@RequestParam(value="workSheetId", required=true) String workSheetId) {
		return labelService.queryLabelBySheetId(url, workSheetId);
	}
	
	/**
	 * 保存标签实例
	 * @param ls
	 * @return
	 */
	@PostMapping(value = "/workflow/labelService/saveLabelInstance")
	public int saveLabelInstance(@RequestBody(required=true) String param) {
		JSONObject pjson = JSONObject.fromObject(param);
		if(pjson.size()==0) {
			log.error("标签实例为空");
			return 0;
		}
		
		JSONArray arr =pjson.getJSONArray("labelArr");
		LabelInstance[] instanceInfo=new LabelInstance[arr.size()];
		for (int i = 0; i < arr.size(); i++) {
			LabelInstance instance=(LabelInstance) JSONObject.toBean(arr.getJSONObject(i),LabelInstance.class);
			instanceInfo[i]=instance;
		}
		return labelService.saveLabelInstance(instanceInfo);
	}

	@PostMapping(value = "/workflow/labelService/getLabelCountByOrderId")
	public int getLabelCountByOrderId(@RequestParam(value = "orderId", required = true) String orderId) {
		return labelService.getLabelCountByOrderId(orderId);
	}

	@PostMapping(value = "/workflow/labelService/getLabelListByOrderId")
	public List getLabelListByOrderId(@RequestParam(value = "orderId", required = true) String orderId) {
		return labelService.getLabelListByOrderId(orderId);
	}
}