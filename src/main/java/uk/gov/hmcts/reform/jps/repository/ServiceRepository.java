package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findByHmctsServiceId(String hmctsServiceId);

    Optional<Service> findByHmctsServiceIdAndOnboardingStartDateLessThanEqual(String hmctsServiceId, LocalDate date);

    @Modifying
    @Query("""
        delete from Service where id in :ids
        """)
    void deleteByIds(List<Long> ids);
}
