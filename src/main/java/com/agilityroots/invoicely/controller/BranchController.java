/**
 *  3 Dec 2018 BranchController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import java.net.URI;
import java.util.Collections;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.service.BranchService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@RestController
@ExposesResourceFor(Branch.class)
public class BranchController {

  private static final Logger LOGGER = LoggerFactory.getLogger(BranchController.class);

  public static final Iterable<Resource<?>> EMPTY_RESOURCE_LIST = Collections.emptyList();

  @Autowired
  private BranchService branchService;

  @Autowired
  private BranchRepository branchRepository;

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private BranchResourceAssembler assembler;

  @PostMapping("/branches")
  public DeferredResult<ResponseEntity<Object>> save(@RequestBody(required = true) @Valid Branch branch,
      HttpServletRequest request) {

    return saveOrUpdateBranch(branch, request);
  }

  private DeferredResult<ResponseEntity<Object>> saveOrUpdateBranch(Branch branch, HttpServletRequest request) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    ListenableFuture<Branch> future = branchService.save(branch);

    future.addCallback(new ListenableFutureCallback<Branch>() {

      @Override
      public void onSuccess(Branch result) {

        URI location = ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}").buildAndExpand(result.getId())
            .toUri();
        log.debug("Created Location Header {} for {}", location.toString(), result.getBranchName());
        ResponseEntity<Object> responseEntity = ResponseEntity.created(location).build();
        log.debug("Reponse Status for {} Request is :: {} ", request.getMethod(), responseEntity.getStatusCodeValue());
        log.debug("Reponse Data for {} Request is :: {} ", request.getMethod(),
            responseEntity.getHeaders().getLocation().toString());
        response.setResult(responseEntity);
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

  @GetMapping("/branches/{id}")
  public DeferredResult<ResponseEntity<Resource<Branch>>> getBranch(@PathVariable Long id, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resource<Branch>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    ListenableFuture<Optional<Branch>> future = branchService.getBranch(id);

    future.addCallback(new ListenableFutureCallback<Optional<Branch>>() {

      @Override
      public void onSuccess(Optional<Branch> result) {

        response.setResult(
            result.map(assembler::toResource).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Failed loading branch details for id {} due to error {}", id, ex.getMessage(), ex.getStackTrace());
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get branch details due to server error."));
      }

    });

    return response;
  }

  @PutMapping("/branches/{id}/contact")
  public DeferredResult<ResponseEntity<Object>> addContact(@PathVariable("id") Long id, HttpServletRequest request,
      @RequestBody(required = true) @Valid Contact contact) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    ListenableFuture<Optional<Branch>> future = branchService.getBranch(id);

    future.addCallback(new ListenableFutureCallback<Optional<Branch>>() {
      @Override
      public void onSuccess(Optional<Branch> result) {

        if (result.isPresent()) {
          Contact saved = contactRepository.saveAndFlush(contact);
          Branch updated = result.get();
          updated.setContact(contact);
          branchRepository.saveAndFlush(updated);
          response.setResult(ResponseEntity.created(
              ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}").buildAndExpand(saved.getId()).toUri())
              .build());
        } else
          response.setErrorResult(ResponseEntity.badRequest().build());

      }

      @Override
      public void onFailure(Throwable ex) {
        LOGGER.error("Could not update due to error : {}", ex.getMessage(), ex);
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
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    ListenableFuture<Optional<Contact>> future = branchService.getBranchContact(id);

    future.addCallback(new ListenableFutureCallback<Optional<Contact>>() {
      @Override
      public void onSuccess(Optional<Contact> result) {
        response.setResult(
            result.map(it -> new Resource<Contact>(it, new Link(getCurrentLocation(request).toString(), "contact")))
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