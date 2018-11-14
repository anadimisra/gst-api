/**
 *  22-Oct-2018 Organisation.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author anadi
 *
 */
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
public class Organisation extends AuditableModel {

	@Column(unique = true, length = 50)
	private String name;

	@Column(unique = true, length = 10, updatable = false)
	private String pan;

	@Column(name = "vat_tin", length = 11)
	private String vatTin;

}
