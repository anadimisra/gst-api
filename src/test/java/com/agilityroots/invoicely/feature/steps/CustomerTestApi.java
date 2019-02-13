/**
 * 
 */
package com.agilityroots.invoicely.feature.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Organisation;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;

import lombok.extern.slf4j.Slf4j;

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
    String customerId = getIdFromLocationHeader(customerLocation);
    assertThat(customerLocation).contains("/customers");
    this.customer = customer;
    customer.setId(Long.valueOf(customerId));
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
    assertThat(contactLocation).endsWith("/customers/" + getSavedCustomerId().toString() + "/contact");
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
    urlBuilder.append(this.customer.getBranches().get(0).getId());
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

  private Contact getBranchContact() {
    Contact result = this.customer.getBranches().get(0).getContact();
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

  /**
   * @param urlBuilder
   * @return
   */
  @SuppressWarnings("unused")
  private Contact getContactFromApi(StringBuffer urlBuilder) {
    Contact contact = getRestTemplate().getForObject(urlBuilder.toString(), Contact.class);
    assertThat(contact).isNotNull();
    return contact;
  }

  private void addBranchToCustomer(Branch branch) {
    List<Branch> branches = new ArrayList<>();
    branches.add(branch);
    this.customer.setBranches(branches);
  }

  private Long getSavedCustomerId() {
    return this.customer.getId();
  }

}
