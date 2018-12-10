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
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.SortDefault;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CustomerAsyncService;

/**
 * @author anadi
 */
@RestController
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

	@Autowired
	private InvoiceResourceAssembler invoiceReourceAssembler;

	@Autowired
	private CustomerAsyncService customerService;

	@GetMapping("/customers")
	public DeferredResult<ResponseEntity<Resources<Resource<Customer>>>> getAllCustomers(
			@PageableDefault(page = 0, size = 20) @SortDefault.SortDefaults({
					@SortDefault(sort = "name", direction = Direction.ASC) }) Pageable pageable,
			PagedResourcesAssembler<Customer> assembler, HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<Resource<Customer>>>> response = new DeferredResult<>(
				Long.valueOf(1000000));
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Page<Customer>> future = customerService.findAll(pageable);

		future.addCallback(new ListenableFutureCallback<Page<Customer>>() {

			@Override
			public void onSuccess(Page<Customer> result) {
				Link self = new Link(
						ServletUriComponentsBuilder.fromRequestUri(request).buildAndExpand().toUri().toString(),
						"self");
				LOGGER.debug("Generated Self Link {} for Customer Resource Collection", self.getHref());
				if (result.hasContent())
					response.setResult(
							ResponseEntity.ok(assembler.toResource(result, customerResourceAssembler, self)));
				else
					response.setErrorResult(ResponseEntity.notFound().build());
				LOGGER.debug("Returning Response with {} customers", result.getNumber());
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

		ListenableFuture<Customer> future = customerService.saveAndFlsuh(customer);

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
	public DeferredResult<ResponseEntity<Resource<Customer>>> getCustomer(@PathVariable Long id,
			HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resource<Customer>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});
		ListenableFuture<Optional<Customer>> future = customerService.findById(id);

		future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

			@Override
			public void onSuccess(Optional<Customer> customer) {
				response.setResult(customer.map(customerResourceAssembler::toResource).map(ResponseEntity::ok)
						.orElse(ResponseEntity.notFound().build()));

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
	public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});
		ListenableFuture<Page<Invoice>> future = invoiceRepository.findAllByCustomer_Id(id, pageable);

		future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

			@Override
			public void onSuccess(Page<Invoice> invoices) {

				Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
						"self");
				response.setResult(Optional.of(invoices)
						.<Resources<Resource<Invoice>>>map(
								it -> assembler.toResource(it, invoiceReourceAssembler, link))
						.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));

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
	public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPaidInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request)
			throws InterruptedException, ExecutionException {

		DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});
		ListenableFuture<Page<Invoice>> future = invoiceRepository.findByPaymentsIsNotNullAndCustomer_Id(id, pageable);

		future.addCallback(new ListenableFutureCallback<Page<Invoice>>() {

			@Override
			public void onSuccess(Page<Invoice> invoices) {

				Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
						"self");
				response.setResult(Optional.of(invoices)
						.<Resources<Resource<Invoice>>>map(
								it -> assembler.toResource(it, invoiceReourceAssembler, link))
						.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));

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

	@GetMapping("/customers/{id}/invoices/pending")
	public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getPendingInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
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

				Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
						"self");
				response.setResult(Optional.of(invoices)
						.<Resources<Resource<Invoice>>>map(
								it -> assembler.toResource(it, invoiceReourceAssembler, link))
						.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));

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
	public DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> getOverdueInvoicesByCustomer(
			@PathVariable("id") Long id, @PageableDefault(page = 0, size = 10) Pageable pageable,
			PagedResourcesAssembler<Invoice> assembler, HttpServletRequest request) {

		DeferredResult<ResponseEntity<Resources<Resource<Invoice>>>> response = new DeferredResult<>();
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

				Link link = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
						"self");
				response.setResult(Optional.of(invoices)
						.<Resources<Resource<Invoice>>>map(
								it -> assembler.toResource(it, invoiceReourceAssembler, link))
						.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));

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
	public DeferredResult<ResponseEntity<PagedResources<Resource<Branch>>>> getAllBranches(@PathVariable("id") Long id,
			@PageableDefault(page = 0, size = 20) Pageable pageable, PagedResourcesAssembler<Branch> assembler,
			HttpServletRequest request) {

		DeferredResult<ResponseEntity<PagedResources<Resource<Branch>>>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred."));
		});
		ListenableFuture<Optional<Customer>> future = customerService.findWithAllBranches(id);

		future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

			@Override
			public void onSuccess(Optional<Customer> result) {
				Link rootLink = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
						"self");

				response.setResult(result
						.map(it -> assembler.toResource(
								new PageImpl<>(it.getBranches(), pageable, it.getBranches().size()),
								branchResourceAssembler, rootLink))
						.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
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

		ListenableFuture<Optional<Customer>> future = customerService.findWithAllBranches(id);
		future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {

			@Override
			public void onSuccess(Optional<Customer> result) {
				List<Branch> allBranches = new ArrayList<>();

				result.map(Customer::getBranches).map(allBranches::addAll)
						.orElse(allBranches.addAll(Collections.emptyList()));

				if (result.isPresent()) {
					Branch saved = branchRepository.saveAndFlush(branch);
					allBranches.add(saved);
					Customer customer = result.get();
					customer.setBranches(allBranches);
					customerRepository.saveAndFlush(customer);
					response.setResult(ResponseEntity.created(ServletUriComponentsBuilder.fromRequestUri(request)
							.path("{id}").buildAndExpand(saved.getId()).toUri()).build());
				} else
					response.setErrorResult(ResponseEntity.unprocessableEntity().build());

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
	public DeferredResult<ResponseEntity<?>> getContact(@PathVariable("id") Long id, HttpServletRequest request) {
		DeferredResult<ResponseEntity<?>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Optional<Contact>> future = customerService.getContact(id);

		future.addCallback(new ListenableFutureCallback<Optional<Contact>>() {

			@Override
			public void onSuccess(Optional<Contact> contact) {
				LOGGER.debug("Rendering customer contact details {}", contact.map(Contact::getName).orElse("None"));
				response.setResult(
						contact.map(
								it -> new Resource<>(it,
										new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri()
												.toString(), "contact")))
								.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()));
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

	@PutMapping("/customers/{id}/contact")
	private DeferredResult<ResponseEntity<Object>> addContact(@PathVariable("id") Long id, HttpServletRequest request,
			@RequestBody @Valid Contact contact) {
		DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
		response.onTimeout(() -> response
				.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
		response.onError((Throwable t) -> {
			response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
		});

		ListenableFuture<Optional<Customer>> future = customerService.findById(id);

		future.addCallback(new ListenableFutureCallback<Optional<Customer>>() {
			@Override
			public void onSuccess(Optional<Customer> customer) {

				if (customer.isPresent()) {
					LOGGER.debug("Adding contact details {} to customer {}", contact.toString(),
							customer.map(Customer::getName).orElse("None"));
					Contact saved = contactRepository.saveAndFlush(contact);
					Customer unsaved = customer.get();
					unsaved.setContact(saved);
					customerRepository.saveAndFlush(unsaved);
					URI location = ServletUriComponentsBuilder.fromRequestUri(request).path("/{id}")
							.buildAndExpand(saved.getId()).toUri();
					response.setResult(ResponseEntity.created(location).build());
					LOGGER.debug("Rendered Location header {}", location.toString());
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

	private Date getTodaysDate() {
		Date today = Date.from(LocalDate.now()
				.atStartOfDay(ZoneId.of(environment.getProperty("spring.jackson.time-zone"))).toInstant());
		LOGGER.debug("Returning Date filter for today, value is {}", new SimpleDateFormat("dd-MM-yyyy").format(today));
		return today;
	}

}
