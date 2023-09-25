package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.model.in.ServiceDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.ServiceRequest;
import uk.gov.hmcts.reform.jps.repository.ServiceRepository;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ServiceService {
    private final ServiceRepository serviceRepository;

    public Optional<uk.gov.hmcts.reform.jps.domain.Service> findService(String hmctsServiceId) {
        return serviceRepository.findByHmctsServiceId(hmctsServiceId);
    }

    @Transactional
    public uk.gov.hmcts.reform.jps.domain.Service save(uk.gov.hmcts.reform.jps.domain.Service service) {
        return serviceRepository.save(service);
    }

    @Transactional
    public List<Long> save(ServiceRequest serviceRequests) {
        return Optional.ofNullable(serviceRequests.getServices())
            .orElseThrow(() -> new IllegalArgumentException("Services missing"))
            .stream()
            .map(serviceRequest -> uk.gov.hmcts.reform.jps.domain.Service.builder()
                .hmctsServiceId(serviceRequest.getHmctsServiceId())
                .serviceName(serviceRequest.getServiceName())
                .accountCenterCode(serviceRequest.getAccountCenterCode())
                .onboardingStartDate(serviceRequest.getOnboardingStartDate())
                .retentionTimeInMonths(serviceRequest.getRetentionTimeInMonths())
                .closeRecordedRecordAfterTimeInMonths(serviceRequest.getCloseRecordedRecordAfterTimeInMonths())
                .build())
            .collect(collectingAndThen(
                toList(),
                services ->
                    serviceRepository.saveAll(services).stream()
                        .map(uk.gov.hmcts.reform.jps.domain.Service::getId)
                        .toList()
            ));

    }

    @Transactional
    public void delete(ServiceDeleteRequest serviceRequests) {
        List<Long> ids = Optional.ofNullable(serviceRequests.getServiceIds())
            .orElseThrow(() -> new IllegalArgumentException("Service ids missing"));
        serviceRepository.deleteByIds(ids);
    }
}
