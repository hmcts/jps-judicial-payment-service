package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.jps.domain.SittingDays;

public interface SittingDaysRepository extends JpaRepository<SittingDays, Long> {
}
