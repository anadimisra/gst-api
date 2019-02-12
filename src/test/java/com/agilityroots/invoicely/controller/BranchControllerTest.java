/**
 * 
 */
package com.agilityroots.invoicely.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.agilityroots.invoicely.EntityObjectsBuilder;
import com.agilityroots.invoicely.entity.Contact;
import com.agilityroots.invoicely.resource.assembler.BranchResourceAssembler;
import com.agilityroots.invoicely.service.BranchService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author anadi
 *
 */
@RunWith(SpringRunner.class)
@WebMvcTest(BranchController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
@Import({ BranchService.class, BranchResourceAssembler.class })
public class BranchControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private BranchService branchService;

  EntityObjectsBuilder builder = new EntityObjectsBuilder();

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    JacksonTester.initFields(this, objectMapper);
  }

  @Test
  public void testGetBranchWhenNoneGivesNotFound() throws Exception {

    // Given
    BDDMockito.given(branchService.getBranch(any(Long.class))).willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc.perform(get("/branches/1")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testGetBranchGivesHALDocument() throws Exception {

    // Given
    BDDMockito.given(branchService.getBranch(any(Long.class)))
        .willReturn(AsyncResult.forValue(Optional.of(builder.getBranchWithContactObject())));

    // When
    MvcResult result = mockMvc.perform(get("/branches/10")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.branch_name", endsWith(" Branch"))).andExpect(jsonPath("$.contact", is(notNullValue())));

  }

  @Test
  public void testAddingContactToBranchThatDoesNotExist() throws Exception {

    // Given
    BDDMockito.given(branchService.addContact(any(Long.class), any(Contact.class), any(URI.class)))
        .willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc
        .perform(put("/branches/1/contact").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getBranchWithContactObject().getContact())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGettingContactToBranchThatDoesNotExist() throws Exception {

    // Given
    BDDMockito.given(branchService.getBranchContact(any(Long.class)))
        .willReturn(AsyncResult.forValue(Optional.empty()));

    // When
    MvcResult result = mockMvc.perform(get("/branches/1/contact")).andExpect(request().asyncStarted()).andDo(print())
        .andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testAddingContactToBranch() throws Exception {

    // Given
    BDDMockito.given(branchService.addContact(any(Long.class), any(Contact.class), any(URI.class)))
        .willReturn(AsyncResult.forValue(Optional.of(URI.create("http://locahost/branches/1/contact/20"))));

    // When
    MvcResult result = mockMvc
        .perform(put("/branches/1/contact").contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(builder.getBranchWithContactObject().getContact())))
        .andExpect(request().asyncStarted()).andDo(print()).andReturn();

    // Then
    mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isCreated());
    assertThat(result.getResponse().getHeader("Location")).isEqualTo("http://locahost/branches/1/contact/20");
  }

}
