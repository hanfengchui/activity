package com.timesontransfar.controller;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.service.IserviceComplaintInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServiceComplaintInfoController {

	@Autowired
	private IserviceComplaintInfo iserviceComplaintInfo;

	@PostMapping(value = "/workflow/serviceComplaintInfo/getData")
	public GridDataInfo getData(@RequestParam(value = "acceptTime", required = true) String acceptTime,
								@RequestParam(value = "ipAddress", required = true) String ipAddress,
								@RequestParam(value = "currentPage", required = true) int currentPage,
								@RequestParam(value = "pageSize", required = true) int pageSize,
								@RequestParam(value = "activeName", required = true) String activeName) {
		return iserviceComplaintInfo.getData(acceptTime, ipAddress,currentPage,pageSize,activeName);
	}
}