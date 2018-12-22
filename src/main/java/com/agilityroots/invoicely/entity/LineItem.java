/**
 *  23-Oct-2018 LineItem.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

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
public class LineItem implements Serializable {

  private static final long serialVersionUID = -977449988990379565L;

  @NotNull
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private Integer serialNumber;

  @NotEmpty
  private String item;

  @NotEmpty
  private String description;

  private String hsn;

  private String sac;

  @NotNull
  private Double price;

  private String unit;

  private Integer quantity;

  @NotNull
  private Double discount = Double.valueOf(0.0);

  @NotNull
  private Double amount;

  @NotNull
  private Double tax;

  @Override
  public int hashCode() {
    return Objects.hash(amount, description, discount, hsn, item, price, quantity, sac, serialNumber, tax, unit);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    LineItem other = (LineItem) obj;
    return Objects.equals(amount, other.getAmount()) && Objects.equals(description, other.getDescription())
        && Objects.equals(discount, other.getDiscount()) && Objects.equals(hsn, other.getHsn())
        && Objects.equals(item, other.getItem()) && Objects.equals(price, other.getPrice())
        && Objects.equals(quantity, other.getQuantity()) && Objects.equals(sac, other.getSac())
        && Objects.equals(serialNumber, other.getSerialNumber()) && Objects.equals(tax, other.getTax())
        && Objects.equals(unit, other.getUnit());
  }

}
