package uk.gov.hmcts.reform.jps.model.out.errors;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class FieldError {
    private final String fieldName;
    private final String message;
}
