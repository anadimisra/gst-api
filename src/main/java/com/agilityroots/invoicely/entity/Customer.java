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
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author anadi
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(indexes = { @Index(name = "customer_name_index", columnList = "name", unique = false) })
public class Customer extends Organisation implements Serializable {

	private static final long serialVersionUID = 8101819808147191270L;

	@Column(nullable = false, length = 3)
	private String currecny;

	private Double tds = 0.10;

	@Column(updatable = false, length = 3)
	private String invoicePrefix;

	@PrePersist
	public void setDefaultValues() {
		if (null == invoicePrefix)
			invoicePrefix = "INV";
		if (null == currecny)
			currecny = "INR";
	}

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "customer_branches", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
	private List<Branch> branches;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "customer_contact", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"))
	private Contact contact;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "customer_invoices", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"))
	private List<Invoice> invoices;

}