package com.agilityroots.invoicely.service;

import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.Payment;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.*;

/**
 * @author anadi
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
  public ListenableFuture<Optional<Invoice>> getInvoice(String invoiceNumber) {
    Optional<Invoice> result = invoiceRepository.findByInvoiceNumber(invoiceNumber);
    return AsyncResult.forValue(result);
  }

  public ListenableFuture<Optional<Invoice>> getInvoiceWithCustomer(String invoiceNumber) {
    Optional<Invoice> result = invoiceRepository.findCustomerDetailsByInvoiceNumber(invoiceNumber);
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
    Set<Payment> invoicePayments = result.map(Invoice::getPayments).orElse(new HashSet<Payment>());
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
