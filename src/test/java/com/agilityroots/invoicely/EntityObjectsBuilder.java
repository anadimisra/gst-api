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

import com.agilityroots.invoicely.entity.Address;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.entity.LineItem;
import com.agilityroots.invoicely.entity.Payment;
import com.github.javafaker.Faker;

/**
 * @author anadi
 *
 */
public class EntityObjectsBuilder {

  private Faker faker = new Faker(new Locale("en-IND"));

  protected Invoice getInvoiceObjectWithLineItems() {
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
    List<LineItem> lineItems = new ArrayList<LineItem>();
    lineItems.add(lineItem);
    invoice.setLineItems(lineItems);
    return invoice;
  }

  protected Invoice getInvoiceObject() {
    Invoice invoice = new Invoice();
    invoice.setId(Long.valueOf(20));
    invoice.setInvoiceDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setPaymentTerms("NET-30");
    invoice.setDueDate(Date.from(LocalDate.now().plusDays(30).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()));
    invoice.setInvoiceNumber("INV-20180918");
    invoice.setPlaceOfSupply("Karnataka");
    return invoice;
  }

  protected Invoice getInvoiceObjectWithPayments() {
    Invoice invoice = getInvoiceObject();
    invoice.setCustomer(getCustomerObject());
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

  protected Customer getCustomerObject() {
    Customer minty = new Customer();
    minty.setId(Long.valueOf(10));
    minty.setName("Minty and Sons Private Limited");
    minty.setPan("ABCDE1234Q");
    minty.setInvoicePrefix("MNT");
    minty.setTds(0.10);
    minty.setCurrecny("INR");
    return minty;
  }

  protected Branch getBranchObject() {

    Address address = new Address();
    address.setStreetAddress(faker.address().streetAddress());
    address.setArea(faker.address().streetName());
    address.setCity(faker.address().city());
    address.setState(faker.address().state());
    address.setPincode(faker.address().zipCode());

    Branch branch = new Branch();
    branch.setId(Long.valueOf(20));
    branch.setBranchName("Main Branch");
    branch.setGstin(RandomStringUtils.randomAlphabetic(15));
    branch.setSez(Boolean.FALSE);
    branch.setAddress(address);
    return branch;
  }

  protected Contact getContactObject() {

    Contact contact = new Contact();
    contact.setId(Long.valueOf(30));
    contact.setName(faker.name().fullName());
    contact.setEmail(faker.internet().emailAddress());
    contact.setPhone("8067601867");
    return contact;
  }
}
