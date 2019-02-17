/**
 *  20-Nov-2018 CustomersFeatureStepDefinitions.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.feature.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.feature.DataApiStepDefinition;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
public class CustomersFeatureStepDefinitions extends DataApiStepDefinition implements En {

  private GreenMail smtpServer;

  private final EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @Autowired
  private CustomerTestApi customerApi;

  @Autowired
  private InvoiceTestApi invoiceApi;

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
      List<Invoice> result = invoiceApi.getDueInvoices(customerApi.getSavedCustomerId());
      Invoice invoice = result.stream().filter(inv -> invoiceNumber.equals(inv.getInvoiceNumber())).findAny()
          .orElse(null);
      assertThat(invoice).isNotNull();
    });
  }

}