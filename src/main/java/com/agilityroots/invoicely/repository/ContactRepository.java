/**
 *  13-Nov-2018 ContactRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.agilityroots.invoicely.entity.Contact;

/**
 * @author anadi
 *
 */
@RepositoryRestResource(exported = false)
public interface ContactRepository extends DataApiRepository<Contact, Long> {

}
