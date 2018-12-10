/**
 * 
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.agilityroots.invoicely.entity.Contact;

/**
 * @author anadi
 *
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {

}
