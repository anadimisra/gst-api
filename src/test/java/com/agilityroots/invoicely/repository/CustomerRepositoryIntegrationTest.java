/**
 * 30 Nov 2018 CustomerRepositoryIntegrationTests.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author anadi
 *
 */
@Slf4j
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = {DataApiJpaConfiguration.class})
@TestPropertySource(locations = {"classpath:application-it.properties", "classpath:application-test.properties"})
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CustomerRepositoryIntegrationTest {

  @Autowired
  private CustomerRepository customerRepository;

  @PersistenceContext
  private EntityManager em;

  @Test
  public void testPrePersistAddsMandatoryFields() {
    log.info("This test is only for documenting default value configration done via @DynamicInsert and @ColumnDefault");
    Customer bhau = new Customer();
    bhau.setName("Bhau & Sons Pvt. Ltd.");
    bhau.setDomain(RandomStringUtils.randomAlphanumeric(8));
    bhau = customerRepository.saveAndFlush(bhau);
    em.clear();
    Customer badaBhau = customerRepository.findById(bhau.getId()).get();
    assertThat(badaBhau.getTds().doubleValue()).isEqualTo(0.10);
    assertThat(badaBhau.getInvoicePrefix()).isEqualTo("INV");
    assertThat(badaBhau.getCurrency()).isEqualTo("INR");
  }

}
