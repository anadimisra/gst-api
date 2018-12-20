/**
 * 
 */
package com.agilityroots.invoicely.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.InvoiceRepository;

/**
 * @author anadi
 *
 */
@Async
@Service
public class InvoiceService {

	private InvoiceRepository invoiceRepository;

	@Autowired
	public InvoiceService(InvoiceRepository invoiceRepository) {
		this.invoiceRepository = invoiceRepository;
	}

	public ListenableFuture<Page<Invoice>> getInvoices(Pageable pageable) {
		return AsyncResult.forValue(invoiceRepository.findAll(pageable));
	}

	public ListenableFuture<Optional<Invoice>> getInvoice(Long id) {
		return AsyncResult.forValue(invoiceRepository.findById(id));
	}
}
