package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.SittingDays;

import java.util.Optional;

@Repository
public interface SittingDaysRepository extends JpaRepository<SittingDays, Long> {


    @Query("""
        select sd.sittingCount
        from SittingDays sd
        where sd.personalCode = :personalCode
        and sd.financialYear = :financialYear
        """)
    Optional<Long> findSittingCountByPersonalCodeAndFinancialYear(String personalCode, String financialYear);
}
