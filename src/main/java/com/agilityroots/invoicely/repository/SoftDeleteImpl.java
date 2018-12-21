/**
 *  13-Nov-2018 SoftDeleteImpl.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.repository;

import com.agilityroots.invoicely.entity.Invoice;

/**
 * @author anadi
 *
 */
public class SoftDeleteImpl implements SoftDelete<Invoice> {

  @Override
  public void delete(Invoice entity) {
    entity.setDeleted(1);

  }

}
