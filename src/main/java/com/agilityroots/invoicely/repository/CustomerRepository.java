/**
 *  22-Oct-2018 CustomerRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.EntityGraph.EntityGraphType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.Customer;

/**
 * @author anadi
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Optional<Customer> findById(Long id);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Page<Customer> findAll(Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  @EntityGraph(value = "graph.Customer.branches", type = EntityGraphType.LOAD)
  Customer findEagerFetchBranchesById(@Param("id") Long id);

  @Lock(LockModeType.OPTIMISTIC)
  @CachePut("customers")
  <S extends Customer> S saveAndFlush(S entity);
}