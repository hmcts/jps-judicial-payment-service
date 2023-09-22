package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.in.CourtVenue;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueRequest;
import uk.gov.hmcts.reform.jps.repository.CourtVenueRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtVenueServiceTest {

    @Mock
    private CourtVenueRepository courtVenueRepository;

    @InjectMocks
    private CourtVenueService courtVenueService;

    @Test
    void shouldReturnCourtVenueIdsWhenCourVenueRequestSaved() {
        CourtVenueRequest request = new CourtVenueRequest();
        List<CourtVenue> courtVenueList = List.of(
            getCourtVenue(), getCourtVenue()
        );

        request.setCourtVenues(courtVenueList);

        when(courtVenueRepository.saveAll(anyList())).thenReturn(
            List.of(
                getDomainCourtVenue(1L), getDomainCourtVenue(2L)
            )
        );

        List<Long> ids = courtVenueService.save(request);
        assertThat(ids)
            .hasSize(2)
            .containsExactlyInAnyOrder(1L, 2L);

        verify(courtVenueRepository).saveAll(anyList());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCourtVenuesMissing() {
        CourtVenueRequest request = new CourtVenueRequest();

        assertThatThrownBy(() -> courtVenueService.save(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Court venues missing");
    }

    @Test
    void shouldDeleteWhenRequestContainsIds() {
        CourtVenueDeleteRequest courtVenueDeleteRequest = new CourtVenueDeleteRequest();
        courtVenueDeleteRequest.setCourtVenueIds(
            List.of(
                1L, 2L
            )
        );
        courtVenueService.delete(courtVenueDeleteRequest);
        verify(courtVenueRepository).deleteByIds(courtVenueDeleteRequest.getCourtVenueIds());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenIdsMissing() {
        CourtVenueDeleteRequest courtVenueDeleteRequest = new CourtVenueDeleteRequest();
        assertThatThrownBy(() -> courtVenueService.delete(courtVenueDeleteRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Court venue ids missing");
    }

    private uk.gov.hmcts.reform.jps.domain.CourtVenue getDomainCourtVenue(Long id) {
        uk.gov.hmcts.reform.jps.domain.CourtVenue courtVenue = new uk.gov.hmcts.reform.jps.domain.CourtVenue();
        courtVenue.setId(id);
        return courtVenue;
    }

    CourtVenue getCourtVenue() {
        CourtVenue courtVenue = new CourtVenue();
        courtVenue.setCostCenterCode("123");
        courtVenue.setHmctsServiceId("ABA5");
        courtVenue.setEpimmsId("1234");
        return courtVenue;
    }

}
