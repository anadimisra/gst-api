/**
 * 30 Nov 2018 BranchRepositoryIntegrationTests.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Customer;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = {DataApiJpaConfiguration.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class BranchRepositoryIntegrationTest {

  Customer customer;
  @Autowired
  private CustomerRepository customerRepository;
  @Autowired
  private BranchRepository branchRepository;
  @Autowired
  private ContactRepository contactRepository;
  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @Before
  public void setup() {

    customer = builder.getCustomerWithContact();
    contactRepository.save(customer.getContact());
    customerRepository.saveAndFlush(customer);
    Branch branch = builder.getBranchWithContactObject();
    branch.setOwner(customer);
    contactRepository.save(branch.getContact());
    branchRepository.saveAndFlush(branch);
  }

  @Test
  public void testFindAllBranchesByOwnerIdPageable() {

    Page<Branch> result = branchRepository.findAllByOwner_Id(customer.getId(), PageRequest.of(0, 10));
    assertThat(result.getContent().size()).isEqualTo(1);
  }

  public void testFindAllBranchesByOwnerId() {

    List<Branch> result = branchRepository.findAllByOwner_Id(customer.getId());
    assertThat(result.size()).isEqualTo(1);
  }
}
