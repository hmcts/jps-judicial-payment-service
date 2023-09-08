package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;

@Repository
public interface JohAttributesRepository extends JpaRepository<JohAttributes, Long> {
}
