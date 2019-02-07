package com.agilityroots.invoicely.service;

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
import com.agilityroots.invoicely.repository.InvoiceRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@Service
public class InvoiceService {

  private InvoiceRepository invoiceRepository;

  @Autowired
  public InvoiceService(InvoiceRepository invoiceRepository) {
    this.invoiceRepository = invoiceRepository;
  }

  @Async
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getInvoices(Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findAll(pageable));
  }

  @Async
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Invoice>> getInvoice(Long id) {
    return AsyncResult.forValue(invoiceRepository.findById(id));
  }

  @Async
  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Invoice> asyncSave(Invoice invoice) {
    return AsyncResult.forValue(invoiceRepository.saveAndFlush(invoice));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public Invoice save(Invoice invoice) {
    log.debug("Saving invoice number: {}", invoice.getInvoiceNumber());
    return invoiceRepository.saveAndFlush(invoice);
  }
}
