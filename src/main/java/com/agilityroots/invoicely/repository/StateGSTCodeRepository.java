/**
 * 10-Nov-2018 StateGSTCodeRepository.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.StateGSTCode;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @author anadi
 *
 */
@Repository
public interface StateGSTCodeRepository extends CrudRepository<StateGSTCode, Long> {

}
