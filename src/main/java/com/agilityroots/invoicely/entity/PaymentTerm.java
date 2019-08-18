/**
 * 23-Oct-2018 PaymentTerm.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * @author anadi
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class PaymentTerm extends AuditableEntity implements Serializable {

  private static final long serialVersionUID = -9205180346170113156L;

  @Column(unique = true, length = 6)
  private String name;

  @Column(nullable = false)
  private Integer value;

}
