package uk.gov.hmcts.reform.jps.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;

import java.util.Optional;

@Repository
public interface JudicialOfficeHolderRepository extends JpaRepository<JudicialOfficeHolder, Long> {
    Optional<JudicialOfficeHolder> findByPersonalCode(String personalCode);
}
