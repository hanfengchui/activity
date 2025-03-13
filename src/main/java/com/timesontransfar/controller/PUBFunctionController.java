package com.timesontransfar.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.timesontransfar.common.pubFunction.IPUBFunctionService;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.especiallyCust.IespeciallyCust;
import com.timesontransfar.customservice.especiallyCust.TsEspeciallyCustInfo;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.tablePage.PageResVO;
import com.transfar.common.utils.tool.StaticUtil;

import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class PUBFunctionController {
	private static Logger log = LoggerFactory.getLogger(PUBFunctionController.class);
	
	@Autowired
	private IPUBFunctionService pubFunction;
	@Autowired
	private IserviceOrderAsk serviceOrderAskImpl;
	@Autowired
	private IespeciallyCust especiallyCustImpl;
	@Autowired
	private PubFunc pubFunc;
	
	private static final String KEY_FUNCID = "funcId";
	

	@PostMapping(value = "/workflow/PubFunction/qryData")
	public PageResVO qryData(@RequestBody(required=true) Map param) {
		String funcId = param.get(KEY_FUNCID).toString();
		log.info("qryData funcId:{}  param:{}", funcId, param);
		PageResVO arr = pubFunction.qryData(funcId, param);
		log.info("funcId：{} return currentPageSize：{}", funcId, arr.getTotalCount());
		return arr;
	}
	

	@PostMapping(value = "/workflow/PubFunction/qrySheetList")
	public PageResVO qrySheetList(@RequestBody(required=true) Map param) {
		String funcId = param.get(KEY_FUNCID).toString();
		//追加where条件
		Map newParam = pubFunction.addOrderStr(param);
		return pubFunction.qryData(funcId, newParam);
	}
	

	@PostMapping(value = "/workflow/PubFunction/qryAccpetList")
	public Map qryAccpetList(@RequestBody(required=true) Map param) {
		String funcId = param.get(KEY_FUNCID).toString();
		//追加where条件
		String sql = pubFunction.addAccpetStr(param,funcId);
		return serviceOrderAskImpl.qrySheetList(sql);
	}

	@PostMapping(value = "/workflow/PubFunction/qryOrderAskList")
	public Map qryOrderAskList(@RequestBody(required=true) Map param) {
		return serviceOrderAskImpl.qryOrderAskList(param);
	}
	
	@PostMapping(value = "/workflow/PubFunction/qryLinkOrder")
	public PageResVO qryLinkOrder(@RequestBody(required=true) Map param) {
		String funcId = param.get(KEY_FUNCID).toString();
		Map newParam = pubFunction.addLinkStr(param,funcId);
		PageResVO arr = pubFunction.qryData(funcId, newParam);
		//后端键值转换逻辑
		arr.setResultList(StaticUtil.getQueryOrderInfo(arr.getResultList(), funcId));
		return arr;
	}
	

	@PostMapping(value = "/workflow/PubFunction/qryActionOrder")
	public PageResVO qryActionOrder(@RequestBody(required=true) Map param) {
		String funcId = param.get(KEY_FUNCID).toString();
		Map newParam = pubFunction.addActionWhere(param, funcId);
		PageResVO arr = pubFunction.qryData(funcId, newParam);
		//后端键值转换逻辑
		arr.setResultList(StaticUtil.getQuerySheetAction(arr.getResultList(), funcId));
		return arr;
	}
	

	@PostMapping(value = "/workflow/PubFunction/qrCliqueList")
	public Map qrCliqueList(@RequestBody(required=true) Map param) {
		String sql = pubFunction.getQryClieque(param);
		return serviceOrderAskImpl.qrCliqueList(sql);
	}

	@PostMapping(value = "/workflow/PubFunction/getFeedbackInfo")
	public JSONObject getFeedbackInfo(@RequestParam("orderId") String orderId) {
		return pubFunction.addFeedbackInfo(orderId);
	}

	@PostMapping(value = "/workflow/PubFunction/qrSnList")
	public Map qrSnList(@RequestBody(required=true) Map param) {
		String sql = pubFunction.getQrySn(param);
		return serviceOrderAskImpl.qrCliqueList(sql);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getBssStaffCode")
	public String getBssStaffCode(@RequestParam(value="logoName", required=true) String logoName){
		return serviceOrderAskImpl.getBssStaffCode(logoName);
	}
	

	@PostMapping(value = "/workflow/PubFunction/qrySpecialInfo")
	public Map qrySpecialInfo(@RequestBody(required=true) Map param) {
		String tableType = param.get("tableType").toString();
		String sql = pubFunction.getSpecialInfoStr(tableType, param);
		return serviceOrderAskImpl.qrySpecialInfo(tableType, sql);
	}
	
	@PostMapping(value = "/workflow/PubFunction/addSpecialInf")
	public int addSpecialInf(@RequestBody(required=true) Map param) {
		return serviceOrderAskImpl.addSpecialInf(param);
	}
	
	@PostMapping(value = "/workflow/PubFunction/addSpecialIvr")
	public int addSpecialIvr(@RequestBody(required=true) Map param) {
		return serviceOrderAskImpl.addSpecialInf(param);
	}
	
	@PostMapping(value = "/workflow/PubFunction/removeSpecial")
	public int removeSpecial(@RequestBody(required=true) Map param) {
		return serviceOrderAskImpl.removeSpecial(param);
	}
	
	@PostMapping(value = "/workflow/PubFunction/updateSpecial")
	public int updateSpecial(@RequestBody(required=true) Map param) {
		return serviceOrderAskImpl.updateSpecial(param);
	}
	
	@PostMapping(value = "/workflow/PubFunction/deleteEspeciallyCust")
	public int deleteEspeciallyCust(@RequestBody Map map) {
		TsEspeciallyCustInfo info = especiallyCustImpl.getSpeciaObj(map);
		return especiallyCustImpl.deleteEspeciallyCust(info);
	}
	
	@PostMapping(value = "/workflow/PubFunction/saveEspeciallyCust")
	public String saveEspeciallyCust(@RequestBody Map map) {
		TsEspeciallyCustInfo info = especiallyCustImpl.getSpeciaObj(map);
		return especiallyCustImpl.saveEspeciallyCustObj(info);
	}
	
	@RequestMapping(value = "/workflow/ftp/tsSpeciaUploadFile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public boolean tsSpeciaUploadFile(@RequestPart("file") MultipartFile file,@RequestParam(value="regionId",required=false) String regionId,
			@RequestParam(value="modlFlag",required=false) String modlFlag) throws IOException {
		byte[] bytes = file.getBytes();
		InputStream in = new ByteArrayInputStream(bytes);
		return especiallyCustImpl.saveEspeciallyCust(Integer.parseInt(regionId), in, Integer.parseInt(modlFlag));
	}
	@PostMapping(value = "/workflow/PubFunction/getTuiFeitType")
	public List getTuiFeitType() {
		return pubFunc.getTuiFeitType();
	}
	
	@PostMapping(value = "/workflow/PubFunction/getGzOorderList")
	public List getGzOorderList(@RequestParam("orderId") String orderId) {
		return serviceOrderAskImpl.getGzOorderList(orderId);
	}
	
	@PostMapping(value = "/workflow/PubFunction/qryZDSSOToken")
	public String qryZDSSOToken(@RequestBody JSONObject body) {
		return pubFunction.qryZDSSOToken(body);
	}


	@PostMapping(value = "/workflow/PubFunction/getReturnBlackList")
	public PageResVO getReturnBlackList(@RequestBody(required=true) Map param) {
		log.info("getReturnBlackList  param:{}", param);
		PageResVO arr = pubFunction.getReturnBlackList(param);
		return arr;
	}
}