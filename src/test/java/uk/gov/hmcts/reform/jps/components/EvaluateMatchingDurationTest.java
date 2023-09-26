package uk.gov.hmcts.reform.jps.components;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.google.common.io.Resources;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@ExtendWith(MockitoExtension.class)
class EvaluateMatchingDurationTest extends BaseEvaluateDuplicate {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private DuplicateChecker duplicateChecker;
    private EvaluateMatchingDuration evaluateMatchingDuration;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        evaluateMatchingDuration = new EvaluateMatchingDuration();
        evaluateMatchingDuration.next(duplicateChecker);
    }

    @Test
    void shouldSetInvalidDuplicateWhenJudgeRoleTypeMatch() throws IOException {
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
            sittingRecordRequest.getJudgeRoleTypeId(),
            RECORDED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateMatchingDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(INVALID_DUPLICATE_RECORD);
    }

    @Test
    void shouldSetPotentialDuplicateWhenJudgeRoleTypeDontMatch() throws IOException {
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
        evaluateMatchingDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(POTENTIAL_DUPLICATE_RECORD);
    }

    @Test
    void shouldSetPotentialDuplicateWhenEpimmsIdDontMatch() throws IOException {
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
            "999999",
            sittingRecordRequest.getPersonalCode(),
            sittingRecordRequest.getDurationBoolean().getAm(),
            sittingRecordRequest.getDurationBoolean().getPm(),
            sittingRecordRequest.getJudgeRoleTypeId(),
            RECORDED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateMatchingDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(POTENTIAL_DUPLICATE_RECORD);
    }

    @Test
    void shouldSetValidWhenJudgeRoleTypeDontMatchAndReplaceDuplicateIsTrue() throws IOException {
        String requestJson = Resources.toString(getResource("duplicateRecordSittingReplaceDuplicate.json"), UTF_8);
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
        evaluateMatchingDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(POTENTIAL_DUPLICATE_RECORD);
    }

    @Test
    void shouldSetInValidWhenRecordStatusIsSubmitted() throws IOException {
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
            SUBMITTED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateMatchingDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(INVALID_DUPLICATE_RECORD);
    }

    @Test
    void shouldSetValidWhenRecordStatusIsDeleted() throws IOException {
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
            DELETED
        );
        SittingRecordWrapper sittingRecordWrapper = sittingRecordWrappers.get(0);
        evaluateMatchingDuration.evaluate(sittingRecordWrapper, sittingRecordDuplicateCheckFields);

        assertThat(sittingRecordWrapper.getErrorCode())
            .isEqualTo(VALID);
    }


    @Test
    void shouldNInvokeNextDuplicateCheckerWhenDurationDontMatch() throws IOException {
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
            !sittingRecordRequest.getDurationBoolean().getAm(),
            !sittingRecordRequest.getDurationBoolean().getPm(),
            "Tester",
            RECORDED
        );
        evaluateMatchingDuration.evaluate(sittingRecordWrappers.get(0), sittingRecordDuplicateCheckFields);
        verify(duplicateChecker).evaluate(any(), any());
    }
}
