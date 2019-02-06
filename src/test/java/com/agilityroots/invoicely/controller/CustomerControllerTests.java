/**
 *  4 Dec 2018 CustomerControllerTests.java
 *  data-api
 *  Copyright 2018 Agility Roots Private Limited. All Rights Reserved
 */
package com.agilityroots.invoicely.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(CustomerController.class)
@Import({ CustomerResourceAssember.class, BranchResourceAssembler.class, InvoiceResourceAssembler.class,
    CustomerService.class })
public class CustomerControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @MockBean
  private BranchRepository branchRepository;

  @MockBean
  private ContactRepository contactRepository;

  @MockBean
  private InvoiceRepository invoiceRepository;

  @MockBean
  private CustomerRepository customerRepository;

  @InjectMocks
  CustomerService customerService = new CustomerService(customerRepository);

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
    MvcResult result = mockMvc.perform(get("/customers")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
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
    MvcResult result = mockMvc.perform(get("/customers")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then return a PagedResource with One Customer
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.customers", hasSize(1)));
  }

  @Test
  public void testSavingCustomerReturnsLocationHeader() throws Exception {

    // Given
    Customer minty = builder.getCustomerObject();
    BDDMockito.given(customerRepository.saveAndFlush(any(Customer.class))).willReturn(minty);

    // When
    MvcResult result = mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(minty)))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
    assertThat(result.getResponse().getHeader("Location")).contains("customers/10");
  }

  @Test
  public void testFailedSavingCustomerReturnsInternalServerError() throws Exception {

    // Given
    Customer minty = builder.getCustomerObject();
    BDDMockito.given(customerRepository.saveAndFlush(any(Customer.class))).willThrow(new RuntimeException());

    // When
    MvcResult result = mockMvc
        .perform(post("/customers").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(minty)))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().is5xxServerError());
    assertThat(result.getResponse().getContentAsString()).contains("Cannot save customer details due to server error.");
  }

  @Test
  public void testGetWhenCustomerNotExistsReturnsNotFoundResponse() throws Exception {

    // Given
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.empty());

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
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(minty));

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
        .andExpect(jsonPath("$._links.pending-invoices.href", containsString("/customers/10/invoices/pending")))
        .andExpect(jsonPath("$._links.overdue-invoices.href", containsString("/customers/10/invoices/overdue")));
  }

  @Test
  public void testWhenNoBranchesThenGetsNotFoundResponse() throws Exception {

    // Given
    Customer customer = builder.getCustomerObject();
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/branches")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testWhenNoContactThenGetsNotFoundResponse() throws Exception {

    // Given
    Customer customer = builder.getCustomerObject();
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/contact")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testWhenNoInvoicesThenGetsOkResponseWithNoContent() throws Exception {

    // Given
    BDDMockito.given(invoiceRepository.findAllByCustomer_Id(any(Long.class), any(Pageable.class)))
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
    BDDMockito
        .given(
            invoiceRepository.findByPayments_PaymentDateIsNotNullAndCustomer_Id(any(Long.class), any(Pageable.class)))
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
    BDDMockito
        .given(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBeforeAndCustomer_Id(any(Date.class),
            any(Long.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<Invoice>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/invoices/overdue")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices").doesNotHaveJsonPath())
        .andExpect(jsonPath("$._links.self.href", containsString("/customers/1/invoices/overdue")));

  }

  @Test
  public void testWhenNoPendingInvoicesThenGetsOkResponseWithNoContent() throws Exception {

    // Given
    BDDMockito
        .given(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfterAndCustomer_Id(any(Date.class),
            any(Long.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<Invoice>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/customers/1/invoices/pending")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices").doesNotHaveJsonPath())
        .andExpect(jsonPath("$._links.self.href", containsString("/customers/1/invoices/pending")));

  }

  public void testAddBranchAndCustomerNotExistsGivesBadRequest() throws Exception {

    // Given
    BDDMockito.given(customerRepository.findEagerFetchBranchesById(any(Long.class))).willReturn(null);

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
    BDDMockito.given(customerRepository.findEagerFetchBranchesById(any(Long.class)))
        .willReturn(builder.getCustomerObject());
    BDDMockito.given(branchRepository.saveAndFlush(any(Branch.class))).willReturn(builder.getBranchObject());
    Customer withBranch = builder.getCustomerObject();
    withBranch.setBranches(Arrays.asList(builder.getBranchObject()));
    BDDMockito.given(customerRepository.saveAndFlush(any(Customer.class))).willReturn((withBranch));

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
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // When
    MvcResult result = mockMvc
        .perform(put("/customers/1/invoices").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getBranchObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testAddInvoiceReturnsLocationHeader() throws Exception {

    // Given
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(builder.getCustomerObject()));
    BDDMockito.given(invoiceRepository.saveAndFlush(any(Invoice.class)))
        .willReturn(builder.getInvoiceObjectWithCustomer());

    // When
    MvcResult result = mockMvc
        .perform(put("/customers/1/invoices").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getBranchObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testAddContactToNonExistingCustomerGivesBadRequest() throws Exception {

    // Given
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.empty());

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
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(builder.getCustomerObject()));
    BDDMockito.given(contactRepository.saveAndFlush(any(Contact.class))).willReturn(builder.getContactObject());
    Customer withContact = builder.getCustomerObject();
    withContact.setContact(builder.getContactObject());
    BDDMockito.given(customerRepository.saveAndFlush(any(Customer.class))).willReturn((withContact));

    // When
    MvcResult result = mockMvc
        .perform(put("/customers/10/contact").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getContactObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
    assertThat(result.getResponse().getHeader("Location")).contains("/customers/10/contact");
  }

}
