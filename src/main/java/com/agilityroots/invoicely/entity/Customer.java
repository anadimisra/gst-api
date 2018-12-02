/**
 *  22-Oct-2018 Customer.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.NamedSubgraph;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.springframework.hateoas.Identifiable;

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
@Table(indexes = { @Index(name = "customer_name_index", columnList = "name", unique = false) })
@NamedEntityGraphs({
		@NamedEntityGraph(name = "graph.Customer.invoices", attributeNodes = @NamedAttributeNode("invoices")),
		@NamedEntityGraph(name = "graph.Customer.branches", attributeNodes = @NamedAttributeNode("branches")),
		@NamedEntityGraph(name = "graph.Customer.invoices.payments", attributeNodes = @NamedAttributeNode(value = "invoices", subgraph = "invoices"), subgraphs = @NamedSubgraph(name = "invoices", attributeNodes = @NamedAttributeNode("payments"))) })
public class Customer extends Organisation implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = 8101819808147191270L;

	@NotEmpty(message = "Cannot save customer without billing currency.")
	@Column(nullable = false, length = 3, updatable = false)
	private String currecny = new String("INR");

	private Double tds = 0.10;

	@NotEmpty(message = "Cannot save customer without invoice prefix.")
	@Column(updatable = false, length = 3, nullable = false)
	private String invoicePrefix = new String("INV");

	@OneToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "customer_contact", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"))
	private Contact contact;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "customer_invoices", joinColumns = @JoinColumn(name = "customer_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "invoice_id", referencedColumnName = "id"))
	private List<Invoice> invoices;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_id", referencedColumnName = "id")
	private List<Branch> branches = new ArrayList<Branch>();

}