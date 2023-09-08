package uk.gov.hmcts.reform.jps.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.components.BaseEvaluateDuplicate;
import uk.gov.hmcts.reform.jps.components.EvaluateDuplicate;
import uk.gov.hmcts.reform.jps.components.EvaluateMatchingDuration;
import uk.gov.hmcts.reform.jps.components.EvaluateOverlapDuration;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ExtendWith(MockitoExtension.class)
class DuplicateCheckerServiceTest extends BaseEvaluateDuplicate {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private EvaluateDuplicate evaluateDuplicate;
    @Mock
    private EvaluateMatchingDuration evaluateMatchingDuration;
    @Mock
    private EvaluateOverlapDuration evaluateOverlapDuration;
    @Mock
    private StatusHistoryService statusHistoryService;
    private DuplicateCheckerService duplicateCheckerService;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        duplicateCheckerService = new DuplicateCheckerService(
                evaluateDuplicate,
                evaluateMatchingDuration,
                evaluateOverlapDuration,
                statusHistoryService
        );
    }

    @Test
    void shouldInvokeNextDuplicateCheckerWhenInvalidDuplicateRecord() throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSitting.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
                requestJson,
                RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
                recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .peek(sittingRecordWrapper -> sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD))
                .toList();

        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
                = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                sittingRecordRequest.getDurationBoolean().getAm(),
                sittingRecordRequest.getDurationBoolean().getPm(),
                "Tester",
                RECORDED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);

        duplicateCheckerService.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        verify(evaluateDuplicate).evaluate(any(), any());
        verify(statusHistoryService).updateFromStatusHistory(
                isA(SittingRecordWrapper.class),
                isA(SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields.class));
        assertThat(sittingRecordWrapper.getJudgeRoleTypeId())
                .isEqualTo(sittingRecordDuplicateCheckFields.getJudgeRoleTypeId());
        assertThat(sittingRecordWrapper.getAm())
                .isEqualTo(sittingRecordDuplicateCheckFields.getAm());
        assertThat(sittingRecordWrapper.getPm())
                .isEqualTo(sittingRecordDuplicateCheckFields.getPm());
    }

    @Test
    void shouldInvokeNextDuplicateCheckerWhenValidRecord() throws IOException {
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
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                sittingRecordRequest.getDurationBoolean().getAm(),
                sittingRecordRequest.getDurationBoolean().getPm(),
                "Tester",
                RECORDED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);

        duplicateCheckerService.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        verify(evaluateDuplicate).evaluate(any(), any());
        verify(statusHistoryService, never()).updateFromStatusHistory(
                isA(SittingRecordWrapper.class),
                isA(SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields.class));
    }

    @Test
    void shouldInvokeNextDuplicateCheckerWhenPotentialDuplicateRecordWithReplaceDuplicate()
            throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSittingReplaceDuplicate.json"), UTF_8);
        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
                requestJson,
                RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
                recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .peek(sittingRecordWrapper ->
                        sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD)
                )
                .toList();


        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
                = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                sittingRecordRequest.getDurationBoolean().getAm(),
                sittingRecordRequest.getDurationBoolean().getPm(),
                "Tester",
                RECORDED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);

        duplicateCheckerService.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        verify(evaluateDuplicate).evaluate(any(), any());
        verify(statusHistoryService, never()).updateFromStatusHistory(
                isA(SittingRecordWrapper.class),
                isA(SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields.class));
    }

    @Test
    void shouldInvokeNextDuplicateCheckerWhenPotentialDuplicateRecordWithReplaceDuplicateFalse()
            throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSitting.json"), UTF_8);

        RecordSittingRecordRequest recordSittingRecordRequest = objectMapper.readValue(
                requestJson,
                RecordSittingRecordRequest.class
        );

        List<SittingRecordWrapper> sittingRecordWrappers =
                recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .peek(sittingRecordWrapper ->
                        sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD)
                )
                .toList();


        SittingRecordRequest sittingRecordRequest = recordSittingRecordRequest.getRecordedSittingRecords().get(0);
        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields
                = getDbRecord(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                sittingRecordRequest.getDurationBoolean().getAm(),
                sittingRecordRequest.getDurationBoolean().getPm(),
                "Tester",
                RECORDED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);

        duplicateCheckerService.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        verify(evaluateDuplicate).evaluate(any(), any());
        verify(statusHistoryService).updateFromStatusHistory(
                isA(SittingRecordWrapper.class),
                isA(SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields.class));
        assertThat(sittingRecordWrapper.getJudgeRoleTypeId())
                .isEqualTo(sittingRecordDuplicateCheckFields.getJudgeRoleTypeId());
        assertThat(sittingRecordWrapper.getAm())
                .isEqualTo(sittingRecordDuplicateCheckFields.getAm());
        assertThat(sittingRecordWrapper.getPm())
                .isEqualTo(sittingRecordDuplicateCheckFields.getPm());

    }
}
