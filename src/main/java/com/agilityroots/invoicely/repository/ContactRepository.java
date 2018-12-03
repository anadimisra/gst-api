/**
 *  13-Nov-2018 ContactRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.Contact;

/**
 * @author anadi
 *
 */
@Repository
public interface ContactRepository extends DataApiRepository<Contact, Long> {

}
