package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author anadi
 */
@RunWith(SpringRunner.class)
@WebMvcTest(CompanyController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({CompanyService.class, CompanyController.class, InvoiceResourceAssembler.class, BranchResourceAssembler.class})
public class CompanyControllerTest {

  private final EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CompanyService companyService;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    JacksonTester.initFields(this, objectMapper);
  }

  @Test
  public void testSavingBranchReturnsLocationHeader() throws Exception {

    // Given
    Branch branch = builder.getBranchObject();
    URI location = URI.create("http://localhost/branches/" + branch.getId().toString());
    BDDMockito.given(companyService.addBranch(any(Long.class), any(Branch.class), any(StringBuffer.class)))
        .willReturn(AsyncResult.forValue(Optional.of(location)));

    // When
    MvcResult result = mockMvc
        .perform(put("/companies/20/branches").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(branch)))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/branches/" + branch.getId().toString())));
  }

  @Test
  public void testReturnsPagedResponseForAllCompanyInvoices() throws Exception {
    //Given
    BDDMockito.given(companyService.getAllInvoices(anyLong(), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<>(Stream.of(builder.getInvoiceObjectWithLineItems()).collect(Collectors.toList()))));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("20190902"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices")));
  }

  @Test
  public void testReturns404WhenNoInvoicesAdded() throws Exception {
    //Given
    BDDMockito.given(companyService.getAllInvoices(anyLong(), any(Pageable.class))).willReturn(AsyncResult.forValue(new PageImpl<>(Collections.emptyList())));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReturnsPagedResponseForAllCompanyPaidInvoices() throws Exception {
    //Given
    BDDMockito.given(companyService.getPaidInvoices(anyLong(), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<>(Stream.of(builder.getInvoiceObjectWithLineItems()).collect(Collectors.toList()))));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices/paid")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("20190902"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices")));
  }

  @Test
  public void testReturns404WhenNoPaidInvoices() throws Exception {
    //Given
    BDDMockito.given(companyService.getPaidInvoices(anyLong(), any(Pageable.class))).willReturn(AsyncResult.forValue(new PageImpl<>(Collections.emptyList())));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices/paid")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReturnsPagedResponseForAllCompanyDueInvoices() throws Exception {
    //Given
    BDDMockito.given(companyService.getDueInvoices(anyLong(), any(Date.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<>(Stream.of(builder.getInvoiceObjectWithLineItems()).collect(Collectors.toList()))));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices/due")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("20190902"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices")));
  }

  @Test
  public void testReturns404WhenNoDueInvoices() throws Exception {
    //Given
    BDDMockito.given(companyService.getDueInvoices(anyLong(), any(Date.class), any(Pageable.class))).willReturn(AsyncResult.forValue(new PageImpl<>(Collections.emptyList())));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices/due")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReturnsPagedResponseForAllCompanyOverdueInvoices() throws Exception {
    //Given
    BDDMockito.given(companyService.getOverDueInvoices(anyLong(), any(Date.class), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<>(Stream.of(builder.getInvoiceObjectWithLineItems()).collect(Collectors.toList()))));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices/overdue")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("20190902"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices")));
  }

  @Test
  public void testReturns404WhenNoOverdueInvoices() throws Exception {
    //Given
    BDDMockito.given(companyService.getOverDueInvoices(anyLong(), any(Date.class), any(Pageable.class))).willReturn(AsyncResult.forValue(new PageImpl<>(Collections.emptyList())));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/invoices/overdue")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testReturnsPagedResponseForAllCompanyBranches() throws Exception {
    //Given
    BDDMockito.given(companyService.getBranches(anyLong(), any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<>(Stream.of(builder.getBranchObject()).collect(Collectors.toList()))));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/branches")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.branches", hasSize(1)))
        .andExpect(jsonPath("$._embedded.branches[0].branch_name", endsWith("Branch")))
        .andExpect(jsonPath("$._links.self.href", containsString("/branches")));
  }

  @Test
  public void testReturns404WhenNoBranchesAdded() throws Exception {
    //Given
    BDDMockito.given(companyService.getBranches(anyLong(), any(Pageable.class))).willReturn(AsyncResult.forValue(new PageImpl<>(Collections.emptyList())));

    //When
    MvcResult result = mockMvc.perform(get("/companies/20/branches")).andExpect(request().asyncStarted())
        .andDo(print()).andReturn();

    //Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }
}
