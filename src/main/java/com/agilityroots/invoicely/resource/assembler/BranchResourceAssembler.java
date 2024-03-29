/**
 * 3 Dec 2018 BranchResourceAssembler.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.resource.assembler;

import com.agilityroots.invoicely.controller.BranchController;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;

/**
 * @author anadi
 *
 */
@Component
public class BranchResourceAssembler extends SimpleIdentifiableResourceAssembler<Branch> {

  public BranchResourceAssembler() {
    super(BranchController.class);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void addLinks(Resource<Branch> resource) {
    super.addLinks(resource);
    resource.add(getCollectionLinkBuilder().slash(resource.getContent())
        .slash(getRelProvider().getItemResourceRelFor(Contact.class)).withRel("contact"));
  }
}
