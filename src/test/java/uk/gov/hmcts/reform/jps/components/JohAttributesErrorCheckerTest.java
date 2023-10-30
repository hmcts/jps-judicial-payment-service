package uk.gov.hmcts.reform.jps.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
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
class JohAttributesErrorCheckerTest extends BasePublishSittingRecord {
    @Mock
    private JudicialOfficeHolderService judicialOfficeHolderService;
    @Mock
    private JudicialUserDetailsService judicialUserDetailsService;

    @InjectMocks
    private JohAttributesErrorChecker johAttributesErrorChecker;

    @BeforeEach
    void setUp() {
        super.setup(johAttributesErrorChecker);
    }

    @Test
    void shouldNotAddJohAttributesInErrorWhenJohAttributePresent() {
        when(judicialOfficeHolderService.getJudicialOfficeHolderWithJohAttributes(PERSONAL_CODE,
                                                                             SITTING_DATE))
            .thenReturn(Optional.of(JudicialOfficeHolder.builder().build()));
        johAttributesErrorChecker.evaluate(publishErrors,
                                   HMCTS_SERVICE_CODE,
                                   getDefaultDbSittingRecord());
        assertThat(publishErrors.getErrorCount()).isZero();
        verify(publishErrorChecker).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishFields.class)
        );
    }

    @Test
    void shouldAddJohAttributesInErrorWhenJohAttributeIsNotPresent() {
        when(judicialOfficeHolderService.getJudicialOfficeHolderWithJohAttributes(PERSONAL_CODE,
                                                                                  SITTING_DATE))
            .thenReturn(Optional.empty());
        when(judicialUserDetailsService.getJudicialUserDetails(PERSONAL_CODE))
            .thenReturn(Optional.of(JudicialUserDetailsApiResponse.builder().build()));

        johAttributesErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            getDefaultDbSittingRecord()
        );
        assertThat(publishErrors.getErrorCount()).isEqualTo(1);
        assertThat(publishErrors.getJohAttributesInErrors()).hasSize(1);
        verify(publishErrorChecker, never()).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishFields.class)
        );
        verify(judicialUserDetailsService).getJudicialUserDetails(PERSONAL_CODE);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenPersonalCodeIsInValid() {
        when(judicialOfficeHolderService.getJudicialOfficeHolderWithJohAttributes(PERSONAL_CODE,
                                                                                  SITTING_DATE))
            .thenReturn(Optional.empty());
        SittingRecordPublishFields defaultDbSittingRecord = getDefaultDbSittingRecord();
        assertThatThrownBy(() -> johAttributesErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            defaultDbSittingRecord
        )).isInstanceOf(IllegalArgumentException.class)
        .hasMessage("JudicialUserDetails not found for personal code: " + PERSONAL_CODE);

        assertThat(publishErrors.getErrorCount()).isZero();
        assertThat(publishErrors.getJohAttributesInErrors()).isEmpty();
        verify(publishErrorChecker, never()).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishFields.class)
        );
        verify(judicialUserDetailsService).getJudicialUserDetails(PERSONAL_CODE);
    }
}
