/**
 *  30 Nov 2018 CustomerRepositoryIntegrationTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.persistence.EntityManager;

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
import org.springframework.test.context.junit4.SpringRunner;

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
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class CustomerRepositoryIntegrationTests {

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
		customer = customerRepository.save(customer);
		customerId = customer.getId();
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
	public void testNonExistingCustomerGivesEmptyOptional() throws InterruptedException, ExecutionException {

		Optional<Customer> result = Optional
				.ofNullable(customerRepository.findEagerFetchBranchesById(Long.valueOf(10)));
		assertThat(result).isNotNull();
		assertThat(result).isEmpty();

	}

	@Test
	public void testEagerLoadBranchesByCustomer() throws InterruptedException, ExecutionException {

		Optional<Customer> result = Optional.ofNullable(customerRepository.findEagerFetchBranchesById(customerId));
		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
		assertThat(result.map(Customer::getBranches).map(List::size).get()).isEqualTo(1);

	}

	@Test
	public void testPageWhenNoCustomerRecordsArePresent() {

		customerRepository.deleteAll();
		customerRepository.flush();
		Page<Customer> page = customerRepository.findAll(PageRequest.of(0, 20));
		assertThat(page).isNotNull();
		assertThat(page.hasContent()).isFalse();
	}

	@Test
	public void testPrePersistAddsMandatoryFields() {

		Customer minty = new Customer();
		minty.setName("Minty & Sons Pvt. Ltd.");
		minty.setPan(RandomStringUtils.randomAlphanumeric(10));
		Customer badiMinty = customerRepository.saveAndFlush(minty);
		assertThat(badiMinty.getTds().doubleValue()).isEqualTo(0.10);
		assertThat(badiMinty.getInvoicePrefix()).isEqualTo("INV");
		assertThat(badiMinty.getCurrecny()).isEqualTo("INR");
	}
}
