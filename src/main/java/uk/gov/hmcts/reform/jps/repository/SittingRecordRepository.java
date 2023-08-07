package uk.gov.hmcts.reform.jps.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;

@Repository
public interface SittingRecordRepository extends JpaRepository<SittingRecord, Long>, SittingRecordRepositorySearch {

    @Query("select sh.changedByUserId from SittingRecord sr inner join StatusHistory sh "
        + "on sh.sittingRecord.id = sr.id and sh.statusId = 'RECORDED' "
        + "where sh.sittingRecord.id = :id ")
    String findCreatedByUserId(@Param("id") Long id);

    Streamable<SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields>
        findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
            LocalDate sittingDate,
            String epimmsId,
            String personalCode,
            StatusId statusId
        );
}
