package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.TestIdamConfiguration;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.model.out.SittingRecordSearchResponse;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;

@WebMvcTest(controllers = {SittingRecordController.class, SittingRecordDeleteController.class},
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = {SecurityConfiguration.class,
                JwtGrantedAuthoritiesConverter.class})})
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class SittingRecordControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordControllerTest.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SittingRecordService sittingRecordService;
    @MockBean
    private LocationService regionService;
    @MockBean
    private JudicialUserDetailsService judicialUserDetailsService;

    private static final String SSCS = "sscs";

    @Test
    void shouldReturn400WhenHmctsServiceCode() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
                                                      "/sitting-records/searchSittingRecords"
                                                  )
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson)
            ).andDo(print())
            .andExpectAll(status().isBadRequest(),
                          content().contentType(MediaType.APPLICATION_JSON),
                          jsonPath("$.errors[0].fieldName").value("PathVariable"),
                          jsonPath("$.errors[0].message").value("hmctsServiceCode is mandatory")
            )
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsByteArray()).isNotNull();
    }

    @Test
    void shouldReturn400ResponseWhenMandatoryFieldsMissing() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecordsWithoutMandatoryFields.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
                                                      "/sitting-records/searchSittingRecords/{hmctsServiceCode}",
                                                      SSCS
                                                  )
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson)
            ).andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        ModelValidationError actualErrors = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            ModelValidationError.class
        );

        ModelValidationError expectedErrors = objectMapper.readValue(
            getResource("searchMandatoryFieldError.json"),
            ModelValidationError.class
        );


        assertThat(actualErrors.getErrors()).isNotEmpty();
        assertThat(actualErrors.getErrors())
            .containsAll(expectedErrors.getErrors());
    }

    @Test
    void shouldReturnResponseWithSittingRecordsWhenNoRecordsForGivenCriteria() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
                                                      "/sitting-records/searchSittingRecords/{hmctsServiceCode}",
                                                      SSCS
                                                  )
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson)
            ).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        SittingRecordSearchResponse sittingRecordSearchResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            SittingRecordSearchResponse.class
        );

        assertThat(sittingRecordSearchResponse.getRecordCount()).isZero();
        assertThat(sittingRecordSearchResponse.getSittingRecords()).isNullOrEmpty();
    }

    @Test
    void shouldReturnResponseWithSittingRecordsWhenRecordsExitsForGivenCriteria() throws Exception {

        StatusHistory statusHistory1 = StatusHistory.builder()
            .id(1L)
            .statusId(StatusId.RECORDED.name())
            .changeByUserId("11233")
            .changeDateTime(LocalDateTime.now())
            .changeByName("Jason Bourne")
            .build();
        SittingRecord sittingRecord1 = SittingRecord.builder()
            .sittingRecordId(1L)
            .statusId(statusHistory1.getStatusId())
            .createdDateTime(statusHistory1.getChangeDateTime())
            .createdByUserId(statusHistory1.getChangeByUserId())
            .createdByUserName(statusHistory1.getChangeByName())
            .changeDateTime(statusHistory1.getChangeDateTime())
            .changeByUserId(statusHistory1.getChangeByUserId())
            .changeByUserName(statusHistory1.getChangeByName())
            .build();
        sittingRecord1.setStatusHistories(List.of(statusHistory1));

        StatusHistory statusHistory2a = StatusHistory.builder()
            .statusId(StatusId.RECORDED.name())
            .changeByUserId("11244")
            .changeDateTime(LocalDateTime.now().minusDays(2))
            .changeByName("Matt Murdock")
            .build();
        StatusHistory statusHistory2b = StatusHistory.builder()
            .statusId(StatusId.PUBLISHED.name())
            .changeByUserId("11245")
            .changeDateTime(LocalDateTime.now().minusDays(1))
            .changeByName("Peter Parker")
            .build();
        StatusHistory statusHistory2c = StatusHistory.builder()
            .statusId(StatusId.SUBMITTED.name())
            .changeByUserId("11246")
            .changeDateTime(LocalDateTime.now())
            .changeByName("Stephen Strange")
            .build();
        SittingRecord sittingRecord2 = SittingRecord.builder()
            .sittingRecordId(2L)
            .statusId(statusHistory2c.getStatusId())
            .createdDateTime(statusHistory2a.getChangeDateTime())
            .createdByUserId(statusHistory2a.getChangeByUserId())
            .createdByUserName(statusHistory2a.getChangeByName())
            .changeDateTime(statusHistory2c.getChangeDateTime())
            .changeByUserId(statusHistory2c.getChangeByUserId())
            .changeByUserName(statusHistory2c.getChangeByName())
            .build();
        sittingRecord2.setStatusHistories(List.of(statusHistory2a, statusHistory2b, statusHistory2c));

        List<SittingRecord> sittingRecords = List.of(sittingRecord1, sittingRecord2);

        when(sittingRecordService.getTotalRecordCount(isA(SittingRecordSearchRequest.class),eq(SSCS)))
            .thenReturn(sittingRecords.size());
        when(sittingRecordService.getSittingRecords(isA(SittingRecordSearchRequest.class), eq(SSCS)))
            .thenReturn(sittingRecords);

        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
                                                      "/sitting-records/searchSittingRecords/{hmctsServiceCode}",
                                                      SSCS
                                                  )
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson)
            ).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        SittingRecordSearchResponse sittingRecordSearchResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            SittingRecordSearchResponse.class
        );
        LOGGER.debug("sittingRecordSearchResponse:{}", sittingRecordSearchResponse);
        LOGGER.debug("sittingRecordSearchResponse.getSittingRecords():{}",
                     sittingRecordSearchResponse.getSittingRecords());

        verify(regionService).setRegionName(SSCS, sittingRecords);
        verify(judicialUserDetailsService).setJudicialUserDetails(sittingRecords);
        assertThat(sittingRecordSearchResponse.getRecordCount()).isEqualTo(sittingRecords.size());
        assertEquals(sittingRecordSearchResponse.getSittingRecords().size(), sittingRecords.size());
        if (sittingRecordSearchResponse.getSittingRecords().size() > 1) {
            assertEquals(sittingRecordSearchResponse.getSittingRecords().get(1), sittingRecords.get(1));
        }
    }

    @Test
    void shouldReturnResponseWithSittingRecordsCountButNoSittingRecordsForRequestedOffset() throws Exception {
        when(sittingRecordService.getTotalRecordCount(
            isA(SittingRecordSearchRequest.class),
            eq(SSCS)
        ))
            .thenReturn(2);

        List<SittingRecord> sittingRecords = Collections.emptyList();
        when(sittingRecordService.getSittingRecords(isA(SittingRecordSearchRequest.class), eq(SSCS)))
            .thenReturn(sittingRecords);

        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
                                                      "/sitting-records/searchSittingRecords/{hmctsServiceCode}",
                                                      SSCS
                                                  )
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson)
            ).andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        SittingRecordSearchResponse sittingRecordSearchResponse = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            SittingRecordSearchResponse.class
        );
        assertThat(sittingRecordSearchResponse.getRecordCount()).isEqualTo(2);
        assertEquals(0, sittingRecordSearchResponse.getSittingRecords().size());
        verify(regionService, never()).setRegionName(SSCS, sittingRecords);
        verify(judicialUserDetailsService, never()).setJudicialUserDetails(sittingRecords);
    }

    @Test
    @WithMockUser(authorities = {JPS_RECORDER})
    void shouldDeleteSittingRecordWhenSittingRecordPresent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 2))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {JPS_RECORDER})
    void shouldThrowSittingRecordNotFoundWhenSittingRecordNotFoundInDb() throws Exception {
        doThrow(new ResourceNotFoundException("SITTING_RECORD_ID_NOT_FOUND"))
            .when(sittingRecordService)
            .deleteSittingRecord(anyLong());

        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 2000))
            .andDo(print())
            .andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value("NOT_FOUND"),
                jsonPath("$.errors").value("SITTING_RECORD_ID_NOT_FOUND")
            );
    }
}
