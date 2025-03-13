package com.timesontransfar.common.utils;

import org.springframework.stereotype.Component;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component("dataUtil")
public class DataUtils {

	public String getLocalCode(String code) {
        if ( "0517".equals(code)) return "0000010"; //淮安
        if ( "0515".equals(code)) return "0000011"; //盐城
        if ( "0514".equals(code)) return "0000012"; //扬州
        if ( "0511".equals(code)) return "0000013"; //镇江
        if ( "0523".equals(code)) return "0000014"; //泰州
        if ( "0527".equals(code)) return "0000015"; //宿迁
        if ( "025".equals(code))  return "0000003"; //南京
        if ( "0510".equals(code)) return "0000004"; //无锡
        if ( "0516".equals(code)) return "0000005"; //徐州
        if ( "0519".equals(code)) return "0000006"; //常州
        if ( "0512".equals(code)) return "0000007"; //苏州
        if ( "0513".equals(code)) return "0000008"; //南通
        if ( "0518".equals(code)) return "0000009"; //连云港
		return"";
	}
	
	public String getZDbusinessCodes(String areaId,String prodId) {
		JSONObject j = new JSONObject();
		String str =
				"[\r\n" + 
				"   {\"regionId\":\"0512\", \"prodId\":\"14\",\"code\":\"IDB_SA_00023\"},//专线光纤宽带\r\n" + 
				"   {\"regionId\":\"0512\", \"prodId\":\"11\",\"code\":\"IDB_SA_00019\"},//LAN智能小区\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"370\",\"code\":\"IDB_SA_00045\"},//号码百事通-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"31\",\"code\":\"IDB_SA_00020\"},//卡业务-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"797\",\"code\":\"IDB_SA_00096\"},//平安商铺-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"364\",\"code\":\"IDB_SA_00028\"},//商务领航-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"502\",\"code\":\"IDB_SA_00028\"},//商务领航-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"503\",\"code\":\"IDB_SA_00028\"},//商务领航-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"389\",\"code\":\"IDB_SA_00043\"},//网络视讯-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"390\",\"code\":\"IDB_SA_00043\"},//网络视讯-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"613\",\"code\":\"IDB_SA_00043\"},//网络视讯-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"215\",\"code\":\"IDB_SA_00043\"},//网络视讯-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"881\",\"code\":\"IDB_SA_00043\"},//网络视讯-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"613538\",\"code\":\"IDB_SA_00043\"},//FTTB网络视讯-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"970\",\"code\":\"IDB_SA_00044\"},//iTV互联网电视{ott}-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"607\",\"code\":\"IDB_SA_00014\"},//PHS公话卡-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"1\",\"code\":\"IDB_SA_00014\"},//小灵通-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"371\",\"code\":\"IDB_SA_00014\"},//小灵通-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"789\",\"code\":\"IDB_SA_00097\"},//学子E行-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"B05\",\"code\":\"IDB_SA_00021\"},//XDSL接入 对应 VDSL -->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"25\",\"code\":\"IDB_SA_00026\"},//WLAN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"610\",\"code\":\"IDB_SA_00026\"},//WLAN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"B03\",\"code\":\"IDB_SA_00023\"},//光纤接入-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"11\",\"code\":\"IDB_SA_00023\"},//光纤宽带-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"14\",\"code\":\"IDB_SA_00023\"},//专线光纤宽带-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"13\",\"code\":\"IDB_SA_00023\"},//专线LAN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"13538\",\"code\":\"IDB_SA_00023\"},//FTTB专线LAN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"7\",\"code\":\"IDB_SA_00017\"},//B_ISDN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"6\",\"code\":\"IDB_SA_00017\"},//N_ISDN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"379\",\"code\":\"IDB_SA_00070\"},//CDMA-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"804\",\"code\":\"IDB_SA_00070\"},//CDMA无线公商话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"805\",\"code\":\"IDB_SA_00098\"},//CDMA无线网卡-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"9\",\"code\":\"IDB_SA_20000\"},//宽带业务-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"B02\",\"code\":\"IDB_SA_00018\"},//ADSL接入-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"9538\",\"code\":\"IDB_SA_00018\"},//FTTB ADSL-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"375\",\"code\":\"IDB_SA_00018\"},//IPHOTEL_ADSL-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"12538\",\"code\":\"IDB_SA_00024\"},//FTTB专线ADSL-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"12\",\"code\":\"IDB_SA_00024\"},//专线ADSL-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"10538\",\"code\":\"IDB_SA_00019\"},//FTTB LAN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"10\",\"code\":\"IDB_SA_00019\"},//LAN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"B01\",\"code\":\"IDB_SA_00019\"},//LAN接入-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"24\",\"code\":\"IDB_SA_00019\"},//模拟专线接入端-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"293\",\"code\":\"IDB_SA_00047\"},//宝宝在线-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"8\",\"code\":\"IDB_SA_00001\"},//800电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"323\",\"code\":\"IDB_SA_00001\"},//中兴电子售卡电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"297\",\"code\":\"IDB_SA_00001\"},//智能网电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"280\",\"code\":\"IDB_SA_00001\"},//宽带套餐-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"374\",\"code\":\"IDB_SA_00001\"},//一号双机-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"385\",\"code\":\"IDB_SA_00001\"},//IAD-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"386\",\"code\":\"IDB_SA_00001\"},//NGN固话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"501\",\"code\":\"IDB_SA_00001\"},//网络传真-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"373\",\"code\":\"IDB_SA_00001\"},//企业总机-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"415\",\"code\":\"IDB_SA_00001\"},//网络电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"787\",\"code\":\"IDB_SA_00090\"},//电话看家-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"790\",\"code\":\"IDB_SA_00091\"},//电脑保姆-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"2538\",\"code\":\"IDB_SA_00001\"},//FTTB固话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"6538\",\"code\":\"IDB_SA_00001\"},//FTTB N_ISDN-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"500538\",\"code\":\"IDB_SA_00001\"},//FTTB升级版网络传真-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"373538\",\"code\":\"IDB_SA_00001\"},//FTTB企业总机-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"34538\",\"code\":\"IDB_SA_00001\"},//FTTB计价器公用电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"37538\",\"code\":\"IDB_SA_00001\"},//FTTB智能公话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"272538\",\"code\":\"IDB_SA_00001\"},//FTTB IP话吧-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"322538\",\"code\":\"IDB_SA_00001\"},//FTTB华为智能公话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"35538\",\"code\":\"IDB_SA_00001\"},//FTTB投币式公用电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"36538\",\"code\":\"IDB_SA_00001\"},//FTTB IC卡公用电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"265538\",\"code\":\"IDB_SA_00001\"},//FTTB校园电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"2\",\"code\":\"IDB_SA_00001\"},//普通电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"3\",\"code\":\"IDB_SA_00001\"},//电话虚拟网-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"4\",\"code\":\"IDB_SA_00001\"},//中继成员-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"29\",\"code\":\"IDB_SA_00001\"},//公用电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"30\",\"code\":\"IDB_SA_00001\"},//临时电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"34\",\"code\":\"IDB_SA_00001\"},//有人值守{计价器}公用电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"35\",\"code\":\"IDB_SA_00001\"},//投币式公用电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"36\",\"code\":\"IDB_SA_00001\"},//IC卡公用电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"37\",\"code\":\"IDB_SA_00001\"},//智能公话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"265\",\"code\":\"IDB_SA_00001\"},//校园电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"267\",\"code\":\"IDB_SA_00001\"},//多媒体公话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"272\",\"code\":\"IDB_SA_00001\"},//IP话吧电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"275\",\"code\":\"IDB_SA_00001\"},//新钛智能网电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"261\",\"code\":\"IDB_SA_00001\"},//专网电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"289\",\"code\":\"IDB_SA_00001\"},//亲情电话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"322\",\"code\":\"IDB_SA_00001\"},//华为智能公话-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"26\",\"code\":\"IDB_SA_00144\"},//16300注册-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"901\",\"code\":\"IDB_SA_60017\"},//智机通-->\r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"40\",\"code\":\"IDB_SA_00006\"},//数字电路--> \r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"301\",\"code\":\"IDB_SA_00006\"},//数字电路--> \r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"18\",\"code\":\"IDB_SA_00008\"},//DDN--> \r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"16\",\"code\":\"IDB_SA_00008\"},//DDN--> \r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"15\",\"code\":\"IDB_SA_00008\"},//DDN--> \r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"258\",\"code\":\"IDB_SA_00007\"},//DDN--> \r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"354\",\"code\":\"IDB_SA_00024\"},//ADSL专线--> \r\n" + 
				"   {\"regionId\":\"\", \"prodId\":\"23\",\"code\":\"IDB_SA_00011\"}//ATM-->\r\n" + 
				"]";
		JSONArray arr = JSONArray.fromObject(str);
		for(int i=0;i<arr.size();i++) {
			j = arr.getJSONObject(i);
			if("0512".equals(areaId) && "0512".equals(j.getString("regionId")) && prodId.contains(j.getString("prodId")) 
					|| !"0512".equals(areaId) && prodId.contains(j.getString("prodId"))) {
				break;
			}
		}
		return j.getString("code");
	}


