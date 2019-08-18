/*
  23-Oct-2018 Payment.java
  data-api
  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonFormat;

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

  @NotEmpty(message = "Cannot update Payment Details without payment date")
  @Temporal(TemporalType.DATE)
  @Column(nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  private Date paymentDate;

  @NotEmpty(message = "Cannot update Payment Details without payment amount")
  @Column(nullable = false)
  private Double amount;

  @NotEmpty(message = "Cannot update Payment Details without adjustment name")
  @Column(nullable = false, length = 10)
  private String adjustmentName;

  @NotEmpty(message = "Cannot update Payment Details without adjustment value")
  @Column(nullable = false)
  private Double adjustmentValue;

}