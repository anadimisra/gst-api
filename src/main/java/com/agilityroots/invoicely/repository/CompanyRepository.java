/**
 *  19-Oct-2018 CompanyRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.Company;

/**
 * @author anadi
 *
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

  @Cacheable("company")
  Page<Company> findAll(Pageable pageable);

  @Cacheable("company")
  Optional<Company> findById(Long id);
}