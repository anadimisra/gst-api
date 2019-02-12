/**
 * 
 */
package com.agilityroots.invoicely.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.event.service.ContactAddedEvent;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;

/**
 * @author anadi
 *
 */
@Async
@Service
public class BranchService {

  @Autowired
  private BranchRepository branchRepository;

  @Autowired
  private ContactRepository contactRepository;

  private final ApplicationEventPublisher eventPublisher;

  @Autowired
  public BranchService(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Branch>> getBranch(Long id) {
    return AsyncResult.forValue(branchRepository.findById(id));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<Contact>> getBranchContact(Long id) {
    return AsyncResult.forValue(branchRepository.findById(id).map(Branch::getContact));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<URI>> addContact(Long id, Contact contact, URI currentURI) {

    URI location = null;
    Optional<Branch> result = branchRepository.findById(id);
    if (result.isPresent()) {
      contactRepository.save(contact);
      Branch branch = result.get();
      branch.setContact(contact);
      branchRepository.saveAndFlush(branch);
      location = URI.create(currentURI.toString());
      eventPublisher.publishEvent(new ContactAddedEvent(contact));
    }
    return AsyncResult.forValue(Optional.ofNullable(location));
  }

  @Transactional(readOnly = false, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Branch> save(Branch branch) {
    return AsyncResult.forValue(branchRepository.saveAndFlush(branch));
  }

}
