/**
 *  30 Nov 2018 CustomerResourceAssember.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.resource.assembler;

import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import com.agilityroots.invoicely.controller.CustomerController;
import com.agilityroots.invoicely.entity.Customer;

/**
 * @author anadi
 *
 */
@Component
public class CustomerResourceAssember extends SimpleIdentifiableResourceAssembler<Customer>{

	public CustomerResourceAssember() {
		super(CustomerController.class);
	}
}
