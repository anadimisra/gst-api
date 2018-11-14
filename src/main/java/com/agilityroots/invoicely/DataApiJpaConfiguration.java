/**
 *  20-Oct-2018 DataApiJpaConfiguration.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author anadi
 *
 */
@Configuration
@EntityScan(basePackages = { "com.agilityroots.invoicely.entity" })
@EnableJpaRepositories(basePackages = { "com.agilityroots.invoicely.repository" })
@EnableJpaAuditing
@EnableTransactionManagement
public class DataApiJpaConfiguration {

}
