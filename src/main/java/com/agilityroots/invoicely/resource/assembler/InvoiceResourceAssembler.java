/**
 *  5 Dec 2018 InvoiceResourceAssembler.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.resource.assembler;

import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;

import com.agilityroots.invoicely.controller.InvoiceController;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;

/**
 * @author anadi
 *
 */
@Component
public class InvoiceResourceAssembler extends SimpleIdentifiableResourceAssembler<Invoice> {

  public InvoiceResourceAssembler() {
    super(InvoiceController.class);
  }

  @Override
  protected void addLinks(Resource<Invoice> resource) {
    super.addLinks(resource);
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getItemResourceRelFor(Customer.class)).withRel("customer"));
  }
}
