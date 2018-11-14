/**
 *  13-Nov-2018 CustomerController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityLinks;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.InvoiceRepository;

/**
 * @author anadi
 *
 */
@Async
@RepositoryRestController
public class CustomerController {

	private InvoiceRepository invoiceRepository;

	private EntityLinks links;

	@Autowired
	public void setEntityLinks(EntityLinks entityLinks) {
		this.links = entityLinks;
	}

	@Autowired
	public void setCustomerRepository(InvoiceRepository invoiceRepository) {
		this.invoiceRepository = invoiceRepository;
	}

	@GetMapping("/customers/{id}/invoices/paid")
	public CompletableFuture<ResponseEntity<Page<Invoice>>> getPaidInvoicesByCustomer(@PathVariable("id") Long id, @RequestParam(defaultValue = "1", required = false) Integer page, @RequestParam(defaultValue = "10", required = false) Integer size, PagedResourcesAssembler<Invoice> assembler) throws InterruptedException, ExecutionException{
		
		Page<Invoice> paidInvoices = invoiceRepository.findByPaymentsIsNotNullAndCustomer_Id(id, PageRequest.of(page, size)).get();
		return CompletableFuture.completedFuture(ResponseEntity.ok(paidInvoices));
	}

	@GetMapping("/customers/{id}/invoices/pending")
	public CompletableFuture<ResponseEntity<Page<Invoice>>> getPendingInvoicesByCustomer(@PathVariable("id") Long id,
			@RequestParam(defaultValue = "1", required = false) Integer page,
			@RequestParam(defaultValue = "10", required = false) Integer size)
			throws InterruptedException, ExecutionException {

		Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
		return CompletableFuture.completedFuture(ResponseEntity.ok(invoiceRepository
				.findByPaymentsIsNullAndDueDateAfterAndCustomer_Id(today, id, PageRequest.of(page, size)).get()));
	}

	@GetMapping("/customers/{id}/invoices/overdue")
	public CompletableFuture<ResponseEntity<Page<Invoice>>> getOverdueInvoicesByCustomer(@PathVariable("id") Long id,
			@RequestParam(defaultValue = "1", required = false) Integer page,
			@RequestParam(defaultValue = "10", required = false) Integer size)
			throws InterruptedException, ExecutionException {

		Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
		return CompletableFuture.completedFuture(ResponseEntity.ok(invoiceRepository
				.findByPaymentsIsNullAndDueDateBeforeAndCustomer_Id(today, id, PageRequest.of(page, size)).get()));
	}
}
