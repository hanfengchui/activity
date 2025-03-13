package com.timesontransfar.menu.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmMainMenu;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.util.DateUtil;
import com.timesontransfar.common.web.pojo.MenuVo;
import com.timesontransfar.customservice.common.DESEncryptUtil;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.menu.IMenuService;
import com.timesontransfar.menu.MenuUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Component(value="menuService")
public class MenuServiceImpl implements IMenuService {
	private static Logger log = LoggerFactory.getLogger(MenuServiceImpl.class);
	
	@Autowired
	private PubFunc pubFun;
	
	@Autowired
	private ISystemAuthorization systemAuthorization;

	@Autowired
	private RedisUtils redisUtils;
    
	@Override
    public String loadLeftMenu(String logonName){
    	Map ls = systemAuthorization.getMenuMap(logonName, null);
    	List list = loadMenu(ls);
    	return JSONArray.fromObject(list).toString();
    }
    
    @Override
    public String refreshMenu(String logonName){
    	try {
    		this.redisUtils.del(RedisType.WORKSHEET,"LOGONNAME__"+logonName);
			this.redisUtils.batchDelByPreKey("ROLEPERMIT__",RedisType.WORKSHEET);
			this.redisUtils.batchDelByPreKey("MENU__",RedisType.WORKSHEET);//删除菜单
			this.redisUtils.batchDelByPreKey("URL__",RedisType.WORKSHEET);
			this.redisUtils.batchDelByPreKey("FUNC__",RedisType.WORKSHEET);
		} catch (Exception e) {
    		log.error("refreshMenu error: {}", e.getMessage(), e);
			return "fail";
		}
		return "success";
    }
    
    /**
    public Map sort(Map map) {
        //获取内容
        Set<Map.Entry<String, WebMainMenu>> entries = map.entrySet();

        //存入到list，用来排序
        ArrayList<Map.Entry<String, WebMainMenu>> list = new ArrayList<>(entries);
        //调用Collections排序
        //第一个参数：集合
        //第二个参数：排序核心方法
        Collections.sort(list, new Comparator<Map.Entry<String, WebMainMenu>>() {
            @Override
            public int compare(Map.Entry<String, WebMainMenu> o1, Map.Entry<String, WebMainMenu> o2) {
                //倒序
                return o1.getValue().getSequence() - o2.getValue().getSequence();
            }
        });
        
        //创建有序集合LinkedHashMap，存放数据
        LinkedHashMap<String, WebMainMenu> linkedHashMap = new LinkedHashMap<>();
        //遍历list
        for (Map.Entry<String, WebMainMenu> entry : list) {
            //将数据存到linkedHashMap
            linkedHashMap.put(entry.getKey(),entry.getValue());
        }

        //返回数据
        return linkedHashMap;
    }
    **/
	
	private List loadMenu(Map map){
//		map = sort(map);

		Iterator it = map.keySet().iterator();
		List  topList = new ArrayList();
		
		MenuVo index = new MenuVo();
		index.setIcon("el-icon-menu");
		index.setIndex("dashboard");
		index.setTitle("首视图");
		topList.add(index);
		
		while(it.hasNext()) {
			String key = it.next().toString();
			TsmMainMenu m = new TsmMainMenu();
			if(map.get(key) instanceof TsmMainMenu) {
				m = (TsmMainMenu)map.get(key);
			}
			else {
				m = JSON.parseObject(map.get(key).toString(), TsmMainMenu.class);
			}
			if(m != null) {
				String id = m.getId();
				String name = m.getName();
				MenuVo v = new MenuVo();
				if(m.getParentList() == null || m.getParentList().isEmpty()) {
					v.setIcon(MenuUtil.getMenuIcon(name));
					v.setIndex(id);
					v.setTitle(name);
					List<MenuVo> subs = getSubList(m,map);
					if(subs != null && !subs.isEmpty()) {
						v.setSubs(subs);
					}
					JSONObject t1 = JSONObject.fromObject(v);
					topList.add(t1);
				}
			}
		}
		log.debug("topList ===> {}", topList);
		return topList;
	}
	
	public String getVueIndex(String s1) {
		log.info("原始url:{}" , s1);
		if(null == s1 || "null".equals(s1))return "";
		String s2 = s1.substring(0,s1.lastIndexOf("."));
		String vindex = s2.substring(s1.lastIndexOf("/") + 1,s2.length());
		
		log.info("vindex:{}" , vindex);
		return vindex;
	}
	
	private List getSubList(TsmMainMenu m ,Map map){
		if(m == null || m.getChildList() == null ) return null;
		List ls = m.getChildList();
		List  menuList = new ArrayList();
		for(int i=0;i<ls.size();i++) {
			Object smObj = map.get(ls.get(i));
			if(smObj != null) {
				TsmMainMenu sm = new TsmMainMenu();
				if(smObj instanceof TsmMainMenu) {
					sm = (TsmMainMenu)smObj;
				}
				else {
					sm = JSON.parseObject(smObj.toString(), TsmMainMenu.class);
				}
				/*
				if(sm.getUrl() != null) {
					if(!sm.getUrl().equals("null")) {
						//菜单删除不掉，做特殊处理
						if(sm.getName().equals("差异化管理"))continue;
						String id = "";
						log.info("name: {}" , sm.getName());
						id = getVueIndex(sm.getUrl());
						MenuVo v = new MenuVo("el-icon-kfzc",id,sm.getName());
						menuList.add(v);
					}
				}
				*/
				MenuVo v = new MenuVo("el-icon-kfzc",sm.getId(),sm.getName());
				List<MenuVo> subs = getSubList(sm,map);
				if(subs != null && subs.size()>0) {
					v.setSubs(subs);
				}
				else {
					String url = sm.getUrl();
					if(url != null) {
						
						String menuType = "0";//0-系统菜单 1-单点登录老菜单 2-打开外部系统菜单  3  打开vue 多页面模式
						String menuUrl = "";
						if(url.indexOf("http:")>-1) {
							
							menuType = "2";
							if(url.startsWith("http:/ReportTemplet.aspx")) {
								String staffId = (pubFun.getLogonStaff()).getId();
								String replaceUrl = "http://ccsreportservice/reportNJ";
								String urlStr = url.replace("http:",replaceUrl);
								menuUrl = "openIE:" + urlStr + "&staffId=" + staffId;
								//增加加密时间戳
								String currentTime = DateUtil.getCurrentTime();
						        String timeStamp = DESEncryptUtil.encrypt(currentTime, "customerservice");
						        menuUrl = menuUrl + "&token=" + timeStamp;
							}
							else if("3".equals(sm.getOpenType())){
								menuUrl = url;
							}
							else if("4".equals(sm.getOpenType())){
								menuUrl = "openIE:" + url;
							}
						}else if(sm.getOpenType().equals("3")){
							menuType="3";
						}
						v.setMenuType(menuType);
						v.setUrl(menuUrl);
					}
				}
				JSONObject tmp = JSONObject.fromObject(v);
				if(v.getSubs() == null) {
					tmp.discard("subs");
				}
				menuList.add(tmp);
			}
		}
		return menuList;
	}

}