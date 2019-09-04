/**
 * 22-Oct-2018 Customer.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Where;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.core.Relation;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author anadi
 */
@Entity
@Getter
@Setter
@ToString
@DynamicInsert
@NoArgsConstructor
@Where(clause = "DELETED = 0")
@Relation(collectionRelation = "customers")
public class Customer extends Organisation implements Identifiable<Long>, Serializable {

  private static final long serialVersionUID = 8101819808147191270L;

  @Column(nullable = false, updatable = false, length = 3)
  @ColumnDefault("'INR'")
  private String currency;

  @Column(nullable = false, scale = 2)
  @ColumnDefault("0.10")
  private Double tds;

  @Column(nullable = false, updatable = false, length = 3)
  @ColumnDefault("'INV'")
  private String invoicePrefix;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "customer_contact", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"))
  private Contact contact;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinTable(name = "customer_of_company", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "company_id", referencedColumnName = "id"))
  private Company company;

  @JsonIgnore
  @Column(nullable = false)
  @ColumnDefault("0")
  private Integer deleted;

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Customer other = (Customer) obj;
    return Objects.equals(this.getOrganisationId(), other.getOrganisationId());
  }
}