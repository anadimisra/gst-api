/**
 * 20-Nov-2018 CustomersFeatureStepDefinitions.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.feature.steps;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.feature.DataApiStepDefinition;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.jayway.jsonpath.JsonPath;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author anadi
 *
 */
@Slf4j
public class CustomersFeatureStepDefinitions extends DataApiStepDefinition implements En {

  private final EntityObjectsBuilder builder = new EntityObjectsBuilder();
  private GreenMail smtpServer;
  @Autowired
  private CustomerTestApi customerApi;

  @Autowired
  private InvoiceTestApi invoiceApi;

  public CustomersFeatureStepDefinitions() throws Exception {

    Given("I add new Customer {string} with relevant details", (String customerName) -> {
      Customer customer = builder.getCustomerObject();
      customer.setName(customerName);
      customerApi.addCustomer(customer);
    });

    When("I add contact information with email {string} to customer", (String contactEmail) -> {
      Contact contact = builder.getContactObject();
      contact.setEmail(contactEmail);
      customerApi.addContactToCustomer(contact);
    });

    Then("{string} receives a welcome email from {string}", (String customerEmail, String financeEmail) -> {
      MimeMessage[] receivedMessages = smtpServer.getReceivedMessages();
      MimeMessage message = receivedMessages[0];
      assertThat(message.getFrom()[0]).isEqualTo(new InternetAddress(financeEmail));
      assertThat(message.getAllRecipients()[0].toString()).contains(customerEmail);
    });

    Given("I have customer {string}", (String customerName) -> {
      Customer customer = builder.getCustomerWithContact();
      customer.setName(customerName);
      customerApi.addCustomerWithContact(customer);
    });

    When("I add branch named {string} with all relevant details", (String branchName) -> {
      Branch branch = builder.getBranchObject();
      branch.setBranchName(branchName);
      customerApi.addBranch(branch);
    });

    And("I add contact information with email {string} to branch", (String email) -> {
      Contact branchContact = builder.getContactObject();
      branchContact.setEmail(email);
      customerApi.addContactToBranch(branchContact);
    });

    Given("I have customer {string} with contact details", (String customerName) -> {
      Customer customer = builder.getCustomerWithContact();
      customer.setName(customerName);
      customerApi.addCustomerWithContact(customer);
    });

    When("I update contact information with email {string} to customer", (String contactEmail) -> {
      customerApi.updateCustomerContactEmail(contactEmail);
    });

    Given("I have customer {string} with branch name {string}", (String customerName, String branchName) -> {
      Customer customer = builder.getCustomerWithContact();
      customer.setName(customerName);
      customerApi.addCustomerWithContact(customer);
      Branch branch = builder.getBranchWithContactObject();
      branch.setBranchName(branchName);
      customerApi.addBranchWithContact(branch);
    });

    And("I update contact information with email {string} to branch", (String contactEmail) -> {
      customerApi.updateBranchContactEmail(contactEmail);
    });

    When("I raise invoice {string} to customer", (String invoiceNumber) -> {
      Invoice invoice = builder.getInvoiceObjectWithLineItems();
      invoice.setInvoiceNumber(invoiceNumber);
      customerApi.addInvoice(invoice, builder.getBranchObject());
    });

    Then("due invoices listing contains invoice number {string}", (String invoiceNumber) -> {
      String invoicesJson = invoiceApi.getDueInvoicesJson(customerApi.getSavedCustomerId());
      Map<String, Object> dueInvoice = JsonPath.parse(invoicesJson).read("$._embedded.invoices[0]");
      assertThat(dueInvoice.keySet()).contains("invoice_number");
      assertThat(dueInvoice.get("invoice_number")).isEqualTo(invoiceNumber);
    });

    When("the customer has {int} due invoices", (Integer dueInvoices) -> {
      Branch billedFrom = builder.getBranchObject();
      for (int i = 0; i < dueInvoices; i++) {
        Invoice invoice = builder.getInvoiceObjectWithLineItems();
        invoice
            .setInvoiceDate(Date.from(LocalDate.now().plusDays(i).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        invoice.setDueDate(
            Date.from(LocalDate.now().plusDays((i + 30)).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        invoice.setInvoiceNumber(invoice.getInvoiceNumber() + (i + 1));
        customerApi.addInvoice(invoice, billedFrom);
      }
    });

    Then("invoice with {string} number of days till due is the {string} in list of {string} invoices",
        (String daysTillDue, String positionInList, String invoiceType) -> {
          int dueInvoiceAtIndex = 0;
          int daysTillDueDate = 30;
          if (daysTillDue.equals("most")) {
            dueInvoiceAtIndex += 2;
            daysTillDueDate += 2;
          }
          String dueInvoicesJson = customerApi.getCustomerInvoicesJson(invoiceType);
          log.debug("Parsing JSON to Objects");
          List<Map<String, Object>> dueInvoices = JsonPath.parse(dueInvoicesJson).read("$._embedded.invoices");
          assertThat(dueInvoices.size()).isEqualTo(3);
          assertThat(new SimpleDateFormat("dd-MM-yyyy")
              .parse(String.valueOf(dueInvoices.get(dueInvoiceAtIndex).get("due_date"))))
              .isEqualTo(Date.from(
                  LocalDate.now().plusDays(daysTillDueDate).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        });

    When("the customer has {int} overdue invoices", (Integer overDueInvoices) -> {
      Branch billedFrom = builder.getBranchObject();
      for (int i = 0; i < overDueInvoices; i++) {
        Invoice invoice = builder.getInvoiceObjectWithLineItems();
        invoice.setInvoiceNumber(invoice.getInvoiceNumber() + (i + 1));
        invoice.setInvoiceDate(
            Date.from(LocalDate.now().minusDays(40 + i).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        invoice.setDueDate(
            Date.from(LocalDate.now().minusDays(10 + i).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        customerApi.addInvoice(invoice, billedFrom);
      }
    });

    Then("invoice with {string} number of days since overdue is the {string} in list of {string} invoices",
        (String daysTillOverdue, String positionInList, String invoiceType) -> {
          int dueInvoiceAtIndex = 0;
          int daysSinceOverdue = 12;
          if (daysTillOverdue.equals("least")) {
            dueInvoiceAtIndex += 2;
            daysSinceOverdue -= 2;
          }
          String dueInvoicesJson = customerApi.getCustomerInvoicesJson(invoiceType);
          log.debug("Parsing JSON to Objects");
          List<Map<String, Object>> dueInvoices = JsonPath.parse(dueInvoicesJson).read("$._embedded.invoices");
          assertThat(dueInvoices.size()).isEqualTo(3);
          assertThat(new SimpleDateFormat("dd-MM-yyyy")
              .parse(String.valueOf(dueInvoices.get(dueInvoiceAtIndex).get("due_date"))))
              .isEqualTo(Date.from(
                  LocalDate.now().minusDays(daysSinceOverdue).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        });

    Given("the customer has {int} paid invoices", (Integer paidInvoices) -> {
      Branch billedFrom = builder.getBranchObject();
      for (int i = 0; i < paidInvoices; i++) {
        Invoice invoice = builder.getInvoiceObjectWithLineItems();
        invoice.setInvoiceNumber(invoice.getInvoiceNumber() + (i + 1));
        invoice.setInvoiceDate(
            Date.from(LocalDate.now().minusDays(40 + i).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        invoice.setDueDate(
            Date.from(LocalDate.now().minusDays(30 + i).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        invoice.setPayments(builder.getInvoiceWithPayments().getPayments());
        customerApi.addInvoice(invoice, billedFrom);
      }
    });

    Then("invoice with {string} recent payment date is {string} in list of {string} invoices",
        (String daysSincePaid, String positionInList, String invoiceType) -> {
          int index = 0;
          int paidSinceDays = 12;
          if (daysSincePaid.equals("most")) {
            index += 2;
            paidSinceDays -= 2;
          }
          String paidInvoicesJson = customerApi.getCustomerInvoicesJson(invoiceType);
          List<Map<String, Object>> payments = JsonPath.parse(paidInvoicesJson).read("$._embedded.invoices[?].payments");
          log.debug("Payments should be in ascending order of dates");
          assertThat(payments.size()).isEqualTo(3);
          assertThat(new SimpleDateFormat("dd-MM-yyyy")
              .parse(String.valueOf(payments.get(index).get("payment_date"))))
              .isEqualTo(Date.from(
                  LocalDate.now().minusDays(paidSinceDays).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
        });

  }

  @Before
  public void setUp() throws Exception {
    smtpServer = new GreenMail(new ServerSetup(2025, null, "smtp"));
    smtpServer.start();
    log.debug("Started SMTP server at port 2025");
  }

  @After
  public void tearDown() throws Exception {
    smtpServer.stop();
    log.debug("Shutting Down SMTP Server");
  }

}