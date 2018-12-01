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
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.RandomStringUtils;
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
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.entity.Address;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.github.javafaker.Faker;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
@ContextConfiguration(classes = { DataApiJpaConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class BranchRepositoryIntegrationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(BranchRepositoryIntegrationTests.class);

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	EntityManager entityManager;

	private Faker faker = new Faker(new Locale("en-IND"));

	private Long customerId;

	@Before
	public void setup() {
		Customer customer = new Customer();
		customer.setName(faker.company().name());
		customer.setPan(RandomStringUtils.randomAlphanumeric(10));
		customer.setTds(0.10);

		Branch branch = getBranchObject();
		branch = branchRepository.save(branch);

		customer.setBranches(Arrays.asList(branch));
		Customer saved = customerRepository.save(customer);
		customerId = saved.getId();
	}

	/**
	 * @return {@link Branch} object
	 */
	private Branch getBranchObject() {
		Address address = new Address();
		address.setStreetAddress(faker.address().streetAddress());
		address.setArea(faker.address().streetName());
		address.setCity(faker.address().city());
		address.setState(faker.address().state());
		address.setPincode(faker.address().zipCode());

		Contact contact = new Contact();
		contact.setName(faker.name().fullName());
		contact.setEmail(faker.internet().emailAddress());
		contact.setPhone(RandomStringUtils.randomNumeric(10));
		contact = contactRepository.save(contact);

		Branch branch = new Branch();
		branch.setBranchName("Main Branch");
		branch.setGstin(RandomStringUtils.randomAlphabetic(15));
		branch.setSez(Boolean.FALSE);
		branch.setContact(contact);
		branch.setAddress(address);
		return branch;
	}

	@Test
	public void testBranchEqualityWhenAddingBranchToCustomers() throws InterruptedException, ExecutionException {
		Customer customer = customerRepository.findEagerFetchBranchesById(customerId).get();
		Branch branch = getBranchObject();
		branch.setBranchName("Other Branch");
		branch = branchRepository.save(branch);
		LOGGER.debug("Saved Branch with id {} and details {}", branch.getId(), branch.toString());
		List<Branch> branches = new ArrayList<Branch>();
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
