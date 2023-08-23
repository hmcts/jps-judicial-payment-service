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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.hmcts.reform.jps.TestIdamConfiguration;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = {SittingRecordDeleteController.class},
    excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = {SecurityConfiguration.class,
                JwtGrantedAuthoritiesConverter.class})})
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class SittingRecordDeleteControllerTest {

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

    @Test
    @WithMockUser(authorities = {"jps-recorder"})
    void shouldDeleteSittingRecordWhenSittingRecordPresent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/sittingRecord/{sittingRecordId}", 2))
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
