/**
 *  30 Nov 2018 BranchRepositoryIntegrationTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
@ContextConfiguration(classes = { DataApiJpaConfiguration.class })
@TestPropertySource(locations = "classpath:application-unit-test.properties")
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class BranchRepositoryIntegrationTests {

  private static final Logger LOGGER = LoggerFactory.getLogger(BranchRepositoryIntegrationTests.class);

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private BranchRepository branchRepository;

  @Autowired
  private ContactRepository contactRepository;

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  private Customer customer;

  @Before
  public void setup() {
    Contact contact = builder.getContactObject();
    contact = contactRepository.save(contact);
    Branch branch = builder.getBranchObject();
    branch.setContact(contact);
    branch = branchRepository.save(branch);
    customer = builder.getCustomerObject();
    customer.setBranches(Arrays.asList(branch));
    customer = customerRepository.save(customer);
  }

  @Test
  public void testBranchEqualityWhenAddingBranchToCustomers() throws InterruptedException, ExecutionException {
    Branch branch = builder.getBranchObject();
    branch.setBranchName("Other Branch");
    branch = branchRepository.save(branch);
    LOGGER.debug("Saved Branch with id {} and details {}", branch.getId(), branch.toString());
    List<Branch> branches = new ArrayList<>();
    branches.addAll(customer.getBranches());
    branches.add(branch);
    customer.setBranches(branches);
    Customer saved = customerRepository.save(customer);
    assertThat(saved.getBranches().size()).isEqualTo(2);

    Branch updatedBranch = saved.getBranches().get(1);
    updatedBranch.setBranchName("Corporate Office");
    branchRepository.save(updatedBranch);
    // Changing the name should not create a new branch object
    List<Branch> allBranches = branchRepository.findAll();
    assertThat(allBranches.size()).isEqualTo(2);
    // Changed name should not change equality
    assertThat(allBranches.contains(branch));
  }
}
