package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.jps.domain.Service;
import uk.gov.hmcts.reform.jps.repository.ServiceRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ServiceService.class})
@ExtendWith(SpringExtension.class)
class ServiceServiceTest {

    @MockBean
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceService serviceService;
    private Service service;

    @BeforeEach
    void beforeEach() {
        service = new Service();
        service.setAccountCenterCode("3");
        service.setCloseRecordedRecordAfterTimeInMonths(1);
        service.setHmctsServiceId("42");
        service.setId(1L);
        service.setOnboardingStartDate(LocalDate.of(1970, 1, 1));
        service.setRetentionTimeInMonths(1);
        service.setServiceName("Service Name");
    }

    /**
     * Method under test: {@link ServiceService#findService(String)}.
     */
    @Test
    void testFindService() {
        when(serviceRepository.findByHmctsServiceId(anyString()))
            .thenReturn(Optional.of(service));

        assertThat(serviceService.findService("42"))
            .isPresent()
            .hasValue(service);
        verify(serviceRepository).findByHmctsServiceId(anyString());
    }

    @Test
    void shouldReturnOnBoardedServiceWhenPresent() {
        when(serviceRepository.findByHmctsServiceIdAndOnboardingStartDateLessThanEqual(
            anyString(),
            any()))
            .thenReturn(Optional.of(service));
        assertThat(serviceService.isServiceOnboarded("42"))
            .isTrue();
    }

    @Test
    void shouldReturnEmptyOptionalWhenServiceIsNotOnboarded() {
        when(serviceRepository.findByHmctsServiceIdAndOnboardingStartDateLessThanEqual(
            anyString(),
            any()))
            .thenReturn(Optional.empty());
        assertThat(serviceService.isServiceOnboarded("33"))
            .isFalse();
    }
}

