/*
  3 Dec 2018 BranchController.java
  data-api
  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
*/
package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.service.BranchService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

/**
 * @author anadi
 *
 */
@Slf4j
@RestController
@ExposesResourceFor(Branch.class)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BranchController {

  private final BranchService branchService;

  private final BranchResourceAssembler assembler;

  @GetMapping("/branches/{id}")
  public DeferredResult<ResponseEntity<Resource<Branch>>> getBranch(@PathVariable Long id) {

    DeferredResult<ResponseEntity<Resource<Branch>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) ->
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.")));
    ListenableFuture<Optional<Branch>> future = branchService.getBranch(id);

    future.addCallback(new ListenableFutureCallback<Optional<Branch>>() {

      @Override
      public void onSuccess(Optional<Branch> result) {

        response.setResult(
            result.map(assembler::toResource).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Failed loading branch details due to error {}", ex.getMessage(), ex.getStackTrace());
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get branch details due to server error."));
      }

    });

    return response;
  }

  @PutMapping("/branches/{id}/contact")
  public DeferredResult<ResponseEntity<Object>> addContact(@PathVariable("id") Long id, HttpServletRequest request,
                                                           @RequestBody @Valid Contact contact) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.")));

    ListenableFuture<Optional<URI>> future = branchService.addContact(id, contact, getCurrentLocation(request));

    future.addCallback(new ListenableFutureCallback<Optional<URI>>() {
      @Override
      public void onSuccess(Optional<URI> result) {

        response.setResult(result.map(location -> ResponseEntity.created(location).build())
            .orElse(ResponseEntity.badRequest().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Could not update due to error : {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
      }
    });

    return response;
  }

  @GetMapping("/branches/{id}/contact")
  public DeferredResult<ResponseEntity<Resource<Contact>>> getContact(@PathVariable("id") Long id,
                                                                      HttpServletRequest request) {
    DeferredResult<ResponseEntity<Resource<Contact>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.")));

    ListenableFuture<Optional<Contact>> future = branchService.getBranchContact(id);

    future.addCallback(new ListenableFutureCallback<Optional<Contact>>() {
      @Override
      public void onSuccess(Optional<Contact> result) {
        response.setResult(
            result.map(it -> new Resource<>(it, new Link(getCurrentLocation(request).toString(), "contact")))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Could not update Contact {} due to error : {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get branch contact due to server error."));

      }
    });

    return response;
  }

  private URI getCurrentLocation(HttpServletRequest request) {
    return ServletUriComponentsBuilder.fromRequestUri(request).build().toUri();
  }

}