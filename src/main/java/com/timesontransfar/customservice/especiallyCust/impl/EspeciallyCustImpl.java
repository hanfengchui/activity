/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.especiallyCust.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import net.sf.json.JSONObject;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.especiallyCust.IespeciallyCust;
import com.timesontransfar.customservice.especiallyCust.IespeciallyCustDao;
import com.timesontransfar.customservice.especiallyCust.TsEspeciallyCustInfo;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
@Component("EspeciallyCustImpl")
public class EspeciallyCustImpl implements IespeciallyCust {
	protected Logger log = LoggerFactory.getLogger(EspeciallyCustImpl.class);
	
	@Autowired
	private PubFunc pubFunc;
	
	@Autowired
	private IespeciallyCustDao especiallyCustDao;

	public boolean saveEspeciallyCust(int regionId,InputStream file,int modlFlag) {
        try {   
            Workbook book = Workbook.getWorkbook(file);
            //获得第一个工作表对象    
            Sheet sheet = book.getSheet(0);   
            //得到第一列第一行的单元格   计算所有行 
            int j = sheet.getRows();//行数
            TsEspeciallyCustInfo bean = new TsEspeciallyCustInfo();
            bean.setModelFlag(modlFlag);
            String regionName = this.pubFunc.getRegionName(regionId);
            String regionTel = this.pubFunc.getRegionTelNo(regionId);
            bean.setRegionTel(regionTel);
            bean.setRegionId(regionId);
            bean.setRegionName(regionName);
            bean.setStatu(1);
            
            Cell cell = null;
            for(int i=1;i<j;i++) {
            	cell = sheet.getCell(0,i);            	
            	if(cell == null) {
            		break;
            	}
            	String check =cell.getContents();
            	if(check==null || check.equals("")) {
            		break;
            	}       	
	           	String clom1 = sheet.getCell(1,i).getContents();//用户号码
	           	String clom2 = sheet.getCell(2,i).getContents();//用户名
	           	String clom3 = sheet.getCell(3,i).getContents();//投诉特征
	           	String clom4 = sheet.getCell(4,i).getContents();//备注
	           	String clom5 = sheet.getCell(5,i).getContents();//接续事项
	           	bean.setCustName(clom2);
	           	bean.setCustNum(clom1);
	           	bean.setTsEspecially(clom3);
	           	bean.setRemark(clom4);
	           	bean.setMeetProceeding(clom5);
	           	this.especiallyCustDao.saveCustInfo(bean);
            }  
            book.close(); 
        } catch (Exception e) {  
        	log.error("saveEspeciallyCust error：{}", e.getMessage(), e);
        }
		return true;
	}
	/**
	 * 保存投诉特殊对象
	 * @param bean
	 * @return
	 */
	public String saveEspeciallyCustObj(TsEspeciallyCustInfo bean) {
		int regionId = bean.getRegionId();
        String regionName = this.pubFunc.getRegionName(regionId) ;
        String regionTel = this.pubFunc.getRegionTelNo(regionId);
        bean.setRegionTel(regionTel);
        bean.setRegionId(regionId);
        bean.setRegionName(regionName);
        bean.setStatu(1);
        JSONObject json=new JSONObject();
        int code=999;
        List qrtResult=this.especiallyCustDao.qryTsEspecial(regionId, bean.getCustNum());
        if(qrtResult.isEmpty()) {
            int size = this.especiallyCustDao.saveCustInfo(bean);
            if(size > 0) {
            	code=0;
            }
        }
        json.put("code", code);
        json.put("msg", qrtResult);
        return json.toString();
	}
	/**
	 * 更新投诉特殊客户
	 * @param bean
	 * @return
	 */
	public int updateEspeciallyCust(TsEspeciallyCustInfo bean) {
		bean.setStatu(1);
		return this.especiallyCustDao.updataCustInfo(bean);
	}
	/**
	 * 删除投诉客户记录
	 * @param bean
	 * @return
	 */
	public int deleteEspeciallyCust(TsEspeciallyCustInfo bean) {
		bean.setStatu(0);
		return this.especiallyCustDao.updataCustInfo(bean);
	}
	/**
	 * @return pubFunc
	 */
	public PubFunc getPubFunc() {
		return pubFunc;
	}

	/**
	 * @param pubFunc 要设置的 pubFunc
	 */
	public void setPubFunc(PubFunc pubFunc) {
		this.pubFunc = pubFunc;
	}

	/**
	 * @return especiallyCustDao
	 */
	public IespeciallyCustDao getEspeciallyCustDao() {
		return especiallyCustDao;
	}

	/**
	 * @param especiallyCustDao 要设置的 especiallyCustDao
	 */
	public void setEspeciallyCustDao(IespeciallyCustDao especiallyCustDao) {
		this.especiallyCustDao = especiallyCustDao;
	}
	@Override
	public TsEspeciallyCustInfo getSpeciaObj(Map m) {
		TsEspeciallyCustInfo info=new TsEspeciallyCustInfo();
		Map map=(Map) m.get("obj");
		if(map.get("CUST_NAME")!=null) {
			info.setCustName(map.get("CUST_NAME").toString());	
		}
		if(map.get("TS_ESPECIALLY")!=null) {
			info.setTsEspecially(map.get("TS_ESPECIALLY").toString());	
		}
		if(map.get("REMARK")!=null) {
			info.setRemark(map.get("REMARK").toString());	
		}
		if(map.get("MEET_PROCEEDING")!=null) {
			info.setMeetProceeding(map.get("MEET_PROCEEDING").toString());	
		}
		if(map.get("STATU")!=null) {
			info.setStatu(Integer.parseInt(map.get("STATU").toString()));	
		}
		if(map.get("MODIFI_STAFF")!=null) {
			info.setStaffId(Integer.parseInt(map.get("MODIFI_STAFF").toString()));	
		}
		if(map.get("MODIFI_DATA")!=null) {
			info.setModifiData(map.get("MODIFI_DATA").toString());	
		}
		if(map.get("REGION_ID")!=null) {
			info.setRegionId(Integer.parseInt(map.get("REGION_ID").toString()));	
		}
		if(map.get("CUST_NUM")!=null) {
			info.setCustNum(map.get("CUST_NUM").toString());	
		}
		if(map.get("REGION_NAME")!=null) {
			info.setRegionName(map.get("REGION_NAME").toString());	
		}
		if(map.get("REGION_TELNO")!=null) {
			info.setRegionTel(map.get("REGION_TELNO").toString());	
		}
		return info;
	}
}
