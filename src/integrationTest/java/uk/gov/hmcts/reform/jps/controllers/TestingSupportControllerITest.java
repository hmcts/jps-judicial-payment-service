package uk.gov.hmcts.reform.jps.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.repository.JohAttributesRepository;
import uk.gov.hmcts.reform.jps.repository.JohPayrollRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_COURT_VENUE;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_FEE;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_JOH;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_SERVICE;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ConditionalOnProperty(prefix = "testing", value = "support.enabled", havingValue = "true")
class TestingSupportControllerITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JohAttributesRepository johAttributesRepository;

    @Autowired
    private JohPayrollRepository johPayrollRepository;

    @Test
    @Sql(RESET_DATABASE)
    void saveJudicialOfficeHoldersIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("submitJohRecords.json"), UTF_8);

        mockMvc.perform(post("/testing-support/save-judicial-office-holders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.judicialOfficeHolderIds[?(@ === 1)]").exists(),
                jsonPath("$.judicialOfficeHolderIds[?(@ === 2)]").exists()
            );
    }

    @Test
    void saveJudicialOfficeHoldersIsInValid() throws Exception {
        String requestJson = "{}";
        MvcResult mvcResult = mockMvc.perform(post("/testing-support/save-judicial-office-holders")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest()
            )
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Joh records missing");
    }

    @Test
    @Sql({RESET_DATABASE, INSERT_JOH})
    void deleteJudicialOfficeHoldersWhenRequestIsValid() throws Exception {
        String requestJson = "{\"judicialOfficeHolderIds\":[1,2]}";

        mockMvc.perform(post("/testing-support/delete-judicial-office-holders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isOk()
            );

        assertThat(johAttributesRepository.findAll()).isEmpty();
        assertThat(johPayrollRepository.findAll()).isEmpty();
    }

    @Test
    void deleteJudicialOfficeHoldersWhenRequestIsInValid() throws Exception {
        String requestJson = "{}";

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/delete-judicial-office-holders")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest()
            )
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Joh ids missing");
    }

    @Test
    @Sql(RESET_DATABASE)
    void saveServicesWhenRequestIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("submitService.json"), UTF_8);

        mockMvc.perform(post("/testing-support/save-service")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.serviceIds[?(@ === 1)]").exists(),
                jsonPath("$.serviceIds[?(@ === 2)]").exists()
            );
    }

    @Test
    void saveServicesWhenRequestIsInValid() throws Exception {
        String requestJson = "{}";

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/save-service")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest()
            ).andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Services missing");
    }

    @Test
    void deleteServicesWhenRequestIsInValid() throws Exception {
        String requestJson = "{}";

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/delete-service")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest()
            ).andReturn();
        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Service ids missing");
    }

    @Test
    @Sql({RESET_DATABASE, INSERT_SERVICE})
    void deleteServicesWhenRequestIsValid() throws Exception {
        String requestJson = "{\"serviceIds\":[1,2]}";

        mockMvc.perform(post("/testing-support/delete-service")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isOk()
            );
    }

    @Test
    @Sql(RESET_DATABASE)
    void saveCourtVenuesIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("courtVenues.json"), UTF_8);

        mockMvc.perform(post("/testing-support/save-court-venue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.courtVenueIds[?(@ === 1)]").exists(),
                jsonPath("$.courtVenueIds[?(@ === 2)]").exists()
            );
    }

    @Test
    void saveCourtVenuesIsInValid() throws Exception {
        String requestJson = "{}";

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/save-court-venue")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest()
            )
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Court venues missing");
    }

    @Test
    @Sql({RESET_DATABASE, INSERT_COURT_VENUE})
    void deleteCourtVenuesWhenRequestIsValid() throws Exception {
        String requestJson = "{\"courtVenueIds\":[1,2]}";

        mockMvc.perform(post("/testing-support/delete-court-venue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isOk()
            );
    }

    @Test
    void deleteCourtVenuesWhenRequestIsInValid() throws Exception {
        String requestJson = "{}";

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/delete-court-venue")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest(
                ))
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Court venue ids missing");
    }

    @Test
    @Sql(RESET_DATABASE)
    void saveFeeWhenRequestIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("submitFees.json"), UTF_8);

        mockMvc.perform(post("/testing-support/save-fee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isCreated(),
                jsonPath("$.fees[?(@ === 1)]").exists(),
                jsonPath("$.fees[?(@ === 2)]").exists()
            );
    }

    @Test
    void saveFeeWhenRequestIsInValid() throws Exception {
        String requestJson = "{}";

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/save-fee")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest()
            ).andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Fees missing");
    }

    @Test
    @Sql({RESET_DATABASE, INSERT_FEE})
    void deleteFeeWhenRequestIsValid() throws Exception {
        String requestJson = "{\"fees\":[1,2]}";
        mockMvc.perform(post("/testing-support/delete-fee")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isOk()
            );
    }

    @Test
    void deleteFeeWhenRequestIsInValid() throws Exception {
        String requestJson = "{}";

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/delete-fee")
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isBadRequest()
            )
            .andReturn();

        assertThat(mvcResult.getResponse().getContentAsString())
            .isEqualTo("Fee ids missing");

    }
}
