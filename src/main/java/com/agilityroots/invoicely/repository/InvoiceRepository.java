/**
 *  24-Oct-2018 InvoiceRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import java.util.Date;
import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Invoice;

/**
 * @author anadi
 *
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, SoftDelete<Invoice> {

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  Optional<Invoice> findById(Long id);

  @SuppressWarnings("unchecked")
  @Lock(LockModeType.OPTIMISTIC)
  @CacheEvict("invoices")
  Invoice saveAndFlush(Invoice entity);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  Page<Invoice> findAll(Pageable pageable);

  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Page<Invoice>> findAllByCustomer_Id(Long id, Pageable pageable);

  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Invoice> findOneByInvoiceNumber(String invoiceNumber);

  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Page<Invoice>> findByPayments_PaymentDateIsNotNull(Pageable pageable);

  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Page<Invoice>> findByPayments_PaymentDateIsNullAndDueDateAfter(@Param("today") Date today,
      Pageable pageable);

  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Page<Invoice>> findByPayments_PaymentDateIsNullAndDueDateBefore(@Param("today") Date today,
      Pageable pageable);

  /**
   * Paid invoices by Customer
   * 
   * @param today
   * @param id
   * @param pageable
   * @return
   */
  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Page<Invoice>> findByPayments_PaymentDateIsNotNullAndCustomer_Id(Long id, Pageable pageable);

  /**
   * Pending invoices by Customer
   * 
   * @param today
   * @param id
   * @param pageable
   * @return
   */
  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Page<Invoice>> findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_Id(Date today, Long id,
      Pageable pageable);

  /**
   * Overdue Invoiced by Customer
   * 
   * @param today
   * @param id
   * @param pageable
   * @return
   */
  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  @Cacheable("invoices")
  ListenableFuture<Page<Invoice>> findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_Id(Date today, Long id,
      Pageable pageable);

}