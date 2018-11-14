/**
 *  19-Oct-2018 CompanyRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import com.agilityroots.invoicely.entity.Company;

/**
 * @author anadi
 *
 */
@RepositoryRestResource(path = "companies", collectionResourceRel = "companies")
public interface CompanyRepository extends CrudRepository<Company, Long> {

	@Override
	@RestResource(exported = false)
	void delete(Company entity);

	@Override
	@RestResource(exported = false)
	void deleteById(Long id);

	@Override
	@RestResource(exported = false)
	void deleteAll();

	@Override
	@RestResource(exported = false)
	void deleteAll(Iterable<? extends Company> entities);
}