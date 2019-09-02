package com.agilityroots.invoicely;

import com.agilityroots.invoicely.entity.*;
import com.agilityroots.invoicely.http.payload.InvoiceHttpPayload;
import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.format.datetime.DateFormatter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author anadi
 *
 */
public class EntityObjectsBuilder {

  private Faker faker = new Faker(new Locale("en-IND"));

  public InvoiceHttpPayload getValidInvoicePayloadObject() {
    InvoiceHttpPayload payload = new InvoiceHttpPayload();
    payload.setInvoice(getInvoiceObjectWithLineItems());
    payload.setBilledFrom(1L);
    payload.setBilledTo(2L);
    payload.setShippedTo(2L);
    return payload;
  }

  public Invoice getInvoiceObjectWithLineItems() {
    Invoice invoice = getInvoiceObject();
    LineItem item1 = new LineItem();
    item1.setSerialNumber(1);
    item1.setDescription("That Service");
    item1.setDiscount(0.0);
    item1.setHsn("998313");
    item1.setItem("That Item");
    item1.setSerialNumber(1);
    item1.setTax(0.18);
    item1.setPrice(1000.00);
    LineItem item2 = new LineItem();
    item2.setSerialNumber(2);
    item2.setDescription("Another Service");
    item2.setDiscount(0.0);
    item2.setHsn("998313");
    item2.setItem("Another Item");
    item2.setTax(0.18);
    item2.setPrice(1000.00);
    Set<LineItem> lineItems = new HashSet<>(2);
    lineItems.add(item1);
    lineItems.add(item2);
    invoice.setLineItems(lineItems);
    return invoice;
  }

  private Invoice getInvoiceObject() {
    Invoice invoice = new Invoice();
    invoice.setId(20L);
    Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant());
    invoice.setInvoiceDate(today);
    invoice.setPaymentTerms("NET-30");
    invoice.setDueDate(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    DateFormatter formatter = new DateFormatter("yyyyMMdd");
    invoice.setInvoiceNumber(formatter.print(today, new Locale("en", "IN")));
    invoice.setPlaceOfSupply("Karnataka");
    return invoice;
  }

  public Invoice getInvoiceWithPayments() {
    Invoice invoice = getInvoiceObjectWithLineItems();
    Payment payment = new Payment();
    payment.setAdjustmentName("TDS");
    payment.setAdjustmentValue(100.00);
    payment.setAmount(1080.00);
    payment.setPaymentDate(invoice.getDueDate());
    Set<Payment> payments = new HashSet<>(1);
    payments.add(payment);
    invoice.setPayments(payments);
    return invoice;
  }

  public Invoice getSavedInvoiceObject() {
    Invoice invoice = getInvoiceObjectWithLineItems();
    invoice.setBilledFrom(getBranchWithContactObject());
    invoice.setShippedTo(getBranchWithContactObject());
    invoice.setBilledTo(getBranchWithContactObject());
    return invoice;
  }

  public Customer getCustomerObject() {
    Customer minty = new Customer();
    minty.setOrganisationId(RandomStringUtils.randomAlphanumeric(8));
    minty.setName("Minty and Sons Private Limited");
    minty.setId(10L);
    minty.setInvoicePrefix("MNT");
    return minty;
  }

  public Customer getCustomerWithContact() {
    Customer customer = getCustomerObject();
    customer.setContact(getContactObject());
    return customer;
  }

  public Branch getBranchObject() {
    Address address = new Address();
    address.setStreetAddress(faker.address().streetAddress());
    address.setArea(faker.address().streetName());
    address.setCity(faker.address().city());
    address.setState(faker.address().state());
    address.setPincode(faker.address().zipCode());
    Branch branch = new Branch();
    branch.setId(10L);
    branch.setBranchName(RandomStringUtils.randomAlphabetic(5) + " Branch");
    branch.setGstin(RandomStringUtils.randomAlphanumeric(15).toUpperCase());
    branch.setSez(Boolean.FALSE);
    branch.setAddress(address);
    return branch;
  }

  public Contact getContactObject() {
    Contact contact = new Contact();
    contact.setId(30L);
    contact.setName(faker.name().fullName());
    contact.setEmail(faker.internet().emailAddress());
    contact.setPhone("8067601867");
    return contact;
  }

  public Company getCompanyObject() {
    Company company = new Company();
    company.setName("Ruchi And Sons Pvt. Ltd.");
    company.setOrganisationId(RandomStringUtils.randomAlphanumeric(8));
    company.setCin(RandomStringUtils.randomAlphanumeric(21));
    company.setPan(RandomStringUtils.randomAlphanumeric(10));
    company.setTan(RandomStringUtils.randomAlphabetic(10));
    return company;
  }

  public Branch getBranchWithContactObject() {
    Branch branch = getBranchObject();
    branch.setContact(getContactObject());
    return branch;
  }
}