/**
 *  22-Oct-2018 Customer.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.core.Relation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author anadi
 */
@Entity
@Getter
@Setter
@ToString
@DynamicInsert
@NoArgsConstructor
@Relation(collectionRelation = "customers")
public class Customer extends Organisation implements Identifiable<Long>, Serializable {

  private static final long serialVersionUID = 8101819808147191270L;
  
  @Column(nullable = false, updatable = false, length = 3)
  @ColumnDefault("'INR'")
  private String currency;

  @Column(scale = 2)
  @ColumnDefault("0.10")
  private Double tds;

  @Column(nullable = false, updatable = false, length = 3)
  @ColumnDefault("'INV'")
  private String invoicePrefix;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "customer_contact", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"))
  private Contact contact;

  @OneToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "customer_invoices", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"))
  private List<Invoice> invoices;

}