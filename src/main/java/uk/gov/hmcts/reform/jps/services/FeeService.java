package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.Fee;
import uk.gov.hmcts.reform.jps.model.in.FeeDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.FeeRequest;
import uk.gov.hmcts.reform.jps.repository.FeeRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class FeeService {
    private final FeeRepository feeRepository;

    @Transactional
    public List<Long> save(FeeRequest feeRequest) {
        return Optional.ofNullable(feeRequest.getFees())
            .orElseThrow(() -> new IllegalArgumentException("Fees missing"))
            .stream()
            .map(fee -> Fee.builder()
                .hmctsServiceId(fee.getHmctsServiceId())
                .judgeRoleId(fee.getJudgeRoleId())
                .standardFee(fee.getStandardFee())
                .higherThresholdFee(fee.getHigherThresholdFee())
                .londonWeightedFee(fee.getLondonWeightedFee())
                .effectiveFrom(fee.getEffectiveFrom())
                .feeCreatedDate(fee.getFeeCreatedDate())
                .build())
            .collect(Collectors.collectingAndThen(Collectors.toList(),
                                                  fees -> feeRepository.saveAll(fees).stream()
                                                      .map(Fee::getId)
                                                      .toList()));
    }

    @Transactional
    public void delete(FeeDeleteRequest feeDeleteRequest) {
        List<Long> ids = Optional.ofNullable(feeDeleteRequest.getFees())
            .orElseThrow(() -> new IllegalArgumentException("Fee ids missing"));
        feeRepository.deleteByIds(ids);
    }

    public Fee findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
        String hmctsServiceCode,
        String judgeRoleId,
        LocalDate sittingDate
    ) {

        return feeRepository.findByHmctsServiceIdAndJudgeRoleIdAndEffectiveFromIsLessThanEqual(
            hmctsServiceCode,
            judgeRoleId,
            sittingDate
        ).orElseThrow(() -> new IllegalArgumentException(
            "Fee not set/active for hmctsServiceCode and judgeRoleTypeId " + judgeRoleId)
        );
    }
}
