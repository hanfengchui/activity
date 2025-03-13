package com.timesontransfar.controller;

import com.timesontransfar.common.utils.JwtUtils;
import com.timesontransfar.customservice.common.PubFunc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class SingleUrlController {
	
	protected Logger log = LoggerFactory.getLogger(SingleUrlController.class);
	
	@Autowired
	private PubFunc pubFunc;

	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/SingleUrl/getSingleUrl")
	public String getSingleUrl(@RequestParam(value="logonName", required=true) String logonName){
		log.info("getSingleUrl: logonName is {}", logonName);
		String singleUrl = "";
		try{
			Map intfMap = pubFunc.getIntfMap("getSingleUrlToMyOtq");
	        if(null == intfMap){
	        	return "";
	        }
			singleUrl = intfMap.get("ADDRESS_IP").toString() + "?staffId=" + JwtUtils.encrypt(logonName) + 
					"&token=" + JwtUtils.createJWT(UUID.randomUUID().toString(), logonName, null);
		}catch (Exception e){
			log.error("getSingleUrl: 失败 {}", e.getMessage(), e);
		}
		return singleUrl;
	}
}
