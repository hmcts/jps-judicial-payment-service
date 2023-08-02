package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class SittingRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordService.class);

    private final SittingRecordRepository sittingRecordRepository;

    public List<SittingRecord> getSittingRecords(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = sittingRecordRepository.find(
            recordSearchRequest,
            hmctsServiceCode
        );

        return dbSittingRecords.stream()
            .map(sittingRecord -> SittingRecord.builder()
                .am(sittingRecord.isAm() ? AM.name() : null)
                .changedByUserId(sittingRecord.getChangedByUserId())
                .changedByUserName(sittingRecord.getChangedByUserName())
                .changedDateTime(sittingRecord.getChangedByDateTime())
                .contractTypeId(sittingRecord.getContractTypeId())
                .createdByUserId(sittingRecord.getCreatedByUserId())
                .createdByUserName(sittingRecord.getCreatedByUserName())
                .createdDateTime(sittingRecord.getCreatedDateTime())
                .epimsId(sittingRecord.getEpimsId())
                .hmctsServiceId(sittingRecord.getHmctsServiceId())
                .judgeRoleTypeId(sittingRecord.getJudgeRoleTypeId())
                .personalCode(sittingRecord.getPersonalCode())
                .pm(sittingRecord.isPm() ? PM.name() : null)
                .regionId(sittingRecord.getRegionId())
                .sittingDate(sittingRecord.getSittingDate())
                .sittingRecordId(sittingRecord.getId())
                .statusHistories(List.copyOf(sittingRecord.getStatusHistories()))
                .statusId(sittingRecord.getStatusId())
                .venueName(null)
                .build())
            .toList();
    }

    public int getTotalRecordCount(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {
        LOGGER.debug("getTotalRecordCount");

        return sittingRecordRepository.totalRecords(recordSearchRequest,
                                                    hmctsServiceCode);
    }

    @Transactional
    public void saveSittingRecords(String hmctsServiceCode,
                                   RecordSittingRecordRequest recordSittingRecordRequest) {
        LOGGER.debug("saveSittingRecords");
        recordSittingRecordRequest.getRecordedSittingRecords()
            .forEach(recordSittingRecord -> {
                uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
                    uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getAm).orElse(false))
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .epimsId(recordSittingRecord.getEpimsId())
                        .hmctsServiceId(hmctsServiceCode)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .pm(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                            .map(DurationBoolean::getPm).orElse(false))
                        .regionId(recordSittingRecord.getRegionId())
                        .sittingDate(recordSittingRecord.getSittingDate())
                        .statusId(StatusId.RECORDED.name())
                        .build();

                recordSittingRecord.setCreatedDateTime(LocalDateTime.now());

                StatusHistory statusHistory = StatusHistory.builder()
                    .changedByName(recordSittingRecordRequest.getRecordedByName())
                    .changedByUserId(recordSittingRecordRequest.getRecordedByIdamId())
                    .changedDateTime(recordSittingRecord.getCreatedDateTime())
                    .statusId(StatusId.RECORDED.name())
                    .build();

                sittingRecord.addStatusHistory(statusHistory);
                sittingRecordRepository.save(sittingRecord);
            });
    }

}
