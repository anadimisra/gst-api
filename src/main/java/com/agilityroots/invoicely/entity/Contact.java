/**
 *  22-Oct-2018 Contact.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;

import org.apache.commons.lang3.builder.HashCodeBuilder;
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
@Embeddable
public class Contact extends AuditableEntity implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = -7756870986677314517L;

	@Column(nullable = false, updatable = false, length = 50)
	private String name;

	@Column(nullable = false, updatable = false, length = 50)
	private String email;

	@Column(length = 10, updatable = false)
	private String phone;

	@Override
	public int hashCode() {

		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(name).append(email);
		return hashCodeBuilder.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (email == null) {
			if (other.getEmail() != null)
				return false;
		} else if (!email.equals(other.getEmail()))
			return false;
		if (name == null) {
			if (other.getName() != null)
				return false;
		} else if (!name.equals(other.getName()))
			return false;
		return true;
	}
}
