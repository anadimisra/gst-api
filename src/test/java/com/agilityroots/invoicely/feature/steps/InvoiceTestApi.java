/**
 * 
 */
package com.agilityroots.invoicely.feature.steps;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * @author anadi
 *
 */
@Component
public class InvoiceTestApi extends TestApi {

  public String getDueInvoicesJson(Long savedCustomerId) {

    ResponseEntity<String> result = getRestTemplate().exchange("/invoices/due", HttpMethod.GET, null, String.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    return result.getBody();
  }

}
