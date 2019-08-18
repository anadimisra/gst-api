/**
 * 19-Oct-2018 Company.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author anadi
 *
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Company extends Organisation implements Serializable {

  private static final long serialVersionUID = 4627788171283297107L;

  @Column(unique = true, length = 21, updatable = false, nullable = false)
  private String cin;

  @Column(unique = true, length = 10, updatable = false)
  private String tan;

  @NotEmpty(message = "PAN is mandatory while adding your Company")
  @Column(unique = true, length = 10, updatable = false, nullable = false)
  private String pan;

  @Override
  public int hashCode() {
    return (Objects.hash(cin) * 97);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    Company other = (Company) obj;
    return Objects.equals(cin, other.getCin());
  }

}