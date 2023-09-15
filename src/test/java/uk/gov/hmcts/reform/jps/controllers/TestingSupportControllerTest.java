package uk.gov.hmcts.reform.jps.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.config.SecurityConfiguration;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueRequest;
import uk.gov.hmcts.reform.jps.model.in.FeeDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.FeeRequest;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderRequest;
import uk.gov.hmcts.reform.jps.model.in.ServiceDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.ServiceRequest;
import uk.gov.hmcts.reform.jps.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.jps.services.CourtVenueService;
import uk.gov.hmcts.reform.jps.services.FeeService;
import uk.gov.hmcts.reform.jps.services.JudicialOfficeHolderService;
import uk.gov.hmcts.reform.jps.services.ServiceService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;

@WebMvcTest(value = TestingSupportController.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ConditionalOnProperty(prefix = "testing", value = "support.enabled", havingValue = "true")
class TestingSupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JudicialOfficeHolderService judicialOfficeHolderService;

    @MockBean
    private ServiceService serviceService;
    @MockBean
    private CourtVenueService courtVenueService;
    @MockBean
    private FeeService feeService;

    @Test
    void saveJudicialOfficeHoldersIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("submitJohRecords.json"), UTF_8);

        when(judicialOfficeHolderService.save(isA(JudicialOfficeHolderRequest.class)))
            .thenReturn(List.of(1L, 2L));

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

        when(judicialOfficeHolderService.save(isA(JudicialOfficeHolderRequest.class)))
            .thenThrow(new IllegalArgumentException("Joh records missing"));

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
    void deleteJudicialOfficeHoldersWhenRequestIsValid() throws Exception {
        String requestJson = "{\"judicialOfficeHolderIds\":[1,2]}";

        mockMvc.perform(post("/testing-support/delete-judicial-office-holders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
            .andDo(print())
            .andExpectAll(
                status().isOk()
            );
    }

    @Test
    void deleteJudicialOfficeHoldersWhenRequestIsInValid() throws Exception {

        String requestJson = "{}";
        doThrow(new IllegalArgumentException("Joh ids missing"))
            .when(judicialOfficeHolderService).delete(isA(JudicialOfficeHolderDeleteRequest.class));

        MvcResult mvcResult =  mockMvc.perform(post("/testing-support/delete-judicial-office-holders")
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
    void saveServicesWhenRequestIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("submitService.json"), UTF_8);
        when(serviceService.save(isA(ServiceRequest.class)))
            .thenReturn(List.of(1L, 2L));

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
        when(serviceService.save(isA(ServiceRequest.class)))
            .thenThrow(new IllegalArgumentException("Services missing"));

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
        doThrow(new IllegalArgumentException("Service ids missing"))
            .when(serviceService).delete(isA(ServiceDeleteRequest.class));

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
    void saveCourtVenuesIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("courtVenues.json"), UTF_8);
        when(courtVenueService.save(isA(CourtVenueRequest.class)))
            .thenReturn(List.of(1L, 2L));

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
        doThrow(new IllegalArgumentException("Court venues missing"))
           .when(courtVenueService).save(isA(CourtVenueRequest.class));

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
        doThrow(new IllegalArgumentException("Court venue ids missing"))
            .when(courtVenueService).delete(isA(CourtVenueDeleteRequest.class));

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
    void saveFeeWhenRequestIsValid() throws Exception {
        String requestJson = Resources.toString(getResource("submitFees.json"), UTF_8);
        when(feeService.save(isA(FeeRequest.class)))
            .thenReturn(List.of(1L, 2L));

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
        doThrow(new IllegalArgumentException("Service ids missing"))
            .when(feeService).save(isA(FeeRequest.class));

        MvcResult mvcResult = mockMvc.perform(post("/testing-support/save-fee")
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
        doThrow(new IllegalArgumentException("Fee ids missing"))
            .when(feeService).delete(isA(FeeDeleteRequest.class));

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
