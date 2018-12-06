/**
 *  3 Dec 2018 BranchController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;

/**
 * @author anadi
 *
 */
@RestController
@ExposesResourceFor(Branch.class)
public class BranchController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BranchController.class);

	public static final Iterable<Resource<?>> EMPTY_RESOURCE_LIST = Collections.emptyList();

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private ContactRepository contactRepository;

	@GetMapping("/branches")
	public DeferredResult<ResponseEntity<Resources<Resource<Branch>>>> getAll(
			@PageableDefault(page = 0, size = 20) @SortDefault.SortDefaults({
					@SortDefault(sort = "name", direction = Direction.ASC) }) Pageable pageable,
			PagedResourcesAssembler<Branch> assembler, HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<Resource<Branch>>>> response = new DeferredResult<>();
		response.setErrorResult(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
		return response;
	}

	@PostMapping("/branches")
	public DeferredResult<ResponseEntity<Object>> save(HttpServletRequest request) {

		DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
		response.setErrorResult(ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build());
		return response;
	}

	@GetMapping("/branches/{id}")
	public DeferredResult<ResponseEntity<ResourceSupport>> getBranch(@PathVariable Long id,
			HttpServletRequest request) {

		DeferredResult<ResponseEntity<ResourceSupport>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});
		ListenableFuture<Branch> future = branchRepository.findOneById(id);

		future.addCallback(new ListenableFutureCallback<Branch>() {

			@Override
			public void onSuccess(Branch branch) {
				if (branch == null)
					response.setResult(ResponseEntity.notFound().build());
				else {
					LOGGER.debug("Returning details of {}", branch.getBranchName());
					Resource<Branch> resource = new Resource<Branch>(branch, getEntityLinks(request));
					response.setResult(ResponseEntity.ok(resource));
				}

			}

			@Override
			public void onFailure(Throwable ex) {
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Cannot get branch details due to server error."));
			}

		});

		return response;
	}

	@PutMapping("/branches/{id}/contact")
	public DeferredResult<ResponseEntity<Object>> addContact(@PathVariable("id") Long id, HttpServletRequest request,
			@RequestBody @Valid Contact contact) {
		DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Contact> future = AsyncResult.forValue(contactRepository.saveAndFlush(contact));

		future.addCallback(new ListenableFutureCallback<Contact>() {
			@Override
			public void onSuccess(Contact result) {
				response.setResult(ResponseEntity.ok().location(ServletUriComponentsBuilder.fromRequestUri(request)
						.path("/{id}").buildAndExpand(result.getId()).toUri()).build());
			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not update due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(
						ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));

			}
		});

		return response;
	}

	@GetMapping("/branches/{id}/contact")
	public DeferredResult<ResponseEntity<Object>> getContact(@PathVariable("id") Long id, HttpServletRequest request) {
		DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Optional<Contact>> future = AsyncResult
				.forValue(branchRepository.findById(id).map(it -> it.getContact()));

		future.addCallback(new ListenableFutureCallback<Optional<Contact>>() {
			@Override
			public void onSuccess(Optional<Contact> result) {

				Resource<Contact> resource = result.<Resource<Contact>>map(
						it -> new Resource<Contact>(it, new Link(getCurrentLocation(request).toString(), "contact")))
						.orElse(new Resource<Contact>(new Contact(),
								new Link(getCurrentLocation(request).toString(), "contact")));
				response.setResult(ResponseEntity.ok().body(resource));
			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not update due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(
						ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));

			}
		});

		return response;
	}

	private URI getCurrentLocation(HttpServletRequest request) {
		return ServletUriComponentsBuilder.fromRequestUri(request).build().toUri();
	}

	private List<Link> getEntityLinks(HttpServletRequest request) {
		String location = getCurrentLocation(request).toString();
		List<Link> links = new ArrayList<>();
		links.add(new Link(location, "self"));
		links.add(new Link(location, "branch"));
		links.add(new Link(new StringBuilder(location).append("/").append("contact").toString(), "contact"));
		return links;

	}
}
