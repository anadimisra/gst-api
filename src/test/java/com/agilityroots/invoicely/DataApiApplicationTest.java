/**
 * 21-Oct-2018 DataApiApplicationTest.java
 * data-api
 * Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely;

import com.agilityroots.invoicely.controller.BranchController;
import com.agilityroots.invoicely.controller.CustomerController;
import com.agilityroots.invoicely.controller.InvoiceController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author anadi
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class DataApiApplicationTest {

  @Autowired
  private CustomerController customerController;

  @Autowired
  private InvoiceController invoiceController;

  @Autowired
  private BranchController branchController;

  @Test
  public void contextLoads() throws Exception {
    assertThat(customerController).isNotNull();
    assertThat(invoiceController).isNotNull();
    assertThat(branchController).isNotNull();
  }

}
