/**
 *  19-Oct-2018 CompanyRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.Company;

/**
 * @author anadi
 *
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

}