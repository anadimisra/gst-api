package com.agilityroots.invoicely.service;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.event.service.ContactAddedEvent;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.net.URI;
import java.util.*;

/**
 * @author anadi
 *
 */
@Async
@Slf4j
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CustomerService {

  private final ApplicationEventPublisher eventPublisher;

  private final CustomerRepository customerRepository;

  private final InvoiceRepository invoiceRepository;

  private final BranchRepository branchRepository;

  private final ContactRepository contactRepository;

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
    return AsyncResult.forValue(invoiceRepository.findByCustomer_IdOrderByInvoiceDateDesc(id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getPaidInvoices(Long id, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNotNullAndCustomer_IdOrderByPayments_PaymentDateDesc(id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getDueInvoices(Date today, Long id, Pageable pageable) {
    return AsyncResult
        .forValue(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_IdOrderByDueDateAsc(today, id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getOverdueInvoices(Date today, Long id, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_IdOrderByDueDateAsc(today, id, pageable));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<URI>> addBranch(Long id, Branch branch, StringBuffer uriBuilder) {
    URI location = null;
    Optional<Customer> result = customerRepository.findById(id);
    if (result.isPresent()) {
      Customer customer = result.get();
      branch.setOwner(customer);
      branchRepository.saveAndFlush(branch);
      location = URI.create(uriBuilder.append(branch.getId()).toString());
    }
    return AsyncResult.forValue(Optional.ofNullable(location));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Contact>> getContact(Long id) {
    Optional<Contact> contact;
    Optional<Customer> result = customerRepository.findById(id);
    try {
      log.debug("Loading contact details: {} for customer {}", result.get().getContact(), id);
      contact = result.map(Customer::getContact);
    } catch (NoSuchElementException e) {
      contact = Optional.empty();
      log.warn("No contact found for customer with id {}", id);
    }
    return AsyncResult.forValue(contact);
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
                                                    StringBuffer locationBuilder, Invoice invoice) {

    URI location = null;
    Optional<Customer> result = customerRepository.findById(customerId);
    if (result.isPresent()) {
      List<Branch> branches = new ArrayList<>(branchRepository.findAllByOwner_Id(customerId));
      Customer customer = result.get();
      invoice.setCustomer(customer);
      branches.stream().filter(b -> b.getId().equals(billedTo)).findFirst().ifPresent(it -> invoice.setBilledTo(it));
      branches.stream().filter(b -> b.getId().equals(shippedTo)).findFirst().ifPresent(it -> invoice.setShippedTo(it));
      branchRepository.findById(billedFrom).ifPresent(it -> invoice.setBilledFrom(it));
      log.debug("Added branches: billed to: {} | shipped to: {} | billed from: {}", invoice.getBilledTo(),
          invoice.getShippedTo(), invoice.getBilledFrom());
      invoiceRepository.saveAndFlush(invoice);
      location = URI.create(locationBuilder.append(invoice.getInvoiceNumber()).toString());
    }
    return AsyncResult.forValue(Optional.ofNullable(location));
  }
}
