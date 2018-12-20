/**
 *  15-Nov-2018 InvoiceController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.InvoiceService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@RestController
@ExposesResourceFor(Invoice.class)
public class InvoiceController {

	@Autowired
	private InvoiceService invoiceService;

	@Autowired
	private InvoiceResourceAssembler invoiceResourceAssembler;

	@GetMapping(value = "/invoices", produces = MediaTypes.HAL_JSON_VALUE)
	public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getInvoices(
			@PageableDefault(page = 0, size = 20, sort = "name", direction = Direction.ASC) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Page<Invoice>> result = invoiceService.getInvoices(pageable);

		result.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

			@Override
			public void onSuccess(Page<Invoice> result) {
				Link self = new Link(
						ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand(pageable).toUri().toString(),
						"self");
				log.debug("Generated Self Link {} for Invoice Resource Collection", self.getHref());
				if (result.hasContent())
					response.setResult(ResponseEntity.ok(assembler.toResource(result, invoiceResourceAssembler, self)));
				else
					response.setErrorResult(ResponseEntity.notFound().build());
				log.debug("Returning Response with {} invoices", result.getNumber());
			}

			@Override
			public void onFailure(Throwable ex) {
				log.error("Cannot retrieve invoices due to error: {}", ex.getMessage(), ex);
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Cannot save invoices list due to server error."));

			}
		});

		return response;

	}

	@GetMapping(value = "invoices/{id}", produces = MediaTypes.HAL_JSON_VALUE)
	public DeferredResult<ResponseEntity<Resource<Invoice>>> getInvoice(@PathVariable("id") Long id,
			HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resource<Invoice>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Optional<Invoice>> result = invoiceService.getInvoice(id);

		result.addCallback(new ListenableFutureCallback<Optional<Invoice>>() {

			@Override
			public void onSuccess(Optional<Invoice> result) {

				response.setResult(result.map(invoiceResourceAssembler::toResource).map(ResponseEntity::ok)
						.orElse(ResponseEntity.notFound().build()));

			}

			@Override
			public void onFailure(Throwable ex) {
				log.error("Cannot get invoice details for id {} due to error: {}", id, ex.getMessage(), ex);
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Cannot get invoice details due to server error."));

			}
		});

		return response;
	}
}
