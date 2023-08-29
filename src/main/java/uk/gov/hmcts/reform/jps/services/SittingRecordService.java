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
import uk.gov.hmcts.reform.jps.exceptions.ForbiddenException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
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
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.CLOSED;
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
        try (Stream<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = sittingRecordRepository.find(
            recordSearchRequest,
            hmctsServiceCode
        )) {
            List<CourtVenue> courtVenues = locationService.getCourtVenues(hmctsServiceCode);
            String accountCode = getAccountCode(hmctsServiceCode);

            return dbSittingRecords
                 .map(sittingRecord -> SittingRecord.builder()
                     .accountCode(accountCode)
                     .am(sittingRecord.isAm())
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
                     .pm(sittingRecord.isPm())
                     .regionId(sittingRecord.getRegionId())
                     .sittingDate(sittingRecord.getSittingDate())
                     .sittingRecordId(sittingRecord.getId())
                     .statusHistories(List.copyOf(sittingRecord.getStatusHistories()))
                     .statusId(sittingRecord.getStatusId())
                     .venueName(getVenueName(courtVenues, sittingRecord.getEpimmsId()))
                     .build()
                 )
                .toList();
        }
    }

    private String getVenueName(List<CourtVenue> courtVenues, String empimmsId) {
        return courtVenues.stream()
                .filter(courtVenue ->  courtVenue.getEpimmsId().equals(empimmsId))
                .map(CourtVenue::getVenueName)
                .findAny()
                .orElse("");
    }

    public long getTotalRecordCount(
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
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .epimmsId(recordSittingRecord.getEpimmsId())
                        .hmctsServiceId(hmctsServiceCode)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                            .map(DurationBoolean::getAm).orElse(false))
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
                     = sittingRecordRepository.findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNotIn(
                sittingRecordRequest.getSittingDate(),
                sittingRecordRequest.getEpimmsId(),
                sittingRecordRequest.getPersonalCode(),
                List.of(DELETED, CLOSED)
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
            = sittingRecordRepository.findRecorderSittingRecord(sittingRecordId, DELETED)
            .orElseThrow(() -> new ResourceNotFoundException("Sitting Record ID Not Found"));

        if (securityUtils.getUserInfo().getRoles().contains("jps-recorder")) {
            recorderDelete(sittingRecord);
        } else if (securityUtils.getUserInfo().getRoles().contains("jps-submitter")) {
            stateCheck(sittingRecord, RECORDED);
        } else if (securityUtils.getUserInfo().getRoles().contains("jps-admin")) {
            stateCheck(sittingRecord, SUBMITTED);
        }
    }

    private void deleteSittingRecord(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(StatusId.DELETED)
            .changedDateTime(LocalDateTime.now())
            .changedByUserId(securityUtils.getUserInfo().getUid())
            .changedByName(securityUtils.getUserInfo().getName())
            .build();

        sittingRecord.addStatusHistory(statusHistory);
        sittingRecord.setStatusId(DELETED);
        sittingRecordRepository.save(sittingRecord);
    }

    private void stateCheck(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord,
                            StatusId recorded) {

        if (sittingRecord.getStatusId() == recorded) {
            deleteSittingRecord(sittingRecord);
        } else {
            throw new ForbiddenException("Sitting Record Status ID is in wrong state");
        }
    }

    private void recorderDelete(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        if (sittingRecord.getStatusId() == RECORDED) {
            StatusHistory recordedStatusHistory = sittingRecord.getStatusHistories().stream()
                .filter(statusHistory -> statusHistory.getStatusId() == RECORDED)
                .filter(statusHistory -> statusHistory.getChangedByUserId().equals(
                    securityUtils.getUserInfo().getUid()))
                .findAny()
                .orElseThrow(() -> new ForbiddenException(
                    "User IDAM ID does not match the oldest Changed by IDAM ID "));

            deleteSittingRecord(recordedStatusHistory.getSittingRecord());
        } else {
            throw new ForbiddenException("Sitting Record Status ID is in wrong state");
        }
    }

    private String getAccountCode(String hmctsServiceCode) {
        return serviceService.findService(hmctsServiceCode)
            .map(uk.gov.hmcts.reform.jps.domain.Service::getAccountCenterCode)
            .orElse(null);

    }
}
