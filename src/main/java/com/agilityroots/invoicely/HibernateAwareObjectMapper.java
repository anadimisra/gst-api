/**
 *  22-Oct-2018 HibernateAwareObjectMapper.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;

/**
 * @author anadi
 *
 */
public class HibernateAwareObjectMapper extends ObjectMapper {

	private static final long serialVersionUID = -4934273698008915161L;

	public HibernateAwareObjectMapper() {
		registerModule(new Hibernate5Module());
	}

}
