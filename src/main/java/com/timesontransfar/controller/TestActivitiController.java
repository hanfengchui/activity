package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
//@RefreshScope
public class TestActivitiController {
	protected Logger log = LoggerFactory.getLogger(TestActivitiController.class);
	
	
	@GetMapping(value = "/active/getId")
    public String getId() {
		return "gate网关请求通用配置成功";
	}
	
}
