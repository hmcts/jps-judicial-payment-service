package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SittingRecordService {
    private final SittingRecordRepository sittingRecordRepository;
    private final DuplicateCheckerService duplicateCheckerService;
    private final SecurityUtils securityUtils;

    @Lazy
    private final SittingRecordService self;

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
                if (TRUE.equals(recordSittingRecord.getReplaceDuplicate())) {
                    self.deleteSittingRecord(recordSittingRecord.getSittingRecordId());
                }

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
            });
    }

    public void checkDuplicateRecords(List<SittingRecordWrapper> sittingRecordWrappers) {
        sittingRecordWrappers.stream()
            .filter(not(sittingRecordWrapper ->
                            nonNull(sittingRecordWrapper.getSittingRecordRequest().getReplaceDuplicate())
                                && sittingRecordWrapper.getSittingRecordRequest().getReplaceDuplicate()))
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
                                                     duplicateCheckerService
                                                         .evaluate(sittingRecordWrapper,
                                                                   sittingRecordDuplicateCheckFields));
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

        if (sittingRecord.getStatusId() == recorded) {
            deleteSittingRecord(sittingRecord);
        } else {
            throw new ConflictException("Sitting Record Status ID is in wrong state");
        }
    }

    private void deleteSittingRecord(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(DELETED)
            .changeDateTime(LocalDateTime.now())
            .changeByUserId(securityUtils.getUserInfo().getUid())
            .changeByName(securityUtils.getUserInfo().getName())
            .build();

        sittingRecord.addStatusHistory(statusHistory);
        sittingRecord.setStatusId(DELETED);
        sittingRecordRepository.save(sittingRecord);
    }

    private void recorderDelete(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        if (sittingRecord.getStatusId() == RECORDED) {
            StatusHistory recordedStatusHistory = sittingRecord.getStatusHistories().stream()
                .filter(statusHistory -> statusHistory.getStatusId() == RECORDED)
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
