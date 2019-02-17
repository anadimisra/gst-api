/**
 * 
 */
package com.agilityroots.invoicely;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.format.datetime.DateFormatter;

import com.agilityroots.invoicely.entity.Address;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Company;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.LineItem;
import com.agilityroots.invoicely.entity.Payment;
import com.agilityroots.invoicely.http.payload.InvoiceHttpPayload;
import com.github.javafaker.Faker;

/**
 * @author anadi
 *
 */
public class EntityObjectsBuilder {

  private Faker faker = new Faker(new Locale("en-IND"));

  public InvoiceHttpPayload getValidInvoicePayloadObject() {
    InvoiceHttpPayload payload = new InvoiceHttpPayload();
    payload.setInvoice(getInvoiceObjectWithLineItems());
    payload.setBilledFrom(Long.valueOf(1));
    payload.setBilledTo(Long.valueOf(2));
    payload.setShippedTo(Long.valueOf(2));
    return payload;
  }

  public Invoice getInvoiceObjectWithLineItems() {
    Invoice invoice = getInvoiceObject();
    LineItem item1 = new LineItem();
    item1.setSerialNumber(1);
    item1.setAmount(1180.00);
    item1.setDescription("That Service");
    item1.setDiscount(0.0);
    item1.setHsn("998313");
    item1.setItem("That Item");
    item1.setSerialNumber(1);
    item1.setTax(0.18);
    item1.setPrice(1000.00);
    LineItem item2 = new LineItem();
    item2.setSerialNumber(2);
    item2.setAmount(1180.00);
    item2.setDescription("Another Service");
    item2.setDiscount(0.0);
    item2.setHsn("998313");
    item2.setItem("Another Item");
    item2.setTax(0.18);
    item2.setPrice(1000.00);
    List<LineItem> lineItems = new ArrayList<LineItem>();
    lineItems.add(item1);
    lineItems.add(item2);
    invoice.setLineItems(lineItems);
    return invoice;
  }

  public Invoice getInvoiceObject() {
    Invoice invoice = new Invoice();
    invoice.setId(Long.valueOf(20));
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
    Invoice invoice = getInvoiceObjectWithCustomer();
    Payment payment = new Payment();
    payment.setAdjustmentName("TDS");
    payment.setAdjustmentValue(100.00);
    payment.setAmount(1080.00);
    payment.setPaymentDate(invoice.getDueDate());
    List<Payment> payments = new ArrayList<>();
    payments.add(payment);
    invoice.setPayments(payments);
    return invoice;
  }

  public Invoice getInvoiceObjectWithCustomer() {
    Invoice invoice = getInvoiceObjectWithLineItems();
    invoice.setCustomer(getCustomerObject());
    return invoice;
  }

  public Invoice getSavedInvoiceObject() {
    Invoice invoice = getInvoiceObjectWithCustomer();
    invoice.setBilledFrom(getBranchWithContactObject());
    invoice.setShippedTo(getBranchWithContactObject());
    invoice.setBilledTo(getBranchWithContactObject());
    return invoice;
  }

  public Customer getCustomerObject() {
    Customer minty = new Customer();
    minty.setId(Long.valueOf(10));
    minty.setName("Minty and Sons Private Limited");
    minty.setPan("ABCDE1234Q");
    minty.setInvoicePrefix("MNT");
    minty.setTds(0.10);
    minty.setCurrecny("INR");
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
    branch.setId(Long.valueOf(20));
    branch.setBranchName(RandomStringUtils.randomAlphabetic(5) + " Branch");
    branch.setGstin(RandomStringUtils.randomAlphanumeric(15).toUpperCase());
    branch.setSez(Boolean.FALSE);
    branch.setAddress(address);
    return branch;
  }

  public Contact getContactObject() {
    Contact contact = new Contact();
    contact.setId(Long.valueOf(30));
    contact.setName(faker.name().fullName());
    contact.setEmail(faker.internet().emailAddress());
    contact.setPhone("8067601867");
    return contact;
  }

  public Company getCompanyObject() {
    Company company = new Company();
    company.setName("Ruchi And Sons Pvt. Ltd.");
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
