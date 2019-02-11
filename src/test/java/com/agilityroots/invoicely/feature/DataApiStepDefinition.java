/**
 *  20-Nov-2018 DataApiStepDefinition.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.feature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * @author anadi
 *
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("feature-tests")
@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration
public abstract class DataApiStepDefinition {

  @Autowired
  private TestRestTemplate restTemplate;

  protected TestRestTemplate getRestTemplate() {
    return this.restTemplate;
  }

}
