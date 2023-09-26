package uk.gov.hmcts.reform.jps.controllers.util;

import lombok.experimental.UtilityClass;
import uk.gov.hmcts.reform.jps.exceptions.MissingPathVariableException;
import uk.gov.hmcts.reform.jps.exceptions.UnknownValueException;
import uk.gov.hmcts.reform.jps.services.ServiceService;

import java.util.Optional;

@UtilityClass
public class Utility {

    public String validateServiceCode(Optional<String> optionalServiceCode) {
        return optionalServiceCode
            .orElseThrow(() -> new MissingPathVariableException("hmctsServiceCode is mandatory"));
    }

    public String validateServiceCode(Optional<String> optionalServiceCode, ServiceService serviceService) {
        return optionalServiceCode
            .filter(serviceService::isServiceOnboarded)
            .orElseThrow(() -> new UnknownValueException("hmctsServiceCode",
                                                         "004 unknown hmctsServiceCode"));
    }

    public Long validateSittingRecordId(Optional<Long> sittingRecordId) {
        return sittingRecordId
            .orElseThrow(() -> new MissingPathVariableException("sittingRecordId is mandatory"));
    }
}
