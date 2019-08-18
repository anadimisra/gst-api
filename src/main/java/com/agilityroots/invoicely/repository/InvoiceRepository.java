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
  Page<Invoice> findAll(Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("invoices")
  @EntityGraph(value = "invoice_all_details")
  Invoice getOne(Long id);
}