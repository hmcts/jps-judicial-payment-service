package uk.gov.hmcts.reform.jps.validator;

import uk.gov.hmcts.reform.jps.model.DurationBoolean;

import java.util.Objects;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class DurationValidator implements ConstraintValidator<ValidDuration, DurationBoolean> {
    @Override
    public void initialize(ValidDuration constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(DurationBoolean durationBoolean, ConstraintValidatorContext context) {
        return Objects.nonNull(durationBoolean.getPm()) && durationBoolean.getPm()
            || Objects.nonNull(durationBoolean.getAm()) && durationBoolean.getAm();
    }
}
