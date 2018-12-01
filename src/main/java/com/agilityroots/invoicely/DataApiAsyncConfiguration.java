/**
 *  20-Oct-2018 DataApiAsyncConfiguration.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author anadi
 *
 */
@Configuration
@EnableAsync
public class DataApiAsyncConfiguration {

	@Bean(name = "webExecutor")
	public AsyncTaskExecutor webAsyncExecutor() {

		ThreadPoolTaskExecutor webExecutor = new ThreadPoolTaskExecutor();
		webExecutor.setCorePoolSize(5);
		webExecutor.setMaxPoolSize(20);
		webExecutor.setQueueCapacity(100);
		webExecutor.setThreadNamePrefix("ApiPool-");
		webExecutor.initialize();
		return webExecutor;
	}
}
