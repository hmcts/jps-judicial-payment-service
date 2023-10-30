package uk.gov.hmcts.reform.jps.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection;
import uk.gov.hmcts.reform.jps.services.FeeService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeInErrorCheckerTest extends BasePublishSittingRecord {
    @Mock
    private JudicialUserDetailsService judicialUserDetailsService;

    @Mock
    private FeeService feeService;

    @InjectMocks
    private FeeInErrorChecker feeInErrorChecker;

    @BeforeEach
    void setUp() {
        super.setup(feeInErrorChecker);
    }

    @Test
    void shouldNotAddFeeInErrorWhenFeePresent() {
        when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(HMCTS_SERVICE_CODE,
                                                                             JUDGE_ROLE_TYPE_ID,
                                                                             SITTING_DATE))
            .thenReturn(Optional.of(Fee.builder().build()));
        feeInErrorChecker.evaluate(publishErrors,
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
        when(feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(HMCTS_SERVICE_CODE,
                                                                             JUDGE_ROLE_TYPE_ID,
                                                                             SITTING_DATE))
            .thenReturn(Optional.empty());
        feeInErrorChecker.evaluate(
            publishErrors,
            HMCTS_SERVICE_CODE,
            getDefaultDbSittingRecord()
        );
        assertThat(publishErrors.getErrorCount()).isEqualTo(1);
        assertThat(publishErrors.getFeeInErrors()).hasSize(1);
        verify(publishErrorChecker, never()).evaluate(
            same(publishErrors),
            eq(HMCTS_SERVICE_CODE),
            any(SittingRecordPublishProjection.SittingRecordPublishFields.class)
        );
        verify(judicialUserDetailsService).getJudicialUserName(PERSONAL_CODE);
    }
}
