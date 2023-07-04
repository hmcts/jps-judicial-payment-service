package uk.gov.hmcts.reform.jps.controllers.util;

import lombok.experimental.UtilityClass;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.MissingPathVariableException;

import java.util.List;
import java.util.Optional;

@UtilityClass
public class Utility {
    public String validateServiceCode(Optional<String> serviceCode) {
        return serviceCode
            .orElseThrow(() -> new MissingPathVariableException("hmctsServiceCode is mandatory"));
    }

    public Long validateSittingRecordId(Optional<Long> sittingRecordId) {
        return sittingRecordId
            .orElseThrow(() -> new MissingPathVariableException("sittingRecordId is mandatory"));
    }

    public static void validateRolesAllowed(List<String> userRoles, List<String> allowedRoles) {
        userRoles.stream()
            .filter(allowedRoles::contains)
            .findAny()
            .orElseThrow(() -> new ConflictException("Incorrect IDAM Role"));
    }
}
