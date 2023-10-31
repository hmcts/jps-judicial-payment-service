package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

public class PublishErrorCheckerServiceITest extends BaseTest {
    @Autowired
    private PublishErrorCheckerService publishErrorCheckerService;
    @Autowired
    private SittingRecordRepository sittingRecordRepository;
    @MockBean
    private JudicialUserDetailsService judicialUserDetailsService;

    @Test
    @Sql({RESET_DATABASE, INSERT_EVALUATE_PUBLISHED_TEST_DATA})
    void shouldReturnErrorResponseWhenRecordsMissing() {
        PublishErrors publishErrors = PublishErrors.builder().build();
        when(judicialUserDetailsService.getJudicialUserDetails("5918178"))
            .thenReturn(Optional.of(JudicialUserDetailsApiResponse.builder()
                                        .emailId("test@gmail.com")
                                        .postNominals("postNominal")
                                        .fullName("Test User")
                                        .build()));
        when(judicialUserDetailsService.getJudicialUserDetails("6018178"))
            .thenReturn(Optional.of(JudicialUserDetailsApiResponse.builder()
                                        .emailId("judge@gmail.com")
                                        .postNominals("postNominal")
                                        .fullName("Judge User")
                                        .build()));

        when(judicialUserDetailsService.getJudicialUserDetails("6118178"))
            .thenReturn(Optional.of(JudicialUserDetailsApiResponse.builder()
                                        .emailId("health@gmail.com")
                                        .postNominals("postNominal")
                                        .fullName("Health User")
                                        .build()));

        when(judicialUserDetailsService.getJudicialUserDetails("6118179"))
            .thenReturn(Optional.of(JudicialUserDetailsApiResponse.builder()
                                        .emailId("medical@gmail.com")
                                        .postNominals("postNominal")
                                        .fullName("Medical User")
                                        .build()));
        when(judicialUserDetailsService.getJudicialUserName(anyString()))
            .thenReturn("Tribunal User");

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
