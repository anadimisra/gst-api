/**
 * 
 */
package com.agilityroots.invoicely.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import java.net.URI;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.ContactRepository;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@Import({ BranchService.class })
public class BranchServiceTest {

  @MockBean
  private BranchRepository branchRepository;

  @MockBean
  private ContactRepository contactRepository;

  @Autowired
  @InjectMocks
  private BranchService branchService;

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAddingContactWhenBranchDoesNotExist() throws InterruptedException, ExecutionException {

    // Given
    BDDMockito.given(branchRepository.findById(any(Long.class))).willReturn(Optional.empty());

    // When
    Optional<URI> result = branchService
        .addContact(any(Long.class), builder.getContactObject(), URI.create("http://localhost/branches/1/contact"))
        .get();

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  public void testAddingContactToBranch() throws InterruptedException, ExecutionException {

    // Given
    BDDMockito.given(branchRepository.findById(any(Long.class))).willReturn(Optional.of(builder.getBranchObject()));

    // When
    Optional<URI> result = branchService
        .addContact(any(Long.class), builder.getContactObject(), URI.create("http://localhost/branches/1/contact"))
        .get();

    // Then
    assertThat(result.get().toString()).endsWith("/branches/1/contact");
  }
}
