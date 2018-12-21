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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.repository.CustomerRepository;

/**
 * @author anadi
 *
 */
@Async
@Service
public class CustomerAsyncService {

  private CustomerRepository customerRepository;

  @Autowired
  public CustomerAsyncService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Customer>> getCustomers(Pageable pageable) {
    return AsyncResult.forValue(customerRepository.findAll(pageable));
  }

  @Transactional(isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Customer> save(Customer customer) {
    return AsyncResult.forValue(customerRepository.saveAndFlush(customer));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Customer>> getCustomer(Long id) {
    return AsyncResult.forValue(customerRepository.findById(id));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Customer>> getCustomerWithAllBranches(Long id) {
    return AsyncResult.forValue(Optional.ofNullable(customerRepository.findEagerFetchBranchesById(id)));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Contact>> getContact(Long id) {
    return AsyncResult
        .forValue(Optional.ofNullable(customerRepository.findById(id).map(Customer::getContact).orElse(null)));
  }

}
