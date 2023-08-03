package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.repository.ServiceRepository;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ServiceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceService.class);

    private final ServiceRepository serviceRepository;

    public uk.gov.hmcts.reform.jps.domain.Service findService(String hmctsServiceId) {
        return serviceRepository.findByHmctsServiceId(hmctsServiceId);
    }

}
