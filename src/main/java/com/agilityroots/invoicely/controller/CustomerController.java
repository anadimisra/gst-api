/*
  13-Nov-2018 CustomerController.java
  data-api
  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.http.payload.InvoiceHttpPayload;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
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
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * @author anadi
 */
@Slf4j
@RestController
@ExposesResourceFor(Customer.class)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CustomerController {

  private final Environment environment;

  private final CustomerResourceAssember customerResourceAssembler;

  private final BranchResourceAssembler branchResourceAssembler;

  private final InvoiceResourceAssembler invoiceReourceAssembler;

  private final CustomerService customerService;

  @GetMapping(value = "/customers/{id}")
  public DeferredResult<ResponseEntity<Resource<Customer>>> getCustomer(@PathVariable Long id) {

    DeferredResult<ResponseEntity<Resource<Customer>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) ->
            response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.")));

    ListenableFuture<Optional<Customer>> future = customerService.getCustomer(id);
    future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

      @Override
      public void onSuccess(Optional<Customer> customer) {

        response.setResult(customer.map(customerResourceAssembler::toResource).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build()));
        log.debug("Assembling customer resource if it was present");
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot get customer details for id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get customer details due to server error."));
      }

    });
    return response;
  }

  @PutMapping(value = "/customers/{id}/invoices")
  public DeferredResult<ResponseEntity<Object>> addInvoice(@PathVariable("id") Long id,
                                                           @RequestBody @Valid InvoiceHttpPayload payload, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.")));

    StringBuffer builder = new StringBuffer();
    builder.append(request.getScheme()).append("://").append(request.getHeader("Host")).append(request.getContextPath())
        .append("/invoices/");

    ListenableFuture<Optional<URI>> result = customerService.addInvoice(id, payload.getBilledFrom(),
        payload.getBilledTo(), payload.getShippedTo(), builder, payload.getInvoice());

    result.addCallback(new ListenableFutureCallback<Optional<URI>>() {
      @Override
      public void onSuccess(Optional<URI> result) {

        response.setResult(result.map(location -> ResponseEntity.created(location).build())
            .orElse(ResponseEntity.badRequest().build()));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot add invoice:{} to customer {} due to error: {}", payload.getInvoice().toString(), id,
            ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot update invoices for this customer due to error."));
      }
    });
    return response;
  }

  @GetMapping(value = "/customers/{id}/invoices")
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getInvoicesByCustomer(@PathVariable("id") Long id,
                                                                                            @PageableDefault Pageable pageable, PagedResourcesAssembler<Invoice> assembler,
                                                                                            HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.")));

    log.debug("Getting invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = customerService.getAllInvoices(id, pageable);
    future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> invoices) {
        log.debug("Found {} invoices", invoices.getSize());
        Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(), "self");
        response.setResult(Optional.of(invoices)
            .<Resources<Resource<Invoice>>>map(it -> assembler.toResource(it, invoiceReourceAssembler, link))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoices for customer id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get customer invoices due to server error."));
      }

    });
    return response;
  }

  @GetMapping(value = "/customers/{id}/invoices/paid")
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPaidInvoicesByCustomer(
      @PathVariable("id") Long id,
      @PageableDefault Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request)
      throws InterruptedException, ExecutionException {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.")));

    log.debug("Getting paid invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = customerService.getPaidInvoices(id, pageable);
    future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> invoices) {
        log.debug("Found {} paid invoices", invoices.getSize());
        Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(), "self");
        response.setResult(Optional.of(invoices)
            .<Resources<Resource<Invoice>>>map(it -> assembler.toResource(it, invoiceReourceAssembler, link))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve paid invoices for customer id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get customer paid invoices due to server error."));
      }

    });
    return response;
  }

  @GetMapping(value = "/customers/{id}/invoices/due")
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPendingInvoicesByCustomer(
      @PathVariable("id") Long id,
      @PageableDefault Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.")));

    log.debug("Getting unpaid invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = customerService.getDueInvoices(getTodaysDate(), id, pageable);
    future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> invoices) {
        log.debug("Found {} unpaid nvoices", invoices.getSize());
        Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(), "self");
        response.setResult(Optional.of(invoices)
            .<Resources<Resource<Invoice>>>map(it -> assembler.toResource(it, invoiceReourceAssembler, link))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve due invoices for customer id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get customer due invoices due to server error."));
      }

    });
    return response;
  }

  @GetMapping(value = "/customers/{id}/invoices/overdue")
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getOverdueInvoicesByCustomer(
      @PathVariable("id") Long id,
      @PageableDefault Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.")));

    log.debug("Getting overdue invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = customerService.getOverdueInvoices(getTodaysDate(), id, pageable);
    future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> invoices) {
        log.debug("Found {} overdue invoices", invoices.getSize());
        Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(), "self");
        response.setResult(Optional.of(invoices)
            .<Resources<Resource<Invoice>>>map(it -> assembler.toResource(it, invoiceReourceAssembler, link))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve paid invoices for customer id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get customer overdue invoices due to server error."));
      }

    });
    return response;
  }

  @GetMapping(value = "/customers/{id}/branches")
  public DeferredResult<ResponseEntity<PagedResources<Resource<Branch>>>> getAllBranches(@PathVariable("id") Long id,
                                                                                         @PageableDefault Pageable pageable, PagedResourcesAssembler<Branch> assembler,
                                                                                         HttpServletRequest request) {

    DeferredResult<ResponseEntity<PagedResources<Resource<Branch>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.")));

    log.debug("Getting banches for customer {}", id);
    ListenableFuture<Page<Branch>> future = customerService.getAllBranches(id, pageable);

    future.addCallback(new ListenableFutureCallback<Page<Branch>>() {

      @Override
      public void onSuccess(Page<Branch> result) {
        Link rootLink = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
            "self");

        if (!result.hasContent())
          response.setResult(ResponseEntity.notFound().build());
        else
          response.setResult(ResponseEntity.ok(assembler.toResource(result, branchResourceAssembler, rootLink)));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve branches for customer id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot retrieve branches for this customer due to server error."));

      }

    });
    return response;
  }

  @PutMapping(value = "/customers/{id}/branches")
  public DeferredResult<ResponseEntity<Object>> addBranch(@PathVariable("id") Long id,
                                                          @RequestBody @Valid Branch branch, HttpServletRequest request) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.")));

    log.debug("Adding branch {} to customer with id {}", branch, id);

    StringBuffer builder = new StringBuffer();
    builder.append(request.getScheme()).append("://").append(request.getHeader("Host")).append(request.getContextPath())
        .append("/branches/");
    log.debug("Builder branch location: {}", builder.toString());
    ListenableFuture<Optional<URI>> result = customerService.addBranch(id, branch, builder);

    result.addCallback(new ListenableFutureCallback<Optional<URI>>() {

      @Override
      public void onSuccess(Optional<URI> result) {
        response.setResult(result.map(location -> ResponseEntity.created(location).build())
            .orElse(ResponseEntity.badRequest().body("Customer  does not exist")));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot update branch {} for customer id {} due to error: {}", branch.toString(), id, ex.getMessage(),
            ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot update branch for this customer due to error."));
      }
    });
    return response;
  }

  @GetMapping(value = "/customers/{id}/contact")
  public DeferredResult<ResponseEntity<?>> getContact(@PathVariable("id") Long id, HttpServletRequest request) {
    DeferredResult<ResponseEntity<?>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.")));

    ListenableFuture<Optional<Contact>> future = customerService.getContact(id);
    future.addCallback(new ListenableFutureCallback<Optional<Contact>>() {

      @Override
      public void onSuccess(Optional<Contact> contact) {
        log.debug("Rendering customer contact details {}", contact.map(Contact::getName).orElse("None"));
        response.setResult(contact
            .map(it -> new Resource<>(it,
                new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(), "contact")))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve contact due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot retrieve contact for this customer due to server error."));

      }
    });
    return response;
  }

  @PutMapping(value = "/customers/{id}/contact")
  public DeferredResult<ResponseEntity<Object>> addContact(@PathVariable("id") Long id, HttpServletRequest request,
                                                           @RequestBody @Valid Contact contact) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError(
        (Throwable t) -> response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured.")));

    StringBuffer builder = new StringBuffer();
    builder.append(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString());

    ListenableFuture<Optional<URI>> result = customerService.addContact(id, contact, builder);

    result.addCallback(new ListenableFutureCallback<Optional<URI>>() {

      @Override
      public void onSuccess(Optional<URI> result) {

        response.setResult(result.map(location -> ResponseEntity.created(location).build())
            .orElse(ResponseEntity.badRequest().body("Customer Not Found")));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot update contact {} for customer id {} due to error: {}", contact.toString(), id,
            ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot update contact for this customer due to error."));
      }

    });
    return response;
  }

  private Date getTodaysDate() {
    Date today = Date
        .from(LocalDate.now().atStartOfDay(ZoneId.of(environment.getProperty("spring.jackson.time-zone"))).toInstant());
    log.debug("Returning Date filter for today, value is {}", new SimpleDateFormat("dd-MM-yyyy").format(today));
    return today;
  }

}
