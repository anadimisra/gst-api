/**
 *
 */
package com.agilityroots.invoicely.feature.steps;

import com.agilityroots.invoicely.entity.*;
import com.agilityroots.invoicely.http.payload.InvoiceHttpPayload;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author anadi
 *
 */
@Slf4j
@Component
public class CustomerTestApi extends TestApi {

  private Customer customer;

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private ContactRepository contactRepository;

  @Autowired
  private BranchRepository branchRepository;

  public CustomerTestApi() {
    this.customer = new Customer();
  }

  public void addCustomer(Customer customer) {
    log.debug("action: POST | url: /customers | data: {}", customer);
    ResponseEntity<Object> result = getRestTemplate().postForEntity("/customers", customer, Object.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String customerLocation = result.getHeaders().getLocation().toString();
    assertThat(customerLocation).contains("/customers");
    Customer getEntity = getCustomer(customerLocation);
    this.customer = getEntity;
    this.customer.setId(Long.valueOf(getIdFromLocationHeader(customerLocation)));
  }

  public void addContactToCustomer(Contact contact) {
    StringBuffer urlBuilder = new StringBuffer("/customers/");
    urlBuilder.append(getSavedCustomerId().toString());
    urlBuilder.append("/contact");
    log.debug("action: PUT | url: {} | data: {}", urlBuilder.toString(), contact);
    ResponseEntity<Object> response = getRestTemplate().exchange(urlBuilder.toString(), HttpMethod.PUT,
        new HttpEntity<Contact>(contact), Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String contactLocation = response.getHeaders().getLocation().toString();
    Contact getEntity = getCustomerContact(contactLocation);
    assertThat(contactLocation).endsWith("/customers/" + getSavedCustomerId().toString() + "/contact");
    this.customer.setContact(getEntity);
  }

  public void addBranch(Branch branch) {
    StringBuffer urlBuilder = new StringBuffer("/customers/");
    urlBuilder.append(getSavedCustomerId().toString());
    urlBuilder.append("/branches");
    log.debug("action: PUT | url: {} | data: {}", urlBuilder.toString(), branch);
    ResponseEntity<Object> response = getRestTemplate().exchange(urlBuilder.toString(), HttpMethod.PUT,
        new HttpEntity<Branch>(branch), Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    String branchLocation = response.getHeaders().getLocation().toString();
    branch.setId(Long.valueOf(getIdFromLocationHeader(branchLocation)));
    addBranchToCustomer(branch);
  }

  public void addContactToBranch(Contact contact) {
    StringBuffer urlBuilder = new StringBuffer("/branches/");
    urlBuilder.append(this.customer.getBranches().stream().findFirst().get().getId());
    urlBuilder.append("/contact");
    log.debug("action: PUT | url: {} | data: {}", urlBuilder.toString(), contact);
    ResponseEntity<Object> response = getRestTemplate().exchange(urlBuilder.toString(), HttpMethod.PUT,
        new HttpEntity<Contact>(contact), Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getHeaders().getLocation().toString()).endsWith(urlBuilder.toString());
  }

  public void updateCustomerContactEmail(String contactEmail) {
    Contact contact = this.getCustomerContact();
    contact.setEmail(contactEmail);
    addContactToCustomer(contact);
  }

  public void updateBranchContactEmail(String contactEmail) {
    Contact contact = getBranchContact();
    contact.setEmail(contactEmail);
    addContactToBranch(contact);
  }

  public void addCustomerWithContact(Customer customer) {
    log.debug("action: DB SAVE | Customer {}", customer);
    Contact contact = contactRepository.save(customer.getContact());
    customer.setContact(contact);
    customerRepository.saveAndFlush(customer);
    this.customer = customer;
  }

  public void addBranchWithContact(Branch branch) {
    log.debug("action: DB SAVE | Branch {}", branch);
    Contact contact = contactRepository.save(branch.getContact());
    branch.setContact(contact);
    Organisation owner = customerRepository.findById(getSavedCustomerId()).get();
    branch.setOwner(owner);
    branch = branchRepository.saveAndFlush(branch);
    addBranchToCustomer(branch);
  }

  public void addInvoice(Invoice invoice, Branch billedFrom) {
    billedFrom = branchRepository.saveAndFlush(billedFrom);
    InvoiceHttpPayload payload = new InvoiceHttpPayload();
    payload.setInvoice(invoice);
    payload.setBilledTo(this.customer.getBranches().stream().findFirst().get().getId());
    payload.setShippedTo(this.customer.getBranches().stream().findFirst().get().getId());
    payload.setBilledFrom(billedFrom.getId());
    StringBuffer urlBuilder = new StringBuffer("/customers/");
    urlBuilder.append(this.customer.getId());
    urlBuilder.append("/invoices");
    ResponseEntity<Object> result = getRestTemplate().exchange(urlBuilder.toString(), HttpMethod.PUT,
        new HttpEntity<InvoiceHttpPayload>(payload), Object.class);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(result.getHeaders().getLocation().toString()).contains("/invoices");
  }

  private Contact getBranchContact() {
    Contact result = this.customer.getBranches().stream().findFirst().get().getContact();
    return verifyAndGetContact(result);
  }

  private Contact getCustomerContact() {
    Contact result = this.customer.getContact();
    return verifyAndGetContact(result);
  }

  /**
   * @param result
   * @return
   */
  private Contact verifyAndGetContact(Contact result) {
    assertThat(result).isNotNull();
    assertThat(result.getName()).isNotEmpty();
    return result;
  }

  private Customer getCustomer(String location) {
    ResponseEntity<Resource<Customer>> resource = getRestTemplate().exchange(location, HttpMethod.GET, null,
        new ParameterizedTypeReference<Resource<Customer>>() {
        });
    assertThat(resource.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resource.getBody()).isNotNull();
    Customer customer = resource.getBody().getContent();
    assertThat(customer).isNotNull();
    return customer;
  }

  private Contact getCustomerContact(String location) {
    ResponseEntity<Resource<Contact>> resource = getRestTemplate().exchange(location, HttpMethod.GET, null,
        new ParameterizedTypeReference<Resource<Contact>>() {
        });
    assertThat(resource.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(resource.getBody()).isNotNull();
    Contact contact = resource.getBody().getContent();
    assertThat(contact).isNotNull();
    return contact;
  }

  private void addBranchToCustomer(Branch branch) {
    Set<Branch> branches = new HashSet<>();
    branches.add(branch);
    this.customer.setBranches(branches);
  }

  protected Long getSavedCustomerId() {
    return this.customer.getId();
  }

  public String getCustomerInvoicesJson(String invoicePathElement) {
    StringBuffer location = new StringBuffer("/customers/");
    location.append(this.customer.getId());
    location.append("/invoices/");
    location.append(invoicePathElement);
    ResponseEntity<String> invoicesJson = getRestTemplate().exchange(location.toString(), HttpMethod.GET, null,
        String.class);
    assertThat(invoicesJson.getStatusCode()).isEqualTo(HttpStatus.OK);
    return invoicesJson.getBody();
  }

}
