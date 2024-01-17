package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(clearAutomatically = true)
    @Query("""
        update SittingDays sd set sd.sittingCount = :sittingCount
        where sd.judgeRoleTypeId = :judgeRoleTypeId
        and sd.personalCode = :personalCode
        and sd.financialYear = :financialYear
        """)
    void updateSittingCount(Long sittingCount, String judgeRoleTypeId, String personalCode, String financialYear);

}
