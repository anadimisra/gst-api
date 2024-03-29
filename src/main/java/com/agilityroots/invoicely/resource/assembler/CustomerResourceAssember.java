/**
 * 30 Nov 2018 CustomerResourceAssember.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.resource.assembler;

import com.agilityroots.invoicely.controller.CustomerController;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;

/**
 * @author anadi
 *
 */
@Component
public class CustomerResourceAssember extends SimpleIdentifiableResourceAssembler<Customer> {

  public CustomerResourceAssember() {
    super(CustomerController.class);
  }

  @Override
  protected void addLinks(Resource<Customer> resource) {
    super.addLinks(resource);
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getItemResourceRelFor(Contact.class)).withRel("contact"));
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getCollectionResourceRelFor(Branch.class)).withRel("branches"));
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getCollectionResourceRelFor(Invoice.class)).withRel("invoices"));
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getCollectionResourceRelFor(Invoice.class)).slash("paid").withRel("paid-invoices"));
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getCollectionResourceRelFor(Invoice.class)).slash("due").withRel("due-invoices"));
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getCollectionResourceRelFor(Invoice.class)).slash("overdue")
        .withRel("overdue-invoices"));
  }
}
