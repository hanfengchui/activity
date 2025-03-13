package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.common.uploadFile.IAccessories;
import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;

@RestController
public class AccessoriesController {
	protected Logger log = LoggerFactory.getLogger(AccessoriesController.class);
	
	@Autowired
	private IAccessories accessoriesImpl;
	
	@PostMapping(value = "/accessories/queryFileCount")
	public int queryFileCount(@RequestParam(value="orderId", required=true) String orderId) {
		return accessoriesImpl.queryFileCount(orderId);
	}

	@PostMapping(value = "/accessories/queryFile")
	public FileRelatingInfo[] queryFile(@RequestParam(value="orderId", required=true) String orderId) {
		return accessoriesImpl.queryFile(orderId);
	}

	@PostMapping(value = "/accessories/insertFile")
	public int insertFile(@RequestBody FileRelatingInfo[] fileInfo) {
		return accessoriesImpl.insertFile(fileInfo);
	}
	
	@PostMapping(value = "/accessories/saveFile")
	public String saveFile(
			@RequestParam(value="ftpGuid", required=true) String ftpGuid,
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="sheetId", required=false) String sheetId,
			@RequestParam(value="fileName", required=true) String fileName,
			@RequestParam(value="regionId", required=true) String regionId) {
		return accessoriesImpl.saveFile(ftpGuid,orderId,sheetId,fileName,regionId);
	}
	
	@PostMapping(value = "/accessories/insertCallFile")
	public int insertCallFile(@RequestBody FileRelatingInfo fileInfo, 
			@RequestParam(value="ftpHost", required=true) String ftpHost, 
			@RequestParam(value="newFileName", required=true) String newFileName, 
			@RequestParam(value="ftpFileDir", required=true) String ftpFileDir) {
		return accessoriesImpl.insertCallFile(fileInfo, ftpHost, newFileName, ftpFileDir);
	}
	
	@PostMapping(value = "/accessories/deleteFile")
	public int deleteFile(@RequestParam(value="ftpID", required=true) String[] ftpID) {
		return accessoriesImpl.deleteFile(ftpID);
	}
}
