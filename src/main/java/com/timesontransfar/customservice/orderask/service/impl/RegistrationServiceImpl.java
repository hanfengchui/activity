package com.timesontransfar.customservice.orderask.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.dao.RegistrationDao;
import com.timesontransfar.customservice.orderask.pojo.HarassmentScene;
import com.timesontransfar.customservice.orderask.service.RegistrationService;
import com.transfar.common.utils.IdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RegistrationServiceImpl implements RegistrationService {
	private static Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

	@Autowired
	private RegistrationDao registrationDao;
	
	@Autowired
	private PubFunc pubFunc;


	public JSONObject createRegistration(String parm) {
		log.info("createRegistration: {}", parm);
		JSONObject serviceJson = new JSONObject();
		try {
			JSONArray jsonArray = JSON.parseArray(parm);
			String groupId = IdUtils.fastSimpleUUID();
			List<HarassmentScene> harassmentScenes = new ArrayList<>();
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String formattedDateTime = now.format(formatter);
			int i = 0;
			for (Object o : jsonArray) {
				HarassmentScene harassmentScene = JSON.toJavaObject((JSON) o,HarassmentScene.class);
				harassmentScene.setSubmissionTime(formattedDateTime);
				harassmentScene.setDownloadStatus("0");
				harassmentScene.setGroupId(groupId);
				String fileName = "";
				String registrationType = harassmentScene.getRegistrationType();
				if("1".equals(registrationType)){
					String s = "待导入网间";
					fileName = this.generateFileName(s);
				}else if("2".equals(registrationType)){
					String s = "待导入本网他省";
					fileName = this.generateFileName(s);
				}else if("3".equals(registrationType)){
					String s = "待导入外省固话和特服";
					fileName = this.generateFileName(s);
				}
				harassmentScene.setFileName(fileName);
				if(registrationDao.checkRepeatedSubmission(harassmentScene)>0){
					serviceJson.put("code","1");
					serviceJson.put("resultMsg","已存在投诉号码："+harassmentScene.getComplaintNumber()+", 被投诉号码：" + harassmentScene.getComplainedNumber() + "的相关数据，请检查后重新录入" );
					return serviceJson;
				}
				harassmentScenes.add(harassmentScene);
			}
			for (HarassmentScene harassmentScene : harassmentScenes) {
				TsmStaff staff = pubFunc.getLogonStaff();
				harassmentScene.setReporterId(staff.getId());
				harassmentScene.setReporter(staff.getName());
				harassmentScene.setTeamId(staff.getOrganizationId());
				harassmentScene.setTeam(staff.getOrgName());
				i += registrationDao.createRegistrationExcel(harassmentScene);
			}
			serviceJson.put("code","0");
			serviceJson.put("resultMsg","成功录入"+ i + "条数据" );
			return serviceJson;
		} catch (Exception e) {
			e.printStackTrace();
			serviceJson.put("code","999");
			serviceJson.put("resultMsg","录入失败，接口异常" );
			return serviceJson;
		}
	}

	public GridDataInfo getHarassmentScene(int begion,int pageSize,String time, String status){
		log.info("getHarassmentScene :{} {}",time,status);
		return registrationDao.getHarassmentScene(begion,pageSize,time,status);
	}

	public int setDownloadTime(String param){
		log.info("setDownloadTime");
		//获取当前时间
		int i = 0;
		try{
			String[] ids = param.split(",");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String downloadTime = LocalDateTime.now().format(formatter);
			for (String id : ids) {
				int j = registrationDao.setDownloadTime(downloadTime, "1",id);
				i+=j;
			}
		}catch (Exception e){
			log.error("setDownloadTime接口异常 : {}",e.getMessage());
			return 0;
		}
		return i;
	}

	private String generateFileName(String baseName) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String dateTime = LocalDateTime.now().format(dtf);
		int randomNum = ThreadLocalRandom.current().nextInt(1000, 10000);
		return baseName + dateTime + randomNum ;
	}
}
