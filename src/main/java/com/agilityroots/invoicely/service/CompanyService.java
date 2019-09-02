/**
 *
 */
package com.agilityroots.invoicely.service;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.CompanyRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/*
 * @author anadi
 */
@Async
@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class CompanyService {

  private final CompanyRepository companyRepository;

  private final BranchRepository branchRepository;

  private final InvoiceRepository invoiceRepository;

  @Transactional(isolation = Isolation.SERIALIZABLE)
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

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getAllInvoices(Long id, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findAllByCompany_IdOrderByInvoiceDateDesc(id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getPaidInvoices(Long id, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNotNullAndCompany_IdOrderByPayments_PaymentDateDesc(id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getDueInvoices(Long id, Date today, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfterAndCompany_IdOrderByDueDateAsc(today, id, pageable));
  }

  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  public ListenableFuture<Page<Invoice>> getOverDueInvoices(Long id, Date today, Pageable pageable) {
    return AsyncResult.forValue(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBeforeAndCompany_IdOrderByDueDateAsc(today, id, pageable));
  }
}
