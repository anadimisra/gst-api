/**
 *  24-Oct-2018 InvoiceRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import java.util.Date;

import javax.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Invoice;

/**
 * @author anadi
 *
 */
@RepositoryRestResource(path = "invoices", collectionResourceRel = "invoices")
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, SoftDelete<Invoice> {

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	ListenableFuture<Invoice> findOneById(Long id);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	ListenableFuture<Invoice> findOneByInvoiceNumber(String invoiceNumber);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(path = "billed/from", rel = "billedFrom")
	ListenableFuture<Page<Invoice>> findByBilledFrom(Branch billedFrom, Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(path = "billed/to", rel = "billedTo")
	ListenableFuture<Page<Invoice>> findByBilledTo(Branch billedTo, Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(path = "paid", rel = "paid")
	ListenableFuture<Page<Invoice>> findByPaymentsIsNotNull(Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(path = "pending", rel = "pending")
	ListenableFuture<Page<Invoice>> findByDueDateAfter(Date today, Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(path = "overdue", rel = "overdue")
	ListenableFuture<Page<Invoice>> findByPaymentsIsNullAndDueDateBefore(Date today, Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(exported = false)
	ListenableFuture<Page<Invoice>> findByPaymentsIsNotNullAndCustomer_Id(Long Id, Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(exported = false)
	ListenableFuture<Page<Invoice>> findByPaymentsIsNullAndDueDateAfterAndCustomer_Id(Date today, Long Id,
			Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(exported = false)
	ListenableFuture<Page<Invoice>> findByPaymentsIsNullAndDueDateBeforeAndCustomer_Id(Date today, Long Id,
			Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(exported = false)
	ListenableFuture<Page<Invoice>> findByPaymentsIsNotNullAndBilledTo_Id(Long Id, Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(exported = false)
	ListenableFuture<Page<Invoice>> findByPaymentsIsNullAndDueDateAfterAndBilledTo_Id(Date today, Long Id,
			Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(exported = false)
	ListenableFuture<Page<Invoice>> findByPaymentsIsNullAndDueDateBeforeAndBilledTo_Id(Date today, Long Id,
			Pageable pageable);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	@Override
	@RestResource(exported = false)
	void deleteAll();

	@Override
	@RestResource(exported = false)
	void deleteAll(Iterable<? extends Invoice> entities);

}