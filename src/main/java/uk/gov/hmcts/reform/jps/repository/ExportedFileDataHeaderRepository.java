package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.ExportedFileDataHeader;

@Repository
public interface ExportedFileDataHeaderRepository extends JpaRepository<ExportedFileDataHeader, Long> {
}
