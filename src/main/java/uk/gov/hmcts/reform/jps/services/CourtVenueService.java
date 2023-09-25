package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.CourtVenue;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueRequest;
import uk.gov.hmcts.reform.jps.repository.CourtVenueRepository;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Service
public class CourtVenueService {
    private final CourtVenueRepository courtVenueRepository;

    @Transactional
    public List<Long> save(CourtVenueRequest courtVenueRequest) {
        return Optional.ofNullable(courtVenueRequest.getCourtVenues())
            .orElseThrow(() -> new IllegalArgumentException("Court venues missing"))
            .stream()
            .map(courtVenue -> CourtVenue.builder()
                .epimmsId(courtVenue.getEpimmsId())
                .hmctsServiceId(courtVenue.getHmctsServiceId())
                .costCenterCode(courtVenue.getCostCenterCode())
                .build())
            .collect(collectingAndThen(
                toList(),
                courtVenues -> courtVenueRepository.saveAll(courtVenues)
                    .stream().map(CourtVenue::getId)
                    .toList()
            ));
    }

    @Transactional
    public void delete(CourtVenueDeleteRequest courtVenueRequest) {
        List<Long> ids = Optional.ofNullable(courtVenueRequest.getCourtVenueIds())
            .orElseThrow(() -> new IllegalArgumentException("Court venue ids missing"));
        courtVenueRepository.deleteByIds(ids);
    }
}
