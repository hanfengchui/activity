package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.complaintservice.service.ICliqueService;

@RestController
@RefreshScope
public class CliqueServiceController {
	protected Logger log = LoggerFactory.getLogger(CliqueServiceController.class);
	
	@Autowired
	private ICliqueService cliqueService;
	
	@PostMapping(value = "/workflow/clique/updateApplyAuto")
	public int updateApplyAuto(String applyGuid, String sheetId, int auditStatu, String auditReason) {
		return cliqueService.applyAuto(applyGuid, sheetId, auditStatu, auditReason);
	}

}
