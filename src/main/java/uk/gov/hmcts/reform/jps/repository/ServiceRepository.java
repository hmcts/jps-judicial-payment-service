package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    public Service findByHmctsServiceId(String hmctsServiceId);
}
