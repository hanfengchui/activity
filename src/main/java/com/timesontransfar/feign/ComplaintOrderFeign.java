package com.timesontransfar.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.transfar.common.config.FeignConfiguration;

@FeignClient(value = "custom-interface", contextId = "ComplaintOrderFeign", qualifier = "ComplaintOrderFeign", configuration = FeignConfiguration.class)
public interface ComplaintOrderFeign {

	@PostMapping(value = "/interface/order/saveAppealInfo", consumes = "application/json;charset=utf-8", produces = "application/json;charset=utf-8")
	public String saveAppealInfo(@RequestBody String reqJson);
	
	@PostMapping(value = "/interface/order/updateAppealInfo", consumes = "application/json;charset=utf-8", produces = "application/json;charset=utf-8")
	public String updateAppealInfo(@RequestBody String reqJson);

}