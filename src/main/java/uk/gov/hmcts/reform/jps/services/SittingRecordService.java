package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
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


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class SittingRecordService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SittingRecordService.class);

    private final SittingRecordRepository sittingRecordRepository;

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
}
