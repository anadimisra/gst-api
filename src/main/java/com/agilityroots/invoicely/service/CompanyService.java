/**
 * 
 */
package com.agilityroots.invoicely.service;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.CompanyRepository;

/**
 * @author anadi
 *
 */
@Async
@Service
public class CompanyService {

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private BranchRepository branchRepository;

  public ListenableFuture<Optional<URI>> addBranch(Long companyId, Branch branch, URI baseURI) {

    URI location = null;
    Optional<Company> result = companyRepository.findById(companyId);
    if (result.isPresent()) {
      branch.setOwner(result.get());
      branchRepository.saveAndFlush(branch);
      StringBuffer buffer = new StringBuffer(baseURI.toString());
      buffer.append(branch.getId());
      location = URI.create(buffer.toString());
    }
    return AsyncResult.forValue(Optional.ofNullable(location));
  }
}
