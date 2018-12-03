/**
 *  13-Nov-2018 PaymentRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.agilityroots.invoicely.entity.Payment;

/**
 * @author anadi
 *
 */
@RepositoryRestResource(exported = false)
public interface PaymentRepository extends CrudRepository<Payment, Long> {

}
