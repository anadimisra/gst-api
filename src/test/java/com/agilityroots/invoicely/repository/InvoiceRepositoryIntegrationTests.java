/**
 *  13-Nov-2018 InvoiceRepositoryIntegrationTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.entity.Address;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.Payment;
import com.github.javafaker.Faker;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class InvoiceRepositoryIntegrationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(InvoiceRepositoryIntegrationTests.class);

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private CompanyRepository companyRepository;

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private PaymentRepository paymentRepository;

	@Autowired
	private InvoiceRepository invoiceRepository;

	private Faker faker = new Faker(new Locale("en-IND"));

	private Long customerId;

	private Invoice invoice;

	@Before
	public void setup() {
		Customer customer = new Customer();
		customer.setName(faker.company().name());
		customer.setPan(RandomStringUtils.randomAlphanumeric(10));
		customer.setTds(0.10);

		Address address = new Address();
		address.setStreetAddress(faker.address().streetAddress());
		address.setArea(faker.address().streetName());
		address.setCity(faker.address().city());
		address.setState(faker.address().state());
		address.setPincode(faker.address().zipCode());

		Contact contact = new Contact();
		contact.setName(faker.name().fullName());
		contact.setEmail(faker.internet().emailAddress());
		contact.setPhone("0804126182");

		contact = contactRepository.saveAndFlush(contact);

		Branch branch = new Branch();
		branch.setBranchName("Main Branch");
		branch.setGstin(RandomStringUtils.randomAlphabetic(15));
		branch.setSez(Boolean.FALSE);
		branch.setContact(contact);
		branch.setAddress(address);

		branch = branchRepository.saveAndFlush(branch);

		customer.setBranches(Arrays.asList(branch));
		Customer saved = customerRepository.saveAndFlush(customer);
		customerId = saved.getId();

		Address companyAddress = new Address();
		companyAddress.setStreetAddress(faker.address().streetAddress());
		companyAddress.setArea(faker.address().streetName());
		companyAddress.setCity(faker.address().city());
		companyAddress.setState(faker.address().state());
		companyAddress.setPincode(faker.address().zipCode());

		Contact companyContact = new Contact();
		companyContact.setName(faker.name().fullName());
		companyContact.setEmail(faker.internet().emailAddress());
		companyContact.setPhone("0802334601");
		companyContact = contactRepository.saveAndFlush(companyContact);

		Branch companyBranch = new Branch();
		companyBranch.setBranchName("Some Branch");
		companyBranch.setGstin(RandomStringUtils.randomAlphabetic(15));
		companyBranch.setSez(Boolean.FALSE);
		companyBranch.setContact(companyContact);
		companyBranch.setAddress(companyAddress);
		companyBranch = branchRepository.saveAndFlush(companyBranch);

		Company company = new Company();
		company.setName("Ruchi And Sons Pvt. Ltd.");
		company.setCin(RandomStringUtils.randomAlphanumeric(21));
		company.setPan(RandomStringUtils.randomAlphanumeric(10));
		company.setTan(RandomStringUtils.randomAlphabetic(10));
		companyRepository.save(company);

		invoice = new Invoice();
		invoice.setBilledFrom(companyBranch);
		invoice.setBilledTo(saved.getBranches().get(0));
		invoice.setCustomer(saved);

	}

	@Test
	public void testFindAllPaidInvoices() throws Exception {

		Customer customer = customerRepository.findById(customerId).get();
		invoice.setDueDate(
				Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setPaymentTerms("NET-30");
		invoice.setInvoiceDate(
				Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setInvoiceNumber(customer.getInvoicePrefix() + LocalDate.now().minusDays(40).toString());
		invoice.setPlaceOfSupply("Karnataka");

		Payment payment = new Payment();
		payment.setAmount(1000.00);
		payment.setAdjustmentName("TDS");
		payment.setAdjustmentValue(100.00);
		payment.setPaymentDate(
				Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		payment = paymentRepository.save(payment);
		invoice.setPayments(Arrays.asList(payment));
		Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

		Page<Invoice> paidInvoices = invoiceRepository.findByPaymentsIsNotNull(PageRequest.of(0, 10)).get();
		assertThat(paidInvoices).isNotEmpty();
		assertThat(paidInvoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
	}

	@Test
	public void testFindAllPendingInvoices() throws Exception {

		Customer customer = customerRepository.findById(customerId).get();
		invoice.setDueDate(Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setPaymentTerms("NET-30");
		invoice.setInvoiceDate(
				Date.from(LocalDate.now().minusDays(20).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setInvoiceNumber(customer.getInvoicePrefix() + LocalDate.now().minusDays(40).toString());
		invoice.setPlaceOfSupply("Karnataka");
		Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

		Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
		Page<Invoice> pendingInvoices = invoiceRepository
				.findByPaymentsIsNullAndDueDateAfter(today, PageRequest.of(0, 10)).get();
		assertThat(pendingInvoices).isNotEmpty();
		assertThat(pendingInvoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
	}

	@Test
	public void testFindAllOverdueInvoices() throws Exception {

		Customer customer = customerRepository.findById(customerId).get();
		invoice.setDueDate(
				Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setPaymentTerms("NET-30");
		invoice.setInvoiceDate(
				Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setInvoiceNumber(customer.getInvoicePrefix() + LocalDate.now().minusDays(40).toString());
		invoice.setPlaceOfSupply("Karnataka");
		Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

		Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
		Page<Invoice> pendingInvoices = invoiceRepository
				.findByPaymentsIsNullAndDueDateBefore(today, PageRequest.of(0, 10)).get();
		assertThat(pendingInvoices).isNotEmpty();
		assertThat(pendingInvoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
	}

	@Test
	public void testFindAllInvoicesByCustomer() throws Exception {

		Customer customer = customerRepository.findById(customerId).get();
		invoice.setDueDate(
				Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setPaymentTerms("NET-30");
		invoice.setInvoiceDate(
				Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
		invoice.setInvoiceNumber(customer.getInvoicePrefix() + LocalDate.now().minusDays(40).toString());
		invoice.setPlaceOfSupply("Karnataka");
		Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

		Long startTime = System.nanoTime();
		Page<Invoice> overdueInvoices = invoiceRepository.findAllByCustomer_Id(customerId, PageRequest.of(0, 10)).get();
		Long endTime = System.nanoTime();
		LOGGER.info("Elasped time for invoiceRepository.findAllByCustomer_Id() is {}", endTime - startTime);
		assertThat(overdueInvoices).isNotEmpty();
		assertThat(overdueInvoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
	}

}
