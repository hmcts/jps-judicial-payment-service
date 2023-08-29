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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ExtendWith(MockitoExtension.class)
class DuplicateCheckerServiceTest extends BaseEvaluateDuplicate  {
    @Mock
    private EvaluateDuplicate evaluateDuplicate;
    @Mock
    private EvaluateMatchingDuration evaluateMatchingDuration;
    @Mock
    private EvaluateOverlapDuration evaluateOverlapDuration;

    private DuplicateCheckerService duplicateCheckerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        duplicateCheckerService = new DuplicateCheckerService(
            evaluateDuplicate,
            evaluateMatchingDuration,
            evaluateOverlapDuration
        );
    }

    @Test
    void shouldInvokeNextDuplicateCheckerWhenDuplicateCheckFieldsMatch() throws IOException {
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
        duplicateCheckerService.evaluate(sittingRecordWrappers.get(0), sittingRecordDuplicateCheckFields);
        verify(evaluateDuplicate).evaluate(any(), any());
    }
}
