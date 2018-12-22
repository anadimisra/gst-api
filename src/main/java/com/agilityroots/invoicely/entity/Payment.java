/**
 *  23-Oct-2018 Payment.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author anadi
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class Payment implements Serializable {

  private static final long serialVersionUID = 3584499907096911054L;

  @Temporal(TemporalType.DATE)
  @Column(nullable = false)
  private Date paymentDate;

  @Column(nullable = false)
  private Double amount;

  @Column(nullable = false, length = 10)
  private String adjustmentName;

  @Column(nullable = false)
  private Double adjustmentValue;
}
