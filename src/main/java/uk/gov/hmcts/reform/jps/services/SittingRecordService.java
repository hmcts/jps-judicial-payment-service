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
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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

    private final LocationService locationService;

    private final ServiceService serviceService;


    private final SecurityUtils securityUtils;

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

                createJudicialOfficeHolder(recordSittingRecord.getPersonalCode());

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
        if (Objects.isNull(serviceService)) {
            LOGGER.info("serviceService is NULL!");
            return null;
        }

        uk.gov.hmcts.reform.jps.domain.Service service = serviceService.findService(hmctsServiceCode);
        if (Objects.isNull(service)) {
            LOGGER.info("service is NULL!");
            return null;
        }

        return service.getAccountCenterCode();
    }

    private String getVenueName(String hmctsServiceCode, String epimmsId) {
        if (Objects.isNull(locationService)) {
            LOGGER.info("locationService is NULL!");
            return null;
        }

        return locationService.getVenueName(hmctsServiceCode, epimmsId);
    }

    private JudicialOfficeHolder createJudicialOfficeHolder(String personalCode) {
        return JudicialOfficeHolder.builder()
            .personalCode(personalCode)
            .build();

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
                .orElseThrow(() -> new ResourceNotFoundException(
                    "User IDAM ID does not match the oldest Changed by IDAM ID "));

            deleteSittingRecord(recordedStatusHistory.getSittingRecord());
        } else {
            throw new ConflictException("Sitting Record Status ID is in wrong state");
        }
    }

}
