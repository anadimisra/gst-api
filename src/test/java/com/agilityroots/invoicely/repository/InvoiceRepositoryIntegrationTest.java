/**
 * 13-Nov-2018 InvoiceRepositoryIntegrationTests.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.DataApiJpaConfiguration;
import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = false)
@ContextConfiguration(classes = {DataApiJpaConfiguration.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(locations = {"classpath:application-it.properties", "classpath:application-test.properties"})
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class InvoiceRepositoryIntegrationTest {

  EntityObjectsBuilder builder = new EntityObjectsBuilder();
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
  private Invoice invoice;

  @Before
  public void setup() {
    Customer customer = builder.getCustomerObject();
    Branch branch = builder.getBranchObject();
    branch.setContact(contactRepository.saveAndFlush(builder.getContactObject()));
    branch = branchRepository.saveAndFlush(branch);
    Set<Branch> branches = new HashSet<>();
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
    invoice.setBilledTo(customer.getBranches().stream().findFirst().get());
    invoice.setShippedTo(customer.getBranches().stream().findFirst().get());
    invoice.setBilledFrom(companyBranch);
  }

  @Test
  public void testFindByInvoiceNumberLoadsAllDetails() {
    invoiceRepository.saveAndFlush(invoice);
    em.clear();
    Invoice allDetails = invoiceRepository.findByInvoiceNumber(invoice.getInvoiceNumber()).get();
    assertThat(allDetails).isNotNull();
    assertThat(allDetails.getBilledFrom().getBranchName()).isEqualTo(invoice.getBilledFrom().getBranchName());
    assertThat(allDetails.getBilledTo().getBranchName()).isEqualTo(invoice.getBilledTo().getBranchName());
    assertThat(allDetails.getShippedTo().getBranchName()).isEqualTo(invoice.getShippedTo().getBranchName());
  }

  @Test
  public void testGetInvoiceWithDetails() {

    invoice.setDueDate(Date.from(LocalDate.now().plusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(20).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    Invoice savedInvoice = invoiceRepository.saveAndFlush(invoice);
    em.clear();
    Invoice withDetails = invoiceRepository.getOne(savedInvoice.getId());
    assertThat(withDetails.getBilledTo()).isNotNull();
    assertThat(withDetails.getShippedTo()).isNotNull();
    assertThat(withDetails.getBilledTo()).isEqualTo(invoice.getBilledTo());
    assertThat(withDetails.getShippedTo()).isEqualTo(invoice.getShippedTo());
    assertThat(withDetails.getPayments()).isNullOrEmpty();
    assertThat(withDetails.getLineItems()).isNullOrEmpty();
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
    assertThat(savedInvoice.getLineItems().size()).isEqualTo(2);
  }

  @Test
  public void testUpdatingPaymentsForInvoice() {
    invoice.setDueDate(Date.from(LocalDate.now().minusDays(10).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice
        .setInvoiceDate(Date.from(LocalDate.now().minusDays(40).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoiceRepository.saveAndFlush(invoice);

    Optional<Invoice> result = invoiceRepository.findById(invoice.getId());
    Set<Payment> invoicePayments = result.map(Invoice::getPayments).orElse(new HashSet<Payment>());
    assertThat(invoicePayments).isEmpty();

    Payment payment = getPayment();
    Set<Payment> payments = new HashSet<>();
    payments.add(payment);
    invoicePayments.addAll(payments);
    result.ifPresent(it -> it.setPayments(invoicePayments));
    result.ifPresent(it -> invoiceRepository.saveAndFlush(it));

    Optional<Invoice> updated = invoiceRepository.findById(invoice.getId());
    Set<Payment> addedPayments = updated.map(Invoice::getPayments).orElse(new HashSet<Payment>());
    assertThat(addedPayments.size()).isEqualTo(1);
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
