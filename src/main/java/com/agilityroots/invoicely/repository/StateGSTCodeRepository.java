/**
 *  10-Nov-2018 StateGSTCodeRepository.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.agilityroots.invoicely.entity.StateGSTCode;

/**
 * @author anadi
 *
 */
@Repository
public interface StateGSTCodeRepository extends PagingAndSortingRepository<StateGSTCode, Long> {

}
