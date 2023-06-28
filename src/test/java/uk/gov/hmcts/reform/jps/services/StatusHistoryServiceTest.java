package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.components.BaseEvaluateDuplicate;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ExtendWith(MockitoExtension.class)
class StatusHistoryServiceTest extends BaseEvaluateDuplicate {

    @Mock
    private StatusHistoryRepository statusHistoryRepository;

    @InjectMocks
    private StatusHistoryService statusHistoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }


    @Test
    void shouldUpdateWithStatusHistoryWhenDbRecordPresent() throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSitting.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();


        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
            sittingRecordRequest.getSittingDate(),
            "2000",
            sittingRecordRequest.getPersonalCode(),
            sittingRecordRequest.getDurationBoolean().getAm(),
            sittingRecordRequest.getDurationBoolean().getPm(),
            "Tester",
            RECORDED
        );

        StatusHistory statusHistory = StatusHistory.builder()
            .changeByName("Recorder")
            .changeDateTime(LocalDateTime.now().minusSeconds(30))
            .statusId(RECORDED)
            .build();

        when(statusHistoryRepository.findFirstBySittingRecord(any(), any()))
            .thenReturn(Optional.of(statusHistory));

        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        statusHistoryService.updateFromStatusHistory(sittingRecordWrapper,
                                                     sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getCreatedByName())
            .isEqualTo(statusHistory.getChangeByName());
        assertThat(sittingRecordWrapper.getCreatedDateTime())
            .isEqualTo(statusHistory.getChangeDateTime());
        assertThat(sittingRecordWrapper.getStatusId())
            .isEqualTo(statusHistory.getStatusId());

    }

    @Test
    void shouldNotUpdateWithStatusHistoryWhenDbRecordNotPresent() throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSitting.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();


        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
            sittingRecordRequest.getSittingDate(),
            "2000",
            sittingRecordRequest.getPersonalCode(),
            sittingRecordRequest.getDurationBoolean().getAm(),
            sittingRecordRequest.getDurationBoolean().getPm(),
            "Tester",
            RECORDED
        );

        when(statusHistoryRepository.findFirstBySittingRecord(any(), any()))
            .thenReturn(Optional.empty());

        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        statusHistoryService.updateFromStatusHistory(sittingRecordWrapper,
                                                     sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getCreatedByName())
            .isNull();
        assertThat(sittingRecordWrapper.getCreatedDateTime())
            .isNull();
        assertThat(sittingRecordWrapper.getStatusId())
            .isNull();
    }
}
