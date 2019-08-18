/**
 * 22-Oct-2018 CustomerRepository.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.Optional;

/**
 * @author anadi
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Optional<Customer> findById(Long id);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Page<Customer> findAll(Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC)
  @CachePut("customers")
  <S extends Customer> S saveAndFlush(S entity);


  Page<Invoice> findByIdAndInvoice_PaymentDateIsNotNull(Long id, Pageable pageable);

  /*
   * Due invoices by Customer
   * @param id
   * @param today
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByIdAndInvoice_DueDateAfterAndInvoice_PaymentsIsNull(Long id, Date today, Pageable pageable);

  /*
   * Overdue Invoiced by Customer
   * @param id
   * @param today
   * @param pageable
   * @return
   */
  @Cacheable("invoices")
  @Lock(LockModeType.OPTIMISTIC)
  Page<Invoice> findByIdAndInvoice_DueDateBeforeAndInvoice_PaymentsIsNull(Long id, Date today, Pageable pageable);
}