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
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.com.google.common.base.Charsets.UTF_8;
import static org.testcontainers.shaded.com.google.common.io.Resources.getResource;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@ExtendWith(MockitoExtension.class)
class EvaluateDuplicateTest extends BaseEvaluateDuplicate {
    @Mock
    private DuplicateChecker duplicateChecker;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private EvaluateDuplicate evaluateDuplicate;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        evaluateDuplicate = new EvaluateDuplicate();
        evaluateDuplicate.next(duplicateChecker);
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
        evaluateDuplicate.evaluate(sittingRecordWrappers.get(0), sittingRecordDuplicateCheckFields);
        verify(duplicateChecker).evaluate(any(), any());
    }

    @ParameterizedTest
    @CsvSource({"2000, null, null", "null, now, null", "null, null, 404" })
    void shouldNotInvokeNextDuplicateCheckerWhenDuplicateCheckFieldsDontMatch(
        String epimmsId,
        String sittingDate,
        String personalId
    ) throws IOException {
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
            Objects.nonNull(sittingDate) ? LocalDate.now() : sittingRecordRequest.getSittingDate(),
            Objects.nonNull(epimmsId) ? epimmsId : sittingRecordRequest.getEpimmsId(),
            Objects.nonNull(personalId) ? personalId : sittingRecordRequest.getPersonalCode(),
            sittingRecordRequest.getDurationBoolean().getAm(),
            sittingRecordRequest.getDurationBoolean().getPm(),
            "Tester",
            RECORDED
            );
        evaluateDuplicate.evaluate(sittingRecordWrappers.get(0), sittingRecordDuplicateCheckFields);
        verify(duplicateChecker, never()).evaluate(any(), any());
    }

}
