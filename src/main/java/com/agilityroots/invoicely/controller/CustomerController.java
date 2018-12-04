/**
 *  13-Nov-2018 CustomerController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
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
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;

/**
 * @author anadi
 */
@RepositoryRestController
@ExposesResourceFor(Customer.class)
public class CustomerController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

	public static final Iterable<Resource<?>> EMPTY_RESOURCE_LIST = Collections.emptyList();

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private InvoiceRepository invoiceRepository;

	@Autowired
	private BranchRepository branchRepository;

	@Autowired
	private ContactRepository contactRepository;

	@Autowired
	private Environment environment;

	@Autowired
	private CustomerResourceAssember customerResourceAssembler;

	@Autowired
	private BranchResourceAssembler branchResourceAssembler;

	@GetMapping("/customers")
	public DeferredResult<ResponseEntity<Resources<?>>> getAllCustomers(
			@PageableDefault(page = 0, size = 20) @SortDefault.SortDefaults({
					@SortDefault(sort = "name", direction = Direction.ASC) }) Pageable pageable,
			PagedResourcesAssembler<Customer> assembler, HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<?>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Page<Customer>> future = AsyncResult.forValue(customerRepository.findAll(pageable));

		future.addCallback(new ListenableFutureCallback<Page<Customer>>() {

			@Override
			public void onSuccess(Page<Customer> result) {
				Link rootLink = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
						"self");
				if (!result.getContent().isEmpty())
					response.setResult(
							ResponseEntity.ok(assembler.toResource(result, customerResourceAssembler, rootLink)));
				else
					response.setResult(ResponseEntity.ok(assembler.toEmptyResource(result, Customer.class, rootLink)));

			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not retrieve customers due to error", ex);
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Could not save customers list due to server error."));
			}

		});

		return response;
	}

	@PostMapping("/customers")
	public DeferredResult<ResponseEntity<Object>> save(HttpServletRequest request,
			@RequestBody @Valid Customer customer) {

		DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Customer> future = AsyncResult.forValue(customerRepository.saveAndFlush(customer));

		future.addCallback(new ListenableFutureCallback<Customer>() {

			@Override
			public void onSuccess(Customer result) {
				URI location = ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}")
						.buildAndExpand(result.getId()).toUri();
				LOGGER.debug("Created Location Header {} for {}", location.toString(), result.getName());
				ResponseEntity<Object> responseEntity = ResponseEntity.created(location).build();
				LOGGER.debug("Reponse Status for POST Request is :: " + responseEntity.getStatusCodeValue());
				LOGGER.debug(
						"Reponse Data for POST Request is :: " + responseEntity.getHeaders().getLocation().toString());
				response.setResult(responseEntity);
			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not save customer {} due to error", customer.getName(), ex);
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Could not save customer details due to server error."));

			}

		});

		return response;
	}

	@GetMapping("/customers/{id}")
	public DeferredResult<ResponseEntity<ResourceSupport>> getCustomer(@PathVariable Long id,
			HttpServletRequest request) {

		DeferredResult<ResponseEntity<ResourceSupport>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});
		ListenableFuture<Optional<Customer>> future = AsyncResult.forValue(customerRepository.findById(id));

		future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

			@Override
			public void onSuccess(Optional<Customer> customer) {
				response.setResult(ResponseEntity
						.ok(customer.<Resource<Customer>>map(it -> new Resource<Customer>(it, getEntityLinks(request)))
								.orElse(new Resource<Customer>(new Customer(), getEntityLinks(request)))));

			}

			@Override
			public void onFailure(Throwable ex) {
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Cannot get customer details due to server error."));
			}

		});
		return response;
	}

	@GetMapping("/customers/{id}/invoices")
	public DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> getInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler) {

		DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});
		ListenableFuture<Page<Invoice>> future = invoiceRepository.findAllByCustomer_Id(id, pageable);

		future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

			@Override
			public void onSuccess(Page<Invoice> invoices) {
				if (invoices.getContent().size() > 0) {
					LOGGER.debug("Found {} invoices for the customer {}", invoices.getContent().size(), id);
					response.setResult(ResponseEntity.ok(assembler.toResource(invoices)));
				} else {
					LOGGER.debug("No invoices found for the customer {}.", id);
					response.setErrorResult(
							ResponseEntity.status(HttpStatus.NOT_FOUND).body("No invoices found for this customer."));
				}
			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not retrieve results due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(
						ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
			}

		});
		return response;
	}

	@GetMapping("/customers/{id}/invoices/paid")
	public DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> getPaidInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler) throws InterruptedException, ExecutionException {

		DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});

		ListenableFuture<Page<Invoice>> future = invoiceRepository.findByPaymentsIsNotNullAndCustomer_Id(id, pageable);

		future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

			@Override
			public void onSuccess(Page<Invoice> invoices) {
				if (invoices.getContent().size() > 0)
					response.setResult(ResponseEntity.ok(assembler.toResource(invoices)));
				else {
					LOGGER.debug("No invoices paid by the customer {}.", id);
					response.setErrorResult(ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body("No invoices paid till now by the customer."));
				}

			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not retrieve results due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Cannot retrieve results due to server error."));
			}

		});

		return response;
	}

	@GetMapping("/customers/{id}/invoices/pending")
	public DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> getPendingInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler) {

		DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});
		ListenableFuture<Page<Invoice>> future = invoiceRepository
				.findByPaymentsIsNullAndDueDateAfterAndCustomer_Id(getTodaysDate(), id, pageable);
		future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

			@Override
			public void onSuccess(Page<Invoice> invoices) {
				if (invoices.getContent().size() > 0)
					response.setResult(ResponseEntity.ok(assembler.toResource(invoices)));
				else {
					LOGGER.debug("No invoices pending on the customer {}.", id);
					response.setErrorResult(
							ResponseEntity.status(HttpStatus.NOT_FOUND).body("No pending invoices on the customer."));
				}

			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not retrieve results due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(
						ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
			}

		});

		return response;
	}

	@GetMapping("/customers/{id}/invoices/overdue")
	public DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> getOverdueInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler) {

		DeferredResult<ResponseEntity<PagedResources<Resource<Invoice>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});
		ListenableFuture<Page<Invoice>> future = invoiceRepository
				.findByPaymentsIsNullAndDueDateBeforeAndCustomer_Id(getTodaysDate(), id, pageable);
		future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

			@Override
			public void onSuccess(Page<Invoice> invoices) {
				if (invoices.getContent().size() > 0)
					response.setResult(ResponseEntity.ok(assembler.toResource(invoices)));
				else {
					LOGGER.debug("No invoices overdue by the customer {}.", id);
					response.setErrorResult(
							ResponseEntity.status(HttpStatus.NOT_FOUND).body("No overdue invoices on the customer."));
				}

			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not retrieve results due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(
						ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
			}

		});

		return response;

	}

	@GetMapping("/customers/{id}/branches")
	public DeferredResult<ResponseEntity<Resources<?>>> getAllBranches(@PathVariable("id") Long id,
			@PageableDefault(page = 0, size = 20) Pageable pageable, PagedResourcesAssembler<Branch> assembler,
			HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<?>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});
		ListenableFuture<Customer> future = customerRepository.findEagerFetchBranchesById(id);

		future.addCallback(new ListenableFutureCallback<Customer>() {

			@Override
			public void onSuccess(Customer result) {
				Link rootLink = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
						"self");
				List<Branch> branches = result != null ? result.getBranches() : new ArrayList<>();

				Page<Branch> page = new PageImpl<>(branches, pageable, branches.size());
				response.setResult(ResponseEntity.ok(assembler.toResource(page, branchResourceAssembler, rootLink)));
			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not retrieve branches due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Cannot retrieve brnches for this customer due to server error."));

			}
		});

		return response;
	}

	@PutMapping("/customers/{id}/branches")
	public DeferredResult<ResponseEntity<Object>> addBranch(@PathVariable("id") Long id,
			@RequestBody @Valid Branch branch, HttpServletRequest request) {
		DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Customer> future = customerRepository.findEagerFetchBranchesById(id);
		future.addCallback(new ListenableFutureCallback<Customer>() {

			@Override
			public void onSuccess(Customer result) {
				if (result != null) {
					Branch saved = branchRepository.saveAndFlush(branch);
					List<Branch> branches = new ArrayList<>();
					branches.addAll(result.getBranches());
					branches.add(saved);
					result.setBranches(branches);
					customerRepository.saveAndFlush(result);
					URI location = ServletUriComponentsBuilder.fromRequestUri(request).build().toUri();
					LOGGER.debug("Created Location Header {} for {}", location.toString(), saved.getBranchName());
					ResponseEntity<Object> responseEntity = ResponseEntity.created(location).build();
					LOGGER.debug("Reponse Status for PUT Request is :: " + responseEntity.getStatusCodeValue());
					LOGGER.debug("Reponse Data for PUT Request is :: "
							+ responseEntity.getHeaders().getLocation().toString());
					response.setResult(responseEntity);
				} else {
					response.setResult(ResponseEntity.unprocessableEntity().build());
				}

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

	@PutMapping("/customers/{id}/contact")
	private DeferredResult<ResponseEntity<Object>> addContact(@PathVariable("id") Long id, HttpServletRequest request,
			@RequestBody @Valid Contact contact) {
		DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Customer> future = customerRepository.findOneById(id);

		future.addCallback(new ListenableFutureCallback<Customer>() {
			@Override
			public void onSuccess(Customer result) {

				if (result != null) {
					Contact saved = contactRepository.saveAndFlush(contact);
					result.setContact(saved);
					response.setResult(ResponseEntity.created(ServletUriComponentsBuilder.fromRequestUri(request)
							.path("/{id}").buildAndExpand(result.getId()).toUri()).build());
				} else
					response.setResult(ResponseEntity.unprocessableEntity().build());

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

	@GetMapping("/customers/{id}/contact")
	public DeferredResult<ResponseEntity<Resource<Contact>>> getContact(@PathVariable("id") Long id,
			HttpServletRequest request) {
		DeferredResult<ResponseEntity<Resource<Contact>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Customer> future = customerRepository.findOneById(id);

		future.addCallback(new ListenableFutureCallback<Customer>() {

			@Override
			public void onSuccess(Customer result) {
				URI location = ServletUriComponentsBuilder.fromRequestUri(request)
						.buildAndExpand(result.getContact().getId()).toUri();
				Contact contact = result != null ? result.getContact() : new Contact();
				Resource<Contact> resource = new Resource<Contact>(contact,
						new Link(new StringBuilder(location.toString()).toString(), "contact"));
				response.setResult(ResponseEntity.ok(resource));
			}

			@Override
			public void onFailure(Throwable ex) {
				LOGGER.error("Could not retrieve contact due to error : {}", ex.getMessage(), ex);
				response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
						.body("Cannot retrieve contact for this customer due to server error."));

			}
		});

		return response;
	}

	private Date getTodaysDate() {
		Date today = Date.from(LocalDate.now()
				.atStartOfDay(ZoneId.of(environment.getProperty("spring.jackson.time-zone"))).toInstant());
		LOGGER.debug("Returning Date filter for today, value is {}", new SimpleDateFormat("dd-MM-yyyy").format(today));
		return today;
	}

	private List<Link> getEntityLinks(HttpServletRequest request) {
		String location = ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString();
		List<Link> links = new ArrayList<>();
		links.add(new Link(location, "self"));
		links.add(new Link(location, "customer"));
		links.add(new Link(new StringBuilder(location).append("/").append("contact").toString(), "contact"));
		links.add(new Link(new StringBuilder(location).append("/").append("branches").toString(), "branches"));
		links.add(new Link(new StringBuilder(location).append("/").append("invoices").toString(), "invoices"));
		links.add(
				new Link(new StringBuilder(location).append("/").append("invoices/paid").toString(), "paid-invoices"));
		links.add(new Link(new StringBuilder(location).append("/").append("invoices/pending").toString(),
				"pending-invoices"));
		links.add(new Link(new StringBuilder(location).append("/").append("invoices/overdue").toString(),
				"overdue-invoices"));
		return links;
	}

}
