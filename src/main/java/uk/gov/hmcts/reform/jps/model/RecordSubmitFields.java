package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
@Data
@Builder
public class RecordSubmitFields {
    private final Long id;
    private final Long contractTypeId;
    private final String personalCode;
    private final LocalDate sittingDate;
}
