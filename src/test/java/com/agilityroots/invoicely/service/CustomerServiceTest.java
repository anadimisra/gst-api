/**
 * 
 */
package com.agilityroots.invoicely.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.entity.Invoice;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@Import({ CustomerService.class, DummyApplicationEventPublisher.class })
public class CustomerServiceTest {

  @Autowired
  @InjectMocks
  private CustomerService customerService;

  @MockBean
  private CustomerRepository customerRepository;

  @MockBean
  private InvoiceRepository invoiceRepository;

  @MockBean
  private BranchRepository branchRepository;

  @MockBean
  private ContactRepository contactRepository;

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAddingInvoiceWhenNoCustomer() throws InterruptedException, ExecutionException {
    // Given
    BDDMockito.given(branchRepository.findAllByOwner_Id(any(Long.class), any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.emptyList()));
    StringBuilder stringBuilder = new StringBuilder("http://localhost/customers/1/invoices");

    // When
    Optional<URI> location = customerService.addInvoice(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3),
        Long.valueOf(2), stringBuilder, builder.getInvoiceObjectWithLineItems()).get();

    // Then
    assertThat(location).isEmpty();
  }

  @Test
  public void testAddingInvoices() throws InterruptedException, ExecutionException {
    // Given
    StringBuilder stringBuilder = new StringBuilder("http://localhost/invoices/");
    Customer mockCustomer = builder.getCustomerObject();
    List<Branch> branches = new ArrayList<Branch>();
    branches.add(builder.getBranchObject());
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(mockCustomer));
    BDDMockito.given(branchRepository.findAllByOwner_Id(any(Long.class))).willReturn(branches);
    BDDMockito.given(invoiceRepository.saveAndFlush(any(Invoice.class))).willReturn(builder.getSavedInvoiceObject());
    // When
    Optional<URI> result = customerService.addInvoice(Long.valueOf(1), Long.valueOf(2), Long.valueOf(3),
        Long.valueOf(2), stringBuilder, builder.getInvoiceObjectWithLineItems()).get();

    // Then
    assertThat(result.get().toString()).isEqualTo("http://localhost/invoices/20");
  }

  @Test
  public void testAddingContactWhenNoCustomer() throws InterruptedException, ExecutionException {

    // Given
    StringBuffer stringBuilder = new StringBuffer("http://localhost/customers/1/contact/");
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // When
    Optional<URI> result = customerService.addContact(Long.valueOf(1), builder.getContactObject(), stringBuilder).get();

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  public void testAddingContact() throws InterruptedException, ExecutionException {

    // Given
    StringBuffer stringBuilder = new StringBuffer("http://localhost/customers/1/contact");
    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(builder.getCustomerObject()));
    BDDMockito.given(contactRepository.save(any(Contact.class))).willReturn(builder.getContactObject());
    BDDMockito.given(customerRepository.saveAndFlush(any(Customer.class))).willReturn(builder.getCustomerWithContact());

    // When
    Optional<URI> result = customerService.addContact(Long.valueOf(1), builder.getContactObject(), stringBuilder).get();

    // Then
    assertThat(result.get().toString()).endsWith("/customers/1/contact");

  }

  @Test
  public void testAddBranchWhenNoCustomer() throws InterruptedException, ExecutionException {

    // Given
    BDDMockito.given(branchRepository.findAllByOwner_Id(any(Long.class), any(Pageable.class)))
        .willReturn(new PageImpl<>(Collections.emptyList()));

    // When
    Optional<URI> result = customerService.addBranch(Long.valueOf(1), builder.getBranchObject(), new StringBuilder())
        .get();

    // Then
    assertThat(result).isEmpty();

  }

  public void testAddingBranch() throws InterruptedException, ExecutionException {

    // Given
    StringBuilder stringBuilder = new StringBuilder("http://localhost/customers/1/branches/");
    Customer mockCustomer = builder.getCustomerObject();
    List<Branch> branches = new ArrayList<Branch>();
    Branch branch = builder.getBranchObject();
    branches.add(branch);
    mockCustomer.setBranches(branches);
    BDDMockito.given(branchRepository.findAllByOwner_Id(any(Long.class), any(Pageable.class)))
        .willReturn(new PageImpl<>(branches));
    BDDMockito.given(branchRepository.save(any(Branch.class))).willReturn(branch);
    BDDMockito.given(customerRepository.saveAndFlush(any(Customer.class))).willReturn(mockCustomer);

    // When
    Optional<URI> result = customerService.addBranch(Long.valueOf(1), branch, stringBuilder).get();

    // Then
    assertThat(result.get().toString()).endsWith("/customrs/1/branches" + String.valueOf(branch.getId()));
  }
}

@Slf4j
@Component
class DummyApplicationEventPublisher implements ApplicationEventPublisher {

  @Override
  public void publishEvent(Object event) {
    log.info("Do Nothing");
  }

}