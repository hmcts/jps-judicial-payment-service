package uk.gov.hmcts.reform.jps.controllers.util;

import lombok.experimental.UtilityClass;
import uk.gov.hmcts.reform.jps.exceptions.MissingPathVariableException;

import java.util.Optional;

@UtilityClass
public class Utility {
    public String validateServiceCode(Optional<String> serviceCode) {
        return serviceCode
            .orElseThrow(() -> new MissingPathVariableException("hmctsServiceCode is mandatory"));
    }
}
