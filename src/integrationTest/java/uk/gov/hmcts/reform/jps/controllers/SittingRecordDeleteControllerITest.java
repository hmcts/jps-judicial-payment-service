package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.jps.BaseTest.ADD_SITTING_RECORD_STATUS_HISTORY;
import static uk.gov.hmcts.reform.jps.BaseTest.DELETE_SITTING_RECORD_STATUS_HISTORY;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
class SittingRecordDeleteControllerITest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private UserInfo userInfo;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SittingRecordRepository recordRepository;

    @Test
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {"jps-recorder"})
    void shouldDeleteSittingRecordWhenSittingRecordPresentRecorder() throws Exception {
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getRoles()).thenReturn(List.of("jps-recorder"));
        when(userInfo.getUid()).thenReturn("d139a314-eb40-45f4-9e7a-9e13f143cc3a");
        when(userInfo.getName()).thenReturn("Joe Bloggs");

        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 3))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {"jps-submitter"})
    void shouldDeleteSittingRecordWhenSittingRecordPresentSubmitter() throws Exception {
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getRoles()).thenReturn(List.of("jps-submitter"));
        when(userInfo.getUid()).thenReturn("d139a314-eb40-45f4-9e7a-9e13f143cc3a");
        when(userInfo.getName()).thenReturn("Joe Bloggs");

        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 2))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @Sql(scripts = {DELETE_SITTING_RECORD_STATUS_HISTORY, ADD_SITTING_RECORD_STATUS_HISTORY})
    @WithMockUser(authorities = {"jps-admin"})
    void shouldDeleteSittingRecordWhenSittingRecordPresentAdmin() throws Exception {
        when(securityUtils.getUserInfo()).thenReturn(userInfo);
        when(userInfo.getRoles()).thenReturn(List.of("jps-admin"));
        when(userInfo.getUid()).thenReturn("d139a314-eb40-45f4-9e7a-9e13f143cc3a");
        when(userInfo.getName()).thenReturn("Joe Bloggs");

        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 4))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"jps-recorder"})
    void shouldThrowSittingRecordMandatoryWhenSittingRecordMissing() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord"))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(),
                jsonPath("$.errors[0].fieldName").value("PathVariable"),
                jsonPath("$.errors[0].message").value("sittingRecordId is mandatory")
            );
    }


    @Test
    @WithMockUser(authorities = {"jps-recorder"})
    void shouldThrowSittingRecordNotFoundWhenSittingRecordNotFoundInDb() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 2000))
            .andDo(print())
            .andExpectAll(
                status().isNotFound(),
                jsonPath("$.status").value("NOT_FOUND"),
                jsonPath("$.errors").value("Sitting Record ID Not Found")
            );
    }

}
