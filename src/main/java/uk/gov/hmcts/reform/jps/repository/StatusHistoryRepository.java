package uk.gov.hmcts.reform.jps.repository;

import feign.Param;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.RecordingUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long>, SittingRecordRepositorySearch {

    @Query("select sr from StatusHistory sr where sr.sittingRecord.id=:id ORDER BY sr.id asc")
    List<StatusHistory> findStatusHistoryAsc(@Param("id") Long id);

    @Query("select sr from StatusHistory sr where sr.sittingRecord.id=:id ORDER BY sr.id desc")
    List<StatusHistory> findStatusHistoryDesc(@Param("id") Long id);


    @Query("select distinct new uk.gov.hmcts.reform.jps.model.RecordingUser(sh.changedByUserId, sh.changedByName) "
        + "from StatusHistory sh inner join SittingRecord sr "
        + "on sh.sittingRecord.id = sr.id and sh.statusId = 'RECORDED' "
        + "where sr.hmctsServiceId = :hmctsServiceId "
        + "and ( CAST(:regionId as org.hibernate.type.StringType) is null "
        + "or sr.regionId = CAST(:regionId as org.hibernate.type.StringType) ) "
        + "and sr.statusId in :statusIds "
        + "and sr.sittingDate >= :startDate and sr.sittingDate <= :endDate "
        )
    List<RecordingUser> findRecordingUsers(@Param("hmctsServiceId") String hmctsServiceId,
                                           @Param("regionId") String regionId,
                                           @Param("statusIds") List<String> statusIds,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate
    );

    Optional<StatusHistory> findFirstBySittingRecord(SittingRecord sittingRecord, Sort sort);
}
