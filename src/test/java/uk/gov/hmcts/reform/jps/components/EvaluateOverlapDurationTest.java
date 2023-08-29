package uk.gov.hmcts.reform.jps.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.ErrorCode;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.services.StatusHistoryService;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ExtendWith(MockitoExtension.class)
class EvaluateOverlapDurationTest extends BaseEvaluateDuplicate {

    @Mock
    private StatusHistoryService statusHistoryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private EvaluateOverlapDuration evaluateOverlapDuration;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        evaluateOverlapDuration = new EvaluateOverlapDuration(statusHistoryService);
    }

    @ParameterizedTest
    @CsvSource({"SUBMITTED,INVALID_DUPLICATE_RECORD",
        "RECORDED,INVALID_DUPLICATE_RECORD",
        "PUBLISHED,INVALID_DUPLICATE_RECORD"})
    void shouldSetDuplicateWhenRecordStatusIsSubmittedAndDbPmIntersects(StatusId statusId,
                                                                               ErrorCode errorCode) throws IOException {
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

        boolean am;
        if (sittingRecordRequest.getDurationBoolean().getAm()
            && sittingRecordRequest.getDurationBoolean().getPm()) {
            am = false;
        } else {
            am = !sittingRecordRequest.getDurationBoolean().getAm();
        }

        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                am,
                sittingRecordRequest.getDurationBoolean().getPm(),
                sittingRecordRequest.getJudgeRoleTypeId(),
                statusId
            );

        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateOverlapDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(errorCode);

        verify(statusHistoryService).updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
    }

    @ParameterizedTest
    @CsvSource({"SUBMITTED,INVALID_DUPLICATE_RECORD",
        "RECORDED,INVALID_DUPLICATE_RECORD",
        "PUBLISHED,INVALID_DUPLICATE_RECORD"})
    void shouldSetDuplicateWhenRecordStatusIsSubmittedAndDbAmIntersects(StatusId statusId,
                                                                               ErrorCode errorCode) throws IOException {
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

        boolean pm;
        if (sittingRecordRequest.getDurationBoolean().getAm()
            && sittingRecordRequest.getDurationBoolean().getPm()) {
            pm = false;
        } else {
            pm = !sittingRecordRequest.getDurationBoolean().getPm();
        }

        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                sittingRecordRequest.getDurationBoolean().getAm(),
                pm,
                sittingRecordRequest.getJudgeRoleTypeId(),
                statusId
            );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateOverlapDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(errorCode);

        verify(statusHistoryService).updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
    }


    @ParameterizedTest
    @CsvSource({"SUBMITTED,INVALID_DUPLICATE_RECORD",
        "RECORDED,INVALID_DUPLICATE_RECORD",
        "PUBLISHED,INVALID_DUPLICATE_RECORD"})
    void shouldSetDuplicateWhenRecordStatusIsSubmittedAndRequestPmIntersects(StatusId statusId,
                                                                               ErrorCode errorCode) throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSitting.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
            );

        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordRequest intersectingPmRequest = sittingRecordRequest.toBuilder()
            .durationBoolean(new DurationBoolean(false, true))
            .build();

        List<SittingRecordWrapper> sittingRecordWrappers = List.of(SittingRecordWrapper.builder()
                                                                       .sittingRecordRequest(intersectingPmRequest)
                                                                       .build());

        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                true,
                true,
                sittingRecordRequest.getJudgeRoleTypeId(),
                statusId
            );

        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateOverlapDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(errorCode);

        verify(statusHistoryService).updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
    }

    @ParameterizedTest
    @CsvSource({"SUBMITTED,INVALID_DUPLICATE_RECORD",
        "RECORDED,INVALID_DUPLICATE_RECORD",
        "PUBLISHED,INVALID_DUPLICATE_RECORD"})
    void shouldSetDuplicateWhenRecordStatusIsSubmittedAndRequestAmIntersects(StatusId statusId,
                                                                               ErrorCode errorCode) throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSitting.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordRequest intersectingPmRequest = sittingRecordRequest.toBuilder()
            .durationBoolean(new DurationBoolean(true, false))
            .build();

        List<SittingRecordWrapper> sittingRecordWrappers = List.of(SittingRecordWrapper.builder()
                                                                       .sittingRecordRequest(intersectingPmRequest)
                                                                       .build());

        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                true,
                true,
                sittingRecordRequest.getJudgeRoleTypeId(),
                statusId
            );

        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateOverlapDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(errorCode);

        verify(statusHistoryService).updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
    }

    @Test
    void shouldNotUpdateErrorCodeWhenRecordDurationIsFalseInDb() throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSitting.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
            requestJson,
            RecordSittingRecordRequest.class
        );

        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordRequest intersectingPmRequest = sittingRecordRequest.toBuilder()
            .durationBoolean(new DurationBoolean(false, false))
            .build();

        List<SittingRecordWrapper> sittingRecordWrappers = List.of(SittingRecordWrapper.builder()
                                                                       .sittingRecordRequest(intersectingPmRequest)
                                                                       .build());

        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
            = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                sittingRecordRequest.getDurationBoolean().getAm(),
                sittingRecordRequest.getDurationBoolean().getPm(),
                sittingRecordRequest.getJudgeRoleTypeId(),
                RECORDED
            );

        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateOverlapDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(VALID);

        verify(statusHistoryService, never())
            .updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
    }


    @Test
    void shouldThrowUnsupportedOperationExceptionWhenNextInvoked() {
        EvaluateDuplicate evaluateDuplicate = new EvaluateDuplicate();
        assertThatThrownBy(() -> evaluateOverlapDuration.next(evaluateDuplicate))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
