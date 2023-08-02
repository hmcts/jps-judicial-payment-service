package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Slf4j
public class SittingRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordService.class);

    private final SittingRecordRepository sittingRecordRepository;

    private SecurityUtils securityUtils;

    public List<SittingRecord> getSittingRecords(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = sittingRecordRepository.find(
            recordSearchRequest,
            hmctsServiceCode
        );

        return dbSittingRecords.stream()
            .map(sittingRecord -> SittingRecord.builder()
                .sittingRecordId(sittingRecord.getId())
                .sittingDate(sittingRecord.getSittingDate())
                .statusId(sittingRecord.getStatusId())
                .regionId(sittingRecord.getRegionId())
                .epimsId(sittingRecord.getEpimsId())
                .hmctsServiceId(sittingRecord.getHmctsServiceId())
                .personalCode(sittingRecord.getPersonalCode())
                .contractTypeId(sittingRecord.getContractTypeId())
                .judgeRoleTypeId(sittingRecord.getJudgeRoleTypeId())
                .am(sittingRecord.isAm() ? AM.name() : null)
                .pm(sittingRecord.isPm() ? PM.name() : null)
                .createdDateTime(sittingRecord.getCreatedDateTime())
                .createdByUserId(sittingRecord.getCreatedByUserId())
                .createdByUserName(sittingRecord.getCreatedByUserName())
                .changeDateTime(sittingRecord.getChangeByDateTime())
                .changeByUserId(sittingRecord.getChangeByUserId())
                .changeByUserName(sittingRecord.getChangeByUserName())
                .statusHistories(List.copyOf(sittingRecord.getStatusHistories()))
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
    @PreAuthorize("hasAnyAuthority('jps-recorder', 'jps-submitter', 'jps-admin')")
    public void deleteSittingRecord(Long sittingRecordId) {
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
            = sittingRecordRepository.findById(sittingRecordId)
            .orElseThrow(() -> new ResourceNotFoundException("Sitting Record ID Not Found"));

        if (securityUtils.getUserInfo().getRoles().contains("jps-recorder")) {
            recorderDelete(sittingRecord);
        } else if (securityUtils.getUserInfo().getRoles().contains("jps-submitter")) {
            deleteSittingRecord(sittingRecord, RECORDED);
        } else if (securityUtils.getUserInfo().getRoles().contains("jps-admin")) {
            deleteSittingRecord(sittingRecord, SUBMITTED);
        }
    }

    private void deleteSittingRecord(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord,
                                     StatusId recorded) {

        if (sittingRecord.getStatusId().equals(recorded.name())) {
            deleteSittingRecord(sittingRecord);
        } else {
            throw new ConflictException("Sitting Record Status ID is in wrong state");
        }
    }

    private void deleteSittingRecord(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(StatusId.DELETED.name())
            .changeDateTime(LocalDateTime.now())
            .changeByUserId(securityUtils.getUserInfo().getUid())
            .changeByName(securityUtils.getUserInfo().getName())
            .build();

        sittingRecord.addStatusHistory(statusHistory);
        sittingRecord.setStatusId(DELETED.name());
        sittingRecordRepository.save(sittingRecord);
    }

    private void recorderDelete(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        if (sittingRecord.getStatusId().equals(RECORDED.name())) {
            StatusHistory recordedStatusHistory = sittingRecord.getStatusHistories().stream()
                .filter(statusHistory -> statusHistory.getStatusId().equals(RECORDED.name()))
                .filter(statusHistory -> statusHistory.getChangeByUserId().equals(securityUtils.getUserInfo().getUid()))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User IDAM ID does not match the oldest Changed by IDAM ID "));

            deleteSittingRecord(recordedStatusHistory.getSittingRecord());
        } else {
            throw new ConflictException("Sitting Record Status ID is in wrong state");
        }
    }

}
