package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.repository.StatusHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.INVALID_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class SittingRecordService {
    private final SittingRecordRepository sittingRecordRepository;
    private final StatusHistoryRepository statusHistoryRepository;

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
                        .statusId(StatusId.RECORDED)
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
                    .statusId(StatusId.RECORDED)
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
        ).forEach(sittingRecordDuplicateCheckFields -> {
            if (isDuplicate(sittingRecordRequest, sittingRecordDuplicateCheckFields)) {
                if (isMatchingDuration(sittingRecordRequest, sittingRecordDuplicateCheckFields)) {
                    checkRecordedSittingRecords(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
                } else if (isOverlappingDuration(sittingRecordRequest, sittingRecordDuplicateCheckFields)) {
                    if (sittingRecordDuplicateCheckFields.getStatusId() == StatusId.RECORDED) {
                        sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
                    } else if (sittingRecordDuplicateCheckFields.getStatusId() != DELETED) {
                        sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
                    }
                    updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
                }
            }
        });
    }

    private boolean isMatchingDuration(SittingRecordRequest sittingRecordRequest, SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields) {
        return sittingRecordDuplicateCheckFields.getPm()
            .equals(sittingRecordRequest.getDurationBoolean().getPm())
            && sittingRecordDuplicateCheckFields.getAm()
            .equals(sittingRecordRequest.getDurationBoolean().getAm());
    }

    private boolean isOverlappingDuration(SittingRecordRequest sittingRecordRequest, SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields) {
        return ((TRUE.equals(sittingRecordDuplicateCheckFields.getPm())
            && TRUE.equals(sittingRecordDuplicateCheckFields.getAm()))
            && (sittingRecordRequest.getDurationBoolean().getPm()
            || sittingRecordRequest.getDurationBoolean().getAm()))
            || ((sittingRecordRequest.getDurationBoolean().getPm()
            && sittingRecordRequest.getDurationBoolean().getAm())
            && (TRUE.equals(sittingRecordDuplicateCheckFields.getPm())
            || TRUE.equals(sittingRecordDuplicateCheckFields.getAm()))
        );
    }

    private boolean isDuplicate(SittingRecordRequest sittingRecordRequest, SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields sittingRecordDuplicateCheckFields) {
        return sittingRecordDuplicateCheckFields.getEpimmsId().equals(sittingRecordRequest.getEpimmsId())
            && sittingRecordDuplicateCheckFields.getSittingDate().isEqual(sittingRecordRequest.getSittingDate())
            && sittingRecordDuplicateCheckFields.getPersonalCode()
            .equals(sittingRecordRequest.getPersonalCode());
    }

    private void checkRecordedSittingRecords(SittingRecordWrapper sittingRecordWrapper,
                                             SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                                                 sittingRecordDuplicateCheckFields) {
        if (sittingRecordDuplicateCheckFields.getStatusId() == StatusId.RECORDED) {
            if (sittingRecordDuplicateCheckFields.getJudgeRoleTypeId()
                .equals(sittingRecordWrapper.getSittingRecordRequest().getJudgeRoleTypeId())) {
                sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
            } else {
                SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();
                if (TRUE.equals(sittingRecordRequest.getReplaceDuplicate())) {
                    sittingRecordWrapper.setErrorCode(VALID);
                } else {
                    sittingRecordWrapper.setErrorCode(POTENTIAL_DUPLICATE_RECORD);
                }
            }

            if (sittingRecordWrapper.getErrorCode() != VALID) {
                updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
            }
        } else if (sittingRecordDuplicateCheckFields.getStatusId() != DELETED) {
            sittingRecordWrapper.setErrorCode(INVALID_DUPLICATE_RECORD);
            updateFromStatusHistory(sittingRecordWrapper, sittingRecordDuplicateCheckFields);
        }
    }

    private void updateFromStatusHistory(SittingRecordWrapper sittingRecordWrapper,
                                         SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
                                             checkFields) {
        Sort.TypedSort<StatusHistory> sort = Sort.sort(StatusHistory.class);
        Optional<StatusHistory> lastStatusHistory = statusHistoryRepository.findFirstBySittingRecord(
            uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                .id(checkFields.getId())
                .build(),
            sort.by(StatusHistory::getId).descending()
        );

        lastStatusHistory.ifPresent(lastStatusHistoryRecorded -> {
            sittingRecordWrapper.setCreatedByName(lastStatusHistoryRecorded.getChangeByName());
            sittingRecordWrapper.setCreatedDateTime(lastStatusHistoryRecorded.getChangeDateTime());
            sittingRecordWrapper.setStatusId(lastStatusHistoryRecorded.getStatusId());
        });
    }
}
