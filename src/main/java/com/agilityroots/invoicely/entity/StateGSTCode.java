/**
 *  23-Oct-2018 StateGSTCode.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;

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
public class StateGSTCode extends AuditableEntity implements Serializable {

	private static final long serialVersionUID = -2862330986413643014L;

	@Column(unique = true, length = 25)
	private String stateName;

	@Column(unique = true, length = 2)
	private String stateCode;
}
