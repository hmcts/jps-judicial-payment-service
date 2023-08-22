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
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.ForbiddenException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

    private final LocationService locationService;

    private final ServiceService serviceService;


    private final SecurityUtils securityUtils;

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
                                   RecordSittingRecordRequest recordSittingRecordRequest) {
        recordSittingRecordRequest.getRecordedSittingRecords()
            .forEach(recordSittingRecord -> {

                uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord =
                    uk.gov.hmcts.reform.jps.domain.SittingRecord.builder()
                        .am(Optional.ofNullable(recordSittingRecord.getDurationBoolean())
                                .map(DurationBoolean::getAm).orElse(false))
                        .contractTypeId(recordSittingRecord.getContractTypeId())
                        .epimmsId(recordSittingRecord.getEpimmsId())
                        .hmctsServiceId(hmctsServiceCode)
                        .personalCode(recordSittingRecord.getPersonalCode())
                        .judgeRoleTypeId(recordSittingRecord.getJudgeRoleTypeId())
                        .personalCode(recordSittingRecord.getPersonalCode())
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
                save(sittingRecord);
            });
    }

    public void save(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        sittingRecordRepository.save(sittingRecord);
    }

    private String getAccountCode(String hmctsServiceCode) {
        return serviceService.findService(hmctsServiceCode)
            .map(uk.gov.hmcts.reform.jps.domain.Service::getAccountCenterCode)
            .orElse(null);
    }

    @Transactional
    @PreAuthorize("hasAnyAuthority('jps-recorder', 'jps-submitter', 'jps-admin')")
    public void deleteSittingRecord(Long sittingRecordId) {
        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
            = sittingRecordRepository.findRecorderSittingRecord(sittingRecordId, DELETED.name())
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
            .statusId(StatusId.DELETED.name())
            .changedDateTime(LocalDateTime.now())
            .changedByUserId(securityUtils.getUserInfo().getUid())
            .changedByName(securityUtils.getUserInfo().getName())
            .build();

        sittingRecord.addStatusHistory(statusHistory);
        sittingRecord.setStatusId(DELETED.name());
        sittingRecordRepository.save(sittingRecord);
    }

    private void stateCheck(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord,
                            StatusId recorded) {

        if (sittingRecord.getStatusId().equals(recorded.name())) {
            deleteSittingRecord(sittingRecord);
        } else {
            throw new ConflictException("Sitting Record Status ID is in wrong state");
        }
    }

    private void recorderDelete(uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord) {
        if (sittingRecord.getStatusId().equals(RECORDED.name())) {
            StatusHistory recordedStatusHistory = sittingRecord.getStatusHistories().stream()
                .filter(statusHistory -> statusHistory.getStatusId().equals(RECORDED.name()))
                .filter(statusHistory -> statusHistory.getChangedByUserId().equals(
                    securityUtils.getUserInfo().getUid()))
                .findAny()
                .orElseThrow(() -> new ForbiddenException(
                    "User IDAM ID does not match the oldest Changed by IDAM ID "));

            deleteSittingRecord(recordedStatusHistory.getSittingRecord());
        } else {
            throw new ConflictException("Sitting Record Status ID is in wrong state");
        }
    }

}
