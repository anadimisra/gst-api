/**
 *
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author anadi
 *
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {

}
