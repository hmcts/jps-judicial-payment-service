package uk.gov.hmcts.reform.jps.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/wiremock-stubs")
@ActiveProfiles("itest")
public class RecordSittingRecordsControllerITest {
    private static final String TEST_SERVICE = "BBA3";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = {"jps-recorder", "jps-submitter"})
    void shouldRecordSittingRecordsWhenAllDataIsPresent() throws Exception {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = {"jps-publisher", "jps-admin"})
    void shouldReturnUnauthorizedStatusWhenUserIsUnauthorized() throws Exception {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = {"jps-recorder", "jps-submitter"})
    void shouldReturn400WhenHmctsServiceCode() throws Exception {
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(post("/recordSittingRecords")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(status().isBadRequest(),
                          content().contentType(MediaType.APPLICATION_JSON),
                          jsonPath("$.errors[0].fieldName").value("PathVariable"),
                          jsonPath("$.errors[0].message").value("hmctsServiceCode is mandatory")
            )
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsByteArray()).isNotNull();
    }

    @Test
    void shouldReturn400ResponseWhenRequestIsEmpty() throws Exception {
        String requestJson = "{}";
        MvcResult mvcResult = mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        ModelValidationError actualErrors = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            ModelValidationError.class
        );

        ModelValidationError expectedErrors = objectMapper.readValue(
            getResource("recordBlankRequestError.json"),
            ModelValidationError.class
        );

        assertThat(actualErrors.getErrors()).isNotEmpty();
        assertThat(actualErrors.getErrors())
            .hasSameElementsAs(expectedErrors.getErrors());
    }

    @Test
    void shouldReturn400ResponseWhenMandatoryFieldsMissing() throws Exception {
        String requestJson = Resources.toString(getResource("recordMandatoryFieldsMissing.json"), UTF_8);
        MvcResult mvcResult = mockMvc.perform(post("/recordSittingRecords/{hmctsServiceCode}", TEST_SERVICE)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn();

        ModelValidationError actualErrors = objectMapper.readValue(
            mvcResult.getResponse().getContentAsByteArray(),
            ModelValidationError.class
        );

        ModelValidationError expectedErrors = objectMapper.readValue(
            getResource("recordMandatoryFieldsMissingErrors.json"),
            ModelValidationError.class
        );

        assertThat(actualErrors.getErrors()).isNotEmpty();
        assertThat(actualErrors.getErrors())
            .hasSameElementsAs(expectedErrors.getErrors());
    }
}
