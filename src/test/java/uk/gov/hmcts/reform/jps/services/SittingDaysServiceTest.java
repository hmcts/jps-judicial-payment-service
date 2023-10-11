package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.repository.SittingDaysRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SittingDaysServiceTest {
    @Mock
    private SittingDaysRepository sittingDaysRepository;

    @InjectMocks
    private SittingDaysService sittingDaysService;

    @Test
    void shouldSittingCountWhenPersonalCodeIsPresentForFinalYear() {
        String personalCode = "4918178";
        String financialYear = "2023-24";
        when(sittingDaysRepository.findSittingCountByPersonalCodeAndFinancialYear(personalCode, financialYear))
            .thenReturn(Optional.of(2L));
        assertThat(sittingDaysService.getSittingCount(personalCode, financialYear))
            .isEqualTo(2L);
    }

}
