/**
 *  13-Nov-2018 PaymentRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.Payment;

/**
 * @author anadi
 *
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
