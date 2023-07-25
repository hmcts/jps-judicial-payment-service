package uk.gov.hmcts.reform.jps.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {

    @Query("select sr from StatusHistory sr where sr.sittingRecord.id=:id ORDER BY sr.id asc")
    List<StatusHistory> findStatusHistoryAsc(@Param("id") Long id);

    @Query("select sr from StatusHistory sr where sr.sittingRecord.id=:id ORDER BY sr.id desc")
    List<StatusHistory> findStatusHistoryDesc(@Param("id") Long id);

}
