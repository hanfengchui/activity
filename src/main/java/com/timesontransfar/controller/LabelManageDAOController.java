package com.timesontransfar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;

@RestController
@RefreshScope
public class LabelManageDAOController {
	
	@Autowired
	private ILabelManageDAO labelManageDAO;
	
	@RequestMapping(value = "/workflow/labelManageDAO/updateUpgradeIncline", method = RequestMethod.POST)
	public int updateUpgradeIncline(@RequestParam(value="serviceId", required=true) String serviceId,@RequestParam(value="upgradeIncline", required=true) int upgradeIncline) {
		return labelManageDAO.updateUpgradeIncline(serviceId, upgradeIncline);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/updateValidFlag", method = RequestMethod.POST)
	public int updateValidFlag(@RequestParam(value="serviceId", required=true) String serviceId,@RequestParam(value="valiFlag", required=true) int valiFlag) {
		return labelManageDAO.updateValidFlag(serviceId, valiFlag);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/insertNew", method = RequestMethod.POST)
	public int insertNew(@RequestParam(value="serviceId", required=true) String serviceId) {
		return labelManageDAO.insertNew(serviceId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/updateForceCfmFlag", method = RequestMethod.POST)
	public int updateForceCfmFlag(@RequestParam(value="serviceId", required=true)String serviceId, @RequestParam(value="forceId", required=true)String forceId) {
		return labelManageDAO.updateForceCfmFlag(serviceId, forceId);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/saveFormalAnswerDate", method = RequestMethod.POST)
	public int saveFormalAnswerDate(@RequestParam(value="serviceId", required=true)String serviceId) {
		return labelManageDAO.saveFormalAnswerDate(serviceId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/queryServiceLabelById", method = RequestMethod.POST)
	public ServiceLabel queryServiceLabelById(@RequestParam(value="serviceId", required=true)String serviceId,@RequestParam(value="hisFlag", required=true) boolean hisFlag) {
		return this.labelManageDAO.queryServiceLabelById(serviceId, hisFlag);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/queryFinishDate", method = RequestMethod.POST)
	public String queryFinishDate(@RequestParam(value="serviceId", required=true)String serviceId) {
		return this.labelManageDAO.queryFinishDate(serviceId);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/saveLabelHisById", method = RequestMethod.POST)
	public int saveLabelHisById(@RequestParam(value="serviceId", required=true)String serviceId) {
		return this.labelManageDAO.saveLabelHisById(serviceId);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/updateValidHastenNum", method = RequestMethod.POST)
	public int updateValidHastenNum(@RequestParam(value="serviceId", required=true)String serviceId,@RequestParam(value="num", required=true) int num) {
		return this.labelManageDAO.updateValidHastenNum(serviceId, num);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/updateHotlineFlag", method = RequestMethod.POST)
	public int updateHotlineFlag(
			@RequestParam(value="serviceOrderId", required=true)String serviceOrderId, 
			@RequestParam(value="staffId", required=true) int staffId, 
			@RequestParam(value="hotlineFlag", required=true)int hotlineFlag) {
		return this.labelManageDAO.updateHotlineFlag(serviceOrderId, staffId, hotlineFlag);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/updateDealResult", method = RequestMethod.POST)
	public int updateDealResult(
			@RequestParam(value="serviceOrderId", required=true)String serviceOrderId, 
			@RequestParam(value="dealResult", required=true)int dealResult, 
			@RequestParam(value="dealResultName", required=true)String dealResultName) {
		return this.labelManageDAO.updateDealResult(serviceOrderId, dealResult, dealResultName);
	}
	
	@PostMapping(value = "/workflow/labelManageDAO/saveSecFlag")
	public int saveSecFlag(@RequestParam(value="serviceId", required=true)String serviceId) {
		return this.labelManageDAO.saveSecFlag(serviceId);
	}
	
	@RequestMapping(value = "/workflow/labelManageDAO/updateUnusualFlag", method = RequestMethod.POST)
	public int updateUnusualFlag(@RequestParam(value="serviceId", required=true)String serviceId) {
		return this.labelManageDAO.updateUnusualFlag(serviceId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/updateFirstAuditDate", method = RequestMethod.POST)
	public int updateFirstAuditDate(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.updateFirstAuditDate(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/selectFirstAuditDate", method = RequestMethod.POST)
	public String selectFirstAuditDate(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.selectFirstAuditDate(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/updateDealHours", method = RequestMethod.POST)
	public int updateDealHours(@RequestParam(value = "dealHours", required = true) int dealHours,
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.updateDealHours(dealHours, serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/selectDealHours", method = RequestMethod.POST)
	public int selectDealHours(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.selectDealHours(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/updateAuditHours", method = RequestMethod.POST)
	public int updateAuditHours(@RequestParam(value = "auditHours", required = true) int auditHours,
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.updateAuditHours(auditHours, serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/selectAuditHours", method = RequestMethod.POST)
	public int selectAuditHours(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.selectAuditHours(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/updateIsUnified", method = RequestMethod.POST)
	public int updateIsUnified(@RequestParam(value = "isUnified", required = true) int isUnified,
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.updateIsUnified(isUnified, serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/selectIsUnified", method = RequestMethod.POST)
	public int selectIsUnified(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.selectIsUnified(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/labelManageDAO/selectAutoVisitFlag", method = RequestMethod.POST)
	public int selectAutoVisitFlag(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return this.labelManageDAO.selectAutoVisitFlag(serviceOrderId);
	}
}