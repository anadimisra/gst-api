/**
 * 
 */
package com.agilityroots.invoicely.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.repository.BranchRepository;

/**
 * @author anadi
 *
 */
@Async
@Service
public class BranchService {

  @Autowired
  private BranchRepository branchRepository;

  public ListenableFuture<Optional<Branch>> getBranch(Long id) {

    return AsyncResult.forValue(branchRepository.findById(id));
  }

  public ListenableFuture<Optional<Contact>> getBranchContact(Long id) {

    return AsyncResult.forValue(branchRepository.findById(id).map(Branch::getContact));
  }

  public ListenableFuture<Branch> save(Branch branch) {

    return AsyncResult.forValue(branchRepository.saveAndFlush(branch));
  }

}
