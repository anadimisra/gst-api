package com.agilityroots.invoicely.service;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.repository.BranchRepository;
import com.agilityroots.invoicely.repository.CompanyRepository;
import com.agilityroots.invoicely.repository.InvoiceRepository;
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

import java.net.URI;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

@RunWith(SpringRunner.class)
@Import({CompanyService.class})
public class CompanyServiceTest {

  @MockBean
  private CompanyRepository companyRepository;

  @MockBean
  private BranchRepository branchRepository;

  @MockBean
  private InvoiceRepository invoiceRepository;

  @Autowired
  @InjectMocks
  private CompanyService companyService;

  private EntityObjectsBuilder builder;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    builder = new EntityObjectsBuilder();
  }

  @Test
  public void testNullableURIOptionalIsReturnedForNonExistentCompany() throws Exception {
    //Given
    BDDMockito.given(companyRepository.findById(anyLong())).willReturn(Optional.empty());
    //When
    Optional<URI> result = companyService.addBranch(1L, builder.getBranchObject(), new StringBuffer()).get();
    //Then
    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void testURILocationIsReturnedWhenBranchIsAdded() throws Exception {

    //Given
    BDDMockito.given(companyRepository.findById(anyLong())).willReturn(Optional.of(builder.getCompanyObject()));
    Branch branch = builder.getBranchObject();
    BDDMockito.given(branchRepository.saveAndFlush(any(Branch.class))).willReturn(branch);

    //When
    StringBuffer location = new StringBuffer("http://foo/");
    Optional<URI> result = companyService.addBranch(1L, builder.getBranchObject(), location).get();

    //Then
    assertThat(result.isPresent()).isTrue();
    assertThat(result.get()).isEqualTo(URI.create("http://foo/10"));

  }
}
