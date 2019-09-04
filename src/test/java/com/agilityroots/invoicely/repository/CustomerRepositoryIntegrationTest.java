/**
 * 30 Nov 2018 CustomerRepositoryIntegrationTests.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@DataJpaTest
@ContextConfiguration(classes = {DataApiJpaConfiguration.class})
@TestPropertySource(locations = {"classpath:application-it.properties", "classpath:application-test.properties"})
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class CustomerRepositoryIntegrationTest {

  @Autowired
  private CustomerRepository customerRepository;

  private final EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @PersistenceContext
  private EntityManager em;
  @Autowired
  private CompanyRepository companyRepository;

  @Test
  public void testPrePersistAddsMandatoryFields() {
    log.info("This test is only for documenting default value configration done via @DynamicInsert and @ColumnDefault");
    Company company = companyRepository.saveAndFlush(builder.getCompanyObject());
    Customer bhau = new Customer();
    bhau.setCompany(company);
    bhau.setName("Bhau & Sons Pvt. Ltd.");
    bhau.setOrganisationId(RandomStringUtils.randomAlphanumeric(8));
    bhau = customerRepository.saveAndFlush(bhau);
    em.clear();
    Customer badaBhau = customerRepository.findById(bhau.getId()).get();
    assertThat(badaBhau.getTds().doubleValue()).isEqualTo(0.10);
    assertThat(badaBhau.getInvoicePrefix()).isEqualTo("INV");
    assertThat(badaBhau.getCurrency()).isEqualTo("INR");
  }

  @Test
  public void testFindByCompanyId() {
    Company company = companyRepository.saveAndFlush(builder.getCompanyObject());
    Customer customer = builder.getCustomerObject();
    customer.setCompany(company);
    customerRepository.saveAndFlush(customer);
    em.clear();
    Page<Customer> result = customerRepository.findByCompany_Id(company.getId(), PageRequest.of(0, 5));
    assertThat(result.getContent().size()).isEqualTo(1);
    assertThat(result.getContent().get(0).getCompany().getName()).isEqualTo(company.getName());
  }

  @Test
  public void testFindOneByOrgnaisationId() {
    Company company = companyRepository.saveAndFlush(builder.getCompanyObject());
    em.clear();
    Customer customer = builder.getCustomerObject();
    customer.setCompany(company);
    customerRepository.saveAndFlush(customer);
    em.clear();
    Customer result = customerRepository.findOneByOrganisationId(customer.getOrganisationId()).get();
    assertThat(result.getName()).isEqualTo(customer.getName());
  }
}
