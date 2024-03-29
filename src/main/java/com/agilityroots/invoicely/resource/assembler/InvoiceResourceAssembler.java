/*
  5 Dec 2018 InvoiceResourceAssembler.java
  data-api
  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.resource.assembler;

import com.agilityroots.invoicely.controller.InvoiceController;
import com.agilityroots.invoicely.entity.Invoice;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;

/**
 * @author anadi
 */
@Component
public class InvoiceResourceAssembler extends SimpleIdentifiableResourceAssembler<Invoice> {

  public InvoiceResourceAssembler() {
    super(InvoiceController.class);
  }

  /**
   * @param resource We do not need collection resource here hence not calling super method
   */
  @Override
  protected void addLinks(Resource<Invoice> resource) {
    resource.add(getCollectionLinkBuilder().slash(resource.getContent()).withSelfRel());
  }
}
