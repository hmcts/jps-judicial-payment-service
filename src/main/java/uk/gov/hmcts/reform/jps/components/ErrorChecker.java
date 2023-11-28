package uk.gov.hmcts.reform.jps.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
import uk.gov.hmcts.reform.jps.model.PublishErrors;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class ErrorChecker implements PublishErrorChecker {
    protected static Logger LOGGER = LoggerFactory.getLogger(ErrorChecker.class);
    protected PublishErrorChecker publishErrorChecker;

    @Override
    public void next(PublishErrorChecker publishErrorChecker) {
        this.publishErrorChecker = publishErrorChecker;
    }

    protected abstract void addErrorInfo(PublishErrors publishErrors,
                                         String hmctsServiceCode,
                                         SittingRecordPublishFields sittingRecord);

    public void evaluate(PublishErrors publishErrors,
                         String hmctsServiceCode,
                         SittingRecordPublishFields sittingRecord,
                         Supplier<Optional<?>> evaluated) {
        LOGGER.debug("evaluate");

        Optional<?> isValid = evaluated.get();
        if (isValid.isEmpty()) {
            publishErrors.setError(true);
            addErrorInfo(publishErrors, hmctsServiceCode, sittingRecord);
        } else if (Objects.nonNull(publishErrorChecker)) {
            publishErrorChecker.evaluate(publishErrors, hmctsServiceCode, sittingRecord);
        }
    }
}
