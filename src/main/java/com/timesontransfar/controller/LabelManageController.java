package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@RestController
@RefreshScope
public class LabelManageController {
	
	protected static Logger log = LoggerFactory.getLogger(LabelManageController.class);
	@Autowired
	private ILabelManageService LabelManageService;
	@Autowired
	private PubFunc PubFunc;

	/**
	 * Description: 标记省市总热线<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param parm
	 * @return <br>
	 * @CreateDate 2020年11月30日 下午6:36:28 <br>
	 */
	@RequestMapping(value = "/workflow/dynamic/updateHotlineFlag")
	public Object updateHotlineFlag(@RequestBody(required=false) String parm) {
		log.info("parm:{}",parm);
		JSONObject json = JSONObject.fromObject(parm);
		String orderId=json.optString("orderId");
		TsmStaff tsmStaff=PubFunc.getLogonStaff();
		int updateHotlineFlag = LabelManageService.updateHotlineFlag(orderId, Integer.parseInt(tsmStaff.getId()));
		log.info("修改工单 {} 标记省市总热线 打标结果：{}",orderId,(updateHotlineFlag==1?"成功":"失败"));
		return ResultUtil.success(updateHotlineFlag);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getHotlineGrid")
	public Object getHotlineGrid(@RequestBody(required=false) String parm) {
		return LabelManageService.getHotlineGrid(parm);	
	}
	
}
