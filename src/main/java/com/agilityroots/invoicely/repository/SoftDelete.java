/**
 *  13-Nov-2018 SoftDelete.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

/**
 * @author anadi
 *
 */
public interface SoftDelete<T> {

	void delete(T entity);

}
