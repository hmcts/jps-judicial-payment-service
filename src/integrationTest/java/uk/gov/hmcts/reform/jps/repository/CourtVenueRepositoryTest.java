package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.CourtVenue;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_COURT_VENUE;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class CourtVenueRepositoryTest {

    @Autowired
    private CourtVenueRepository courtVenueRepository;

    @Test
    void shouldSaveCourtVenue() {
        CourtVenue courtVenue = createCourtVenue();

        CourtVenue persistedCourtVenue = courtVenueRepository.save(courtVenue);

        assertThat(persistedCourtVenue).isNotNull();
        assertThat(persistedCourtVenue.getId()).isNotNull();
        assertThat(persistedCourtVenue).isEqualTo(courtVenue);
    }

    @Test
    void shouldUpdateCourtVenueWhenRecordIsPresent() {
        CourtVenue courtVenue = createCourtVenue();

        courtVenueRepository.save(courtVenue);
        Optional<CourtVenue> optionalCourtVenue = courtVenueRepository.findById(courtVenue.getId());

        assertThat(optionalCourtVenue).isPresent();

        CourtVenue persistedCourtVenue = optionalCourtVenue.get();
        persistedCourtVenue.setEpimmsId("abc");
        persistedCourtVenue.setHmctsServiceId("xyz");
        persistedCourtVenue.setCostCenterCode("cost");

        CourtVenue updateCourtVenue = courtVenueRepository.save(persistedCourtVenue);
        assertThat(updateCourtVenue).isNotNull();
        assertThat(updateCourtVenue).isEqualTo(persistedCourtVenue);
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<CourtVenue> optionalCourtVenue = courtVenueRepository.findById(100L);
        assertThat(optionalCourtVenue).isEmpty();
    }

    @Test
    void shouldDeleteSelectedRecord() {
        CourtVenue courtVenue = createCourtVenue();
        courtVenueRepository.save(courtVenue);
        courtVenueRepository.deleteById(courtVenue.getId());

        Optional<CourtVenue> optionalCourtVenue = courtVenueRepository.findById(courtVenue.getId());

        assertThat(optionalCourtVenue).isEmpty();
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_COURT_VENUE})
    void shouldDeleteRecordsWhenIdsPassed() {
        List<Long> courtVenueIds = courtVenueRepository.findAll().stream()
            .map(CourtVenue::getId)
            .toList();
        assertThat(courtVenueIds).isNotEmpty();
        courtVenueRepository.deleteByIds(courtVenueIds);
        List<CourtVenue> courtVenues = courtVenueRepository.findAll();
        assertThat(courtVenues).isEmpty();
    }

    private CourtVenue createCourtVenue() {
        return CourtVenue.builder()
            .epimmsId("1234")
            .hmctsServiceId("test")
            .costCenterCode("456")
            .build();
    }

}
