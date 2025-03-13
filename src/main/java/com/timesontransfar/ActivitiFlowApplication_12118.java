package com.timesontransfar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(exclude = {
		DataSourceAutoConfiguration.class,
		RedisAutoConfiguration.class,
		RedisRepositoriesAutoConfiguration.class})
@EnableFeignClients(basePackages = { "com.timesontransfar" })
@EnableDiscoveryClient
@ImportResource(locations = { "classpath:spring-common.xml" })
@ServletComponentScan
@ComponentScan(basePackages = {"com.transfar.common.log", "com.timesontransfar","com.transfar.utils","com.transfar.service.impl","com.transfar.config"})
public class ActivitiFlowApplication_12118 {
	public static void main(String[] args) {
		SpringApplication.run(ActivitiFlowApplication_12118.class, args);
	}
}