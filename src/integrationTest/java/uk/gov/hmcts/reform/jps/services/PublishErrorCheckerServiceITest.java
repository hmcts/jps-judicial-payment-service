package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

public class PublishErrorCheckerServiceITest extends BaseTest {
    @Autowired
    private PublishErrorCheckerService publishErrorCheckerService;
    @Autowired
    private SittingRecordRepository sittingRecordRepository;
    @Autowired
    private JudicialUserDetailsService judicialUserDetailsService;

    @Test
    @Sql({RESET_DATABASE, INSERT_EVALUATE_PUBLISHED_TEST_DATA})
    void shouldReturnErrorResponseWhenRecordsMissing() {
        PublishErrors publishErrors = PublishErrors.builder().build();

        try (Stream<SittingRecordPublishProjection.SittingRecordPublishFields> stream =
                 sittingRecordRepository.findByStatusIdAndSittingDateLessThanEqual(
                     SUBMITTED,
                     LocalDate.now()
                 ).stream()) {
            stream.forEach(sittingRecordPublishFields ->
                               publishErrorCheckerService.evaluate(
                                   "BBA3",
                                   sittingRecordPublishFields,
                                   publishErrors
                               ));
        }
        assertThat(publishErrors.getCourtVenueInErrors().size())
            .as("Court venue")
            .isEqualTo(1);
        assertThat(publishErrors.getJohAttributesInErrors().size())
            .as("Joh attributes")
            .isEqualTo(1);
        assertThat(publishErrors.getJohPayrollInErrors().size())
            .as("Joh payrolls")
            .isEqualTo(1);
        assertThat(publishErrors.getFeeInErrors().size())
            .as("Fees")
            .isEqualTo(1);
        assertThat(publishErrors.getServiceInErrors().size())
            .as("Service")
            .isEqualTo(1);
        assertThat(publishErrors.getErrorCount())
            .isEqualTo(5);
    }
}
