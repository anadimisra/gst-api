/**
 *  4 Dec 2018 CustomerControllerTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CustomerAsyncService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(CustomerController.class)
@Import({ CustomerResourceAssember.class, BranchResourceAssembler.class, InvoiceResourceAssembler.class,
		CustomerAsyncService.class })
public class CustomerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private BranchRepository branchRepository;

	@MockBean
	private ContactRepository contactRepository;

	@MockBean
	private InvoiceRepository invoiceRepository;

	@MockBean
	private CustomerRepository customerRepository;

	@InjectMocks
	CustomerAsyncService customerService = new CustomerAsyncService(customerRepository);

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		JacksonTester.initFields(this, objectMapper);
	}

	@Test
	public void testReturnsNotFoundForEmptyGetAllCustomersResult() throws Exception {

		// Given
		Page<Customer> emptyPage = new PageImpl<Customer>(Collections.emptyList());
		BDDMockito.given(customerRepository.findAll(any(Pageable.class))).willReturn(emptyPage);

		// When
		MvcResult result = mockMvc.perform(get("/customers")).andExpect(request().asyncStarted()).andDo(print()).andReturn();

		// Then
		mockMvc.perform(asyncDispatch(result)).andDo(print());
		assertThat(result.getResponse().getStatus()).isEqualTo(404);
	}

	@Test
	public void testReturnsHALJsonReponseForGetAllCustomers() throws Exception {

		// Given
		Customer minty = new Customer();
		minty.setName("Minty and Sons Private Limited");
		minty.setPan("ABCDE1234Q");
		List<Customer> mintys = new ArrayList<>();
		mintys.add(minty);
		Page<Customer> page = new PageImpl<>(mintys, PageRequest.of(0, 20), Long.valueOf(1));
		BDDMockito.given(customerRepository.findAll(any(Pageable.class))).willReturn(page);

		// When
		MvcResult result = mockMvc.perform(get("/customers")).andExpect(request().asyncStarted()).andDo(print()).andReturn();

		// Then
		mockMvc.perform(asyncDispatch(result)).andDo(print());
		assertThat(result.getResponse().getStatus()).isEqualTo(200);
	}
	
}
