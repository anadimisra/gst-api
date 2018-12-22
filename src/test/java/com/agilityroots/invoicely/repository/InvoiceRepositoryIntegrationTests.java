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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.entity.Address;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.LineItem;
import com.agilityroots.invoicely.entity.Payment;
import com.github.javafaker.Faker;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
@ContextConfiguration(classes = { DataApiJpaConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class InvoiceRepositoryIntegrationTests {

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private BranchRepository branchRepository;

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private InvoiceRepository invoiceRepository;

  private Faker faker = new Faker(new Locale("en-IND"));

  private Invoice invoice;

  @Before
  public void setup() {
    Customer customer = new Customer();
    customer.setName("Minty And Sons Pvt. Ltd.");
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
    customer.setId(Long.valueOf(9));
    Customer saved = customerRepository.saveAndFlush(customer);

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
    invoice.setInvoiceNumber("INV" + LocalDate.now().minusDays(40).toString());
    invoice.setPlaceOfSupply("Karnataka");

  }

  @Test
  public void testFindAllPaidInvoices() throws Exception {

    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    Payment payment = new Payment();
    payment.setAmount(1000.00);
    payment.setAdjustmentName("TDS");
    payment.setAdjustmentValue(100.00);
    payment
        .setPaymentDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPayments(Arrays.asList(payment));
    Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

    Page<Invoice> paidInvoices = invoiceRepository.findByPayments_PaymentDateIsNotNull(PageRequest.of(0, 10)).get();
    assertThat(paidInvoices).isNotEmpty();
    assertThat(paidInvoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
  }

  @Test
  public void testFindAllPendingInvoices() throws Exception {

    invoice.setDueDate(Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(20).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

    Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    Page<Invoice> pendingInvoices = invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfter(today, PageRequest.of(0, 10))
        .get();
    assertThat(pendingInvoices).isNotEmpty();
    assertThat(pendingInvoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
  }

  @Test
  public void testFindAllOverdueInvoices() throws Exception {

    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

    Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    Page<Invoice> pendingInvoices = invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBefore(today, PageRequest.of(0, 10))
        .get();
    assertThat(pendingInvoices).isNotEmpty();
    assertThat(pendingInvoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
  }

  @Test
  public void testFindAllInvoicesByCustomer() throws Exception {

    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);

    Page<Invoice> invoices = invoiceRepository.findAllByCustomer_Id(savedInvoice.getCustomer().getId(), PageRequest.of(0, 10))
        .get();
    assertThat(invoices).isNotEmpty();
    assertThat(invoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
  }

  @Test
  public void testGettingLineItemsForInvoice() {

    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    LineItem lineItem = new LineItem();
    lineItem.setAmount(1180.00);
    lineItem.setDescription("That Service");
    lineItem.setDiscount(0.0);
    lineItem.setHsn("998313");
    lineItem.setItem("That Item");
    lineItem.setSerialNumber(1);
    lineItem.setTax(0.18);
    lineItem.setPrice(1000.00);
    invoice.setLineItems(Arrays.asList(lineItem));
    invoice.setId(Long.valueOf(100));
    invoiceRepository.saveAndFlush(invoice);

    Invoice savedInvoice = invoiceRepository.getOne(invoice.getId());
    assertThat(savedInvoice.getLineItems()).isNotNull();
    assertThat(savedInvoice.getLineItems()).isNotEmpty();
    assertThat(savedInvoice.getLineItems().get(0).getItem()).isEqualTo("That Item");
  }

  @Test
  public void testGetInvoiceCustomerDetails() {
    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoiceRepository.saveAndFlush(invoice);
    Invoice saved = invoiceRepository.getOne(Long.valueOf(7));
    assertThat(saved.getCustomer()).isNotNull();
    assertThat(saved.getCustomer().getName()).isEqualTo("Minty And Sons Pvt. Ltd.");
  }

}
