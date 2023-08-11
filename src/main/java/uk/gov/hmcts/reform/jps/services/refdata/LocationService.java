package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.exceptions.InvalidLocationException;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
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
            (court, epimmId) -> court.getEpimmsId().equals(epimmId)
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
                            List<SittingRecordRequest> recordedSittingRecords) {
        LocationApiResponse serviceCourtInfo = getLocationApiResponse(hmctsServiceCode);
        setRegionId(recordedSittingRecords, serviceCourtInfo);
    }

    private void setRegionId(List<SittingRecordRequest> recordedSittingRecords,
                             LocationApiResponse serviceCourtInfo) {
        recordedSittingRecords.forEach(sittingRecordRequest -> {
            Optional<CourtVenue> courtVenue = getCourtVenue(
                serviceCourtInfo,
                sittingRecordRequest.getEpimmsId(),
                (court, epimmsId) -> court.getEpimmsId().equals(epimmsId)
            );
            sittingRecordRequest.setRegionId(courtVenue.map(CourtVenue::getRegionId)
                                                 .orElseThrow(InvalidLocationException::new));
        });
    }

    private LocationApiResponse getLocationApiResponse(String hmctsServiceCode) {
        return regionServiceClient.getCourtVenue(hmctsServiceCode);
    }

}
