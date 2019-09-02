/**
 *
 */
package com.agilityroots.invoicely.controller;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.InvoiceService;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author anadi
 *
 */
@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(InvoiceController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({InvoiceService.class, InvoiceResourceAssembler.class, CustomerResourceAssember.class})
public class InvoiceControllerTest {

  EntityObjectsBuilder builder = new EntityObjectsBuilder();
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @MockBean
  private InvoiceService invoiceService;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    JacksonTester.initFields(this, objectMapper);
  }

  @Test
  public void testGetInvoicesReturnsNotFoundWhenNoInvoices() throws Exception {

    // Given
    BDDMockito.given(invoiceService.getInvoices(any(Pageable.class)))
        .willReturn(AsyncResult.forValue(new PageImpl<Invoice>(Collections.emptyList())));

    // When
    MvcResult result = mockMvc.perform(get("/invoices")).andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testGetInvoicesReturnsHALDocument() throws Exception {

    // Given
    Page<Invoice> page = new PageImpl<>(Arrays.asList(builder.getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceService.getInvoices(any(Pageable.class))).willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/invoices")).andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices")));

  }

  @Test
  public void testGetInvoiceReturnsNotFoundWhenNotPresent() throws Exception {

    // Given
    BDDMockito.given(invoiceService.getInvoice(any(String.class))).willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/1")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testGetInvoiceReturnsHALDocument() throws Exception {

    // Given
    BDDMockito.given(invoiceService.getInvoice(any(String.class)))
        .willReturn(AsyncResult.forValue(Optional.of(builder.getInvoiceObject())));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/20")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.line_items").doesNotHaveJsonPath())
        .andExpect(jsonPath("$.invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._links.self.href", endsWith("/invoices/20")))
        .andExpect(jsonPath("$._links.invoices.href", endsWith("/invoices")))
        .andExpect(jsonPath("$._links.customer.href", endsWith("/invoices/20/customer")));

  }

  @Test
  public void testWhenInvoiceNotFoundThenCustomerDetailsIsUnprocesseableEntity() throws Exception {
    // Given
    BDDMockito.given(invoiceService.getInvoice(any(String.class))).willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/20/customer")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCannotAddNullPaymentsToInvoice() throws Exception {
    // Given
    BDDMockito.given(invoiceService.getInvoice(any(String.class)))
        .willReturn(AsyncResult.forValue(Optional.of(builder.getInvoiceObject())));

    // When, Then
    mockMvc
        .perform(put("/invoices/20/payments").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getInvoiceObjectWithLineItems().getPayments())))
        .andExpect(request().asyncNotStarted()).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testAddingPaymentsToInvoiceReturnsLocationHeader() throws Exception {
    // Given
    Invoice invoice = builder.getInvoiceWithPayments();
    BDDMockito.given(invoiceService.updatePayments(any(Long.class), any(List.class)))
        .willReturn(AsyncResult.forValue(Optional.of(invoice)));

    String jsonContent = objectMapper.writeValueAsString(invoice.getPayments());
    log.info("Uploading payments {}", jsonContent);
    // When
    MvcResult result = mockMvc
        .perform(put("/invoices/" + invoice.getId().toString() + "/payments")
            .contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonContent))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();
    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/invoices/20/payments")));

  }

  @Test
  @SuppressWarnings("unchecked")
  public void testAddingPaymentsWhenInvoiceNotFoundReturnsBadRequest() throws Exception {
    // Given
    Invoice invoice = builder.getInvoiceWithPayments();
    BDDMockito.given(invoiceService.updatePayments(any(Long.class), any(List.class)))
        .willReturn(AsyncResult.forValue(Optional.empty()));

    String jsonContent = objectMapper.writeValueAsString(invoice.getPayments());
    log.info("Uploading payments {}", jsonContent);
    // When
    MvcResult result = mockMvc
        .perform(put("/invoices/20/payments").contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonContent))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();
    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());

  }

}
