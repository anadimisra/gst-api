/**
 *  23-Oct-2018 Item.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
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
@Table(indexes = { @Index(name = "item_names_index", columnList = "name", unique = false),
		@Index(name = "item_hsn_index", columnList = "hsn", unique = false),
		@Index(name = "item_sac_index", columnList = "sac", unique = false) })
public class Item extends AuditableModel implements Serializable {

	private static final long serialVersionUID = -4121755316741852665L;

	@Column(length = 20)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(length = 6)
	private String hsn;

	@Column(length = 6)
	private String sac;

	private Double price;

	private Double tax;

}
