package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;

public class CourtVenueServiceITest extends BaseTest {

    @Autowired
    private CourtVenueService courtVenueService;

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_COURT_VENUE})
    void shouldReturnCourtVenueWhenRecordPresent() {
        assertThat(courtVenueService.getCourtVenue("BBA3", "852649"))
            .isPresent();
    }
}
