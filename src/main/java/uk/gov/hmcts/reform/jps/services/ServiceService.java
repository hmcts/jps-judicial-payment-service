package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.repository.ServiceRepository;

import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public Optional<uk.gov.hmcts.reform.jps.domain.Service> findService(String hmctsServiceId) {
        return serviceRepository.findByHmctsServiceId(hmctsServiceId);
    }

}
