/**
 * 24-Oct-2018 InvoiceRepository.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.Invoice;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.Optional;

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
  @CacheEvict("invoices")
  <S extends Invoice> S saveAndFlush(S entity);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  @EntityGraph(value = "invoice_all_details")
  Invoice getOne(Long id);

  /**
   * All invoices
   * @param id
   * @param pageable
   * @return All invoices for the company, ordered by latest invoice date first
   */
  Page<Invoice> findAllByCompany_IdOrderByInvoiceDateDesc(Long id, Pageable pageable);

  /**
   * All paid invoices
   * @param id
   * @param pageable
   * @return All paid invoices for the company, ordered by latest payment first
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNotNullAndCompany_IdOrderByPayments_PaymentDateDesc(Long id, Pageable pageable);

  /**
   * All due invoices
   * @param today
   * @param pageable
   * @return All due invoices for the company, ordered by oldest due invoice first
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateAfterAndCompany_IdOrderByDueDateAsc(Date today, Long id, Pageable pageable);

  /**
   * All Overdue invoices
   * @param today
   * @param pageable
   * @return All overdue invoices for the company, ordered by oldest overdue invoice first
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateBeforeAndCompany_IdOrderByDueDateAsc(Date today, Long id, Pageable pageable);

  /**
   * All invoices for a customer
   * @param id
   * @param pageable
   * @return All invoices for a customer, ordered by latest invoice first
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByCustomer_IdOrderByInvoiceDateDesc(Long id, Pageable pageable);

  /**
   * Paid invoices by Customer
   * @param id
   * @param pageable
   * @return All paid invoices by customer, ordered by latest payment first
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNotNullAndCustomer_IdOrderByPayments_PaymentDateDesc(Long id, Pageable pageable);

  /**
   * Due invoices by Customer
   * @param today
   * @param id
   * @param pageable
   * @return All due invoices by customer, ordered by earliest due date first
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_IdOrderByDueDateAsc(Date today, Long id, Pageable pageable);

  /**
   * Overdue Invoices by Customer
   * @param today
   * @param id
   * @param pageable
   * @return All overdue invoices by customer, ordered by overdue since longest period first
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_IdOrderByDueDateAsc(Date today, Long id, Pageable pageable);
}