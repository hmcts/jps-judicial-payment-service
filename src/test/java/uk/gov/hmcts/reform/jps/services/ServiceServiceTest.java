package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.jps.domain.Service;
import uk.gov.hmcts.reform.jps.repository.ServiceRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ServiceService.class})
@ExtendWith(SpringExtension.class)
class ServiceServiceTest {

    @MockBean
    private ServiceRepository serviceRepository;

    @Autowired
    private ServiceService serviceService;

    /**
     * Method under test: {@link ServiceService#findService(String)}.
     */
    @Test
    void testFindService() {
        Service service = createService();
        when(serviceRepository.findByHmctsServiceId(Mockito.<String>any())).thenReturn(service);
        assertSame(service, serviceService.findService("42"));
        verify(serviceRepository).findByHmctsServiceId(Mockito.<String>any());
    }

    private Service createService() {
        Service service = new Service();
        service.setAccountCenterCode("3");
        service.setCloseRecordedRecordAfterTimeInMonths(1);
        service.setHmctsServiceId("42");
        service.setId(1L);
        service.setOnboardingStartDate(LocalDate.of(1970, 1, 1));
        service.setRetentionTimeInMonths(1);
        service.setServiceName("Service Name");
        return service;
    }
}

