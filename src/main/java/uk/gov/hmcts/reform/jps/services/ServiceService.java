package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.repository.ServiceRepository;

import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public Optional<uk.gov.hmcts.reform.jps.domain.Service> findService(String hmctsServiceId) {
        return serviceRepository.findByHmctsServiceId(hmctsServiceId);
    }

    public boolean isServiceOnboarded(String hmctsServiceId) {
        return serviceRepository.findByHmctsServiceIdAndOnboardingStartDateLessThanEqual(
            hmctsServiceId,
            LocalDate.now())
            .isPresent();
    }
}
