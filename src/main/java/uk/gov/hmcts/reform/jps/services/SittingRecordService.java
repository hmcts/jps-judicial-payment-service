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
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
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
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.Boolean.TRUE;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_ADMIN;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_SUBMITTER;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SittingRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordService.class);

    private final SittingRecordRepository sittingRecordRepository;
    private final DuplicateCheckerService duplicateCheckerService;
    private final SecurityUtils securityUtils;


    private final LocationService locationService;

    private final ServiceService serviceService;


    public List<SittingRecord> getSittingRecords(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = sittingRecordRepository.find(
            recordSearchRequest,
            hmctsServiceCode
        );

        return dbSittingRecords.stream()
            .map(sittingRecord -> SittingRecord.builder()
                .accountCode(getAccountCode(hmctsServiceCode))
                .am(sittingRecord.isAm() ? AM.name() : null)
                .changedByUserId(sittingRecord.getChangedByUserId())
                .changedByUserName(sittingRecord.getChangedByUserName())
                .changedDateTime(sittingRecord.getChangedByDateTime())
                .contractTypeId(sittingRecord.getContractTypeId())
                .createdByUserId(sittingRecord.getCreatedByUserId())
                .createdByUserName(sittingRecord.getCreatedByUserName())
                .createdDateTime(sittingRecord.getCreatedDateTime())
                .epimmsId(sittingRecord.getEpimmsId())
                .hmctsServiceId(sittingRecord.getHmctsServiceId())
                .judgeRoleTypeId(sittingRecord.getJudgeRoleTypeId())
                .personalCode(sittingRecord.getPersonalCode())
                .pm(sittingRecord.isPm() ? PM.name() : null)
                .regionId(sittingRecord.getRegionId())
                .sittingDate(sittingRecord.getSittingDate())
                .sittingRecordId(sittingRecord.getId())
                .statusHistories(List.copyOf(sittingRecord.getStatusHistories()))
                .statusId(sittingRecord.getStatusId())
                .venueName(getVenueName(hmctsServiceCode, sittingRecord.getEpimmsId()))
                .build()
            )
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
                                   List<SittingRecordWrapper> sittingRecordWrappers,
                                   String recordedByName,
                                   String recordedByIdamId) {
        LOGGER.debug("saveSittingRecords");
        sittingRecordWrappers
            .forEach(recordSittingRecordWrapper -> {
                SittingRecordRequest recordSittingRecord = recordSittingRecordWrapper.getSittingRecordRequest();
                if (POTENTIAL_DUPLICATE_RECORD == recordSittingRecordWrapper.getErrorCode()
                        && TRUE.equals(recordSittingRecord.getReplaceDuplicate())) {
                    uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
                            = sittingRecordRepository.findById(recordSittingRecordWrapper.getSittingRecordId())
                            .orElseThrow(() -> new ResourceNotFoundException("Sitting Record ID Not Found"));

                    deleteSittingRecord(sittingRecord);
                }

                uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
                    uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getAm).orElse(false))
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .epimmsId(recordSittingRecord.getEpimmsId())
                        .hmctsServiceId(hmctsServiceCode)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .pm(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                            .map(DurationBoolean::getPm).orElse(false))
                        .regionId(recordSittingRecordWrapper.getRegionId())
                        .sittingDate(recordSittingRecord.getSittingDate())
                        .statusId(RECORDED)
                        .build();

                recordSittingRecordWrapper.setCreatedDateTime(LocalDateTime.now());

                StatusHistory statusHistory = StatusHistory.builder()
                    .changedByName(recordedByName)
                    .changedByUserId(recordedByIdamId)
                    .changedDateTime(recordSittingRecordWrapper.getCreatedDateTime())
                    .statusId(RECORDED)
                    .build();

                sittingRecord.addStatusHistory(statusHistory);
                sittingRecordRepository.save(sittingRecord);
            });
    }

    public void checkDuplicateRecords(List<SittingRecordWrapper> sittingRecordWrappers) {
        sittingRecordWrappers.stream()
            .filter(sittingRecordWrapper ->
                            VALID == sittingRecordWrapper.getErrorCode())
            .forEach(this::checkDuplicateRecords);
    }

    private void checkDuplicateRecords(SittingRecordWrapper sittingRecordWrapper) {
        SittingRecordRequest sittingRecordRequest = sittingRecordWrapper.getSittingRecordRequest();

        try (Stream<SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields> stream
                     = sittingRecordRepository.findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                DELETED
        ).stream()) {
            stream.forEach(sittingRecordDuplicateCheckFields ->
                    duplicateCheckerService
                            .evaluate(sittingRecordWrapper,
                                    sittingRecordDuplicateCheckFields));
        }
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('" + JPS_RECORDER + "','" + JPS_SUBMITTER + "','" + JPS_ADMIN + "')")
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
            .changedDateTime(LocalDateTime.now())
            .changedByUserId(securityUtils.getUserInfo().getUid())
            .changedByName(securityUtils.getUserInfo().getName())
            .build();

        sittingRecord.addStatusHistory(statusHistory);
        sittingRecord.setStatusId(DELETED);
        sittingRecordRepository.save(sittingRecord);
    }

    private void recorderDelete(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        if (sittingRecord.getStatusId() == RECORDED) {
            StatusHistory recordedStatusHistory = sittingRecord.getStatusHistories().stream()
                .filter(statusHistory -> statusHistory.getStatusId() == RECORDED)
                .filter(statusHistory ->
                            statusHistory.getChangedByUserId().equals(securityUtils.getUserInfo().getUid()))
                .findAny()
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User IDAM ID does not match the oldest Changed by IDAM ID "));

            deleteSittingRecord(recordedStatusHistory.getSittingRecord());
        } else {
            throw new ConflictException("Sitting Record Status ID is in wrong state");
        }
    }

    private String getAccountCode(String hmctsServiceCode) {
        return serviceService.findService(hmctsServiceCode)
            .map(uk.gov.hmcts.reform.jps.domain.Service::getAccountCenterCode)
            .orElse(null);

    }

    private String getVenueName(String hmctsServiceCode, String epimmsId) {
        return locationService.getVenueName(hmctsServiceCode, epimmsId);
    }
}
