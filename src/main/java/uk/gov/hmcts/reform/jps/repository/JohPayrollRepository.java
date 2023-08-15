package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;

@Repository
public interface JohPayrollRepository extends JpaRepository<JohPayroll, Long> {
}
