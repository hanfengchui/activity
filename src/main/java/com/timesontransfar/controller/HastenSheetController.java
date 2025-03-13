package com.timesontransfar.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.timesontransfar.customservice.dbgridData.IDepartmentCount;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;
import com.timesontransfar.customservice.worksheet.service.IhastenSheet;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.web.ResultUtil;

@RestController
@SuppressWarnings("rawtypes")
public class HastenSheetController {
	protected static Logger log = LoggerFactory.getLogger(HastenSheetController.class);
	
	@Autowired
	private IhastenSheet hastenSheetServ;
	@Autowired
	private IDepartmentCount departmentCountImpl;
	
	/**
	 * 执行催单动作
	 */
	@PostMapping(value = "/workflow/hastenSheet/sendHastenSheet")
	@Transactional
	public String sendHastenSheet(@RequestParam(value = "hastenSheetInfo", required = true) String hastenSheetInfoJson) {
		log.info("sendHastenSheet 入参: {}", hastenSheetInfoJson);
		JsonObject json = new Gson().fromJson(hastenSheetInfoJson, JsonObject.class);
		HastenSheetInfo hastenSheet = new Gson().fromJson(json.getAsJsonObject("hastenSheetInfo"), HastenSheetInfo.class);
		
		String orderId = hastenSheet.getOrderId();
		int hastenCondition = hastenSheetServ.getHastenCondition(orderId);
		if (hastenCondition > 0) {
			return ResultUtil.error("当前工单状态/受理渠道不能催单！");
		}
		String newOrderId = hastenSheetServ.getRefunded(orderId);
		if (StringUtils.isNotEmpty(newOrderId)) {
			return ResultUtil.error("请在关联跟踪单" + newOrderId + "上进行催单。");
		}
		
		String result = ResultUtil.fail(ResultEnum.FALL);
		try {
			if (json.get("sendFlag").getAsBoolean()) {
				JsonObject sendMassage = json.getAsJsonObject("sendMassageInfo");
				//短信发送逻辑处理
				int sendFlag = departmentCountImpl.sendMassage(sendMassage.get("phoneType").getAsInt(), sendMassage.get("phoneValue").getAsString(), sendMassage.get("message").getAsString(),
						hastenSheet.getOrderId(), hastenSheet.getWorkSheetId(), "催单");
				log.info("催单短信记录插入表：{}", (sendFlag > 0 ? "成功" : "失败"));
			}
			int hastenFlag = hastenSheetServ.sendHastenSheet(hastenSheet, json.get("syncFlag").getAsBoolean());
			if (hastenFlag > 0) {
				result = ResultUtil.success();
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw, true));
			log.error("工单催单出现异常，请求参数：{}，出现错误信息：{}", hastenSheetInfoJson, sw);
			result = ResultUtil.error(e.getMessage());
		}
		return result;
	}

    @PostMapping(value = "/workflow/hastenSheet/getHastenCondition")
    public int getHastenCondition(@RequestParam(value="orderId", required=true) String orderId) {
        return hastenSheetServ.getHastenCondition(orderId);
    }

    @PostMapping(value = "/workflow/hastenSheet/getRefunded")
    public String getRefunded(@RequestParam(value="orderId", required=true) String orderId) {
        return hastenSheetServ.getRefunded(orderId);
    }
	
	/**
	 * 业务接口 坐席商机管理平台催单
	 * @return
	 */	
	@PostMapping(value = "/workflow/hastenSheet/sendSyncHasten")
	public String sendSyncHasten(@RequestBody String hastenJson) {
		return hastenSheetServ.sendSyncHasten(hastenJson);
	}
	
	@PostMapping(value = "/workflow/hastenSheet/qryHastenList")
	public HastenSheetInfo[] qryHastenList(@RequestBody Map map) {
		String serId=map.get("serId").toString();
		String flag=map.get("flag").toString();
		return hastenSheetServ.qryHastenList(serId, flag);
	}

}
