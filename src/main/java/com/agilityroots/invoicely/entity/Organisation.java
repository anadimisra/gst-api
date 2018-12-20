/**
 *  22-Oct-2018 Organisation.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.NaturalId;

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

	@NotEmpty(message = "Cannot save customer without registered name")
	@Column(unique = true, length = 50, nullable = false)
	@NaturalId
	private String name;

	@NotEmpty(message = "PAN is mandatory while adding a new Customer")
	@Column(unique = true, length = 10, updatable = false, nullable = false)
	private String pan;

	@Column(length = 11)
	private String vatTin;

	@Override
	public int hashCode() {
		return (Objects.hash(name) * 79);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Organisation other = (Organisation) obj;
		return Objects.equals(name, other.getName());
	}

}
