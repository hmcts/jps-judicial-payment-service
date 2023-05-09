package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class SittingRecordService {
    private final SittingRecordRepository sittingRecordRepository;

    public List<SittingRecord> getSittingRecords(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = sittingRecordRepository.find(
            recordSearchRequest,
            hmctsServiceCode
        );
        String notSet = null;
        return dbSittingRecords.stream()
            .map(record ->
                     SittingRecord.builder()
                         .sittingRecordId(record.getId())
                         .sittingDate(record.getSittingDate())
                         .statusId(record.getStatusId())
                         .regionId(record.getRegionId())
                         .epimsId(record.getEpimsId())
                         .hmctsServiceId(record.getHmctsServiceId())
                         .personalCode(record.getPersonalCode())
                         .contractTypeId(record.getContractTypeId())
                         .judgeRoleTypeId(record.getJudgeRoleTypeId())
                         .am(record.isAm() ? AM.name() : notSet)
                         .pm(record.isPm() ? PM.name() : notSet)
                         .createdDateTime(record.getCreatedDateTime())
                         .createdByUserId(record.getCreatedByUserId())
                         .changeDateTime(record.getChangeDateTime())
                         .changeByUserId(record.getChangeByUserId())
                         .build())
            .collect(toList());

    }

    public int getTotalRecordCount(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {

        return sittingRecordRepository.totalRecords(recordSearchRequest,
                                                    hmctsServiceCode);
    }
}
