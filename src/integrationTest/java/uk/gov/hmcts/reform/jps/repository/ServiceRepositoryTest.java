package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.Service;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class ServiceRepositoryTest {

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    void shouldSaveService() {
        Service persistedService = getPersistedService();

        assertThat(persistedService).isNotNull();
        assertThat(persistedService.getId()).isNotNull();
    }

    @Test
    void shouldUpdateServiceWhenRecordIsPresent() {
        Service service = getPersistedService();

        Optional<Service> optionalServiceToUpdate = serviceRepository
            .findById(service.getId());
        assertThat(optionalServiceToUpdate).isPresent();

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
        Service service = getPersistedService();
        Optional<Service> optionalServiceToUpdate = serviceRepository
            .findById(service.getId());
        assertThat(optionalServiceToUpdate).isPresent();

        Service settingRecordToDelete = optionalServiceToUpdate.get();
        serviceRepository.deleteById(settingRecordToDelete.getId());

        optionalServiceToUpdate = serviceRepository.findById(settingRecordToDelete.getId());
        assertThat(optionalServiceToUpdate).isEmpty();
    }

    private Service getPersistedService() {
        Service service = Service.builder()
            .hmctsServiceId("BB4")
            .accountCenterCode("123")
            .onboardingStartDate(LocalDate.now())
            .retentionTimeInMonths(2)
            .build();

        return serviceRepository.save(service);
    }
}
