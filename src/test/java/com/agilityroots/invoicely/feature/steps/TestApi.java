/**
 * 
 */
package com.agilityroots.invoicely.feature.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

/**
 * @author anadi
 *
 */
public class TestApi {

  @Autowired
  private TestRestTemplate restTemplate;

  /**
   * 
   * @return test Rest Template
   */
  protected TestRestTemplate getRestTemplate() {
    return this.restTemplate;
  }

  /**
   * @param location
   */
  protected String getIdFromLocationHeader(String location) {
    String[] segments = location.split("/");
    return segments[segments.length - 1];
  }
}
