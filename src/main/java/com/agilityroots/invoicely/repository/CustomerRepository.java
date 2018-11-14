/**
 *  22-Oct-2018 CustomerRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Customer;

/**
 * @author anadi
 */
@RepositoryRestResource
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@RestResource(exported = false)
	ListenableFuture<Customer> findOneById(Long id);

	@Override
	@RestResource(exported = false)
	void delete(Customer entity);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	@Override
	@RestResource(exported = false)
	void deleteAll();

	@Override
	@RestResource(exported = false)
	void deleteAll(Iterable<? extends Customer> entities);
}