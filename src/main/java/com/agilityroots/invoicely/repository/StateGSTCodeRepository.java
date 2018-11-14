/**
 *  10-Nov-2018 StateGSTCodeRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import com.agilityroots.invoicely.entity.StateGSTCode;

/**
 * @author anadi
 *
 */
@RepositoryRestResource(collectionResourceRel = "gstcodes", path = "gstcodes")
public interface StateGSTCodeRepository extends PagingAndSortingRepository<StateGSTCode, Long> {

	@Override
	@RestResource(exported = false)
	void delete(StateGSTCode entity);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	@Override
	@RestResource(exported = false)
	void deleteAll();

	@Override
	@RestResource(exported = false)
	void deleteAll(Iterable<? extends StateGSTCode> entities);
}
