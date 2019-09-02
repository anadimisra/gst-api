package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.entity.AuditableEntity;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CompanyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

/**
 * @author anadi
 */
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@ExposesResourceFor(Company.class)
public class CompanyController {

  private final CompanyService companyService;

  private final InvoiceResourceAssembler invoiceResourceAssembler;

  private final BranchResourceAssembler branchResourceAssembler;

  @GetMapping("/companies/{id}/invoices")
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getAllInvoices(@PathVariable("id") Long id
      , @PageableDefault Pageable pageable, PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = getAuditableEntitySubTypeResourceDeferredResult();

    ListenableFuture<Page<Invoice>> result = companyService.getAllInvoices(id, pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {
      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoices due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoices list due to server error."));
      }

      @Override
      public void onSuccess(Page<Invoice> result) {
        if (result.hasContent()) {
          Link self = new Link(
              ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
          log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
          response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
        } else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", result.getSize());
      }
    });

    return response;
  }

  @GetMapping(value = "/companies/{id}/invoices/paid", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPaidInvoices(@PathVariable("id") Long id,
                                                                                      @PageableDefault Pageable pageable,
                                                                                      PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = getAuditableEntitySubTypeResourceDeferredResult();

    ListenableFuture<Page<Invoice>> result = companyService.getPaidInvoices(id, pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> result) {

        if (result.hasContent()) {
          Link self = new Link(
              ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
          log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
          response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
        } else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", result.getSize());
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

  @GetMapping(value = "/companies/{id}/invoices/due", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPendingInvoices(@PathVariable("id") Long id,
                                                                                         @PageableDefault Pageable pageable,
                                                                                         PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request,
                                                                                         @RequestParam(required = false) Date date) {

    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = getAuditableEntitySubTypeResourceDeferredResult();
    ListenableFuture<Page<Invoice>> result = companyService.getDueInvoices(id, getOptinalDateParamter(date), pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

      @Override
      public void onSuccess(Page<Invoice> result) {

        if (result.hasContent()) {
          Link self = new Link(
              ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
          log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
          response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
        } else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", result.getContent().size());
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

  @GetMapping("/companies/{id}/invoices/overdue")
  public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getOverdueInvoices(@PathVariable("id") Long id,
                                                                                         @PageableDefault Pageable pageable,
                                                                                         @RequestParam(required = false) Date date,
                                                                                         PagedResourcesAssembler<Invoice> assembler,
                                                                                         HttpServletRequest request) {
    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = getAuditableEntitySubTypeResourceDeferredResult();
    ListenableFuture<Page<Invoice>> result = companyService.getOverDueInvoices(id, getOptinalDateParamter(date), pageable);

    result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {
      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve invoices due to error: {}", ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoices list due to server error."));
      }

      @Override
      public void onSuccess(Page<Invoice> invoices) {
        if (invoices.hasContent()) {
          Link self = new Link(
              ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(), "self");
          log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
          response.setResult(ResponseEntity.ok(assembler.toResource(invoices, invoiceResourceAssembler, self)));
        } else
          response.setErrorResult(ResponseEntity.notFound().build());
        log.debug("Returning Response with {} invoices", invoices.getContent().size());
      }
    });
    return response;
  }

  @PutMapping("/companies/{id}/branches")
  public DeferredResult<ResponseEntity<Object>> addBranch(@PathVariable("id") Long id,
                                                          @RequestBody(required = true) @Valid Branch branch, HttpServletRequest request) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    StringBuffer urlBuilder = new StringBuffer();
    urlBuilder.append(request.getScheme()).append("://").append(request.getHeader("Host"))
        .append(request.getContextPath()).append("/branches/");
    ListenableFuture<Optional<URI>> result = companyService.addBranch(id, branch, urlBuilder);

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

  @GetMapping("/companies/{id}/branches")
  public DeferredResult<ResponseEntity<Resources<Resource<Branch>>>> getBranches(@PathVariable("id") Long id,
                                                                                 @PageableDefault Pageable pageable,
                                                                                 PagedResourcesAssembler<Branch> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Branch>>>> response = getAuditableEntitySubTypeResourceDeferredResult();
    log.debug("Getting all branches for company");
    ListenableFuture<Page<Branch>> result = companyService.getBranches(id, pageable);

    result.addCallback(new ListenableFutureCallback<Page<Branch>>() {

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
        log.error("Cannot retrieve branches for company id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot retrieve branches for this customer due to server error."));
      }
    });
    return response;
  }

  private URI getCurrentLocation(HttpServletRequest request) {
    return ServletUriComponentsBuilder.fromRequestUri(request).build().toUri();
  }

  private <T extends AuditableEntity> DeferredResult<ResponseEntity<Resources<Resource<T>>>> getAuditableEntitySubTypeResourceDeferredResult() {
    DeferredResult<ResponseEntity<Resources<Resource<T>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    return response;
  }

  /**
   * @param date
   * @return today's date
   */
  private Date getOptinalDateParamter(Date date) {
    Date queryDate = date != null ? date
        : Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    return queryDate;
  }
}
