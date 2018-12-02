/**
 *  24-Oct-2018 BranchRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
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

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
//	@RestResource(path = "name", rel = "findbyname")
	ListenableFuture<Page<Branch>> findByBranchNameLike(@Param("branchName") String branchName, Pageable pageable);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
//	@RestResource(path = "city", rel = "findbycity")
	ListenableFuture<List<Branch>> findByAddress_City(@Param("city") String city);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
//	@RestResource(path = "state", rel = "findbystate")
	ListenableFuture<List<Branch>> findByAddress_State(@Param("state") String state);

	@Async
	@Lock(LockModeType.OPTIMISTIC)
	@Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
//	@RestResource(path = "gstin", rel = "findbygstin")
	ListenableFuture<Page<Branch>> findByGstinLike(@Param("gstin") String gstin, Pageable pageable);

}