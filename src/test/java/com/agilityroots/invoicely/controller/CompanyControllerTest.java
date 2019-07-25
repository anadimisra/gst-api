/**
 * 
 */
package com.agilityroots.invoicely.controller;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Branch;
import com.agilityroots.invoicely.service.CompanyService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(CompanyController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({ CompanyController.class })
public class CompanyControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private CompanyService companyService;

  private EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @Test
  public void testSavingBranchReturnsLocationHeader() throws Exception {

    // Given
    Branch branch = builder.getBranchObject();
    URI location = URI.create("http://localhost/branches/" + branch.getId().toString());
    BDDMockito.given(companyService.addBranch(any(Long.class), any(Branch.class), any(StringBuffer.class)))
        .willReturn(AsyncResult.forValue(Optional.of(location)));

    // When
    MvcResult result = mockMvc
        .perform(put("/companies/20/branches").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(branch)))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/branches/" + branch.getId().toString())));
  }
}
