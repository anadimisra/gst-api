/**
 *
 */
package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.service.CompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

/**
 * @author anadi
 *
 */
@Slf4j
@RestController
@ExposesResourceFor(Company.class)
public class CompanyController {

  @Autowired
  private CompanyService companyService;

  @Autowired
  private BranchResourceAssembler branchResourceAssembler;

  @PutMapping("/companies/{id}/branches")
  public DeferredResult<ResponseEntity<Object>> addBranch(@PathVariable("id") Long id,
                                                          @RequestBody(required = true) @Valid Branch branch, HttpServletRequest request) {
    DeferredResult<ResponseEntity<Object>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });

    StringBuffer urlBuilder = new StringBuffer();
    urlBuilder.append(request.getScheme()).append("://").append(request.getHeader("Host"))
        .append(request.getContextPath()).append("/branches/");
    ListenableFuture<Optional<URI>> result = companyService.addBranch(id, branch, urlBuilder);

    result.addCallback(new ListenableFutureCallback<Optional<URI>>() {

      @Override
      public void onSuccess(Optional<URI> result) {
        response.setResult(result.map(location -> ResponseEntity.created(location).build())
            .orElse(ResponseEntity.badRequest().build()));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot save branch {} due to error: {}", branch.toString(), ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot save branch details due to server error."));
      }
    });

    return response;
  }

  public DeferredResult<ResponseEntity<Resources<Resource<Branch>>>> getBranches(@PathVariable("id") Long id,
                                                                                 @PageableDefault(page = 0, size = 20, sort = "name", direction = Direction.ASC) Pageable pageable,
                                                                                 PagedResourcesAssembler<Branch> assembler, HttpServletRequest request) {

    DeferredResult<ResponseEntity<Resources<Resource<Branch>>>> response = new DeferredResult<>();
    response.onTimeout(
        () -> response.setErrorResult(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body("Request timed out.")));
    response.onError((Throwable t) -> {
      response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occured."));
    });
    log.debug("Getting all branches for company");
    ListenableFuture<Page<Branch>> result = companyService.getBranches(id, pageable);

    result.addCallback(new ListenableFutureCallback<Page<Branch>>() {

      @Override
      public void onSuccess(Page<Branch> result) {
        Link rootLink = new Link(ServletUriComponentsBuilder.fromRequestUri(request).build().toUri().toString(),
            "self");
        if (!result.hasContent())
          response.setResult(ResponseEntity.notFound().build());
        else
          response.setResult(ResponseEntity.ok(assembler.toResource(result, branchResourceAssembler, rootLink)));
      }

      @Override
      public void onFailure(Throwable ex) {
        log.error("Cannot retrieve branches for company id {} due to error: {}", id, ex.getMessage(), ex);
        response.setErrorResult(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Cannot retrieve branches for this customer due to server error."));
      }
    });
    return response;
  }

  private URI getCurrentLocation(HttpServletRequest request) {
    return ServletUriComponentsBuilder.fromRequestUri(request).build().toUri();
  }

}
