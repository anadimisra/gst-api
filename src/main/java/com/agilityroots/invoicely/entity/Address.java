/**
 *  19-Oct-2018 Address.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author anadi
 *
 */
@Setter
@Getter
@Embeddable
public class Address implements Serializable {

	private static final long serialVersionUID = 3066264859071664783L;

	@Column(nullable = false)
	private String streetAddress;

	@Column(nullable = false)
	private String area;

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	private String state;

	@Column(nullable = false)
	private String pincode;

}
