package com.agilityroots.invoicely;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.sendgrid.SendGrid;

@SpringBootApplication
@EnableAsync
@EnableSpringDataWebSupport
public class DataApiApplication {

  @Bean
  public Module hibernate5Module() {
    return new Hibernate5Module();
  }

  @Bean(name = "taskExecutor")
  public AsyncTaskExecutor webAsyncExecutor() {

    ThreadPoolTaskExecutor webExecutor = new ThreadPoolTaskExecutor();
    webExecutor.setCorePoolSize(5);
    webExecutor.setMaxPoolSize(20);
    webExecutor.setQueueCapacity(100);
    webExecutor.setThreadNamePrefix("ApiPool-");
    webExecutor.initialize();
    return webExecutor;
  }

  @Bean(name = "mailExecutor")
  public AsyncTaskExecutor mailAsyncExecutor() {

    ThreadPoolTaskExecutor webExecutor = new ThreadPoolTaskExecutor();
    webExecutor.setCorePoolSize(5);
    webExecutor.setMaxPoolSize(20);
    webExecutor.setQueueCapacity(100);
    webExecutor.setThreadNamePrefix("Mail-");
    webExecutor.initialize();
    return webExecutor;
  }

  @Bean
  FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
    FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
    bean.setFilter(new ForwardedHeaderFilter());
    return bean;
  }

  @Value("${sendgrid.api.key}")
  String sendGridAPIKey;

  @Bean
  public SendGrid sendGrid() {
    return new SendGrid(sendGridAPIKey);
  }

  public static void main(String[] args) {
    SpringApplication.run(DataApiApplication.class, args);
  }
}
