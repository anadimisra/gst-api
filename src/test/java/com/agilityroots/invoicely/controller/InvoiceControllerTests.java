/**
 * 
 */
package com.agilityroots.invoicely.controller;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
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
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@Slf4j
@RunWith(SpringRunner.class)
@WebMvcTest(InvoiceController.class)
@TestPropertySource(locations = "classpath:application-unit-test.properties")
@Import({ InvoiceService.class, InvoiceResourceAssembler.class, CustomerResourceAssember.class })
public class InvoiceControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @MockBean
  private InvoiceRepository invoiceRepository;

  @InjectMocks
  private InvoiceService invoiceService = new InvoiceService(invoiceRepository);

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    JacksonTester.initFields(this, objectMapper);
  }

  @Test
  public void testGetInvoicesReturnsNotFoundWhenNoInvoices() throws Exception {

    // Given
    BDDMockito.given(invoiceRepository.findAll(ArgumentMatchers.any(Pageable.class)))
        .willReturn(new PageImpl<Invoice>(Collections.emptyList()));

    // When
    MvcResult result = mockMvc.perform(get("/invoices")).andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testGetInvoicesReturnsHALDocument() throws Exception {

    // Given
    Page<Invoice> page = new PageImpl<>(Arrays.asList(builder.getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceRepository.findAll(ArgumentMatchers.any(Pageable.class))).willReturn(page);

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
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.any(Long.class))).willReturn(Optional.empty());

    // When
    MvcResult result = mockMvc.perform(get("/invoices/1")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testGetInvoiceReturnsHALDocument() throws Exception {

    // Given
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.any(Long.class)))
        .willReturn(Optional.of(builder.getInvoiceObject()));

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
  public void testGetPaidInvoicesReturnsHALDocument() throws Exception {
    // Given
    Page<Invoice> page = new PageImpl<>(Arrays.asList(builder.getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceRepository.findByPayments_PaymentDateIsNotNull(ArgumentMatchers.any(Pageable.class)))
        .willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/paid")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices/paid")));
  }

  @Test
  public void testGetPendingInvoicesReturnsHALDocument() throws Exception {
    // Given
    Page<Invoice> page = new PageImpl<>(Arrays.asList(builder.getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfter(ArgumentMatchers.any(Date.class),
        ArgumentMatchers.any(Pageable.class))).willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/pending")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices/pending")));
  }

  @Test
  public void testGetOverdueInvoicesReturnsHALDocument() throws Exception {
    // Given
    Page<Invoice> page = new PageImpl<>(Arrays.asList(builder.getInvoiceObjectWithLineItems()));
    BDDMockito
        .given(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBefore(ArgumentMatchers.any(Date.class),
            ArgumentMatchers.any(Pageable.class)))
        .willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/overdue")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(2)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices/overdue")));
  }

  @Test
  public void testWhenInvoiceNotFoundThenCustomerDetailsIsUnprocesseableEntity() throws Exception {
    // Given
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.anyLong())).willReturn(Optional.empty());

    // When
    MvcResult result = mockMvc.perform(get("/invoices/20/customer")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGetCustomerDetailsForInvoice() throws Exception {
    // Given
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.anyLong()))
        .willReturn(Optional.of(builder.getInvoiceObjectWithCustomer()));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/20/customer")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.pan", is(equalTo("ABCDE1234Q"))));
  }

  @Test
  public void testCannotAddNullPaymentsToInvoice() throws Exception {
    // Given
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.anyLong()))
        .willReturn(Optional.of(builder.getInvoiceObjectWithLineItems()));

    // When, Then
    mockMvc
        .perform(put("/invoices/20/payments").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getInvoiceObjectWithLineItems().getPayments())))
        .andExpect(request().asyncNotStarted()).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testAddingPaymentsToInvoiceReturnsLocationHeader() throws Exception {
    // Given
    Invoice invoice = builder.getInvoiceObjectWithLineItems();
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.anyLong())).willReturn(Optional.of(invoice));
    Invoice withPayments = builder.getInvoiceWithPayments();
    BDDMockito.given(invoiceRepository.saveAndFlush(ArgumentMatchers.any(Invoice.class))).willReturn(withPayments);

    String jsonContent = objectMapper.writeValueAsString(withPayments.getPayments());
    log.info("Uploading payments {}", jsonContent);
    // When
    MvcResult result = mockMvc
        .perform(put("/invoices/20/payments").contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonContent))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();
    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/invoices/20/payments")));

  }

  @Test
  public void testAddingPaymentsWhenInvoiceNotFoundReturnsBadRequest() throws Exception {
    // Given
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.anyLong())).willReturn(Optional.empty());
    Invoice withPayments = builder.getInvoiceWithPayments();

    String jsonContent = objectMapper.writeValueAsString(withPayments.getPayments());
    log.info("Uploading payments {}", jsonContent);
    // When
    MvcResult result = mockMvc
        .perform(put("/invoices/20/payments").contentType(MediaType.APPLICATION_JSON_VALUE).content(jsonContent))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();
    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());

  }

}
