/**
 *  20-Nov-2018 CustomerFeatureStepDefinition.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.feature.steps;

import static org.assertj.core.api.Assertions.assertThat;

import cucumber.api.java.Before;
import cucumber.api.java.After;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.feature.DataApiStepDefinition;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import cucumber.api.java8.En;
import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
public class CustomerFeatureStepDefinition extends DataApiStepDefinition implements En {

  private Customer customer;

  private String customerLocation;

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  private GreenMail smtpServer;

  @Before
  public void setUp() throws Exception {
    smtpServer = new GreenMail(new ServerSetup(2025, null, "smtp"));
    smtpServer.start();
    log.debug("Started SMTP server at port 2025");
  }

  @After
  public void tearDown() throws Exception {
    smtpServer.stop();
  }

  public CustomerFeatureStepDefinition() throws Exception {

    Given("I add new Customer {string} with relevant details", (String customerName) -> {
      customer = builder.getCustomerObject();
      customer.setName(customerName);
      customer.setId(null);
      ResponseEntity<Object> response = getRestTemplate().postForEntity("/customers", customer, Object.class);
      customerLocation = response.getHeaders().getLocation().toString();
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(customerLocation).contains("/customers");
    });

    When("I add contact information with email {string} to the customer", (String contactEmail) -> {
      Contact contact = builder.getContactObject();
      contact.setEmail(contactEmail);
      contact.setId(null);
      String[] segments = customerLocation.split("/");
      String customerId = segments[segments.length - 1];
      ResponseEntity<Object> response = getRestTemplate().exchange("/customers/" + customerId + "/contact",
          HttpMethod.PUT, new HttpEntity<Contact>(contact), Object.class);
      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getHeaders().getLocation().toString())
          .endsWith("/customers/" + customerId.toString() + "/contact");
    });

    Then("{string} recieves a welcome email from {string}", (String customerEmail, String financeEmail) -> {
      MimeMessage[] receivedMessages = smtpServer.getReceivedMessages();
      MimeMessage message = receivedMessages[0];
      assertThat(message.getFrom()[0]).isEqualTo(new InternetAddress("finance@agilityroots.com"));
      assertThat(message.getAllRecipients()[0].toString()).contains("foo@bar.com");
    });

  }

}