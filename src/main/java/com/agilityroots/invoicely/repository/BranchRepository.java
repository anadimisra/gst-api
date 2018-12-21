/**
 *  24-Oct-2018 BranchRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.concurrent.ListenableFuture;

import com.agilityroots.invoicely.entity.Branch;

/**
 * @author anadi
 */
@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

  @Async
  @Lock(LockModeType.OPTIMISTIC)
  @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
  ListenableFuture<Branch> findOneById(Long id);

}