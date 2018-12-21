/**
 *  15-Nov-2018 InvoiceController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.InvoiceService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@RestController
@ExposesResourceFor(Invoice.class)
public class InvoiceController {

  @Autowired
  private InvoiceService invoiceService;

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private InvoiceResourceAssembler invoiceResourceAssembler;

  @Autowired
  private CustomerResourceAssember CustomerResourceAssember;

  @GetMapping(value = "/invoices", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getInvoices(
      @PageableDefault(page = 0, size = 20, sort = "name", direction = Direction.ASC) Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = setupDeferredResult();

    ListenableFuture<Page<Invoice>> result = invoiceService.getInvoices(pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> result) {
        Link self = new Link(
            ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
        log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
        if (result.hasContent())
          response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
        else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", result.getNumber());
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoices due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoices list due to server error."));

      }
    });

    return response;

  }

  @GetMapping(value = "invoices/{id}", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resource<Invoice>>> getInvoice(@PathVariable("id") Long id,
      HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resource<Invoice>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    ListenableFuture<Optional<Invoice>> result = invoiceService.getInvoice(id);

    result.addCallback(new ListenableFutureCallback<Optional<Invoice>>() {

      @Override
      public void onSuccess(Optional<Invoice> result) {

        response.setResult(result.map(invoiceResourceAssembler::toResource).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build()));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot get invoice details for id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoice details due to server error."));

      }
    });

    return response;
  }

  @GetMapping(value = "/invoices/paid", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPaidInvoices(
      @PageableDefault(page = 0, size = 20, sort = "name", direction = Direction.ASC) Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = setupDeferredResult();

    ListenableFuture<Page<Invoice>> result = invoiceRepository.findByPaymentsIsNotNull(pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> result) {
        Link self = new Link(
            ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
        log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
        if (result.hasContent())
          response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
        else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", result.getNumber());
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoices due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoices list due to server error."));

      }
    });

    return response;

  }

  @GetMapping(value = "/invoices/pending", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPendingInvoices(
      @PageableDefault(page = 0, size = 20, sort = "name", direction = Direction.ASC) Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request,
      @RequestParam(required = false) Date date) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = setupDeferredResult();
    Date queryDate = getOptinalDateParamter(date);
    ListenableFuture<Page<Invoice>> result = invoiceRepository.findByPaymentsIsNullAndDueDateAfter(queryDate, pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> result) {
        Link self = new Link(
            ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
        log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
        if (result.hasContent())
          response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
        else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", result.getNumber());
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoices due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoices list due to server error."));

      }
    });

    return response;

  }

  @GetMapping(value = "/invoices/overdue", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getOverdueInvoices(
      @PageableDefault(page = 0, size = 20, sort = "name", direction = Direction.ASC) Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request,
      @RequestParam(required = false) Date date) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = setupDeferredResult();
    Date queryDate = getOptinalDateParamter(date);
    ListenableFuture<Page<Invoice>> result = invoiceRepository.findByPaymentsIsNullAndDueDateBefore(queryDate,
        pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> result) {
        Link self = new Link(
            ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
        log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
        if (result.hasContent())
          response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
        else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", result.getNumber());
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoices due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoices list due to server error."));

      }
    });

    return response;

  }

  @GetMapping(value = "/invoices/{id}/customer", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resource<Customer>>> getCustomer(@PathVariable("id") Long id) {

    DeferredResult<ResponseEntity<Resource<Customer>>> response = new DeferredResult<ResponseEntity<Resource<Customer>>>();

    ListenableFuture<Optional<Invoice>> result = invoiceService.getInvoice(id);

    result.addCallback(new ListenableFutureCallback<Optional<Invoice>>() {

      @Override
      public void onSuccess(Optional<Invoice> result) {
        response.setResult(result.map(Invoice::getCustomer).map(CustomerResourceAssember::toResource)
            .map(ResponseEntity::ok).orElse(ResponseEntity.unprocessableEntity().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoice customer due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoice customer due to server error."));

      }
    });

    return response;
  }

  @PostMapping("/invoices")
  public DeferredResult<ResponseEntity<Object>> save(@RequestBody(required = true) @Valid Invoice invoice,
      HttpServletRequest request) {

    return saveOrUpdate(invoice, request);
  }

  @PutMapping("/invoices")
  public DeferredResult<ResponseEntity<Object>> update(@RequestBody(required = true) @Valid Invoice invoice,
      HttpServletRequest request) {

    return saveOrUpdate(invoice, request);
  }

  /**
   * @param invoice
   * @param request
   * @return
   */
  private DeferredResult<ResponseEntity<Object>> saveOrUpdate(Invoice invoice, HttpServletRequest request) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    ListenableFuture<Invoice> result = invoiceService.save(invoice);

    result.addCallback(new ListenableFutureCallback<Invoice>() {

      @Override
      public void onSuccess(Invoice result) {
        URI location = ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}").buildAndExpand(result.getId())
            .toUri();
        log.debug("Created Location Header {} for {}", location.toString(), result.getInvoiceNumber());
        ResponseEntity<Object> responseEntity = ResponseEntity.created(location).build();
        log.debug("Reponse Status for POST Request is :: " + responseEntity.getStatusCodeValue());
        log.debug("Reponse Data for POST Request is :: " + responseEntity.getHeaders().getLocation().toString());
        response.setResult(responseEntity);

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot save invoice {} due to error: {}", invoice.toString(), ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot save invoice details due to server error."));

      }
    });
    return response;
  }

  /**
   * @param date
   * @return
   */
  private Date getOptinalDateParamter(Date date) {
    Date queryDate = date != null ? date
        : Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    return queryDate;
  }

  /**
   * @return
   */
  private DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> setupDeferredResult() {
    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    return response;
  }

}
