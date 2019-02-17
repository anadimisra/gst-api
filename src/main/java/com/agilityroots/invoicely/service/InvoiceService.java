package com.agilityroots.invoicely.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.Payment;
import com.agilityroots.invoicely.repository.InvoiceRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@Async
@Service
public class InvoiceService {

  private InvoiceRepository invoiceRepository;

  @Autowired
  public InvoiceService(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getInvoices(Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findAll(pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Invoice>> getInvoice(Long id) {
    Optional<Invoice> result = Optional.ofNullable(invoiceRepository.getOne(id));
    try {
      log.debug("Loading Invoice details");
      result.get().getLineItems();
    } catch (NoSuchElementException e) {
      result = Optional.empty();
      log.warn("No details fround for invoice with id {}", id);
    }
    return AsyncResult.forValue(result);
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Invoice> asyncSave(Invoice invoice) {
    return AsyncResult.forValue(invoiceRepository.saveAndFlush(invoice));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public Invoice save(Invoice invoice) {
    log.debug("Saving invoice number: {}", invoice.getInvoiceNumber());
    return invoiceRepository.saveAndFlush(invoice);
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getPaidInvoices(Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNotNull(pageable));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getDueInvoices(Date today, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfter(today, pageable));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getOverdueInvoices(Date today, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBefore(today, pageable));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Invoice>> updatePayments(Long id, final List<Payment> payments) {
    Optional<Invoice> result = invoiceRepository.findById(id);
    List<Payment> invoicePayments = result.map(Invoice::getPayments).orElse(new ArrayList<Payment>());
    log.debug("This invoice has {} recorded payments", invoicePayments.size());
    invoicePayments.addAll(payments);
    log.debug("Added {} payments to the invoice", payments.size());
    log.debug("Total {} payments recorded", invoicePayments.size());
    result.ifPresent(it -> {
      it.setPayments(invoicePayments);
      invoiceRepository.saveAndFlush(it);
    });
    return AsyncResult.forValue(result);
  }
}
