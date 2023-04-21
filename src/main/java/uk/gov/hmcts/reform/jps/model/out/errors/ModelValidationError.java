package uk.gov.hmcts.reform.jps.model.out.errors;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class ModelValidationError {

    public final List<FieldError> errors;

    @Override
    public String toString() {
        return "ModelValidationError{errors=" + errors + "}";
    }
}
