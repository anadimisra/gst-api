package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.Payment;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.InvoiceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
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
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author anadi
 *
 */
@Slf4j
@RestController
@ExposesResourceFor(Invoice.class)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class InvoiceController {

  private final InvoiceService invoiceService;

  private final InvoiceResourceAssembler invoiceResourceAssembler;

  private final CustomerResourceAssember CustomerResourceAssember;

  @GetMapping(value = "invoices/{invoiceNumber}", produces = MediaTypes.HAL_JSON_VALUE)
  public DeferredResult<ResponseEntity<Resource<Invoice>>> getInvoice(@PathVariable("invoiceNumber") String invoiceNumber,
                                                                      HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resource<Invoice>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    ListenableFuture<Optional<Invoice>> result = invoiceService.getInvoice(invoiceNumber);

    result.addCallback(new ListenableFutureCallback<Optional<Invoice>>() {

      @Override
      public void onSuccess(Optional<Invoice> result) {

        response.setResult(result.map(invoiceResourceAssembler::toResource).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build()));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot get invoice details for id {} due to error: {}", invoiceNumber, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot get invoice details due to server error."));

      }
    });

    return response;
  }

  @PutMapping("/invoices/{id}/payments")
  public DeferredResult<ResponseEntity<Object>> recordPayments(@PathVariable Long id,
                                                               @RequestBody @Valid List<Payment> payments, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Object>> response = getLocationHeaderDeferredResult();

    if (null == payments) {
      response.setResult(ResponseEntity.badRequest().body("Payment data missing"));
      return response;
    }

    ListenableFuture<Optional<Invoice>> future = invoiceService.updatePayments(id, payments);

    future.addCallback(new ListenableFutureCallback<Optional<Invoice>>() {

      @Override
      public void onSuccess(Optional<Invoice> result) {
        if (result.isPresent())
          response.setResult(ResponseEntity
              .created(ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand().toUri()).build());
        else
          response.setErrorResult(ResponseEntity.badRequest().body("Cannot update pyments for invoice"));

      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot save invoice {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot save invoice details due to server error."));
      }
    });

    return response;
  }

  private DeferredResult<ResponseEntity<Object>> getLocationHeaderDeferredResult() {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
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

  private DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getInvoiceResourceDeferredResult() {
    DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    return response;
  }

}
