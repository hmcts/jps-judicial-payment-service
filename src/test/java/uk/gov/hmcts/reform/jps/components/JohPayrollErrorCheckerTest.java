package uk.gov.hmcts.reform.jps.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;
import uk.gov.hmcts.reform.jps.services.JudicialOfficeHolderService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JohPayrollErrorCheckerTest extends BasePublishSittingRecord {
    @Mock
    private JudicialOfficeHolderService judicialOfficeHolderService;
    @Mock
    private JudicialUserDetailsService judicialUserDetailsService;

    @InjectMocks
    private JohPayrollErrorChecker johPayrollErrorChecker;

    @BeforeEach
    void setUp() {
        super.setup(johPayrollErrorChecker);
    }

    @Test
    void shouldNotAddJohPayrollInErrorWhenJohAttributePresent() {
        when(judicialOfficeHolderService.getJudicialOfficeHolderWithJohPayroll(PERSONAL_CODE,
                                                                                  SITTING_DATE))
            .thenReturn(Optional.of(JudicialOfficeHolder.builder().build()));
        johPayrollErrorChecker.evaluate(publishErrors,
                                           HMCTS_SERVICE_CODE,
                                           getDefaultDbSittingRecord());
        assertThat(publishErrors.getErrorCount()).isEqualTo(0);
        verify(publishErrorChecker).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishProjection.SittingRecordPublishFields.class)
        );
    }

    @Test
    void shouldAddJohPayrollInErrorWhenJohAttributeIsNotPresent() {
        when(judicialOfficeHolderService.getJudicialOfficeHolderWithJohPayroll(PERSONAL_CODE,
                                                                                  SITTING_DATE))
            .thenReturn(Optional.empty());
        when(judicialUserDetailsService.getJudicialUserDetails(PERSONAL_CODE))
            .thenReturn(Optional.of(JudicialUserDetailsApiResponse.builder().build()));

        johPayrollErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            getDefaultDbSittingRecord()
        );
        assertThat(publishErrors.getErrorCount()).isEqualTo(1);
        assertThat(publishErrors.getJohPayrollInErrors()).hasSize(1);
        verify(publishErrorChecker, never()).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishProjection.SittingRecordPublishFields.class)
        );
        verify(judicialUserDetailsService).getJudicialUserDetails(PERSONAL_CODE);
    }


    @Test
    void shouldThrowIllegalArgumentExceptionWhenPersonalCodeIsInValid() {
        when(judicialOfficeHolderService.getJudicialOfficeHolderWithJohPayroll(PERSONAL_CODE,
                                                                                  SITTING_DATE))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> johPayrollErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            getDefaultDbSittingRecord()
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessage("JudicialUserDetails not found for personal code: " + PERSONAL_CODE);

        assertThat(publishErrors.getErrorCount()).isEqualTo(0);
        assertThat(publishErrors.getJohPayrollInErrors()).isEmpty();
        verify(publishErrorChecker, never()).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishProjection.SittingRecordPublishFields.class)
        );
        verify(judicialUserDetailsService).getJudicialUserDetails(PERSONAL_CODE);
    }
}
