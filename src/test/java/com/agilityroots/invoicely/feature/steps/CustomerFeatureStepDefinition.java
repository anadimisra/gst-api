/**
 *  20-Nov-2018 CustomerFeatureStepDefinition.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.feature.steps;

import static org.assertj.core.api.Assertions.assertThat;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.feature.DataApiStepDefinition;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
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
public class CustomerFeatureStepDefinition extends DataApiStepDefinition implements En {

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  private GreenMail smtpServer;

  private String customerId, branchId;

  private Customer customer;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private ContactRepository contactRepository;

  @Before
  public void setUp() throws Exception {
    smtpServer = new GreenMail(new ServerSetup(2025, null, "smtp"));
    smtpServer.start();
    log.debug("Started SMTP server at port 2025");
  }

  @After
  public void tearDown() throws Exception {
    smtpServer.purgeEmailFromAllMailboxes();
    smtpServer.stop();
    log.debug("Shutting Down SMTP Server");
  }

  public CustomerFeatureStepDefinition() throws Exception {

    Given("I add new Customer {string} with relevant details", (String customerName) -> {
      customer = builder.getCustomerObject();
      customer.setName(customerName);
      ResponseEntity<Object> response = getRestTemplate().postForEntity("/customers", customer, Object.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      String customerLocation = response.getHeaders().getLocation().toString();
      customerId = getIdFromLocationHeader(customerLocation);
      assertThat(response.getHeaders().getLocation().toString()).contains("/customers");
    });

    When("I add contact information with email {string} to customer", (String contactEmail) -> {
      Contact contact = builder.getContactObject();
      contact.setEmail(contactEmail);
      StringBuffer urlBuilder = new StringBuffer("/customers/");
      urlBuilder.append(customerId);
      urlBuilder.append("/contact");
      ResponseEntity<Object> response = getRestTemplate().exchange(urlBuilder.toString(), HttpMethod.PUT,
          new HttpEntity<Contact>(contact), Object.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getLocation().toString()).endsWith("/customers/" + customerId + "/contact");
    });

    Then("{string} receives a welcome email from {string}", (String customerEmail, String financeEmail) -> {
      MimeMessage[] receivedMessages = smtpServer.getReceivedMessages();
      MimeMessage message = receivedMessages[0];
      assertThat(message.getFrom()[0]).isEqualTo(new InternetAddress(financeEmail));
      assertThat(message.getAllRecipients()[0].toString()).contains(customerEmail);
    });

    Given("I have customer {string}", (String customerName) -> {
      customer = builder.getCustomerWithContact();
      customer.setName(customerName);
      contactRepository.save(customer.getContact());
      customerRepository.saveAndFlush(customer);
      customerId = customer.getId().toString();
      log.debug("Saved customer {}: {}, with Contact Details: {}", customerName, customer, customer.getContact());
    });

    When("I add branch named {string} with all relevant details", (String branchName) -> {
      Branch branch = builder.getBranchObject();
      branch.setBranchName(branchName);
      StringBuffer buffer = new StringBuffer("/customers/");
      buffer.append(customerId);
      buffer.append("/branches");
      ResponseEntity<Object> response = getRestTemplate().exchange(buffer.toString(), HttpMethod.PUT,
          new HttpEntity<Branch>(branch), Object.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      String branchLocation = response.getHeaders().getLocation().toString();
      branchId = getIdFromLocationHeader(branchLocation);
    });

    And("I add contact information with email {string} to branch", (String email) -> {
      Contact branchContact = builder.getContactObject();
      branchContact.setEmail(email);
      StringBuffer urlBuilder = new StringBuffer("/branches/");
      urlBuilder.append(branchId);
      urlBuilder.append("/contact");
      ResponseEntity<Object> response = getRestTemplate().exchange(urlBuilder.toString(), HttpMethod.PUT,
          new HttpEntity<Contact>(branchContact), Object.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    });

  }

  /**
   * @param location
   */
  private String getIdFromLocationHeader(String location) {
    String[] segments = location.split("/");
    return segments[segments.length - 1];
  }

}