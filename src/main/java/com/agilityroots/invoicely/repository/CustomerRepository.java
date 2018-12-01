/**
 *  22-Oct-2018 CustomerRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import javax.persistence.LockModeType;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Customer;

/**
 * @author anadi
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@Cacheable("customers")
	@RestResource(exported = false)
	ListenableFuture<Customer> findOneById(Long id);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
	@Cacheable("customers")
	@EntityGraph(value = "graph.Customer.branches", type = EntityGraphType.LOAD)
	ListenableFuture<Customer> findEagerFetchBranchesById(@Param("id") Long id);

}