package uk.gov.hmcts.reform.jps.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    @Query("select service from Service service where service.hmctsServiceId=:hmctsServiceId")
    Service findServiceByHmctsServiceId(@Param("hmctsServiceId") String hmctsServiceId);

}
