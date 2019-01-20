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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.ZoneId;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.LineItem;
import com.agilityroots.invoicely.repository.InvoiceRepository;
import com.agilityroots.invoicely.resource.assembler.CustomerResourceAssember;
import com.agilityroots.invoicely.resource.assembler.InvoiceResourceAssembler;
import com.agilityroots.invoicely.service.InvoiceService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(InvoiceController.class)
@Import({ InvoiceService.class, InvoiceResourceAssembler.class, CustomerResourceAssember.class })
public class InvoiceControllerTests {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

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
    Page<Invoice> page = new PageImpl<>(Arrays.asList(getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceRepository.findAll(ArgumentMatchers.any(Pageable.class))).willReturn(page);

    // When
    MvcResult result = mockMvc.perform(get("/invoices")).andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(1)))
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
        .willReturn(Optional.of(getInvoiceObject()));

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
    Page<Invoice> page = new PageImpl<>(Arrays.asList(getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceRepository.findByPayments_PaymentDateIsNotNull(ArgumentMatchers.any(Pageable.class)))
        .willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/paid")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices/paid")));
  }

  @Test
  public void testGetPendingInvoicesReturnsHALDocument() throws Exception {
    // Given
    Page<Invoice> page = new PageImpl<>(Arrays.asList(getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateAfter(ArgumentMatchers.any(Date.class),
        ArgumentMatchers.any(Pageable.class))).willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/pending")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices/pending")));
  }

  @Test
  public void testGetOverdueInvoicesReturnsHALDocument() throws Exception {
    // Given
    Page<Invoice> page = new PageImpl<>(Arrays.asList(getInvoiceObjectWithLineItems()));
    BDDMockito.given(invoiceRepository.findByPayments_PaymentDateIsNullAndDueDateBefore(ArgumentMatchers.any(Date.class),
        ArgumentMatchers.any(Pageable.class))).willReturn(AsyncResult.forValue(page));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/overdue")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.invoices", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].invoice_number", is(equalTo("INV-20180918"))))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items", hasSize(1)))
        .andExpect(jsonPath("$._embedded.invoices[0].line_items[0].item", is(equalTo("That Item"))))
        .andExpect(jsonPath("$._links.self.href", containsString("/invoices/overdue")));
  }

  @Test
  public void testWhenInvoiceNotFoundThenCustomerDetailsIsUnprocesseableEntity() throws Exception {
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.anyLong()))
        .willReturn(Optional.of(getInvoiceObject()));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/20/customer")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isUnprocessableEntity());
  }

  @Test
  public void testGetCustomerDetailsForInvoice() throws Exception {
    BDDMockito.given(invoiceRepository.findById(ArgumentMatchers.anyLong()))
        .willReturn(Optional.of(getInvoiceObjectWithCustomer()));

    // When
    MvcResult result = mockMvc.perform(get("/invoices/20/customer")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.pan", is(equalTo("ADBCD1234E"))));
  }

  @Test
  public void testSavingInvoice() throws Exception {
    BDDMockito.given(invoiceRepository.saveAndFlush(ArgumentMatchers.any(Invoice.class)))
        .willReturn(getInvoiceObjectWithCustomer());

    // When
    MvcResult result = mockMvc
        .perform(post("/invoices").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(getInvoiceObject())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/invoices/20")));
  }

  private Invoice getInvoiceObjectWithLineItems() {
    Invoice invoice = getInvoiceObject();
    LineItem lineItem = new LineItem();
    lineItem.setAmount(1180.00);
    lineItem.setDescription("That Service");
    lineItem.setDiscount(0.0);
    lineItem.setHsn("998313");
    lineItem.setItem("That Item");
    lineItem.setSerialNumber(1);
    lineItem.setTax(0.18);
    lineItem.setPrice(1000.00);
    invoice.setLineItems(Arrays.asList(lineItem));
    return invoice;
  }

  private Invoice getInvoiceObject() {
    Invoice invoice = new Invoice();
    invoice.setId(Long.valueOf(20));
    invoice.setInvoiceDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice.setDueDate(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setInvoiceNumber("INV-20180918");
    invoice.setPlaceOfSupply("Karnataka");
    return invoice;
  }

  private Invoice getInvoiceObjectWithCustomer() {
    Customer customer = new Customer();
    customer.setId(Long.valueOf(30));
    customer.setName("Minty And Sons Pvt. Ltd.");
    customer.setPan("ADBCD1234E");
    Invoice invoice = getInvoiceObject();
    invoice.setCustomer(customer);
    return invoice;
  }
}