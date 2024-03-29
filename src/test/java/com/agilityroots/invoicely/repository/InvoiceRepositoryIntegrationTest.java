/**
 * 13-Nov-2018 InvoiceRepositoryIntegrationTests.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author anadi
 */
@Slf4j
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {DataApiJpaConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = {"classpath:application-it.properties", "classpath:application-test.properties"})
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class InvoiceRepositoryIntegrationTest {

  private Invoice invoice;
  private EntityObjectsBuilder builder;

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
  @PersistenceContext
  private EntityManager em;

  @Before
  public void setup() {
    builder = new EntityObjectsBuilder();
    log.debug("Setting up customer with branches");
    Customer customer = builder.getCustomerObject();
    Branch branch = builder.getBranchObject();
    branch.setContact(contactRepository.saveAndFlush(builder.getContactObject()));
    branch = branchRepository.saveAndFlush(branch);
    Set<Branch> branches = new HashSet<>();
    branches.add(branch);
    customer.setBranches(branches);
    customer = customerRepository.saveAndFlush(customer);
    log.debug("Setting up company with branches");
    Branch companyBranch = builder.getBranchObject();
    companyBranch.setContact(contactRepository.saveAndFlush(builder.getContactObject()));
    companyBranch = branchRepository.saveAndFlush(companyBranch);
    Company company = builder.getCompanyObject();
    List<Branch> companyBranches = new ArrayList<>();
    companyBranches.add(companyBranch);
    company = companyRepository.save(company);
    log.debug("Setting up invoice object");
    invoice = builder.getInvoiceObjectWithLineItems();
    invoice.setCompany(company);
    invoice.setCustomer(customer);
    invoice.setBilledTo(customer.getBranches().stream().findFirst().get());
    invoice.setShippedTo(customer.getBranches().stream().findFirst().get());
    invoice.setBilledFrom(companyBranch);
  }

  @Test
  public void testFindByInvoiceNumberLoadsAllDetails() {
    saveAndClearEntityManager(invoice);
    Invoice allDetails = invoiceRepository.findByInvoiceNumber(invoice.getInvoiceNumber()).get();
    assertThat(allDetails).isNotNull();
    assertThat(allDetails.getBilledFrom().getBranchName()).isEqualTo(invoice.getBilledFrom().getBranchName());
    assertThat(allDetails.getBilledTo().getBranchName()).isEqualTo(invoice.getBilledTo().getBranchName());
    assertThat(allDetails.getShippedTo().getBranchName()).isEqualTo(invoice.getShippedTo().getBranchName());
  }

  @Test
  public void testGetOneInvoiceLoadsAllDetails() {
    Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
    em.clear();
    Invoice withDetails = invoiceRepository.getOne(savedInvoice.getId());
    assertThat(withDetails.getBilledTo()).isNotNull();
    assertThat(withDetails.getShippedTo()).isNotNull();
    assertThat(withDetails.getBilledTo().getBranchName()).isEqualTo(invoice.getBilledTo().getBranchName());
    assertThat(withDetails.getShippedTo().getBranchName()).isEqualTo(invoice.getShippedTo().getBranchName());
    assertThat(withDetails.getLineItems()).isNotNull();
    assertThat(withDetails.getPayments()).isNullOrEmpty();
  }

  @Test
  public void testFindAllInvoicesForCompanyResultsAreSortedByDescendingInvoiceDate() {
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, 1, 31);
    otherInvoice.setInvoiceNumber("INV-12345678");
    saveAndClearEntityManager(otherInvoice);
    List<Invoice> invoices = invoiceRepository.findAllByCompany_IdOrderByInvoiceDateDesc(invoice.getCompany().getId(), PageRequest.of(0, 5)).getContent();
    assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo("INV-12345678");
  }

  @Test
  public void testPaidInvoicesForCompanyAreSortedByDescendingPaymentDate() {
    invoice.setPayments(Stream.of(getPayment()).collect(Collectors.toSet()));
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, 1, 31);
    otherInvoice.setInvoiceNumber("INV-12345678");
    Set<Payment> payments = new HashSet<>();
    Payment partOne = getPayment();
    partOne.setPaymentDate(Date.from(LocalDate.now().plusDays(20).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    payments.add(partOne);
    Payment partTwo = getPayment();
    partTwo.setPaymentDate(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    payments.add(partTwo);
    otherInvoice.setPayments(payments);
    saveAndClearEntityManager(otherInvoice);
    List<Invoice> invoices = invoiceRepository.findByPayments_PaymentDateIsNotNullAndCompany_IdOrderByPayments_PaymentDateDesc(invoice.getCompany().getId()
        , PageRequest.of(0, 2)).getContent();
    assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo("INV-12345678");
  }

  @Test
  public void testDueInvoicesForCompanyAreSortedByAscendingDueDate() {
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, 5, 35);
    otherInvoice.setInvoiceNumber("INV-12345678");
    saveAndClearEntityManager(otherInvoice);
    Date today = Date.from(LocalDate.now().plusDays(0).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    List<Invoice> invoices = invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfterAndCompany_IdOrderByDueDateAsc(today, invoice.getCompany().getId(), PageRequest.of(0, 5)).getContent();
    assertThat(invoices.get(1).getInvoiceNumber()).isEqualTo("INV-12345678");
  }

  @Test
  public void testOverDueInvoicesForCompanyAreSortedByAscendingDueDate() {
    updateDates(invoice, -35, -5);
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, -40, -10);
    otherInvoice.setInvoiceNumber("INV-12345678");
    saveAndClearEntityManager(otherInvoice);
    Date today = Date.from(LocalDate.now().plusDays(0).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    List<Invoice> invoices = invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBeforeAndCompany_IdOrderByDueDateAsc(today, invoice.getCompany().getId(), PageRequest.of(0, 2)).getContent();
    assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo("INV-12345678");
  }

  @Test
  public void testCustomerInvoicesAreOrderedByAscendingInvoiceDate() {
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, 35, 0);
    otherInvoice.setInvoiceNumber("INV-12345678");
    saveAndClearEntityManager(otherInvoice);
    List<Invoice> invoices = invoiceRepository.findByCustomer_IdOrderByInvoiceDateDesc(invoice.getCustomer().getId(), PageRequest.of(0, 2)).getContent();
    assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo("INV-12345678");
  }

  @Test
  public void testPaidInvoicesByCustomerAreOrderedByDescendingPaymentDates() {
    invoice.setPayments(Stream.of(getPayment()).collect(Collectors.toSet()));
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, 1, 31);
    otherInvoice.setInvoiceNumber("INV-12345678");
    Set<Payment> payments = new HashSet<>();
    Payment partOne = getPayment();
    partOne.setPaymentDate(Date.from(LocalDate.now().plusDays(20).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    payments.add(partOne);
    Payment partTwo = getPayment();
    partTwo.setPaymentDate(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    payments.add(partTwo);
    otherInvoice.setPayments(payments);
    saveAndClearEntityManager(otherInvoice);
    List<Invoice> invoices = invoiceRepository.findByPayments_PaymentDateIsNotNullAndCustomer_IdOrderByPayments_PaymentDateDesc(invoice.getCustomer().getId(), PageRequest.of(0,2)).getContent();
    assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo("INV-12345678");
  }

  @Test
  public void testDueInvoicesByCustomerAreOrderedByAscendingDueDate() {
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, 1, 31);
    otherInvoice.setInvoiceNumber("INV-12345678");
    saveAndClearEntityManager(otherInvoice);
    Date today = Date.from(LocalDate.now().plusDays(0).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    List<Invoice> invoices = invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_IdOrderByDueDateAsc(today, invoice.getCustomer().getId(), PageRequest.of(0,2)).getContent();
    assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
  }

  @Test
  public void testOverdueInvoicesByCustomerAreOrderedByAscendingDueDate() {
    invoice.setInvoiceDate(Date.from(LocalDate.now().plusDays(-30).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setDueDate(Date.from(LocalDate.now().plusDays(-5).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    saveAndClearEntityManager(invoice);
    Invoice otherInvoice = getOtherInvoice();
    updateDates(otherInvoice, -20, -2);
    otherInvoice.setInvoiceNumber("INV-12345678");
    saveAndClearEntityManager(otherInvoice);
    Date today = Date.from(LocalDate.now().plusDays(0).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    List<Invoice> invoices = invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_IdOrderByDueDateAsc(today, invoice.getCustomer().getId(), PageRequest.of(0,2)).getContent();
    assertThat(invoices.get(0).getInvoiceNumber()).isEqualTo(invoice.getInvoiceNumber());
  }

  private Payment getPayment() {
    Payment payment = new Payment();
    payment.setAmount(1000.00);
    payment.setAdjustmentName("TDS");
    payment.setAdjustmentValue(100.00);
    payment.setPaymentDate(Date.from(LocalDate.now().plusDays(25).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    return payment;
  }

  private Invoice getOtherInvoice() {
    Invoice otherInvoice = builder.getInvoiceObjectWithLineItems();
    otherInvoice.setCompany(invoice.getCompany());
    otherInvoice.setCustomer(invoice.getCustomer());
    otherInvoice.setBilledTo(invoice.getBilledTo());
    otherInvoice.setShippedTo(invoice.getShippedTo());
    otherInvoice.setBilledFrom(invoice.getBilledFrom());
    return otherInvoice;
  }

  private void updateDates(Invoice invoice, int daysOnInvoiceDate, int daysOnDueDate) {
    invoice.setInvoiceDate(Date.from(LocalDate.now().plusDays(daysOnInvoiceDate).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setDueDate(Date.from(LocalDate.now().plusDays(daysOnDueDate).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
  }

  private void saveAndClearEntityManager(Invoice invoice) {
    invoiceRepository.saveAndFlush(invoice);
    em.clear();
  }
}
