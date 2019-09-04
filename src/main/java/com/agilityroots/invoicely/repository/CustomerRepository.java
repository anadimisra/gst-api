/**
 * 22-Oct-2018 CustomerRepository.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.Customer;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

/**
 * @author anadi
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, SoftDelete<Customer> {

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Optional<Customer> findById(Long id);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Page<Customer> findAll(Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC)
  @CachePut("customers")
  <S extends Customer> S saveAndFlush(S entity);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Optional<Customer> findOneByOrganisationId(String customerId);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("customers")
  Page<Customer> findByCompany_Id(Long id, Pageable pageable);
}