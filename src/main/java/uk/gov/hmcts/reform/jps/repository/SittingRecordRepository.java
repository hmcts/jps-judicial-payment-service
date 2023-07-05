package uk.gov.hmcts.reform.jps.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;

@Repository
public interface SittingRecordRepository extends JpaRepository<SittingRecord, Long>, SittingRecordRepositorySearch {

    @Query("select sh.changeByUserId from SittingRecord sr inner join StatusHistory sh "
        + "on sh.sittingRecord.id = sr.id "
        + "where sh.id = (select min(sh2.id) from StatusHistory sh2 where sh2.sittingRecord.id = :id)")
    String findCreatedByUserId(@Param("id") Long id);

}
