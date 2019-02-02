/**
 * 
 */
package com.agilityroots.invoicely;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
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
@EnableCaching
public class DataApiJpaConfiguration {

}
