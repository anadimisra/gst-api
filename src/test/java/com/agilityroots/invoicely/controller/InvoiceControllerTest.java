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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
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
        .andExpect(jsonPath("$.invoice_number", is(equalTo("20190902"))))
        .andExpect(jsonPath("$._links.self.href", endsWith("/invoices/20")));

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
