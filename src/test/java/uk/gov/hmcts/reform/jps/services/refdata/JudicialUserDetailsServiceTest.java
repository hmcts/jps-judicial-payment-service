package uk.gov.hmcts.reform.jps.services.refdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        List<String> personalCodes = List.of(
            "4918500", "4918179", "4918180"
        );
        List<JudicialUserDetailsApiResponse> response = List.of(
            JudicialUserDetailsApiResponse.builder()
                .personalCode("4918500")
                .fullName("First Judge")
                .build(),
            JudicialUserDetailsApiResponse.builder()
                .personalCode("4918180")
                .fullName("Third Judge")
                .build()
        );
        when(judicialUserServiceClient.getJudicialUserDetails(JudicialUserDetailsApiRequest.builder()
                                                                  .personalCode(personalCodes)
                                                                  .build()))
            .thenReturn(response);
    }

    @Test
    void setJudicialFullNameWhenJudicialDetailsFound() {
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
    void setJudicialFullNameWhenJudicialDetailsFound() {
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
}
