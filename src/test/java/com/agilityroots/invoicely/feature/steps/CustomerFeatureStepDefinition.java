/**
 *  20-Nov-2018 CustomerFeatureStepDefinition.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.feature.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.feature.DataApiStepDefinition;

import cucumber.api.java8.En;

/**
 * @author anadi
 *
 */
public class CustomerFeatureStepDefinition extends DataApiStepDefinition implements En {

  private Customer customer;

  private String customerLocation;

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  public CustomerFeatureStepDefinition() {

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
      // Write code here that turns the phrase above into concrete actions
      throw new cucumber.api.PendingException();
    });

    Then("{string} gets notification from {string}", (String financeHeadEmail, String financeEmail) -> {
      // Write code here that turns the phrase above into concrete actions
      throw new cucumber.api.PendingException();
    });

  }

}
