/**
 *  22-Oct-2018 Contact.java
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
public class Contact extends AuditableModel implements Serializable {

	private static final long serialVersionUID = -7756870986677314517L;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(unique = true, length = 50)
	private String email;

	@Column(length = 10)
	private String phone;

	@Column(unique = true, length = 10)
	private String mobile;
}
