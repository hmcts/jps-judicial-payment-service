package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.caseworker.model.CaseWorkerApiResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseWorkerServiceTest {
    @Mock
    private CaseWorkerClient caseWorkerClient;

    @InjectMocks
    private CaseWorkerService caseWorkerService;

    @Test
    void setCaseWorkerNameWhenCaseWorkerDetailsFound() {
        List<SittingRecord> sittingRecords = List.of(
            SittingRecord.builder()
                .createdByUserId("1")
                .changeByUserId("11")
                .build(),
            SittingRecord.builder()
                .createdByUserId("2")
                .changeByUserId("22")
                .build(),
            SittingRecord.builder()
                .createdByUserId("3")
                .build()
        );

        when(caseWorkerClient.getCaseWorkerDetails(anyString()))
            .thenAnswer(invocation -> {
                String value = invocation.getArgument(0, String.class);
                if ("1".equals(value)) {
                    return CaseWorkerApiResponse.builder()
                        .caseWorkerId("1")
                        .firstName("Single")
                        .lastName("One")
                        .build();
                } else if ("11".equals(value)) {
                    return CaseWorkerApiResponse.builder()
                        .caseWorkerId("11")
                        .firstName("Double")
                        .lastName("One")
                        .build();
                } else if ("2".equals(value)) {
                    return CaseWorkerApiResponse.builder()
                        .caseWorkerId("2")
                        .firstName("Single")
                        .lastName("Two")
                        .build();
                } else if ("22".equals(value)) {
                    return CaseWorkerApiResponse.builder()
                        .caseWorkerId("22")
                        .firstName("Double")
                        .lastName("Two")
                        .build();
                }
                return CaseWorkerApiResponse.builder()
                    .caseWorkerId("3")
                    .firstName("Single")
                    .lastName("Three")
                    .build();
            });


        caseWorkerService.setCaseWorkerDetails(sittingRecords);

        assertThat(sittingRecords)
            .extracting(
                "createdByUserId",
                "createdByUserName",
                "changeByUserId",
                "changeByUserName"
            )
            .containsExactlyInAnyOrder(
                tuple("1", "Single One", "11", "Double One"),
                tuple("2", "Single Two", "22", "Double Two"),
                tuple("3", "Single Three", null, null)
            );

    }
}
