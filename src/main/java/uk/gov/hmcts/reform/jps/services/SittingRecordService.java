package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
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
            .map(sittingRecord ->
                     SittingRecord.builder()
                         .sittingRecordId(sittingRecord.getId())
                         .sittingDate(sittingRecord.getSittingDate())
                         .statusId(sittingRecord.getStatusId())
                         .regionId(sittingRecord.getRegionId())
                         .epimsId(sittingRecord.getEpimsId())
                         .hmctsServiceId(sittingRecord.getHmctsServiceId())
                         .personalCode(sittingRecord.getPersonalCode())
                         .contractTypeId(sittingRecord.getContractTypeId())
                         .judgeRoleTypeId(sittingRecord.getJudgeRoleTypeId())
                         .am(sittingRecord.isAm() ? AM.name() : notSet)
                         .pm(sittingRecord.isPm() ? PM.name() : notSet)
                         .createdDateTime(sittingRecord.getCreatedDateTime())
                         .createdByUserId(sittingRecord.getCreatedByUserId())
                         .changeDateTime(sittingRecord.getChangeDateTime())
                         .changeByUserId(sittingRecord.getChangeByUserId())
                         .build())
            .toList();

    }


    public int getTotalRecordCount(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {

        return sittingRecordRepository.totalRecords(recordSearchRequest,
                                                    hmctsServiceCode);
    }

    @Transactional
    public void saveSittingRecords(String hmctsServiceCode,
                                   RecordSittingRecordRequest recordSittingRecordRequest) {
        recordSittingRecordRequest.getRecordedSittingRecords()
            .forEach(recordSittingRecord -> {
                uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
                    uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                        .sittingDate(recordSittingRecord.getSittingDate())
                        .statusId(StatusId.RECORDED.name())
                        .regionId(recordSittingRecord.getRegionId())
                        .epimsId(recordSittingRecord.getEpimsId())
                        .hmctsServiceId(hmctsServiceCode)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getAm).orElse(false))
                        .pm(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getPm).orElse(false))
                        .build();

                recordSittingRecord.setCreatedDateTime(LocalDateTime.now());

                StatusHistory statusHistory = StatusHistory.builder()
                    .statusId(StatusId.RECORDED.name())
                    .changeDateTime(recordSittingRecord.getCreatedDateTime())
                    .changeByUserId(recordSittingRecordRequest.getRecordedByIdamId())
                    .changeByName(recordSittingRecordRequest.getRecordedByName())
                    .build();

                sittingRecord.addStatusHistory(statusHistory);
                sittingRecordRepository.save(sittingRecord);
            });
    }

    @Transactional
    public void deleteSittingRecords(Long sittingRecordId) {

        if(Idam role = ("Recorder"))(
            if(sitting_record.status_id = ("recorded"))(

                )
        )else if(Idam role = ("Submitter"))(
            if(sitting_record.status_id = ("recorded"))(
            )
            )else if(Idam role = ("Admin"))(

            )else(
                throw new RuntimeException("409");
            )

                StatusHistory statusHistory = StatusHistory.builder()
                    .statusId(StatusId.DELETED.name())
                    .changeDateTime(recordSittingRecord.getCreatedDateTime())
                    .changeByUserId(recordSittingRecordRequest.getRecordedByIdamId())
                    .changeByName(recordSittingRecordRequest.getRecordedByName())
                    .build();

                sittingRecord.addStatusHistory(statusHistory);
                sittingRecordRepository.save(sittingRecord);

    }

    private SittingRecord getSittingRecord(Long sittingRecordId) {
        Optional<SittingRecord> sittingRecordOptional = sittingRecordRepository.findById(sittingRecordId);

        if (sittingRecordOptional.isEmpty()) {
            throw new sittingRecordyNotFoundException(sittingRecordId, HEARING_ACTUALS_ID_NOT_FOUND);
        }
        return sittingRecordOptional.get();
    }
}
}
