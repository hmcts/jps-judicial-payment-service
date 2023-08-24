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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.TestIdamConfiguration;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.model.out.SittingRecordSearchResponse;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.StatusHistoryService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;

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
    private StatusHistoryService statusHistoryService;
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

        List<SittingRecord> sittingRecords = generateSittingRecords();
        List<RecordingUser> recordingUsers = generateRecordingUsers();

        when(sittingRecordService.getTotalRecordCount(isA(SittingRecordSearchRequest.class),eq(SSCS)))
            .thenReturn(Long.valueOf(sittingRecords.size()));
        when(sittingRecordService.getSittingRecords(isA(SittingRecordSearchRequest.class), eq(SSCS)))
            .thenReturn(sittingRecords);
        when(statusHistoryService.findRecordingUsers(anyString(), anyString(), anyList(), any(), any()))
            .thenReturn(recordingUsers);

        String requestJson = Resources.toString(getResource("searchSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post(
                                                 "/sitting-records/searchSittingRecords/{hmctsServiceCode}",
                                                      SSCS
                                                  )
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson)
            )
            .andDo(print())
            .andExpectAll(
                status().isOk(),
                jsonPath("$.recordingUsers").exists(),
                jsonPath("$.recordingUsers").isArray(),
                content().contentType(MediaType.APPLICATION_JSON))
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
        )).thenReturn(2L);

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

    private List<RecordingUser> generateRecordingUsers() {
        RecordingUser recUser1 = RecordingUser.builder()
            .userId("10011")
            .userName("User One")
            .build();
        RecordingUser recUser2 = RecordingUser.builder()
            .userId("10022")
            .userName("User Two")
            .build();
        return List.of(recUser1, recUser2);
    }

    @Test
    void shouldThrowSittingRecordMandatoryWhenSittingRecordMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord"))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.errors[0].fieldName").value("PathVariable"),
                jsonPath("$.errors[0].message").value("sittingRecordId is mandatory")
            );
    }

    private List<SittingRecord> generateSittingRecords() {
        long idSittingRecord = 0;
        long idStatusHistory = 0;

        StatusHistory statusHistory1 = StatusHistory.builder()
            .id(++idStatusHistory)
            .statusId(StatusId.RECORDED)
            .changedByUserId("11233")
            .changedDateTime(LocalDateTime.now())
            .changedByName("Jason Bourne")
            .build();
        SittingRecord sittingRecord1 = SittingRecord.builder()
            .sittingRecordId(++idSittingRecord)
            .accountCode("AC1")
            .am(Boolean.TRUE)
            .contractTypeId(11222L)
            .contractTypeName("Contract Type 1")
            .crownServantFlag(Boolean.TRUE)
            .epimmsId("EP1")
            .fee(10234L)
            .hmctsServiceId("HMCTS1")
            .judgeRoleTypeId("JR1")
            .judgeRoleTypeName("Judge Role Type 1")
            .londonFlag(Boolean.FALSE)
            .payrollId("PR1")
            .personalCode("PC1")
            .personalName("Personal Name")
            .pm(Boolean.TRUE)
            .regionId("EC1")
            .regionName("East Coast US1")
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(statusHistory1.getStatusId())
            .venueName("Venue Name 1")
            .createdDateTime(LocalDateTime.now().minusDays(300))
            .createdByUserId("charlie_chaplin")
            .createdByUserName("Charlie Chaplin")
            .changedDateTime(LocalDateTime.now().minusDays(270))
            .changedByUserId("buster_keaton")
            .changedByUserName("Buster Keaton")
            .build();
        sittingRecord1.setStatusHistories(List.of(statusHistory1));

        StatusHistory statusHistory2a = StatusHistory.builder()
            .id(++idStatusHistory)
            .statusId(StatusId.RECORDED)
            .changedByUserId("11244")
            .changedDateTime(LocalDateTime.now().minusDays(2))
            .changedByName("Matt Murdock")
            .build();
        StatusHistory statusHistory2b = StatusHistory.builder()
            .id(++idStatusHistory)
            .statusId(StatusId.PUBLISHED)
            .changedByUserId("11245")
            .changedDateTime(LocalDateTime.now().minusDays(1))
            .changedByName("Peter Parker")
            .build();
        StatusHistory statusHistory2c = StatusHistory.builder()
            .id(++idStatusHistory)
            .statusId(StatusId.SUBMITTED)
            .changedByUserId("11246")
            .changedDateTime(LocalDateTime.now())
            .changedByName("Stephen Strange")
            .build();
        SittingRecord sittingRecord2 = SittingRecord.builder()
            .sittingRecordId(++idSittingRecord)
            .accountCode("AC2")
            .am(Boolean.TRUE)
            .contractTypeId(11333L)
            .contractTypeName("Contract Type 2")
            .crownServantFlag(Boolean.FALSE)
            .epimmsId("EP2")
            .fee(20123L)
            .hmctsServiceId("HMCTS2")
            .judgeRoleTypeId("JR2")
            .judgeRoleTypeName("Judge Role Type 2")
            .londonFlag(Boolean.FALSE)
            .payrollId("PR2")
            .personalCode("PC2")
            .personalName("Personal Name")
            .pm(Boolean.TRUE)
            .regionId("EC2")
            .regionName("East Coast US2")
            .sittingDate(LocalDate.now().minusDays(1))
            .statusId(statusHistory2c.getStatusId())
            .venueName("Venue Name 1")
            .createdDateTime(LocalDateTime.now().minusDays(300))
            .createdByUserId("charlie_chaplin")
            .createdByUserName("Charlie Chaplin")
            .changedDateTime(LocalDateTime.now().minusDays(270))
            .changedByUserId("buster_keaton")
            .changedByUserName("Buster Keaton")
            .build();
        sittingRecord2.setStatusHistories(List.of(statusHistory2a, statusHistory2b, statusHistory2c));

        return List.of(sittingRecord1, sittingRecord2);
    }

}
