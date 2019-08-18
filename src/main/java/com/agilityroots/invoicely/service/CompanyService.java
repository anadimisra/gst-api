/**
 *
 */
package com.agilityroots.invoicely.service;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.net.URI;
import java.util.Optional;

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

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Optional<URI>> addBranch(Long companyId, Branch branch, StringBuffer urlBuilder) {

    URI location = null;
    Optional<Company> result = companyRepository.findById(companyId);
    if (result.isPresent()) {
      branch.setOwner(result.get());
      branch = branchRepository.saveAndFlush(branch);
      urlBuilder.append(branch.getId());
      location = URI.create(urlBuilder.toString());
    }
    return AsyncResult.forValue(Optional.ofNullable(location));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Branch>> getBranches(Long id, Pageable pageable) {
    return AsyncResult.forValue(branchRepository.findAllByOwner_Id(id, pageable));
  }
}
