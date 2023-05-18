package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.TestIdamConfiguration;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.model.out.SittingRecordSearchResponse;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.CaseWorkerService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;

@WebMvcTest(controllers = SittingRecordController.class,
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = {SecurityConfiguration.class,
                JwtGrantedAuthoritiesConverter.class})})
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class SittingRecordControllerTest {
    private static final String SSCS = "sscs";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;
    @MockBean
    private SittingRecordService sittingRecordService;
    @MockBean
    private LocationService regionService;
    @MockBean
    private JudicialUserDetailsService judicialUserDetailsService;
    @MockBean
    private CaseWorkerService caseWorkerService;


    @Test
    void shouldReturn400WhenHmctsServiceCode() throws Exception {
        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
                                                      "/sitting-records/searchSittingRecords",
                                                      SSCS
                                                  )
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson)
            ).andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors[0].fieldName").value("PathVariable"))
            .andExpect(jsonPath("$.errors[0].message").value("hmctsServiceCode is mandatory"))
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
            getResource("mandatoryFieldError.json"),
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

        assertThat(sittingRecordSearchResponse.getRecordCount()).isEqualTo(0);
        assertThat(sittingRecordSearchResponse.getSittingRecords()).isNullOrEmpty();
    }

    @Test
    void shouldReturnResponseWithSittingRecordsWhenRecordsExitsForGivenCriteria() throws Exception {
        when(sittingRecordService.getTotalRecordCount(
            isA(SittingRecordSearchRequest.class),
            eq(SSCS)
        ))
            .thenReturn(2);
        List<SittingRecord> sittingRecords = List.of(
            SittingRecord.builder()
                .sittingRecordId(1)
                .build(),
            SittingRecord.builder()
                .sittingRecordId(12)
                .build()
        );
        when(sittingRecordService.getSittingRecords(isA(SittingRecordSearchRequest.class), eq(SSCS)))
            .thenReturn(sittingRecords
            );

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

        verify(regionService).setRegionDetails(eq(SSCS), eq(sittingRecords));
        verify(judicialUserDetailsService).setJudicialUserDetails(eq(sittingRecords));
        //verify(caseWorkerService).setCaseWorkerDetails(eq(sittingRecords));
        assertThat(sittingRecordSearchResponse.getRecordCount()).isEqualTo(2);
        assertThat(sittingRecordSearchResponse.getSittingRecords()).isEqualTo(sittingRecords);
    }
}
