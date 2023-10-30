package uk.gov.hmcts.reform.jps.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.services.ServiceService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceErrorCheckerTest extends BasePublishSittingRecord {

    @Mock
    private ServiceService serviceService;

    @InjectMocks
    private ServiceErrorChecker serviceErrorChecker;

    @BeforeEach
    void setUp() {
        super.setup(serviceErrorChecker);
    }

    @Test
    void shouldNotAddFeeInErrorWhenFeePresent() {
        when(serviceService.isServiceOnboarded(HMCTS_SERVICE_CODE))
            .thenReturn(Boolean.TRUE);
        serviceErrorChecker.evaluate(publishErrors,
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
    void shouldAddFeeInErrorWhenFeeIsNotPresent() {
        when(serviceService.isServiceOnboarded(HMCTS_SERVICE_CODE))
            .thenReturn(Boolean.FALSE);
        serviceErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            getDefaultDbSittingRecord()
        );
        assertThat(publishErrors.getErrorCount()).isEqualTo(1);
        assertThat(publishErrors.getServiceInErrors()).hasSize(1);
        verify(publishErrorChecker, never()).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishProjection.SittingRecordPublishFields.class)
        );
    }
}
