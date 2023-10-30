package uk.gov.hmcts.reform.jps.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.CourtVenue;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
import uk.gov.hmcts.reform.jps.services.CourtVenueService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtVenueErrorCheckerTest extends BasePublishSittingRecord {

    @Mock
    private CourtVenueService courtVenueService;
    @InjectMocks
    private CourtVenueErrorChecker courtVenueErrorChecker;

    @BeforeEach
    void setUp() {
        super.setup(courtVenueErrorChecker);
    }

    @Test
    void shouldNotAddCourtVenueErrorWhenCourtVenuePresent() {
        when(courtVenueService.getCourtVenue(anyString(), anyString()))
            .thenReturn(Optional.of(CourtVenue.builder().build()));

        courtVenueErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            getDefaultDbSittingRecord()
        );
        assertThat(publishErrors.getErrorCount()).isZero();
        verify(publishErrorChecker).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishFields.class)
        );
    }

    @Test
    void shouldAddCourtVenueErrorWhenCourtVenueIsNotPresent() {
        when(courtVenueService.getCourtVenue(anyString(), anyString()))
            .thenReturn(Optional.empty());
        courtVenueErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            getDefaultDbSittingRecord()
        );
        assertThat(publishErrors.getErrorCount()).isEqualTo(1);
        assertThat(publishErrors.getCourtVenueInErrors()).hasSize(1);
        verify(publishErrorChecker, never()).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishFields.class)
        );
    }
}

