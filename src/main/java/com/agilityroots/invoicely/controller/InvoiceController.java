/**
 *  15-Nov-2018 InvoiceController.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.web.bind.annotation.RestController;

import com.agilityroots.invoicely.entity.Invoice;

/**
 * @author anadi
 *
 */
@RestController
@ExposesResourceFor(Invoice.class)
public class InvoiceController {

}
