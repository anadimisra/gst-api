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
import javax.persistence.Transient;
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

@ToString
@NoArgsConstructor
@Embeddable
public class LineItem implements Serializable {

  private static final long serialVersionUID = -977449988990379565L;

  @Getter
  @Setter
  @NotNull(message = "Cannot add invoice line item without serinal number")
  @Column(nullable = false)
  private Integer serialNumber;

  @Getter
  @Setter
  @NotEmpty(message = "Cannot add invoice line item without item")
  @Column(nullable = false, length = 100)
  private String item;

  @Getter
  @Setter
  @NotEmpty(message = "Cannot add invoice line item without description")
  @Column(nullable = false, length = 500)
  private String description;

  @Getter
  @Setter
  @Column(length = 6)
  private String hsn;

  @Getter
  @Setter
  @Column(length = 6)
  private String sac;

  @Getter
  @Setter
  @NotNull(message = "Cannot add invoice line item without price")
  @Column(nullable = false, scale = 2)
  private Double price;

  @Getter
  @Setter
  @Column(length = 3)
  private String unit;

  @Getter
  @Setter
  @Column
  private Integer quantity;

  @Getter
  @Setter
  @NotNull
  @Column(nullable = false, scale = 2)
  @ColumnDefault("0.00")
  private Double discount;

  @Transient
  private Double amount;

  @Getter
  @Setter
  @NotNull(message = "Cannot add invoice line item without tax rate")
  @Column(nullable = false, scale = 2)
  @ColumnDefault("0.18")
  private Double tax;

  public Double getAmount() {
    if (this.unit != null)
      return ((this.price - this.price * this.discount) * this.quantity + this.price * this.tax);
    else
      return (this.price - this.price * this.discount + this.price * this.tax);
  }

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
