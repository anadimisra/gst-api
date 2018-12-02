/**
 *  19-Oct-2018 Address.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author anadi
 *
 */
@Setter
@Getter
@ToString
@Embeddable
public class Address implements Serializable {

	private static final long serialVersionUID = 3066264859071664783L;

	@NotEmpty(message = "Street address is mandatory.")
	@Column(nullable = false, updatable = false)
	private String streetAddress;

	@NotEmpty(message = "City area is mandatory.")
	@Column(nullable = false, updatable = false)
	private String area;

	@NotEmpty(message = "City is mandatory.")
	@Column(nullable = false, updatable = false)
	private String city;

	@NotEmpty(message = "State area is mandatory.")
	@Column(nullable = false, updatable = false)
	private String state;

	@NotEmpty(message = "Pincode area is mandatory.")
	@Column(nullable = false, updatable = false)
	private String pincode;

	@Override
	public int hashCode() {
		HashCodeBuilder hcb = new HashCodeBuilder();
		hcb.append(pincode).append(state).append(city).append(area).append(streetAddress);
		return hcb.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Address other = (Address) obj;
		if (area == null) {
			if (other.getArea() != null)
				return false;
		} else if (!area.equals(other.getArea()))
			return false;
		if (city == null) {
			if (other.getCity() != null)
				return false;
		} else if (!city.equals(other.getCity()))
			return false;
		if (pincode == null) {
			if (other.getPincode() != null)
				return false;
		} else if (!pincode.equals(other.getPincode()))
			return false;
		if (state == null) {
			if (other.getState() != null)
				return false;
		} else if (!state.equals(other.getState()))
			return false;
		if (streetAddress == null) {
			if (other.getStreetAddress() != null)
				return false;
		} else if (!streetAddress.equals(other.getStreetAddress()))
			return false;
		return true;
	}

}
