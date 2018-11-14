/**
 *  23-Oct-2018 LineItem.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Index;
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
@Table(name = "line_items", indexes = {
		@Index(name = "invoice_line_items_item_index", columnList = "item", unique = false) })
public class LineItem extends AuditableModel implements Serializable {

	private static final long serialVersionUID = -977449988990379565L;

	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Integer serialNumber;

	private String item;

	private String description;

	private String hsn;

	private String sac;

	private Double price;

	private String unit;

	private Integer quantity;

	private Double discount;

	private Double amount;

	private Double tax;

}
