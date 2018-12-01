/**
 *  22-Oct-2018 Organisation.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

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
public abstract class Organisation extends AuditableEntity {

	@NotNull(message = "Cannot save customer without registered name")
	@Column(unique = true, length = 50, nullable = false)
	private String name;

	@NotNull(message = "PAN is mandatory while adding a new Customer")
	@Column(unique = true, length = 10, updatable = false, nullable = false)
	private String pan;

	@Column(length = 11)
	private String vatTin;

}
