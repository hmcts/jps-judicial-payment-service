package uk.gov.hmcts.reform.jps.services.refdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiRequest;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;

@ExtendWith(MockitoExtension.class)
class JudicialUserDetailsServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JudicialUserDetailsServiceClient judicialUserServiceClient;

    @InjectMocks
    private JudicialUserDetailsService judicialUserDetailsService;
    List<String> personalCodes;
    List<JudicialUserDetailsApiResponse> response;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        personalCodes = List.of(
            "4918500", "4918179", "4918180"
        );
        response = List.of(
            JudicialUserDetailsApiResponse.builder()
                .personalCode("4918500")
                .fullName("First Judge")
                .build(),
            JudicialUserDetailsApiResponse.builder()
                .personalCode("4918180")
                .fullName("Third Judge")
                .build()
        );
    }

    @Test
    void setJudicialFullNameWhenJudicialDetailsFound() {
        when(judicialUserServiceClient.getJudicialUserDetails(JudicialUserDetailsApiRequest.builder()
                                                                  .personalCode(personalCodes)
                                                                  .build()))
            .thenReturn(response);
        List<SittingRecord> sittingRecords = List.of(
            SittingRecord.builder()
                .personalCode("4918500")
                .build(),
            SittingRecord.builder()
                .personalCode("4918179")
                .build(),
            SittingRecord.builder()
                .personalCode("4918180")
                .build()
        );

        judicialUserDetailsService.setJudicialUserDetails(sittingRecords);

        assertThat(sittingRecords)
            .map(SittingRecord::getPersonalCode, SittingRecord::getPersonalName)
            .contains(
                tuple("4918179", "N/A"),
                tuple("4918500", "First Judge"),
                tuple("4918180", "Third Judge")
            );

    }

    @Test
    void setJudicialFullNameWhenJudicialDetailsFoundInTheSittingRecordWrapper() throws IOException {
        when(judicialUserServiceClient.getJudicialUserDetails(JudicialUserDetailsApiRequest.builder()
                                                                  .personalCode(personalCodes)
                                                                  .build()))
            .thenReturn(response);
        String requestJson = Resources.toString(getResource("recordSittingRecords.json"), UTF_8);

        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
            .map(SittingRecordWrapper::new)
            .peek(sittingRecordWrapper ->
                      sittingRecordWrapper.setJudgeRoleTypeId(
                          sittingRecordWrapper.getSittingRecordRequest().getJudgeRoleTypeId()
                      ))
            .toList();

        judicialUserDetailsService.setJudicialUserName(sittingRecordWrappers);

        assertThat(sittingRecordWrappers)
            .map(SittingRecordWrapper::getJudgeRoleTypeId, SittingRecordWrapper::getJudgeRoleTypeName)
            .contains(
                tuple("Judge", "N/A"),
                tuple("Tester", "First Judge"),
                tuple("Judge", "Third Judge")
            );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldNotInvokeJudicialClientWhenInvalidList(List<SittingRecordWrapper> sittingRecordWrappers) {
        judicialUserDetailsService.setJudicialUserName(sittingRecordWrappers);
        verify(judicialUserServiceClient,never())
            .getJudicialUserDetails(isA(JudicialUserDetailsApiRequest.class));
    }

    @Test
    void shouldReturnJudicialUserNameWhenUserPresent() {
        String personalCode = "4918500";
        when(judicialUserServiceClient.getJudicialUserDetails(JudicialUserDetailsApiRequest.builder()
                                                                  .personalCode(List.of(personalCode))
                                                                  .build()))
            .thenReturn(response);

        assertThat(judicialUserDetailsService.getJudicialUserName(personalCode))
            .isEqualTo("First Judge");
    }

    @Test
    void shouldReturnJudicialUserDetailsWhenUserPresent() {
        String personalCode = "4918500";
        when(judicialUserServiceClient.getJudicialUserDetails(JudicialUserDetailsApiRequest.builder()
                                                                  .personalCode(List.of(personalCode))
                                                                  .build()))
            .thenReturn(response);

        assertThat(judicialUserDetailsService.getJudicialUserDetails(personalCode))
            .map(JudicialUserDetailsApiResponse::getFullName)
            .hasValue("First Judge");
    }
}
