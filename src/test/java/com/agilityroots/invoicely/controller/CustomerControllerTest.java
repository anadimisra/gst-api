/*

 4 Dec 2018 CustomerControllerTests.java
 data-api
 Copyright 2018 Agility Roots Private Limited. All Rights Reserved

*/
package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author anadi
 */
@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(CustomerController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({CustomerResourceAssember.class, BranchResourceAssembler.class, InvoiceResourceAssembler.class, CustomerService.class})
public class CustomerControllerTest {

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @MockBean
  CustomerService customerService;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    JacksonTester.initFields(this, objectMapper);
  }

  @Test
  public void testReturnsNotFoundForEmptyGetAllCustomersResult() throws Exception {

    // Given
    Page<Customer> emptyPage = new PageImpl<Customer>(Collections.emptyList());
    BDDMockito.given(customerService.getCustomers(any(Pageable.class))).willReturn(AsyncResult.forValue(emptyPage));

    // When
    MvcResult result = mockMvc.perform(get("/customers")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReturnsHALJsonReponseForGetAllCustomers() throws Exception {

    // Given
    List<Customer> mintys = new ArrayList<>();
    mintys.add(builder.getCustomerObject());
    Page<Customer> page = new PageImpl<>(mintys, PageRequest.of(0, 20), Long.valueOf(1));
    BDDMockito.given(customerService.getCustomers(any(Pageable.class))).willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/customers")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then return a PagedResource with One Customer
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.customers", hasSize(1)));
  }

  @Test
  public void testSavingCustomerReturnsLocationHeader() throws Exception {

    // Given
    BDDMockito.given(customerService.save(any(Customer.class)))
        .willReturn(AsyncResult.forValue(builder.getCustomerObject()));

    // When
    MvcResult result = mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getCustomerObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
    assertThat(result.getResponse().getHeader("Location")).contains("customers/10");
  }

  @Test
  public void testGetWhenCustomerNotExistsReturnsNotFoundResponse() throws Exception {

    // Given
    BDDMockito.given(customerService.getCustomer(any(Long.class))).willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testGeneratesHALDocumentWithLinksWhenCustomerExists() throws Exception {

    // Given
    Customer minty = builder.getCustomerObject();
    BDDMockito.given(customerService.getCustomer(any(Long.class))).willReturn(AsyncResult.forValue(Optional.of(minty)));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.name", is("Minty and Sons Private Limited")))
        .andExpect(jsonPath("$._links.self.href", containsString("/customers/10")))
        .andExpect(jsonPath("$._links.customers.href", containsString("/customers")))
        .andExpect(jsonPath("$._links.contact.href", containsString("/customers/10/contact")))
        .andExpect(jsonPath("$._links.branches.href", containsString("/customers/10/branches")))
        .andExpect(jsonPath("$._links.invoices.href", containsString("/customers/10/invoices")))
        .andExpect(jsonPath("$._links.paid-invoices.href", containsString("/customers/10/invoices/paid")))
        .andExpect(jsonPath("$._links.due-invoices.href", containsString("/customers/10/invoices/due")))
        .andExpect(jsonPath("$._links.overdue-invoices.href", containsString("/customers/10/invoices/overdue")));
  }

  @Test
  public void testWhenNoInvoicesThenGetsOkResponseWithNoContent() throws Exception {

    // Given
    BDDMockito.given(customerService.getAllInvoices(any(Long.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<Invoice>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/invoices")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices").doesNotHaveJsonPath())
        .andExpect(jsonPath("$._links.self.href", containsString("/customers/1/invoices")));

  }

  @Test
  public void testWhenNoPaidInvoicesThenGetsOkResponseWithNoContent() throws Exception {

    // Given
    BDDMockito.given(customerService.getPaidInvoices(any(Long.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<Invoice>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/invoices/paid")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices").doesNotHaveJsonPath())
        .andExpect(jsonPath("$._links.self.href", containsString("/customers/1/invoices/paid")));

  }

  @Test
  public void testWhenNoOverDueInvoicesThenGetsOkResponseWithNoContent() throws Exception {

    // Given
    BDDMockito.given(customerService.getOverdueInvoices(any(Date.class), any(Long.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<Invoice>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/invoices/overdue")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices").doesNotHaveJsonPath())
        .andExpect(jsonPath("$._links.self.href", containsString("/customers/1/invoices/overdue")));

  }

  @Test
  public void testWhenNoDueInvoicesThenGetsOkResponseWithNoContent() throws Exception {

    // Given
    BDDMockito.given(customerService.getDueInvoices(any(Date.class), any(Long.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<Invoice>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/invoices/due")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices").doesNotHaveJsonPath())
        .andExpect(jsonPath("$._links.self.href", containsString("/customers/1/invoices/due")));

  }

  @Test
  public void testWhenNoBranchesThenGetsNotFoundResponse() throws Exception {

    // Given
    BDDMockito.given(customerService.getAllBranches(any(Long.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/branches")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testAddBranchWhenCustomerNotExistsGivesBadRequest() throws Exception {

    // Given
    BDDMockito.given(customerService.addBranch(any(Long.class), any(Branch.class), any(StringBuffer.class)))
        .willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc
        .perform(put("/customers/1/branches").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getBranchObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testAddingBranchReturnsLocationHeader() throws Exception {

    // Given
    URI location = URI.create("http://localhost/customers/10/branches/20");
    BDDMockito.given(customerService.addBranch(any(Long.class), any(Branch.class), any(StringBuffer.class)))
        .willReturn(AsyncResult.forValue(Optional.of(location)));

    // When
    MvcResult result = mockMvc
        .perform(put("/customers/10/branches").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getBranchObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
    assertThat(result.getResponse().getHeader("Location")).contains("/customers/10/branches/20");
  }

  @Test
  public void testAddInvoicesToNonExisitingCustomerGivesBadRequest() throws Exception {

    // Given
    URI location = null;
    BDDMockito
        .given(customerService.addInvoice(any(Long.class), any(Long.class), any(Long.class), any(Long.class),
            any(StringBuffer.class), any(Invoice.class)))
        .willReturn(AsyncResult.forValue(Optional.ofNullable(location)));

    // When
    String invoiceJson = objectMapper.writeValueAsString(builder.getValidInvoicePayloadObject());
    log.debug("Posting invoice JSON: {}", invoiceJson);
    MvcResult result = mockMvc
        .perform(put("/customers/1/invoices").contentType(MediaType.APPLICATION_JSON_VALUE).content(invoiceJson))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testAddInvoiceReturnsLocationHeader() throws Exception {

    // Given
    BDDMockito
        .given(customerService.addInvoice(any(Long.class), any(Long.class), any(Long.class), any(Long.class),
            any(StringBuffer.class), any(Invoice.class)))
        .willReturn(AsyncResult.forValue(Optional.of(URI.create("http://localhost/customers/1/invoices/2"))));

    String invoiceJson = objectMapper.writeValueAsString(builder.getValidInvoicePayloadObject());
    log.debug("Posting invoice JSON: {}", invoiceJson);
    // When
    MvcResult result = mockMvc
        .perform(put("/customers/1/invoices").contentType(MediaType.APPLICATION_JSON_VALUE).content(invoiceJson))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testWhenNoContactThenGetsNotFoundResponse() throws Exception {

    // Given
    Customer customer = builder.getCustomerObject();
    BDDMockito.given(customerService.getContact(any(Long.class)))
        .willReturn(AsyncResult.forValue(Optional.ofNullable(customer.getContact())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/contact")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testAddContactToNonExistingCustomerGivesBadRequest() throws Exception {

    // Given
    BDDMockito.given(customerService.addContact(any(Long.class), any(Contact.class), any(StringBuffer.class)))
        .willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc
        .perform(put("/customers/1/contact").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getContactObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testAddingContactReturnsLocationHeader() throws Exception {

    // Given
    BDDMockito.given(customerService.addContact(any(Long.class), any(Contact.class), any(StringBuffer.class)))
        .willReturn(AsyncResult.forValue(Optional.of(URI.create("http://localhost/customers/10/contact"))));

    // When
    MvcResult result = mockMvc
        .perform(put("/customers/10/contact").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getContactObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
    assertThat(result.getResponse().getHeader("Location")).endsWith("/customers/10/contact");
  }

  @Test
  public void testGettingContactDetailsReturnsHALDocument() throws Exception {
    // Given
    BDDMockito.given(customerService.getContact(any(Long.class)))
        .willReturn(AsyncResult.forValue(Optional.of(builder.getContactObject())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/10/contact").contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.phone", is("8067601867")))
        .andExpect(jsonPath("$._links.contact.href", endsWith("/customers/10/contact")));
  }

  @Test
  public void testGettingContactDetailsWhenNoCustomer() throws Exception {
    // Given
    BDDMockito.given(customerService.getContact(any(Long.class))).willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc.perform(get("/customers/10/contact").contentType(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }
}
