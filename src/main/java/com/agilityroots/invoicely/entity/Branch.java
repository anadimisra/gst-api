/**
 *  19-Oct-2018 Branch.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
@Table(indexes = { @Index(name = "branch_name_index", columnList = "branchName", unique = false) })
public class Branch extends AuditableModel implements Serializable {

	private static final long serialVersionUID = -8841725432779534218L;

	@Column(length = 25)
	private String branchName;

	@Column(length = 15, unique = true)
	private String gstin;

	@Column(length = 11, nullable = true)
	private String vatTin;

	private Address address;

	private Boolean sez;

	public Boolean isSez() {
		return sez;
	}

	@OneToOne(fetch = FetchType.LAZY)
	@JoinTable(name = "branch_contact", joinColumns = @JoinColumn(name = "branch_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "contact_id", referencedColumnName = "id"))
	private Contact contact;

}
