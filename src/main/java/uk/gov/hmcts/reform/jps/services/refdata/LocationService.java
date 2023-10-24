package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.model.ErrorCode;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class LocationService {
    private final LocationServiceClient regionServiceClient;

    public Optional<CourtVenue> getCourtVenue(String hmctsServiceCode, String epimmsId) {
        return getCourtVenue(
            getLocationApiResponse(hmctsServiceCode),
            epimmsId,
            (court, paraEpimmsId) -> court.getEpimmsId().equals(paraEpimmsId)
        );
    }

    private Optional<CourtVenue> getCourtVenue(LocationApiResponse serviceCourtInfo,
                                               String value,
                                               BiPredicate<CourtVenue, String> predicate) {
        return serviceCourtInfo.getCourtVenues().stream()
            .filter(courtVenue -> predicate.test(courtVenue, value))
            .findAny();
    }

    public String getVenueName(String hmctsServiceCode, String epimmsId) {
        return getCourtVenue(hmctsServiceCode, epimmsId)
            .map(CourtVenue::getVenueName)
            .orElse(null);
    }

    public String getCourtName(String hmctsServiceCode, String epimmsId) {
        return getCourtVenue(hmctsServiceCode, epimmsId)
            .map(CourtVenue::getCourtName)
            .orElse("");
    }

    public void setRegionName(String hmctsServiceCode,
                              List<SittingRecord> sittingRecords) {
        LocationApiResponse serviceCourtInfo = regionServiceClient.getCourtVenue(hmctsServiceCode);
        setRegionName(sittingRecords, serviceCourtInfo);
    }

    private void setRegionName(List<SittingRecord> sittingRecords, LocationApiResponse serviceCourtInfo) {
        sittingRecords.forEach(sittingRecord -> {
            Optional<CourtVenue> courtVenue = getCourtVenue(
                serviceCourtInfo,
                sittingRecord.getRegionId(),
                (court, regionId) -> court.getRegionId().equals(regionId)
            );
            sittingRecord.setRegionName(courtVenue.map(CourtVenue::getRegion)
                                            .orElse("N/A"));
        });
    }

    public void setRegionId(String hmctsServiceCode,
                            List<SittingRecordWrapper> recordedSittingWrappers) {
        LocationApiResponse serviceCourtInfo = regionServiceClient.getCourtVenue(hmctsServiceCode);
        setRegionId(recordedSittingWrappers, serviceCourtInfo);
    }

    private void setRegionId(List<SittingRecordWrapper> recordedSittingWrappers,
                             LocationApiResponse serviceCourtInfo) {
        recordedSittingWrappers.forEach(sittingRecordWrapper -> {
            Optional<CourtVenue> courtVenue = getCourtVenue(
                serviceCourtInfo,
                sittingRecordWrapper.getSittingRecordRequest().getEpimmsId(),
                (court, epimmsId) -> court.getEpimmsId().equals(epimmsId)
            );
            if (courtVenue.isPresent()) {
                sittingRecordWrapper.setRegionId(courtVenue.get().getRegionId());
            } else {
                sittingRecordWrapper.setErrorCode(ErrorCode.INVALID_LOCATION);
            }
        });
    }

    private LocationApiResponse getLocationApiResponse(String hmctsServiceCode) {
        return regionServiceClient.getCourtVenue(hmctsServiceCode);
    }

    public List<CourtVenue> getCourtVenues(String hmctsServiceCode) {
        return getLocationApiResponse(hmctsServiceCode).getCourtVenues().stream()
                .toList();
    }
}
