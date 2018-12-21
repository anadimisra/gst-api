/**
 * 
 */
package com.agilityroots.invoicely.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.entity.Customer;
import com.agilityroots.invoicely.repository.CustomerRepository;
import com.agilityroots.invoicely.service.CustomerAsyncService;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@Import({ CustomerAsyncService.class })
public class CustomerServiceTests {

  @Autowired
  private CustomerAsyncService customerService;

  @MockBean
  private CustomerRepository customerRepository;

  private Customer customer;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    customer = new Customer();
    customer.setName("Minty & SOns Pvt. Ltd.");
    customer.setPan(RandomStringUtils.randomAlphanumeric(10));
  }

  /**
   * Test method for
   * {@link com.agilityroots.invoicely.service.CustomerAsyncService#getContact(java.lang.Long)}.
   * 
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Test
  public void testGetContactWhenNoneExistGivesEmptyOptional() throws InterruptedException, ExecutionException {

    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));

    Optional<Contact> contact = customerService.getContact(Long.valueOf(1)).get();

    assertThat(contact).isNotNull();
    assertThat(contact).isEmpty();
    assertThat(contact.map(Contact::getName).orElse("None")).isEqualTo("None");
    assertThat(contact.map(Contact::getPhone).orElse("None")).isEqualTo("None");
  }

  /**
   * Test method for
   * {@link com.agilityroots.invoicely.service.CustomerAsyncService#getContact(java.lang.Long)}.
   * 
   * @throws ExecutionException
   * @throws InterruptedException
   */
  @Test
  public void testGetContactWithExistGivesOptional() throws InterruptedException, ExecutionException {

    Contact contact = new Contact();
    contact.setEmail("foo@bar.com");
    contact.setName("The Name");
    String phoneNumber = RandomStringUtils.randomNumeric(10);
    contact.setPhone(phoneNumber);
    customer.setContact(contact);

    BDDMockito.given(customerRepository.findById(any(Long.class))).willReturn(Optional.of(customer));

    Optional<Contact> result = customerService.getContact(Long.valueOf(1)).get();

    assertThat(result.get()).isNotNull();
    assertThat(result.map(Contact::getName).orElse("None")).isEqualTo("The Name");
    assertThat(result.map(Contact::getPhone).orElse("None")).isEqualTo(phoneNumber);
  }
}
