/**
 *  4 Dec 2018 CustomerControllerTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.agilityroots.invoicely.DataApiAsyncConfiguration;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CustomerAsyncService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(CustomerController.class)
@EnableSpringDataWebSupport
@Import({ CustomerResourceAssember.class, BranchResourceAssembler.class, InvoiceResourceAssembler.class,
		CustomerAsyncService.class, DataApiAsyncConfiguration.class })
public class CustomerControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	CustomerAsyncService customerService;

	@MockBean
	private CustomerRepository customerRepository;

	@MockBean
	private InvoiceRepository invoiceRepository;

	@MockBean
	private BranchRepository branchRepository;

	@MockBean
	private ContactRepository contactRepository;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		// Initializes the JacksonTester
		JacksonTester.initFields(this, objectMapper);
	}

	@Test
	public void testWhenNoCustomersThenReturnsEmptyHALDocument() throws Exception {

		// Given
		BDDMockito.given(customerRepository.findAll(PageRequest.of(0, 20)))
				.willReturn(new PageImpl<Customer>(Collections.emptyList()));
		// When
		MvcResult result = mockMvc.perform(get("/customers").accept(MediaTypes.HAL_JSON_VALUE)).andDo(print())
				.andExpect(request().asyncStarted())
				.andExpect(request().asyncResult(new PageImpl<Customer>(Collections.emptyList()))).andReturn();
		// Then
		mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
	}

	@SuppressWarnings("unused")
	private HttpMessageConverter<?> getHalMessageConverter() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.registerModule(new Jackson2HalModule());
		MappingJackson2HttpMessageConverter halConverter = new MappingJackson2HttpMessageConverter();
		halConverter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON));
		halConverter.setObjectMapper(objectMapper);
		return halConverter;
	}
}
