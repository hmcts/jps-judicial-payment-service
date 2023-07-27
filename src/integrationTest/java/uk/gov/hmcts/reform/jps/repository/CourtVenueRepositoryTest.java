package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.CourtVenue;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class CourtVenueRepositoryTest {

    @Autowired
    private CourtVenueRepository courtVenueRepository;

    @BeforeEach
    void setUp() {
        courtVenueRepository.deleteAll();
    }

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

    private CourtVenue createCourtVenue() {
        return CourtVenue.builder()
            .epimmsId("1234")
            .hmctsServiceId("test")
            .costCenterCode("456")
            .build();
    }

}
