package com.timesontransfar.controller;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.customservice.dbgridData.ComplaintMaterialsService;
import com.transfar.common.web.ResultUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ComplaintMaterialsController {
	protected Logger log = LoggerFactory.getLogger(ComplaintMaterialsController.class);

	@Autowired
	private ComplaintMaterialsService complaintMaterialsService;


	/**
	 * 根据orderId查询数据
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/getData")
	public JSONObject getData(@RequestParam(value="orderId", required=true) String orderId){
		log.info("getData orderId:{}",orderId);
		JSONObject data = complaintMaterialsService.getData(orderId);
		log.info("getData result:{}",data);
		return data;
	}

	/**
	 * 根据Id删除数据
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/removeDataByid")
	public int removeDataByid(@RequestParam(value="id", required=true) String id){
		log.info("removeDataByid id:{}",id);
		int i = complaintMaterialsService.removeDataByid(id);
		log.info("removeDataByid result:{}",i);
		return i;
	}

	/**
	 * 根据custOrderNbr删除数据
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/removeDataByCustOrderNbr")
	public int removeDataByCustOrderNbr(@RequestParam(value="custOrderNbr", required=true) String custOrderNbr,
										@RequestParam(value="orderId", required=true) String orderId){
		log.info("removeDataByCustOrderNbr custOrderNbr:{},orderId:{}",custOrderNbr,orderId);
		int i = complaintMaterialsService.removeDataByCustOrderNbr(custOrderNbr,orderId);
		log.info("removeDataByCustOrderNbr result:{}",i);
		return i;
	}

	/**
	 * 添加数据
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/addData")
	public int addData(@RequestBody String param){
		log.info("addData param:{}",param);
		int i = complaintMaterialsService.addData(param);
		log.info("addData result:{}",i);
		return i;
	}

	/**
	 * 暂存数据
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/stashInfo")
	public int stashInfo(@RequestBody String param){
		log.info("stashInfo param:{}",param);
		int i = complaintMaterialsService.stashInfo(param);
		log.info("stashInfo result:{}",i);
		return i;
	}

	/**
	 * 根据orderId查询数据
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/loadStashData")
	public JSONObject loadStashData(@RequestParam(value="orderId", required=true) String orderId){
		log.info("loadStashData orderId:{}",orderId);
		JSONObject data = complaintMaterialsService.loadStashData(orderId);
		log.info("loadStashData result:{}",data);
		return data;
	}

	/**
	 * 根据orderId查询电子协议定责数据
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/qryContractData")
	public JSONObject qryContractData(@RequestParam(value="orderId", required=true) String orderId){
		log.info("qryContractData orderId:{}",orderId);
		JSONObject data = complaintMaterialsService.qryContractData(orderId);
		log.info("qryContractData result:{}",data);
		return data;
	}

	/**
	 * 根据orderId更新核查情况
	 * */
	@PostMapping(value = "/workflow/complaintMaterials/updateSituationById")
	public int updateSituationById(@RequestParam(value="orderId", required=true) String orderId,
								   @RequestParam(value="situation", required=true) String situation){
		log.info("updateSituationById orderId: {} situation: {}",orderId,situation);
		int result = complaintMaterialsService.updateSituationById(orderId,situation);
		log.info("updateSituationById result:{}",result);
		return result;
	}
	
	@RequestMapping(value = "/workflow/dynamic/getMaterialsData")
	public Object getMaterialsData(@RequestBody(required=false) String param) {
		JSONObject json = JSON.parseObject(param);
		String orderId = json.getString("orderId");
		return complaintMaterialsService.getMaterialsData(orderId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateMaterialStatus")
	public Object updateMaterialStatus(@RequestBody(required=false) String param) {
		JSONObject json = JSON.parseObject(param);
		String id = json.getString("id");
		return ResultUtil.success(complaintMaterialsService.updateMaterialStatus(id));
	}
	
}
