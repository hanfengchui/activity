package com.timesontransfar.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.timesontransfar.customservice.orderask.service.IworkSheetToITSMWebService;
import com.timesontransfar.pubWebservice.WorkSheetToZDWebService;

import java.util.concurrent.Future;

@Component(value = "WorkSheetToITSMAsync")
public class WorkSheetToITSMAsync {
	protected Logger log = LoggerFactory.getLogger(WorkSheetToITSMAsync.class);
	@Autowired 
	private IworkSheetToITSMWebService  workSheetToITSMWebService;
	@Autowired
	private WorkSheetToZDWebService workSheetToZDWebService;
	
	/**
	 * Description: 异步ITSM 接口 请求<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param orderId
	 * @throws InterruptedException <br>
	 * @CreateDate 2020年6月3日 上午9:56:47 <br>
	 */
	@Async
	public void toITSMInfo(String orderId){
		log.info("调用ITSM 推送单号： {}", orderId);
		long startTime = System.currentTimeMillis();
		workSheetToITSMWebService.executeXML(orderId);
		log.info("调用ITSM 服务耗时： {}", (System.currentTimeMillis() - startTime));
	}

	// 异步调用综调受理接口
	@Async
	public void toZDInfo(String orderId) {
		log.info("调用综调 推送单号： {}", orderId);
		long startTime = System.currentTimeMillis();
		workSheetToZDWebService.executeXML(orderId);
		log.info("调用综调 服务耗时： {}", (System.currentTimeMillis() - startTime));
	}

	// 异步方式
	@HystrixCommand(fallbackMethod = "setError")
	public Future<String> setResultAsync(String method) {
		return new AsyncResult<String>() {
			@Override
			public String invoke() {
				try {
					log.info("异步请求方法");
					return "success";
				} catch (Exception e) {
					return "exception";
				}
			}
			@Override
			public String get() {
				return invoke();
			}
		};
	}
}
