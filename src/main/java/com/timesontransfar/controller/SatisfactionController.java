package com.timesontransfar.controller;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.SatisfactionService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class SatisfactionController {
	protected Logger log = LoggerFactory.getLogger(SatisfactionController.class);

	@Autowired
	private SatisfactionService satisfactionService;


	//导入文件数据入库
	@RequestMapping(value = "/workflow/satisfaction/importFile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String importFile(@RequestPart("file") MultipartFile file,
							 @RequestParam(value="logonName", required=true) String logonName,
							 @RequestParam(value="fileName", required=true) String fileName){
		String result = "";
		try{
			result = satisfactionService.importFile(file.getInputStream(),fileName,logonName);
		}catch (IOException e){
			log.error("importFile error: {}", e.getMessage(), e);
		}
		return result;
	}

	@PostMapping(value = "/workflow/satisfaction/getRechargeData")
	public GridDataInfo getRechargeData(
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="importTime", required=false)String importTime,
			@RequestParam(value="taskStatus", required=false)String taskStatus,
			@RequestParam(value="completionFlag", required=false)String completionFlag,
			@RequestParam(value="publisher", required=false)String publisher){
		StringBuilder where = new StringBuilder();
		where.append(" WHERE 1 = 1");
		if(StringUtils.isNotBlank(importTime)){
			String[] ar = importTime.split(",");
			where.append(" AND RELEASE_TIME > '" + ar[0] + "'") ;
			where.append(" AND RELEASE_TIME < '" + ar[1] + "'") ;
		}
		if(StringUtils.isNotBlank(taskStatus)){
			where.append(" AND STATUS = " + taskStatus);
		}
		if(StringUtils.isNotBlank(completionFlag)){
			where.append(" AND COMPLETION_FLAG = " + completionFlag);
		}
		if(StringUtils.isNotBlank(publisher)){
			where.append(" AND PUBLISHER = " + publisher);
		}
		return satisfactionService.getRechargeData(begion,pageSize,where.toString());
	}

	@PostMapping(value = "/workflow/satisfaction/getRechargeItemData")
	public GridDataInfo getRechargeItemData(
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="uniqueFlow", required=false)String uniqueFlow){
		return satisfactionService.getRechargeItemData(begion,pageSize,uniqueFlow);
	}

	@PostMapping(value = "/workflow/satisfaction/startRechargeTask")
	public String startRechargeTask(@RequestParam(value="uniqueFlow", required=true)String uniqueFlow){
		return satisfactionService.startRechargeTask(uniqueFlow);
	}
}
