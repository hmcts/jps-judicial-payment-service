package uk.gov.hmcts.reform.jps.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.components.EvaluateDuplicate;
import uk.gov.hmcts.reform.jps.components.EvaluateMatchingDuration;
import uk.gov.hmcts.reform.jps.components.EvaluateOverlapDuration;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;


@Service
public class SittingRecordService {
    private final SittingRecordRepository sittingRecordRepository;
    private final EvaluateDuplicate evaluateDuplicate;
    private final EvaluateMatchingDuration evaluateMatchingDuration;
    private final EvaluateOverlapDuration evaluateOverlapDuration;
    private final StatusHistoryService statusHistoryService;

    @Autowired
    public SittingRecordService(SittingRecordRepository sittingRecordRepository,
                                EvaluateDuplicate evaluateDuplicate,
                                EvaluateMatchingDuration evaluateMatchingDuration,
                                EvaluateOverlapDuration evaluateOverlapDuration,
                                StatusHistoryService statusHistoryService) {
        this.sittingRecordRepository = sittingRecordRepository;
        this.evaluateDuplicate = evaluateDuplicate;
        this.evaluateMatchingDuration = evaluateMatchingDuration;
        this.evaluateOverlapDuration = evaluateOverlapDuration;
        this.statusHistoryService = statusHistoryService;

        this.evaluateDuplicate.next(this.evaluateMatchingDuration);
        this.evaluateMatchingDuration.next(this.evaluateOverlapDuration);
    }


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
                         .epimmsId(sittingRecord.getEpimmsId())
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
                                   List<SittingRecordWrapper> sittingRecordWrappers,
                                   String recordedByName,
                                   String recordedByIdamId) {
        sittingRecordWrappers
            .forEach(recordSittingRecordWrapper -> {
                SittingRecordRequest recordSittingRecord = recordSittingRecordWrapper.getSittingRecordRequest();
                uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
                    uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                        .sittingDate(recordSittingRecord.getSittingDate())
                        .statusId(RECORDED)
                        .regionId(recordSittingRecordWrapper.getRegionId())
                        .epimmsId(recordSittingRecord.getEpimmsId())
                        .hmctsServiceId(hmctsServiceCode)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getAm).orElse(false))
                        .pm(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getPm).orElse(false))
                        .build();

                recordSittingRecordWrapper.setCreatedDateTime(LocalDateTime.now());

                StatusHistory statusHistory = StatusHistory.builder()
                    .statusId(RECORDED)
                    .changeDateTime(LocalDateTime.now())
                    .changeByUserId(recordedByIdamId)
                    .changeByName(recordedByName)
                    .build();

                sittingRecord.addStatusHistory(statusHistory);
                sittingRecordRepository.save(sittingRecord);

                if (TRUE.equals(recordSittingRecord.getReplaceDuplicate())) {
                    //TODO: DELETE IJPS-49
                }
            });
    }

    public void checkDuplicateRecords(List<SittingRecordWrapper> sittingRecordWrappers) {
        sittingRecordWrappers
            .forEach(this::checkDuplicateRecords);
    }

    private void checkDuplicateRecords(SittingRecordWrapper sittingRecordWrapper) {
        SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();
        sittingRecordRepository.findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
            sittingRecordRequest.getSittingDate(),
            sittingRecordRequest.getEpimmsId(),
            sittingRecordRequest.getPersonalCode(),
            DELETED
        ).forEach(sittingRecordDuplicateCheckFields ->
                                                     evaluateDuplicate
                                                         .evaluate(sittingRecordWrapper,
                                                                   sittingRecordDuplicateCheckFields));
    }


    @Transactional
    public int submitSittingRecords(SubmitSittingRecordRequest submitSittingRecordRequest,
                                    String hmctsServiceCode) {

        List<Long> recordsToSubmit = sittingRecordRepository.findRecordsToSubmit(
            submitSittingRecordRequest,
            hmctsServiceCode
        );

        recordsToSubmit.forEach(sittingRecordId -> {
            statusHistoryService.insertRecord(sittingRecordId,
                                              SUBMITTED,
                                              submitSittingRecordRequest.getSubmittedByIdamId(),
                                              submitSittingRecordRequest.getSubmittedByName());

            sittingRecordRepository.updateToSubmitted(sittingRecordId);
        });

        return recordsToSubmit.size();
    }
}
