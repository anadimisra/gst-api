/**
 *  13-Nov-2018 InvoiceRepositoryIntegrationTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.Payment;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = true)
@ContextConfiguration(classes = { DataApiJpaConfiguration.class })
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = "classpath:application-unit-test.properties")
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

  EntityObjectsBuilder builder = new EntityObjectsBuilder();

  private Invoice invoice;

  @Before
  public void setup() {
    Customer customer = builder.getCustomerObject();
    Branch branch = builder.getBranchObject();
    branch.setContact(contactRepository.saveAndFlush(builder.getContactObject()));
    branch = branchRepository.saveAndFlush(branch);
    List<Branch> branches = new ArrayList<Branch>();
    branches.add(branch);
    customer.setBranches(branches);
    customer = customerRepository.saveAndFlush(customer);
    Branch companyBranch = builder.getBranchObject();
    companyBranch.setContact(contactRepository.saveAndFlush(builder.getContactObject()));
    companyBranch = branchRepository.saveAndFlush(companyBranch);
    Company company = builder.getCompanyObject();
    List<Branch> companyBranches = new ArrayList<>();
    companyBranches.add(companyBranch);
    company = companyRepository.save(company);
    invoice = builder.getInvoiceObjectWithLineItems();
    invoice.setCustomer(customer);
    invoice.setBilledTo(customer.getBranches().get(0));
    invoice.setShippedTo(customer.getBranches().get(0));
    invoice.setBilledFrom(companyBranch);
  }

  @Test
  public void testFindAllPaidInvoices() throws Exception {
    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    Payment payment = getPayment();
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
    Page<Invoice> pendingInvoices = invoiceRepository
        .findByPayments_PaymentDateIsNullAndDueDateAfter(today, PageRequest.of(0, 10)).get();
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
    Page<Invoice> pendingInvoices = invoiceRepository
        .findByPayments_PaymentDateIsNullAndDueDateBefore(today, PageRequest.of(0, 10)).get();
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

    Page<Invoice> invoices = invoiceRepository.findAllByCustomer_Id(savedInvoice.getCustomer().getId(),
        PageRequest.of(0, 10));
    assertThat(invoices).isNotEmpty();
    assertThat(invoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
  }

  @Test
  public void testFindAllPaidInvoicesByCustomer() throws Exception {
    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    Payment payment = getPayment();
    invoice.setPayments(Arrays.asList(payment));
    Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
    Page<Invoice> invoices = invoiceRepository
        .findByPayments_PaymentDateIsNotNullAndCustomer_Id(savedInvoice.getCustomer().getId(), PageRequest.of(0, 10));
    assertThat(invoices).isNotEmpty();
    assertThat(invoices.getContent().get(0).getId()).isEqualTo(savedInvoice.getId());
  }

  @Test
  public void testFindAllPendingInvoicesByCustomer() throws Exception {
    invoice.setDueDate(Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(20).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));

    Invoice paidInvoice = new Invoice();
    paidInvoice.setBilledFrom(invoice.getBilledFrom());
    paidInvoice.setBilledTo(invoice.getBilledTo());
    paidInvoice.setShippedTo(invoice.getShippedTo());
    paidInvoice.setInvoiceDate(invoice.getInvoiceDate());
    paidInvoice.setDueDate(invoice.getDueDate());
    paidInvoice.setCustomer(invoice.getCustomer());
    paidInvoice.setPlaceOfSupply("Karnataka");
    paidInvoice.setInvoiceNumber("INV-OTHER-1");
    paidInvoice.setPaymentTerms(invoice.getPaymentTerms());
    paidInvoice.setPayments(Arrays.asList(getPayment()));
    paidInvoice.setLineItems(invoice.getLineItems());

    Invoice saved = invoiceRepository.saveAndFlush(invoice);

    invoiceRepository.saveAndFlush(paidInvoice);

    Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    Page<Invoice> pendingInvoices = invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_Id(
        today, saved.getCustomer().getId(), PageRequest.of(0, 10));
    assertThat(pendingInvoices).isNotEmpty();
    assertThat(pendingInvoices.getContent().size()).isEqualTo(1);
    assertThat(pendingInvoices.getContent().get(0).getId()).isEqualTo(saved.getId());
  }

  @Test
  public void testGettingLineItemsForInvoice() {

    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoiceRepository.saveAndFlush(invoice);

    Invoice savedInvoice = invoiceRepository.getOne(invoice.getId());
    assertThat(savedInvoice.getLineItems()).isNotNull();
    assertThat(savedInvoice.getLineItems()).isNotEmpty();
    assertThat(savedInvoice.getLineItems().get(0).getItem()).isEqualTo("That Item");
  }

  @Test
  public void testUpdatingPaymentsForInvoice() {
    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoiceRepository.saveAndFlush(invoice);

    Optional<Invoice> result = invoiceRepository.findById(invoice.getId());
    List<Payment> invoicePayments = result.map(Invoice::getPayments).orElse(new ArrayList<Payment>());
    assertThat(invoicePayments).isEmpty();

    Payment payment = getPayment();
    List<Payment> payments = new ArrayList<>();
    payments.add(payment);
    invoicePayments.addAll(payments);
    result.ifPresent(it -> it.setPayments(invoicePayments));
    result.ifPresent(it -> invoiceRepository.saveAndFlush(it));

    Optional<Invoice> updated = invoiceRepository.findById(invoice.getId());
    List<Payment> addedPayments = updated.map(Invoice::getPayments).orElse(new ArrayList<Payment>());
    assertThat(addedPayments.size()).isEqualTo(1);
    assertThat(addedPayments.get(0).getAmount()).isEqualTo(1000.00);
  }

  @Test
  public void testGetInvoiceCustomerDetails() {
    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoiceRepository.saveAndFlush(invoice);
    Invoice saved = invoiceRepository.getOne(invoice.getId());
    assertThat(saved.getCustomer()).isNotNull();
    assertThat(saved.getCustomer().getName()).isEqualTo("Minty and Sons Private Limited");
  }

  /**
   * @return {@link Payment}
   */
  private Payment getPayment() {
    Payment payment = new Payment();
    payment.setAmount(1000.00);
    payment.setAdjustmentName("TDS");
    payment.setAdjustmentValue(100.00);
    payment
        .setPaymentDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    return payment;
  }
}
