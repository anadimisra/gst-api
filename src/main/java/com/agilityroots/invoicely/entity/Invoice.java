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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Where;

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
@Table(indexes = { @Index(name = "place_of_supply_index", columnList = "placeOfSupply", unique = false),
		@Index(name = "invoice_date_index", columnList = "invoiceDate", unique = false),
		@Index(name = "due_date_index", columnList = "dueDate", unique = false),
		@Index(name = "payment_terms_index", columnList = "paymentTerms", unique = false) })
public class Invoice extends AuditableEntity implements Serializable {

	private static final long serialVersionUID = 1560474818107754225L;

	@NaturalId
	@Column(unique = true, length = 20)
	private String invoiceNumber;

	private Integer deleted = 0;

	@Column(nullable = false, length = 25)
	private String placeOfSupply;

	private Double roundOff;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date invoiceDate;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date dueDate;

	@Column(nullable = false)
	private String paymentTerms;

	@OneToMany(fetch = FetchType.EAGER)
	@JoinColumn(name = "invoice_id", referencedColumnName = "id")
	@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
	private List<LineItem> lineItems;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "invoice_id", referencedColumnName = "id")
	@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
	private List<Payment> payments;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "billed_from_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
	private Branch billedFrom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "customer_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"))
	private Customer customer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "billed_to_invoices", joinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
	private Branch billedTo;

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