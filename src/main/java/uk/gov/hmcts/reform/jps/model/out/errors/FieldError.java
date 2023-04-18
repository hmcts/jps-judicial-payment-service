package uk.gov.hmcts.reform.jps.model.out.errors;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class FieldError {
    private final String fieldName;
    private final String message;
}
