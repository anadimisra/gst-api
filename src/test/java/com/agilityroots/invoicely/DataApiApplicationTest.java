/**
 *  21-Oct-2018 DataApiApplicationTest.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.controller.CustomerController;

/**
 * @author anadi
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DataApiApplicationTest {

	@Autowired
	private CustomerController customerController;
	
	@Test
	public void contextLoads() throws Exception {
		assertThat(customerController).isNotNull();
	}

}
