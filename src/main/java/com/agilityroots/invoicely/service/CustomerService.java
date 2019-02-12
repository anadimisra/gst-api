/**
 * 
 */
package com.agilityroots.invoicely.service;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.event.service.ContactAddedEvent;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Async
@Slf4j
@Service
public class CustomerService {

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private InvoiceRepository invoiceRepository;

  @Autowired
  private BranchRepository branchRepository;

  @Autowired
  private ContactRepository contactRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public CustomerService(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Customer>> getCustomers(Pageable pageable) {
    log.debug("Returning page {} of size {}", pageable.getPageNumber(), pageable.getPageSize());
    return AsyncResult.forValue(customerRepository.findAll(pageable));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Customer> save(Customer customer) {
    log.debug("Saving customer {}", customer.toString());
    return AsyncResult.forValue(customerRepository.saveAndFlush(customer));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Customer>> getCustomer(Long id) {
    log.debug("Finding customer with {}", id);
    return AsyncResult.forValue(customerRepository.findById(id));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Branch>> getAllBranches(Long id, Pageable pageable) {
    log.debug("Loading branches for customer with id {}", id);
    return AsyncResult.forValue(branchRepository.findAllByOwner_Id(id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getAllInvoices(Long id, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findAllByCustomer_Id(id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getPaidInvoices(Long id, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNotNullAndCustomer_Id(id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getDueInvoices(Date today, Long id, Pageable pageable) {
    return AsyncResult
        .forValue(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_Id(today, id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getOverdueInvoices(Date today, Long id, Pageable pageable) {
    return AsyncResult.forValue(
        invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_Id(today, id, pageable));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<URI>> addBranch(Long id, Branch branch, StringBuffer uriBuilder) {
    URI location = null;
    Optional<Customer> result = customerRepository.findById(id);
    if (result.isPresent()) {
      Customer customer = result.get();
      branch.setOwner(customer);
      log.debug("Saving branch: {}", branch);
      branchRepository.saveAndFlush(branch);
      location = URI.create(uriBuilder.append(String.valueOf(branch.getId())).toString());
    }
    return AsyncResult.forValue(Optional.ofNullable(location));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Contact>> getContact(Long id) {
    log.debug("Loading contact details for customer with id {}", id);
    return AsyncResult
        .forValue(Optional.ofNullable(customerRepository.findById(id).map(Customer::getContact).orElse(null)));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<URI>> addContact(Long id, Contact contact, StringBuffer uriBuilder) {
    URI location = null;
    Optional<Customer> result = customerRepository.findById(id);
    if (result.isPresent()) {
      contactRepository.save(contact);
      Customer customer = result.get();
      customer.setContact(contact);
      customerRepository.saveAndFlush(customer);
      location = URI.create(uriBuilder.toString());
      eventPublisher.publishEvent(new ContactAddedEvent(contact));
    }
    return AsyncResult.forValue(Optional.ofNullable(location));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<URI>> addInvoice(Long customerId, Long billedFrom, Long billedTo, Long shippedTo,
      StringBuilder locationBuilder, Invoice invoice) {

    URI location = null;
    Optional<Customer> result = customerRepository.findById(customerId);
    if (result.isPresent()) {
      List<Branch> branches = branchRepository.findAllByOwner_Id(customerId);
      Customer customer = result.get();
      log.debug("Adding invoice to customer: {}", customer);
      invoice.setCustomer(customer);
      branches.stream().filter(b -> b.getId().equals(billedTo)).findFirst().ifPresent(it -> invoice.setBilledTo(it));
      branches.stream().filter(b -> b.getId().equals(shippedTo)).findFirst().ifPresent(it -> invoice.setShippedTo(it));
      branchRepository.findById(billedFrom).ifPresent(it -> invoice.setBilledFrom(it));
      log.debug("Added branches: billed to: {} | shipped to: {} | billed from: {}", invoice.getBilledTo(),
          invoice.getShippedTo(), invoice.getBilledFrom());
      invoiceRepository.saveAndFlush(invoice);
      location = URI.create(locationBuilder.append(invoice.getId()).toString());
    }
    return AsyncResult.forValue(Optional.ofNullable(location));

  }
}