	public  String getZDBusinessName(String id){
		JSONObject j=new JSONObject();
		String str="[{\"code\":\"0022950\",\"buissinessId\":\"IDB_SA_00096\",\"businessName\":\"平安商铺\"},\r\n" + 
				"{\"code\":\"0022951\",\"buissinessId\":\"IDB_SA_00032\",\"businessName\":\"机房无忧\"},\r\n" + 
				"{\"code\":\"0022952\",\"buissinessId\":\"IDB_SA_00057\",\"businessName\":\"专网视频监控\"},\r\n" + 
				"{\"code\":\"0022953\",\"buissinessId\":\"IDB_SA_00056\",\"businessName\":\"会易通\"},\r\n" + 
				"{\"code\":\"0022954\",\"buissinessId\":\"IDB_SA_00123\",\"businessName\":\"司法E通\"},\r\n" + 
				"{\"code\":\"0022955\",\"buissinessId\":\"IDB_SA_00051\",\"businessName\":\"网络及IT维护外包\"},\r\n" + 
				"{\"code\":\"0022956\",\"buissinessId\":\"IDB_SA_00050\",\"businessName\":\"系统集成\"},\r\n" + 
				"{\"code\":\"0022957\",\"buissinessId\":\"IDB_SA_00053\",\"businessName\":\"短号码（呼叫中心）\"},\r\n" + 
				"{\"code\":\"0022958\",\"buissinessId\":\"IDB_SA_00052\",\"businessName\":\"安全网关\"},\r\n" + 
				"{\"code\":\"0022970\",\"buissinessId\":\"IDB_SA_00132\",\"businessName\":\"烟草E通\"},\r\n" + 
				"{\"code\":\"0022971\",\"buissinessId\":\"IDB_SA_00139\",\"businessName\":\"数字医院\"},\r\n" + 
				"{\"code\":\"0022972\",\"buissinessId\":\"IDB_SA_00134\",\"businessName\":\"客运E通\"},\r\n" + 
				"{\"code\":\"0022973\",\"buissinessId\":\"IDB_SA_00129\",\"businessName\":\"电子政务\"},\r\n" + 
				"{\"code\":\"0022974\",\"buissinessId\":\"IDB_SA_00136\",\"businessName\":\"综合配货\"},\r\n" + 
				"{\"code\":\"62473644\",\"buissinessId\":\"IDB_SA_60000\",\"businessName\":\"行业应用\"},\r\n" + 
				"{\"code\":\"62479329\",\"buissinessId\":\"IDB_SA_60001\",\"businessName\":\"政务及监管执法应用\"},\r\n" + 
				"{\"code\":\"62480678\",\"buissinessId\":\"IDB_SA_60002\",\"businessName\":\"交通物流应用\"},\r\n" + 
				"{\"code\":\"62480687\",\"buissinessId\":\"IDB_SA_60004\",\"businessName\":\"数字医院应用\"},\r\n" + 
				"{\"code\":\"62480689\",\"buissinessId\":\"IDB_SA_60005\",\"businessName\":\"其他行业应用\"},\r\n" + 
				"{\"code\":\"62486311\",\"buissinessId\":\"IDB_SA_60010\",\"businessName\":\"VPN{VPDN}{CDMA}\"},\r\n" + 
				"{\"code\":\"62502456\",\"buissinessId\":\"IDB_SA_60011\",\"businessName\":\"水利E通\"},\r\n" + 
				"{\"code\":\"62503358\",\"buissinessId\":\"IDB_SA_60012\",\"businessName\":\"人员定位\"},\r\n" + 
				"{\"code\":\"62503916\",\"buissinessId\":\"IDB_SA_60013\",\"businessName\":\"黑莓\"},\r\n" + 
				"{\"code\":\"62503917\",\"buissinessId\":\"IDB_SA_60014\",\"businessName\":\"加密通讯\"},\r\n" + 
				"{\"code\":\"50364353\",\"buissinessId\":\"IDB_SA_00161\",\"businessName\":\"旺铺助手{招财宝}\"},\r\n" + 
				"{\"code\":\"35442227\",\"buissinessId\":\"IDB_SA_00090\",\"businessName\":\"电话看家\"},\r\n" + 
				"{\"code\":\"39331261\",\"buissinessId\":\"IDB_SA_80002\",\"businessName\":\"远程支持\"},\r\n" + 
				"{\"code\":\"65096225\",\"buissinessId\":\"IDB_SA_90001\",\"businessName\":\"IDC{南京}\"},\r\n" + 
				"{\"code\":\"54403918\",\"buissinessId\":\"IDB_SA_COMMCAUSE\",\"businessName\":\"设备障碍\"},\r\n" + 
				"{\"code\":\"147243192\",\"buissinessId\":\"IDB_SA_00072\",\"businessName\":\"政企WIFI\"},\r\n" + 
				"{\"code\":\"94714482\",\"buissinessId\":\"IDB_SA_90002\",\"businessName\":\"政务外网\"},\r\n" + 
				"{\"code\":\"5959327\",\"buissinessId\":\"IDB_SA_00145\",\"businessName\":\"翼通宝\"},\r\n" + 
				"{\"code\":\"35441502\",\"buissinessId\":\"IDB_SA_00098\",\"businessName\":\"C+W无线网卡\"},\r\n" + 
				"{\"code\":\"46885051\",\"buissinessId\":\"IDB_SA_00155\",\"businessName\":\"AP\"},\r\n" + 
				"{\"code\":\"0022880\",\"buissinessId\":\"IDB_SA_00018\",\"businessName\":\"ADSL\"},\r\n" + 
				"{\"code\":\"148841374\",\"buissinessId\":\"IDB_SA_00185\",\"businessName\":\"ICT\"},\r\n" + 
				"{\"code\":\"194071251\",\"buissinessId\":\"IDB_SA_60016\",\"businessName\":\"综合办公\"},\r\n" + 
				"{\"code\":\"194071366\",\"buissinessId\":\"IDB_SA_60017\",\"businessName\":\"智机通\"},\r\n" + 
				"{\"code\":\"194071424\",\"buissinessId\":\"IDB_SA_60018\",\"businessName\":\"翼校通\"},\r\n" + 
				"{\"code\":\"194071498\",\"buissinessId\":\"IDB_SA_60019\",\"businessName\":\"外勤助手\"},\r\n" + 
				"{\"code\":\"211118499\",\"buissinessId\":\"IDB_SA_80004\",\"businessName\":\"延伸服务\"},\r\n" + 
				"{\"code\":\"147243037\",\"buissinessId\":\"IDB_SA_00078\",\"businessName\":\"普通WIFI\"},\r\n" + 
				"{\"code\":\"152995135\",\"buissinessId\":\"IDB_SA_00165\",\"businessName\":\"天翼对讲\"},\r\n" + 
				"{\"code\":\"0022977\",\"buissinessId\":\"IDB_SA_00140\",\"businessName\":\"无线数字医疗\"},\r\n" + 
				"{\"code\":\"0022978\",\"buissinessId\":\"IDB_SA_00138\",\"businessName\":\"车载信息服务\"},\r\n" + 
				"{\"code\":\"0022979\",\"buissinessId\":\"IDB_SA_00143\",\"businessName\":\"警务E通\"},\r\n" + 
				"{\"code\":\"0022980\",\"buissinessId\":\"IDB_SA_00131\",\"businessName\":\"政府应急管理\"},\r\n" + 
				"{\"code\":\"0022981\",\"buissinessId\":\"IDB_SA_00137\",\"businessName\":\"物流E通\"},\r\n" + 
				"{\"code\":\"0022982\",\"buissinessId\":\"IDB_SA_00133\",\"businessName\":\"环保E通\"},\r\n" + 
				"{\"code\":\"0022983\",\"buissinessId\":\"IDB_SA_00142\",\"businessName\":\"区域卫生信息平台\"},\r\n" + 
				"{\"code\":\"0022984\",\"buissinessId\":\"IDB_SA_00141\",\"businessName\":\"健康管理服务\"},\r\n" + 
				"{\"code\":\"0022985\",\"buissinessId\":\"IDB_SA_00130\",\"businessName\":\"税务E通\"},\r\n" + 
				"{\"code\":\"1631012\",\"buissinessId\":\"IDB_SA_00144\",\"businessName\":\"16300注册\"},\r\n" + 
				"{\"code\":\"17390377\",\"buissinessId\":\"IDB_SA_60015\",\"businessName\":\"工商放心通\"},\r\n" + 
				"{\"code\":\"35443116\",\"buissinessId\":\"IDB_SA_00091\",\"businessName\":\"电脑保姆\"},\r\n" + 
				"{\"code\":\"46884885\",\"buissinessId\":\"IDB_SA_00064\",\"businessName\":\"内部管理\"},\r\n" + 
				"{\"code\":\"49551083\",\"buissinessId\":\"IDB_SA_00160\",\"businessName\":\"外勤手机\"},\r\n" + 
				"{\"code\":\"36137323\",\"buissinessId\":\"IDB_SA_80001\",\"businessName\":\"整治工单\"},\r\n" + 
				"{\"code\":\"36353462\",\"buissinessId\":\"IDB_SA_80000\",\"businessName\":\"特殊工单\"},\r\n" + 
				"{\"code\":\"61866495\",\"buissinessId\":\"IDB_SA_00080\",\"businessName\":\"网络电话{NTX}\"},\r\n" + 
				"{\"code\":\"0001414\",\"buissinessId\":\"IDB_SA_50000\",\"businessName\":\"移动业务\"},\r\n" + 
				"{\"code\":\"0001415\",\"buissinessId\":\"IDB_SA_20000\",\"businessName\":\"宽带业务\"},\r\n" + 
				"{\"code\":\"0001416\",\"buissinessId\":\"IDB_SA_10000\",\"businessName\":\"语音业务\"},\r\n" + 
				"{\"code\":\"0003511\",\"buissinessId\":\"IDB_SA_90000\",\"businessName\":\"其他\"},\r\n" + 
				"{\"code\":\"022853\",\"buissinessId\":\"IDB_SA_30000\",\"businessName\":\"专线及专网业务\"},\r\n" + 
				"{\"code\":\"0022855\",\"buissinessId\":\"IDB_SA_40000\",\"businessName\":\"转型业务\"},\r\n" + 
				"{\"code\":\"0001953\",\"buissinessId\":\"IDB_SA_00014\",\"businessName\":\"小灵通障碍\"},\r\n" + 
				"{\"code\":\"0001959\",\"buissinessId\":\"IDB_SA_00070\",\"businessName\":\"CDMA\"},\r\n" + 
				"{\"code\":\"0001957\",\"buissinessId\":\"IDB_SA_00019\",\"businessName\":\"LAN{智能小区}\"},\r\n" + 
				"{\"code\":\"0022878\",\"buissinessId\":\"IDB_SA_00043\",\"businessName\":\"网络视讯{iTV}\"},\r\n" + 
				"{\"code\":\"0022927\",\"buissinessId\":\"IDB_SA_00059\",\"businessName\":\"定制终端\"},\r\n" + 
				"{\"code\":\"0022928\",\"buissinessId\":\"IDB_SA_00097\",\"businessName\":\"学子E行\"},\r\n" + 
				"{\"code\":\"0001954\",\"buissinessId\":\"IDB_SA_00020\",\"businessName\":\"卡业务\"},\r\n" + 
				"{\"code\":\"0022858\",\"buissinessId\":\"IDB_SA_00001\",\"businessName\":\"电话障碍\"},\r\n" + 
				"{\"code\":\"0022860\",\"buissinessId\":\"IDB_SA_00046\",\"businessName\":\"公用电话\"},\r\n" + 
				"{\"code\":\"0022929\",\"buissinessId\":\"IDB_SA_00103\",\"businessName\":\"综合虚拟网{IVPN}\"},\r\n" + 
				"{\"code\":\"0022899\",\"buissinessId\":\"IDB_SA_00037\",\"businessName\":\"彩铃平台\"},\r\n" + 
				"{\"code\":\"0022961\",\"buissinessId\":\"IDB_SA_00038\",\"businessName\":\"SP短信平台\"},\r\n" + 
				"{\"code\":\"0022962\",\"buissinessId\":\"IDB_SA_00017\",\"businessName\":\"ISDN\"},\r\n" + 
				"{\"code\":\"0022963\",\"buissinessId\":\"IDB_SA_00039\",\"businessName\":\"VOD平台\"},\r\n" + 
				"{\"code\":\"0022964\",\"buissinessId\":\"IDB_SA_00036\",\"businessName\":\"省集中邮件平台\"},\r\n" + 
				"{\"code\":\"0022965\",\"buissinessId\":\"IDB_SA_00110\",\"businessName\":\"全球眼{泰州}\"},\r\n" + 
				"{\"code\":\"0022966\",\"buissinessId\":\"IDB_SA_00113\",\"businessName\":\"10000号特殊事务\"},\r\n" + 
				"{\"code\":\"0022967\",\"buissinessId\":\"IDB_SA_00040\",\"businessName\":\"180申告\"},\r\n" + 
				"{\"code\":\"0022968\",\"buissinessId\":\"IDB_SA_00109\",\"businessName\":\"GOTA\"},\r\n" + 
				"{\"code\":\"0492964\",\"buissinessId\":\"IDB_SA_00021\",\"businessName\":\"VDSL\"},\r\n" + 
				"{\"code\":\"0022902\",\"buissinessId\":\"IDB_SA_00048\",\"businessName\":\"VPN{VPDN}\"},\r\n" + 
				"{\"code\":\"0022903\",\"buissinessId\":\"IDB_SA_00008\",\"businessName\":\"DDN\"},\r\n" + 
				"{\"code\":\"0022904\",\"buissinessId\":\"IDB_SA_00009\",\"businessName\":\"城域网\"},\r\n" + 
				"{\"code\":\"0022905\",\"buissinessId\":\"IDB_SA_00012\",\"businessName\":\"帧中继\"},\r\n" + 
				"{\"code\":\"0022906\",\"buissinessId\":\"IDB_SA_00011\",\"businessName\":\"ATM\"},\r\n" + 
				"{\"code\":\"0022907\",\"buissinessId\":\"IDB_SA_00054\",\"businessName\":\"专网电话（DID）\"},\r\n" + 
				"{\"code\":\"0022908\",\"buissinessId\":\"IDB_SA_00055\",\"businessName\":\"电话虚拟网\"},\r\n" + 
				"{\"code\":\"0022909\",\"buissinessId\":\"IDB_SA_00005\",\"businessName\":\"MSTP\"},\r\n" + 
				"{\"code\":\"0022910\",\"buissinessId\":\"IDB_SA_00007\",\"businessName\":\"租纤\"},\r\n" + 
				"{\"code\":\"0022911\",\"buissinessId\":\"IDB_SA_00006\",\"businessName\":\"数字电路\"},\r\n" + 
				"{\"code\":\"0022912\",\"buissinessId\":\"IDB_SA_00030\",\"businessName\":\"教育网\"},\r\n" + 
				"{\"code\":\"0022913\",\"buissinessId\":\"IDB_SA_00024\",\"businessName\":\"ADSL{专线}\"},\r\n" + 
				"{\"code\":\"0022914\",\"buissinessId\":\"IDB_SA_00041\",\"businessName\":\"话务台\"},\r\n" + 
				"{\"code\":\"0022915\",\"buissinessId\":\"IDB_SA_00023\",\"businessName\":\"专线光纤宽带{LAN专线}\"},\r\n" + 
				"{\"code\":\"0022916\",\"buissinessId\":\"IDB_SA_00102\",\"businessName\":\"国际及港澳台A端业务\"},\r\n" + 
				"{\"code\":\"0022917\",\"buissinessId\":\"IDB_SA_00013\",\"businessName\":\"PRI\"},\r\n" + 
				"{\"code\":\"0022918\",\"buissinessId\":\"IDB_SA_00042\",\"businessName\":\"MPLS VPN\"},\r\n" + 
				"{\"code\":\"0022920\",\"buissinessId\":\"IDB_SA_00015\",\"businessName\":\"X.25分组\"},\r\n" + 
				"{\"code\":\"0022857\",\"buissinessId\":\"IDB_SA_00045\",\"businessName\":\"号码百事通\"},\r\n" + 
				"{\"code\":\"0022879\",\"buissinessId\":\"IDB_SA_00047\",\"businessName\":\"宝宝在线\"},\r\n" + 
				"{\"code\":\"0022919\",\"buissinessId\":\"IDB_SA_00101\",\"businessName\":\"WAC{超级汇线通}\"},\r\n" + 
				"{\"code\":\"0022930\",\"buissinessId\":\"IDB_SA_00100\",\"businessName\":\"协同通信\"},\r\n" + 
				"{\"code\":\"0022931\",\"buissinessId\":\"IDB_SA_00121\",\"businessName\":\"企业翼机通\"},\r\n" + 
				"{\"code\":\"0022932\",\"buissinessId\":\"IDB_SA_00120\",\"businessName\":\"校园翼机通\"},\r\n" + 
				"{\"code\":\"0022933\",\"buissinessId\":\"IDB_SA_00112\",\"businessName\":\"4008业务\"},\r\n" + 
				"{\"code\":\"0022934\",\"buissinessId\":\"IDB_SA_00105\",\"businessName\":\"移动全球眼\"},\r\n" + 
				"{\"code\":\"0022935\",\"buissinessId\":\"IDB_SA_00058\",\"businessName\":\"中小企业IT外包\"},\r\n" + 
				"{\"code\":\"0022936\",\"buissinessId\":\"IDB_SA_00106\",\"businessName\":\"物流E通\"},\r\n" + 
				"{\"code\":\"0022937\",\"buissinessId\":\"IDB_SA_00122\",\"businessName\":\"数字城管\"},\r\n" + 
				"{\"code\":\"0022938\",\"buissinessId\":\"IDB_SA_00107\",\"businessName\":\"销售管家\"},\r\n" + 
				"{\"code\":\"0022939\",\"buissinessId\":\"IDB_SA_00126\",\"businessName\":\"综合办公定制版（EMA）\"},\r\n" + 
				"{\"code\":\"0022940\",\"buissinessId\":\"IDB_SA_00029\",\"businessName\":\"新视通\"},\r\n" + 
				"{\"code\":\"0022941\",\"buissinessId\":\"IDB_SA_00125\",\"businessName\":\"其他行业应用\"},\r\n" + 
				"{\"code\":\"0022942\",\"buissinessId\":\"IDB_SA_00049\",\"businessName\":\"IDC\"},\r\n" + 
				"{\"code\":\"0022943\",\"buissinessId\":\"IDB_SA_00124\",\"businessName\":\"车管专家\"},\r\n" + 
				"{\"code\":\"0022944\",\"buissinessId\":\"IDB_SA_00016\",\"businessName\":\"全球眼\"},\r\n" + 
				"{\"code\":\"0022945\",\"buissinessId\":\"IDB_SA_00002\",\"businessName\":\"特殊事务\"},\r\n" + 
				"{\"code\":\"0022946\",\"buissinessId\":\"IDB_SA_00128\",\"businessName\":\"工商E通\"},\r\n" + 
				"{\"code\":\"0022947\",\"buissinessId\":\"IDB_SA_00028\",\"businessName\":\"商务领航\"},\r\n" + 
				"{\"code\":\"0022948\",\"buissinessId\":\"IDB_SA_00127\",\"businessName\":\"渔信E通\"},\r\n" + 
				"{\"code\":\"0022949\",\"buissinessId\":\"IDB_SA_00031\",\"businessName\":\"网管专家\"},\r\n" + 
				"{\"code\":\"471280958\",\"buissinessId\":\"IDB_SA_00066\",\"businessName\":\"平安监控（无锡）\"},\r\n" + 
				"{\"code\":\"589173180\",\"buissinessId\":\"IDB_SA_00099\",\"businessName\":\"IPRAN\"},\r\n" + 
				"{\"code\":\"602809230\",\"buissinessId\":\"IDB_SA_80023\",\"businessName\":\"群障确认\"},\r\n" + 
				"{\"code\":\"642527747\",\"buissinessId\":\"IDB_SA_80024\",\"businessName\":\"续约工单\"},\r\n" + 
				"{\"code\":\"664780390\",\"buissinessId\":\"IDB_SA_000720\",\"businessName\":\"awifi行业版\"},\r\n" + 
				"{\"code\":\"720356818\",\"buissinessId\":\"IDB_SA_300214\",\"businessName\":\"调度工单修障\"},\r\n" + 
				"{\"code\":\"234997133\",\"buissinessId\":\"IDB_SA_90004\",\"businessName\":\"综合办公网络版（NMA）\"},\r\n" + 
				"{\"code\":\"317181764\",\"buissinessId\":\"IDB_SA_SERVICE_001\",\"businessName\":\"客户网络监控\"},\r\n" + 
				"{\"code\":\"239446714\",\"buissinessId\":\"IDB_SA_80003\",\"businessName\":\"WLAN和学子E行卡\"},\r\n" + 
				"{\"code\":\"448411140\",\"buissinessId\":\"IDB_SA_\",\"businessName\":\"安全领航产品\"},\r\n" + 
				"{\"code\":\"448413455\",\"buissinessId\":\"IDB_SA_00060\",\"businessName\":\"安全评估\"},\r\n" + 
				"{\"code\":\"448414171\",\"buissinessId\":\"IDB_SA_00062\",\"businessName\":\"流量安全管理\"},\r\n" + 
				"{\"code\":\"448414426\",\"buissinessId\":\"IDB_SA_00063\",\"businessName\":\"云清洗\"},\r\n" + 
				"{\"code\":\"460697811\",\"buissinessId\":\"IDB_SA_80010\",\"businessName\":\"行业应用卡单\"},\r\n" + 
				"{\"code\":\"505640882\",\"buissinessId\":\"IDB_SA_40521\",\"businessName\":\"IPOSS系统\"},\r\n" + 
				"{\"code\":\"505641165\",\"buissinessId\":\"IDB_SA_40523\",\"businessName\":\"OSS系统\"},\r\n" + 
				"{\"code\":\"505642391\",\"buissinessId\":\"IDB_SA_40527\",\"businessName\":\"其他系统\"},\r\n" + 
				"{\"code\":\"514251092\",\"buissinessId\":\"IDB_SA_WIFI\",\"businessName\":\"WIFI联盟行业版\"},\r\n" + 
				"{\"code\":\"720355800\",\"buissinessId\":\"IDB_SA_300212\",\"businessName\":\"调度工单\"},\r\n" + 
				"{\"code\":\"720356478\",\"buissinessId\":\"IDB_SA_300213\",\"businessName\":\"调度工单装机\"},\r\n" + 
				"{\"code\":\"317183423\",\"buissinessId\":\"IDB_SA_00108\",\"businessName\":\"客户端接入设备\"},\r\n" + 
				"{\"code\":\"369218210\",\"buissinessId\":\"IDB_SA_00115\",\"businessName\":\"A-WIFI障碍\"},\r\n" + 
				"{\"code\":\"505642249\",\"buissinessId\":\"IDB_SA_40526\",\"businessName\":\"ITMS系统\"},\r\n" + 
				"{\"code\":\"505640752\",\"buissinessId\":\"IDB_SA_40520\",\"businessName\":\"综调系统\"},\r\n" + 
				"{\"code\":\"505640996\",\"buissinessId\":\"IDB_SA_40522\",\"businessName\":\"测速平台\"},\r\n" + 
				"{\"code\":\"513395124\",\"buissinessId\":\"IDB_SA_80022\",\"businessName\":\"现场核查\"},\r\n" + 
				"{\"code\":\"541853572\",\"buissinessId\":\"IDB_SA_00085\",\"businessName\":\"一呼通（无锡）\"},\r\n" + 
				"{\"code\":\"582335403\",\"buissinessId\":\"IDB_SA_300211\",\"businessName\":\"督办单{盐城}\"},\r\n" + 
				"{\"code\":\"243707937\",\"buissinessId\":\"IDB_SA_90006\",\"businessName\":\"天翼看店\"},\r\n" + 
				"{\"code\":\"243708213\",\"buissinessId\":\"IDB_SA_90007\",\"businessName\":\"手机管家\"},\r\n" + 
				"{\"code\":\"448413931\",\"buissinessId\":\"IDB_SA_00061\",\"businessName\":\"WEB网站安全监控\"},\r\n" + 
				"{\"code\":\"705713614\",\"buissinessId\":\"IDB_SA_01017\",\"businessName\":\"智能iTV\"},\r\n" + 
				"{\"code\":\"662207718\",\"buissinessId\":\"IDB_SA_01015\",\"businessName\":\"天翼高清田园版\"},\r\n" + 
				"{\"code\":\"317182362\",\"buissinessId\":\"IDB_SA_00104\",\"businessName\":\"电路监控\"},\r\n" + 
				"{\"code\":\"369217619\",\"buissinessId\":\"IDB_SA_00114\",\"businessName\":\"A-WIFI开通\"},\r\n" + 
				"{\"code\":\"394982376\",\"buissinessId\":\"IDB_SA_00119\",\"businessName\":\"预检预修\"},\r\n" + 
				"{\"code\":\"494724636\",\"buissinessId\":\"IDB_SA_80021\",\"businessName\":\"投申诉工单\"},\r\n" + 
				"{\"code\":\"505641460\",\"buissinessId\":\"IDB_SA_40524\",\"businessName\":\"话务系统\"},\r\n" + 
				"{\"code\":\"505642047\",\"buissinessId\":\"IDB_SA_40525\",\"businessName\":\"掌调系统\"},\r\n" + 
				"{\"code\":\"505640532\",\"buissinessId\":\"IDB_SA_300210\",\"businessName\":\"IT支撑系统\"},\r\n" + 
				"{\"code\":\"546878140\",\"buissinessId\":\"IDB_SA_00004\",\"businessName\":\"客户需求\"},\r\n" + 
				"{\"code\":\"426545086\",\"buissinessId\":\"IDB_SA_00716\",\"businessName\":\"疑难障碍\"},\r\n" + 
				"{\"code\":\"234993029\",\"buissinessId\":\"IDB_SA_90003\",\"businessName\":\"集约化产品\"},\r\n" + 
				"{\"code\":\"234999061\",\"buissinessId\":\"IDB_SA_90005\",\"businessName\":\"外包呼叫中心\"},\r\n" + 
				"{\"code\":\"317183809\",\"buissinessId\":\"IDB_SA_00111\",\"businessName\":\"私网监控\"},\r\n" + 
				"{\"code\":\"0022876\",\"buissinessId\":\"IDB_SA_00044\",\"businessName\":\"iTV互联网电视{ott}\"},\r\n" + 
				"{\"code\":\"458721090\",\"buissinessId\":\"IDB_SA_00150\",\"businessName\":\"云业务\"},\r\n" + 
				"{\"code\":\"584140551\",\"buissinessId\":\"IDB_SA_00086\",\"businessName\":\"大面积确认单\"}]";
		JSONArray arr=JSONArray.fromObject(str);
		for(int i=0;i<arr.size();i++) {
			j=arr.getJSONObject(i);
			if(id.equals(j.getString("buissinessId"))) {
				break;
			}
		}
		return j.getString("code");
	}
	
}
