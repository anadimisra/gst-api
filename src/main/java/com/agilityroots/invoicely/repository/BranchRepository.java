/*
  24-Oct-2018 BranchRepository.java
  data-api
  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.Branch;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.Optional;

/**
 * @author anadi
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long>, SoftDelete<Branch> {

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("branches")
  Optional<Branch> findById(Long id);

  @Lock(LockModeType.OPTIMISTIC)
  @CachePut("branches")
  <S extends Branch> S saveAndFlush(S entity);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("branches")
  Page<Branch> findAllByOwner_Id(Long id, Pageable pageable);

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("branches")
  Collection<Branch> findAllByOwner_Id(Long id);

}