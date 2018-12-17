package com.agilityroots.invoicely;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

@SpringBootApplication
@EnableAsync
@EnableSpringDataWebSupport
@EntityScan(basePackages = { "com.agilityroots.invoicely.entity" })
@EnableJpaRepositories(basePackages = { "com.agilityroots.invoicely.repository" })
@EnableJpaAuditing
@EnableTransactionManagement
public class DataApiApplication {

	@Bean
	public Module hibernate5Module() {
		return new Hibernate5Module();
	}
	
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
	
	public static void main(String[] args) {
		SpringApplication.run(DataApiApplication.class, args);
	}
}
