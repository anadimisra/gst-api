/**
 * 
 */
package com.agilityroots.invoicely.feature.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.agilityroots.invoicely.entity.Invoice;

/**
 * @author anadi
 *
 */
@Component
public class InvoiceTestApi extends TestApi {

  public List<Invoice> getDueInvoices(Long savedCustomerId) {

    ResponseEntity<Resources<Resource<Invoice>>> result = getRestTemplate().exchange("/invoices/due", HttpMethod.GET,
        null, new ParameterizedTypeReference<Resources<Resource<Invoice>>>() {
        });
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody().getContent()).isNotEmpty();
    return result.getBody().getContent().stream().map(Resource::getContent).collect(Collectors.toList());
  }

}
