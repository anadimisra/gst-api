/**
 *  30 Nov 2018 CustomerRepositoryIntegrationTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
@ContextConfiguration(classes = { DataApiJpaConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-unit-test.properties")
public class CustomerRepositoryIntegrationTest {

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private BranchRepository branchRepository;

  @Autowired
  private ContactRepository contactRepository;

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  private Contact contact;

  private static long id;

  @Before
  public void setup() {
    Customer customer = builder.getCustomerObject();
    customer = customerRepository.saveAndFlush(customer);
    id = customer.getId();
    log.debug("Persisted with id:{} in DB the test Customer object: {}", id, customer);

  }

  @Test
  public void testPrePersistAddsMandatoryFields() {

    Customer chanchu = new Customer();
    chanchu.setName("Chanchu & Sons Pvt. Ltd.");
    chanchu.setPan(RandomStringUtils.randomAlphanumeric(10));
    Customer badaChanchu = customerRepository.saveAndFlush(chanchu);
    assertThat(badaChanchu.getTds().doubleValue()).isEqualTo(0.10);
    assertThat(badaChanchu.getInvoicePrefix()).isEqualTo("INV");
    assertThat(badaChanchu.getCurrecny()).isEqualTo("INR");
  }

  @Test
  public void testAddingBranchesToCustomer() {
    Customer customer = customerRepository.findById(id).get();
    Branch branch = builder.getBranchObject();
    branch.setOwner(customer);
    branchRepository.saveAndFlush(branch);
    Page<Branch> branches = branchRepository.findAllByOwner_Id(customer.getId(), PageRequest.of(0, 10));
    assertThat(branches.getContent().size()).isEqualTo(1);
  }

  @Test
  public void testAddingContactToCustomer() {
    contact = contactRepository.saveAndFlush(builder.getContactObject());
    Optional<Customer> customer = customerRepository.findById(id);
    customer.ifPresent(it -> {

      it.setContact(contact);
      customerRepository.saveAndFlush(it);
    });
    assertThat(customerRepository.findById(id).get().getContact()).isEqualTo(contact);
  }

}
