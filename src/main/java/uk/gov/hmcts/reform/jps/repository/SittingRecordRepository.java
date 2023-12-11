package uk.gov.hmcts.reform.jps.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SittingRecordRepository extends JpaRepository<SittingRecord, Long>, SittingRecordRepositorySearch {

    @Query("select sh.changedByUserId from SittingRecord sr inner join StatusHistory sh "
        + "on sh.sittingRecord.id = sr.id and sh.statusId = 'RECORDED' "
        + "where sh.sittingRecord.id = :id ")
    String findCreatedByUserId(@Param("id") Long id);

    @Query("""
             select sr
             from SittingRecord sr inner join fetch sr.statusHistories sh
             where sr.id = :id
             and sr.statusId <> :statusId
             and sh.statusId = 'RECORDED'
        """)
    Optional<SittingRecord> findRecorderSittingRecord(Long id, StatusId statusId);

    Streamable<SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields>
        findBySittingDateAndPersonalCodeAndStatusIdNotIn(
            LocalDate sittingDate,
            String personalCode,
            Collection<StatusId> statusId
        );

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("update SittingRecord s"
        + " set s.statusId = :statusId"
        + " where s.id = :sittingRecordId and s.statusId = 'RECORDED'"
    )
    void updateRecordedStatus(Long sittingRecordId, StatusId statusId);

    @Query("""
        select count(sr.id)
        from SittingRecord sr
        where sr.personalCode = :personalCode
        and sr.statusId = :statusId
        and sr.sittingDate between :startDateTime and :endDateTime
        """)
    Long findCountByPersonalCodeAndStatusIdAndFinancialYearBetween(
        String personalCode,
        StatusId statusId,
        LocalDate startDateTime,
        LocalDate endDateTime
    );

    @Query(
        "select distinct sr.judgeRoleTypeId "
        + "from SittingRecord sr inner join StatusHistory sh on sr.id=sh.sittingRecord.id "
        + "where sr.hmctsServiceId = :hmctsServiceId "
        + "and ( CAST(:regionId as org.hibernate.type.StringType) is null "
        + "or sr.regionId = CAST(:regionId as org.hibernate.type.StringType) ) "
        + "and ( CAST(:statusId as org.hibernate.type.StringType) is null "
        + "or sr.statusId = CAST(:statusId as org.hibernate.type.StringType) ) "
        + "or (sr.statusId='PUBLISHED' and (sr.sittingDate between :startDate and now()) "
        + "or sr.statusId='SUBMITTED' and (sr.sittingDate between :serviceOnboardedDate and :endDate)) "
        )
    List<String> findJohRoles(@Param("hmctsServiceId") String hmctsServiceId,
                              @Param("regionId") String regionId,
                              @Param("statusId") String statusId,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("serviceOnboardedDate") LocalDate serviceOnboardedDate
    );
}
