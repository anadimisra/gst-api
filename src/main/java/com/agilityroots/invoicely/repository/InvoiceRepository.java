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
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.Invoice;

/**
 * @author anadi
 *
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, SoftDelete<Invoice> {

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  @EntityGraph(value = "invoice_all_details")
  Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
  
  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  @EntityGraph(value = "invoice_customer_details")
  Optional<Invoice> findCustomerDetailsByInvoiceNumber(String invoiceNumber);

  @Lock(LockModeType.OPTIMISTIC)
  @CacheEvict("invoices")
  <S extends Invoice> S saveAndFlush(S entity);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  Page<Invoice> findAll(Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  Page<Invoice> findAllByCustomer_Id(Long id, Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  @EntityGraph(value = "invoice_details")
  Invoice getOne(Long id);

  /**
   * All paid invoices
   * 
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNotNull(Pageable pageable);

  /**
   * All due invoices
   * 
   * @param today
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateAfter(@Param("today") Date today, Pageable pageable);

  /**
   * All Overdue invoices
   * 
   * @param today
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateBefore(@Param("today") Date today, Pageable pageable);

  /**
   * Paid invoices by Customer
   * 
   * @param today
   * @param id
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNotNullAndCustomer_Id(Long id, Pageable pageable);

  /**
   * Due invoices by Customer
   * 
   * @param today
   * @param id
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_Id(Date today, Long id, Pageable pageable);

  /**
   * Overdue Invoiced by Customer
   * 
   * @param today
   * @param id
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_Id(Date today, Long id, Pageable pageable);

}