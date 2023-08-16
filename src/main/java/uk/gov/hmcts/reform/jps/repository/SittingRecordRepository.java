package uk.gov.hmcts.reform.jps.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;

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
    Optional<SittingRecord> findRecorderSittingRecord(Long id, String statusId);
}
