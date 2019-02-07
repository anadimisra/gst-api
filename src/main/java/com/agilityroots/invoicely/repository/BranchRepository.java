/**
 *  24-Oct-2018 BranchRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import java.util.Optional;

import javax.persistence.LockModeType;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.Branch;

/**
 * @author anadi
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

  @Lock(LockModeType.OPTIMISTIC)
  @Cacheable("branches")
  Optional<Branch> findById(Long id);

  @Lock(LockModeType.OPTIMISTIC)
  @CacheEvict("branches")
  <S extends Branch> S saveAndFlush(S entity);

}