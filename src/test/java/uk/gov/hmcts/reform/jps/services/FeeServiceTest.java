package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.Fee;
import uk.gov.hmcts.reform.jps.model.in.FeeDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.FeeRequest;
import uk.gov.hmcts.reform.jps.repository.FeeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {
    @Mock
    private FeeRepository feeRepository;

    @InjectMocks
    private FeeService feeService;

    @Test
    void shouldReturnFeeIdsWhenFeeRequestSaved() {
        FeeRequest feeRequest = new FeeRequest();
        feeRequest.setFees(
            List.of(
                getFee("AA"), getFee("BB")
            )
        );
        when(feeRepository.saveAll(anyList()))
            .thenReturn(
                List.of(
                    getDomainFee(1L), getDomainFee(2L)
                )
            );
        List<Long> ids = feeService.save(feeRequest);
        assertThat(ids)
            .hasSize(2)
            .containsExactlyInAnyOrder(1L,2L);
        verify(feeRepository).saveAll(anyList());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFeesMissing() {
        FeeRequest feeRequest = new FeeRequest();
        assertThatThrownBy(() -> feeService.save(feeRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Fees missing");
    }

    @Test
    void shouldDeleteWhenRequestContainsIds() {
        FeeDeleteRequest feeDeleteRequest = new FeeDeleteRequest();
        feeDeleteRequest.setFees(
            List.of(
                1L, 2L
            )
        );
        feeService.delete(feeDeleteRequest);
        verify(feeRepository).deleteByIds(feeDeleteRequest.getFees());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenIdsMissing() {
        FeeDeleteRequest feeDeleteRequest = new FeeDeleteRequest();
        assertThatThrownBy(() -> feeService.delete(feeDeleteRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Fee ids missing");
    }

    @Test
    void shouldReturnFeeWhenRecordPresent() {
        long id = 100L;
        when(feeRepository.findByHmctsServiceIdAndJudgeRoleIdAndEffectiveFromIsLessThanEqual(
            anyString(),
            anyString(),
            any(LocalDate.class)
        )).thenReturn(Optional.of(uk.gov.hmcts.reform.jps.domain.Fee.builder()
                                              .id(id)
                                              .build()));
        uk.gov.hmcts.reform.jps.domain.Fee fees = feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            "BBA3",
            "Judge",
            LocalDate.now()
        );

        assertThat(fees.getId()).isEqualTo(id);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenFeeMissing() {
        LocalDate now = LocalDate.now();
        assertThatThrownBy(() -> feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
            "BBA3",
            "Judge",
            now
        )).isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Fee not set/active for hmctsServiceCode and judgeRoleTypeId Judge");
    }

    private uk.gov.hmcts.reform.jps.domain.Fee getDomainFee(long id) {
        uk.gov.hmcts.reform.jps.domain.Fee fee = new uk.gov.hmcts.reform.jps.domain.Fee();
        fee.setId(id);
        return fee;
    }

    private Fee getFee(String serviceId) {
        Fee fee = new Fee();
        fee.setHmctsServiceId(serviceId);
        return fee;
    }
}
