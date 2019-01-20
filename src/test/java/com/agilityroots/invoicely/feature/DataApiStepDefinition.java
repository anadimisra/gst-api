/**
 *  20-Nov-2018 DataApiStepDefinition.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.feature;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author anadi
 *
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("Feature_Tests")
@ContextConfiguration
public abstract class DataApiStepDefinition {

  private final String SERVER_URL = "http://localhost";

  private TestRestTemplate restTemplate;

  @LocalServerPort
  protected int port;

  private String thingsEndpoint() {
    return SERVER_URL + ":" + port;
  }

}
