/**
 *  23-Oct-2018 LineItem.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author anadi
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Embeddable
public class LineItem implements Serializable {

  private static final long serialVersionUID = -977449988990379565L;

  @NotNull
  @Column(nullable = false)
  private Integer serialNumber;

  @NotEmpty
  @Column(length = 100, nullable = false)
  private String item;

  @NotEmpty
  @Column(nullable = false)
  private String description;

  @Column(length = 5)
  private String hsn;

  @Column(length = 5)
  private String sac;

  @NotNull
  @Column(scale = 2)
  private Double price;

  @Column(length = 3)
  private String unit;

  @Column
  @ColumnDefault("1")
  private Integer quantity;

  @NotNull
  @Column(scale = 2)
  @ColumnDefault("0.00")
  private Double discount;

  @NotNull
  @Column(scale = 2)
  private Double amount;

  @NotNull
  @Column(scale = 2)
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
