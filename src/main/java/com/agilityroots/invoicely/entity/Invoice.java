/**
 *  23-Oct-2018 Invoice.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Where;
import org.springframework.hateoas.Identifiable;
import org.springframework.hateoas.core.Relation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
@NoArgsConstructor
@Where(clause = "DELETED = 0")
@Relation(collectionRelation = "invoices")
@Table(indexes = { @Index(name = "place_of_supply_index", columnList = "placeOfSupply", unique = false),
    @Index(name = "invoice_date_index", columnList = "invoiceDate", unique = false),
    @Index(name = "due_date_index", columnList = "dueDate", unique = false),
    @Index(name = "payment_terms_index", columnList = "paymentTerms", unique = false) })
public class Invoice extends AuditableEntity implements Identifiable<Long>, Serializable {

  private static final long serialVersionUID = 1560474818107754225L;

  @NaturalId
  @NotEmpty(message = "Cannot save invoice without Invoice Number")
  @Column(unique = true, length = 20)
  private String invoiceNumber;

  @JsonIgnore
  private Integer deleted = 0;

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
  @Column(nullable = false)
  private String paymentTerms;

  @NotEmpty(message = "Cannot save invoice without Line Items")
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "invoice_line_items", joinColumns = @JoinColumn(name = "invoice_id"))
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
  @OrderBy("serialNumber ASC")
  private List<LineItem> lineItems;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "invoice_payments", joinColumns = @JoinColumn(name = "invoice_id"))
  @JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
  @OrderBy("paymentDate DESC")
  private List<Payment> payments;

  @NotNull(message = "Cannot save invoice without Billed From Branch")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "billed_from_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Branch billedFrom;

  @NotNull(message = "Cannot save invoice without Customer Details")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "customer_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"))
  private Customer customer;

  @NotNull(message = "Cannot save invoice without Billed To Branch")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "billed_to_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Branch billedTo;

  @NotNull(message = "Cannot save invoice without Shipped To Branch")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinTable(name = "shipped_to_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
  private Branch shippedTo;

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