/*
  23-Oct-2018 Invoice.java
  data-api
  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Where;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.core.Relation;

import javax.naming.directory.InvalidAttributesException;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @author anadi
 */
@Slf4j
@Entity
@Setter
@Getter
@ToString
@DynamicInsert
@NoArgsConstructor
@Where(clause = "DELETED = 0")
@Relation(collectionRelation = "invoices")
@Table(indexes = {@Index(name = "place_of_supply_index", columnList = "placeOfSupply"),
    @Index(name = "invoice_date_index", columnList = "invoiceDate"),
    @Index(name = "due_date_index", columnList = "dueDate"),
    @Index(name = "payment_terms_index", columnList = "paymentTerms")})
@NamedEntityGraphs({
    @NamedEntityGraph(name = "invoice_details", attributeNodes = {@NamedAttributeNode("lineItems"),
        @NamedAttributeNode("payments")}),
    @NamedEntityGraph(name = "invoice_billing_details", attributeNodes = {@NamedAttributeNode("billedFrom"),
        @NamedAttributeNode("billedTo"), @NamedAttributeNode("shippedTo")}),
    @NamedEntityGraph(name = "invoice_all_details", attributeNodes = {@NamedAttributeNode("billedFrom"),
        @NamedAttributeNode("billedTo"), @NamedAttributeNode("shippedTo"), @NamedAttributeNode("lineItems"),
        @NamedAttributeNode("payments"), @NamedAttributeNode("company"), @NamedAttributeNode("customer")})})
public class Invoice extends AuditableEntity implements Identifiable<Long>, Serializable {

  private static final long serialVersionUID = 1560474818107754225L;

  @NaturalId
  @NotEmpty(message = "Cannot save invoice without Invoice Number")
  @Column(unique = true, length = 12, nullable = false, updatable = false)
  private String invoiceNumber;

  @JsonIgnore
  @Column(nullable = false)
  @ColumnDefault("0")
  private Integer deleted;

  @NotEmpty(message = "Cannot save invoice without place of supply")
  @Column(nullable = false, length = 25)
  private String placeOfSupply;

  @Column(precision = 2, scale = 2)
  private Double roundOff;

  @NotNull(message = "Cannot save invoice without Invoice Date")
  @Temporal(TemporalType.DATE)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  @Column(nullable = false)
  private Date invoiceDate;

  @NotNull(message = "Cannot save invoice without Payment Date")
  @Temporal(TemporalType.DATE)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
  @Column(nullable = false)
  private Date dueDate;

  @NotNull(message = "Cannot save invoice without Payment Terms")
  @Column(length = 6, nullable = false)
  private String paymentTerms;

  @NotEmpty(message = "Cannot save invoice without Line Items")
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "invoice_line_items", joinColumns = @JoinColumn(name = "invoice_id"))
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
  @OrderBy("serialNumber ASC")
  private Set<LineItem> lineItems;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "invoice_payments", joinColumns = @JoinColumn(name = "invoice_id"))
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
  @OrderBy("paymentDate DESC")
  private Set<Payment> payments;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinTable(name = "invoice_company", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "company_id", referencedColumnName = "id"))
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinTable(name = "invoice_customer", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "company_id", referencedColumnName = "id"))
  private Customer customer;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinTable(name = "billed_from_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Branch billedFrom;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinTable(name = "billed_to_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Branch billedTo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinTable(name = "shipped_to_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Branch shippedTo;

  @PrePersist
  public void checkInvoiceNumber() throws InvalidAttributesException {

    if (this.billedFrom.getId().equals(this.billedTo.getId()) || this.billedFrom.getId().equals(this.shippedTo.getId()))
      throw new InvalidAttributesException("One of Customer branches is same as Company branches");
  }

  @Override
  public int hashCode() {
    return (Objects.hash(invoiceNumber) * 17);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Invoice other = (Invoice) obj;
    return Objects.equals(invoiceNumber, other.getInvoiceNumber());
  }
}