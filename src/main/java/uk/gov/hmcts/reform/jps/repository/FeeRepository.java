package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.Fee;

import java.util.List;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {
    @Modifying
    @Query("""
        delete from Fee where id in :ids
        """)
    void deleteByIds(List<Long> ids);
}
