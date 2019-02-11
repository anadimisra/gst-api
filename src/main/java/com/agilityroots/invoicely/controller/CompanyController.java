/**
 * 
 */
package com.agilityroots.invoicely.controller;

import java.net.URI;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.service.CompanyService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@RestController
@ExposesResourceFor(Company.class)
public class CompanyController {

  @Autowired
  private CompanyService companyService;

  @PutMapping("/companies/{id}/branches")
  public DeferredResult<ResponseEntity<Object>> addBranch(@PathVariable("id") Long id,
      @RequestBody(required = true) @Valid Branch branch, HttpServletRequest request) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    ListenableFuture<Optional<URI>> result = companyService.addBranch(id, branch, getCurrentLocation(request));

    result.addCallback(new ListenableFutureCallback<Optional<URI>>() {

      @Override
      public void onSuccess(Optional<URI> result) {
        response.setResult(result.map(location -> ResponseEntity.created(location).build())
            .orElse(ResponseEntity.badRequest().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot save branch {} due to error: {}", branch.toString(), ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot save branch details due to server error."));
      }
    });

    return response;
  }

  private URI getCurrentLocation(HttpServletRequest request) {
    return ServletUriComponentsBuilder.fromRequestUri(request).build().toUri();
  }

}
