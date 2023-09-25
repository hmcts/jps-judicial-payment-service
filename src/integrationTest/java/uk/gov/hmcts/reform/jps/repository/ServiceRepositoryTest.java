package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.domain.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.jps.BaseTest.INSERT_SERVICE;
import static uk.gov.hmcts.reform.jps.BaseTest.RESET_DATABASE;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class ServiceRepositoryTest {

    public static final String TEST_SERVICE = "TestService";
    @Autowired
    private ServiceRepository serviceRepository;

    private static final String HMCTS_SERVICE_ID = "BB4";
    private static Service service;

    @BeforeAll
    void beforeAll() {
        service = serviceRepository
            .save(
                Service.builder()
                     .hmctsServiceId(HMCTS_SERVICE_ID)
                     .serviceName(TEST_SERVICE)
                     .accountCenterCode("123")
                     .onboardingStartDate(LocalDate.now())
                     .retentionTimeInMonths(2)
                     .closeRecordedRecordAfterTimeInMonths(2)
                     .build());
    }

    @Test
    void shouldSaveService() {
        assertThat(service)
            .isNotNull()
            .matches(persistedService -> Objects.nonNull(persistedService.getId()));
    }

    @Test
    void shouldUpdateServiceWhenRecordIsPresent() {
        Optional<Service> optionalServiceToUpdate = serviceRepository
            .findById(service.getId());
        assertTrue(optionalServiceToUpdate.isPresent());

        Service serviceToUpdate = optionalServiceToUpdate.get();
        serviceToUpdate.setHmctsServiceId("Updated");
        serviceToUpdate.setOnboardingStartDate(LocalDate.now().minusDays(2));
        serviceToUpdate.setRetentionTimeInMonths(3);

        Service updatedService = serviceRepository.save(serviceToUpdate);
        assertThat(updatedService).isNotNull();
        assertThat(updatedService).isEqualTo(serviceToUpdate);
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<Service> service = serviceRepository.findById(100L);
        assertThat(service).isEmpty();
    }

    @Test
    void shouldDeleteSelectedRecord() {
        Optional<Service> optionalServiceToUpdate = serviceRepository
            .findById(service.getId());
        assertTrue(optionalServiceToUpdate.isPresent());

        Service settingRecordToDelete = optionalServiceToUpdate.get();
        serviceRepository.deleteById(settingRecordToDelete.getId());

        optionalServiceToUpdate = serviceRepository.findById(settingRecordToDelete.getId());
        assertTrue(optionalServiceToUpdate.isEmpty());
    }

    @Test
    void shouldSuccessfullyFindServiceByHmctsServiceId() {
        assertThat(serviceRepository.findByHmctsServiceId(HMCTS_SERVICE_ID))
            .isPresent();
    }

    @Test
    void shouldFailToFindServiceByHmctsServiceId() {
        assertThat(serviceRepository.findByHmctsServiceId("FFGHJHUJ"))
            .isEmpty();
    }

    @Test
    @Sql(scripts = {RESET_DATABASE, INSERT_SERVICE})
    void shouldDeleteRecordsWhenIdsPassed() {
        List<Long> courtVenueIds = serviceRepository.findAll().stream()
            .map(Service::getId)
            .toList();
        assertThat(courtVenueIds).isNotEmpty();
        serviceRepository.deleteByIds(courtVenueIds);
        List<Service> courtVenues = serviceRepository.findAll();
        assertThat(courtVenues).isEmpty();
    }
}
