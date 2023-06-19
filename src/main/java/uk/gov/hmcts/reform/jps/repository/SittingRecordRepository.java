package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;

@Repository
public interface SittingRecordRepository extends JpaRepository<SittingRecord, Long>, SittingRecordRepositorySearch {
    Streamable<SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields>
        findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNot(
            LocalDate sittingDate,
            String epimmsId,
            String personalCode,
            StatusId statusId
        );
}
