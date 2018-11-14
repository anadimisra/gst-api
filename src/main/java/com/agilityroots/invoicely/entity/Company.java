/**
 *  19-Oct-2018 Company.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author anadi
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Company extends Organisation implements Serializable {

	private static final long serialVersionUID = 4627788171283297107L;

	@Column(unique = true, length = 21, updatable = false)
	private String cin;

	@Column(unique = true, length = 10, updatable = false)
	private String tan;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "company_branches", joinColumns = @JoinColumn(name = "company_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"))
	private List<Branch> branches;

}