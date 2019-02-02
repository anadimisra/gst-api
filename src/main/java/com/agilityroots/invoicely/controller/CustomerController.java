/**
 *  13-Nov-2018 CustomerController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
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
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CustomerService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 */
@Slf4j
@RestController
@ExposesResourceFor(Customer.class)
public class CustomerController {

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private BranchRepository branchRepository;

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private Environment environment;

  @Autowired
  private CustomerResourceAssember customerResourceAssembler;

  @Autowired
  private BranchResourceAssembler branchResourceAssembler;

  @Autowired
  private InvoiceResourceAssembler invoiceReourceAssembler;

  @Autowired
  private CustomerService customerService;

  @GetMapping(value = "/customers", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Customer>>>> getAllCustomers(
      @PageableDefault(page = 0, size = 20, sort = "name", direction = Direction.ASC) Pageable pageable,
      PagedResourcesAssembler<Customer> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Customer>>>> response = new DeferredResult<>(
        Long.valueOf(1000000));
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    ListenableFuture<Page<Customer>> future = customerService.getCustomers(pageable);
    future.addCallback(new ListenableFutureCallback<Page<Customer>>() {

      @Override
      public void onSuccess(Page<Customer> result) {
        Link self = new Link(
            ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
        log.debug("Generated Self Link {} for Customer Resource Collection", self.getHref());
        if (result.hasContent())
          response.setResult(ResponseEntity.ok(assembler.toResource(result, customerResourceAssembler, self)));
        else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} customers", result.getNumber());
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve customers due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot save customers list due to server error."));
      }

    });

    return response;
  }

  @PostMapping("/customers")
  public DeferredResult<ResponseEntity<Object>> save(HttpServletRequest request,
      @RequestBody @Valid Customer customer) {

    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    ListenableFuture<Customer> future = customerService.save(customer);
    future.addCallback(new ListenableFutureCallback<Customer>() {

      @Override
      public void onSuccess(Customer result) {
        URI location = ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}").buildAndExpand(result.getId())
            .toUri();
        log.debug("Created Location Header {} for {}", location.toString(), result.getName());
        ResponseEntity<Object> responseEntity = ResponseEntity.created(location).build();
        log.debug("Reponse Status for POST Request is :: " + responseEntity.getStatusCodeValue());
        log.debug("Reponse Data for POST Request is :: " + responseEntity.getHeaders().getLocation().toString());
        response.setResult(responseEntity);
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot save customer {} due to error: {}", customer.toString(), ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot save customer details due to server error."));

      }

    });

    return response;
  }

  @GetMapping(value = "/customers/{id}", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resource<Customer>>> getCustomer(@PathVariable Long id,
      HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resource<Customer>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    log.debug("Finding customer details for {}", id);
    ListenableFuture<Optional<Customer>> future = customerService.getCustomer(id);
    future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

      @Override
      public void onSuccess(Optional<Customer> customer) {
        response.setResult(customer.map(customerResourceAssembler::toResource).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build()));

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

  @GetMapping(value = "/customers/{id}/invoices", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getInvoicesByCustomer(@PathVariable("id") Long id,
      @PageableDefault(page = 0, size = 10) Pageable pageable, PagedResourcesAssembler<Invoice> assembler,
      HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
    });
    log.debug("Getting invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = invoiceRepository.findAllByCustomer_Id(id, pageable);
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

  @GetMapping(value = "/customers/{id}/invoices/paid", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPaidInvoicesByCustomer(
      @PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request)
      throws InterruptedException, ExecutionException {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
    });
    log.debug("Getting paid invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = invoiceRepository.findByPayments_PaymentDateIsNotNullAndCustomer_Id(id,
        pageable);
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

  @GetMapping(value = "/customers/{id}/invoices/pending", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPendingInvoicesByCustomer(
      @PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
    });
    log.debug("Getting unpaid invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = invoiceRepository
        .findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_Id(getTodaysDate(), id, pageable);
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
        log.error("Cannot retrieve pending invoices for customer id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get customer pending invoices due to server error."));
      }

    });
    return response;
  }

  @GetMapping(value = "/customers/{id}/invoices/overdue", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getOverdueInvoicesByCustomer(
      @PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
    });
    log.debug("Getting overdue invoices for customer id {}", id);
    ListenableFuture<Page<Invoice>> future = invoiceRepository
        .findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_Id(getTodaysDate(), id, pageable);
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

  @GetMapping(value = "/customers/{id}/branches", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<PagedResources<Resource<Branch>>>> getAllBranches(@PathVariable("id") Long id,
      @PageableDefault(page = 0, size = 20) Pageable pageable, PagedResourcesAssembler<Branch> assembler,
      HttpServletRequest request) {

    DeferredResult<ResponseEntity<PagedResources<Resource<Branch>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
    });
    log.debug("Getting banches for customer {}", id);
    ListenableFuture<Optional<Customer>> future = customerService.getCustomerWithAllBranches(id);
    future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

      @Override
      public void onSuccess(Optional<Customer> result) {
        
        Link rootLink = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
            "self");
        response
            .setResult(result
                .map(it -> assembler.toResource(new PageImpl<>(it.getBranches(), pageable, it.getBranches().size()),
                    branchResourceAssembler, rootLink))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
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

  @PutMapping(value = "/customers/{id}/branches", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Object>> addBranch(@PathVariable("id") Long id,
      @RequestBody @Valid Branch branch, HttpServletRequest request) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    log.debug("Adding branch {} to customer with id {}", branch, id);
    ListenableFuture<Optional<Customer>> future = customerService.getCustomerWithAllBranches(id);
    future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

      @Override
      public void onSuccess(Optional<Customer> result) {
        List<Branch> allBranches = new ArrayList<>();

        result.map(Customer::getBranches).map(allBranches::addAll).orElse(allBranches.addAll(Collections.emptyList()));

        if (result.isPresent()) {
          Branch saved = branchRepository.saveAndFlush(branch);
          allBranches.add(saved);
          Customer customer = result.get();
          customer.setBranches(allBranches);
          customerRepository.saveAndFlush(customer);
          response.setResult(ResponseEntity.created(
              ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}").buildAndExpand(saved.getId()).toUri())
              .build());
        } else
          response.setErrorResult(ResponseEntity.unprocessableEntity().build());

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

  @GetMapping(value = "/customers/{id}/contact", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<?>> getContact(@PathVariable("id") Long id, HttpServletRequest request) {
    DeferredResult<ResponseEntity<?>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    log.debug("Finding customer details for id {}", id);
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

  @PutMapping(value = "/customers/{id}/contact", produces = MediaTypes.HAL_JSON_VALUE)
  private DeferredResult<ResponseEntity<Object>> addContact(@PathVariable("id") Long id, HttpServletRequest request,
      @RequestBody @Valid Contact contact) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    log.debug("Finding customer with id {}", id);
    ListenableFuture<Optional<Customer>> future = customerService.getCustomer(id);
    future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {
      @Override
      public void onSuccess(Optional<Customer> customer) {

        if (customer.isPresent()) {
          log.debug("Adding contact details {} to customer {}", contact.toString(),
              customer.map(Customer::getName).orElse("None"));
          Contact saved = contactRepository.saveAndFlush(contact);
          Customer unsaved = customer.get();
          unsaved.setContact(saved);
          customerRepository.saveAndFlush(unsaved);
          URI location = ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}").buildAndExpand(saved.getId())
              .toUri();
          response.setResult(ResponseEntity.created(location).build());
          log.debug("Rendered Location header {}", location.toString());
        } else
          response.setResult(ResponseEntity.unprocessableEntity().build());

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

  @PutMapping(value = "/customers/{id}/invoices", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Object>> addInvoice(@PathVariable("id") Long id,
      @RequestBody @Valid Invoice invoice, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    log.debug("Finding customer with id {}", id);
    ListenableFuture<Optional<Customer>> result = customerService.getCustomer(id);
    result.addCallback(new ListenableFutureCallback<Optional<Customer>>() {
      @Override
      public void onSuccess(Optional<Customer> result) {
        if (result.isPresent()) {
          Invoice saved = invoiceRepository.saveAndFlush(invoice);
          Customer customer = result.get();
          List<Invoice> invoices = new ArrayList<>();
          invoices.addAll(customer.getInvoices());
          invoices.add(saved);
          customer.setInvoices(invoices);
          customerService.save(customer);
          URI location = ServletUriComponentsBuilder.fromRequestUri(request).host(request.getRemoteHost())
              .port(request.getRemotePort()).path(request.getContextPath()).path("/invoices/{id}")
              .buildAndExpand(saved.getId()).toUri();
          response.setResult(ResponseEntity.created(location).build());

        } else {
          response.setResult(ResponseEntity.badRequest().build());
        }
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot add invoice:{} to customer {} due to error: {}", invoice.toString(), id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot update invoices for this customer due to error."));
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
