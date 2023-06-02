package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.location.model.CourtVenue;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class LocationService {
    private final LocationServiceClient regionServiceClient;

    public void setRegionDetails(String hmctsServiceCode, List<SittingRecord> sittingRecords) {
        LocationApiResponse serviceCourtInfo = regionServiceClient.getCourtVenue(hmctsServiceCode);
        setRegionDetails(sittingRecords, serviceCourtInfo);
    }

    private void setRegionDetails(List<SittingRecord> sittingRecords, LocationApiResponse serviceCourtInfo) {
        sittingRecords.forEach(sittingRecord -> {
            Optional<CourtVenue> courtVenue = getCourtVenue(
                serviceCourtInfo,
                sittingRecord.getRegionId()
            );
            sittingRecord.setRegionName(courtVenue.map(CourtVenue::getRegion)
                                            .orElse("N/A"));
        });
    }

    private Optional<CourtVenue> getCourtVenue(LocationApiResponse serviceCourtInfo, String regionId) {
        return serviceCourtInfo.getCourtVenues().stream()
            .filter(courtVenue -> courtVenue.getRegionId().equals(regionId))
            .findAny();
    }
}
