package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface JudicialOfficeHolderRepository extends JpaRepository<JudicialOfficeHolder, Long> {

    Optional<JudicialOfficeHolder> findByPersonalCode(String personalCode);

    @Query("""
        select jfh from JudicialOfficeHolder jfh
        inner join fetch jfh.johAttributes a
        where jfh.personalCode = :personalCode
        and a.effectiveStartDate <= :dateEqualOrBefore
        """)
    Optional<JudicialOfficeHolder> findJudicialOfficeHolderWithJohAttributesFilteredByEffectiveStartDate(
        @Param("personalCode") String personalCode,
        @Param("dateEqualOrBefore") LocalDate dateEqualOrBefore);


    @Query("""
        select jfh from JudicialOfficeHolder jfh
        inner join fetch jfh.johPayrolls p
        where jfh.personalCode = :personalCode
        and p.effectiveStartDate <= :dateEqualOrBefore
        """)
    Optional<JudicialOfficeHolder> findJudicialOfficeHolderWithJohPayrollFilteredByEffectiveStartDate(
        @Param("personalCode") String personalCode,
        @Param("dateEqualOrBefore") LocalDate dateEqualOrBefore);

}
