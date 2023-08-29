package uk.gov.hmcts.reform.jps.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Autowired)

public class DurationBoolean {
    private final Boolean am;
    private final Boolean pm;
}
