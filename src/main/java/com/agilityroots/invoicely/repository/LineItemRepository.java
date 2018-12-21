/**
 * 
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.repository.CrudRepository;

import com.agilityroots.invoicely.entity.LineItem;

/**
 * @author anadi
 */
public interface LineItemRepository extends CrudRepository<LineItem, Long> {

}
