package uk.gov.hmcts.reform.jps.controllers.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.jps.exceptions.MissingPathVariableException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UtilityTest {

    @Test
    void shouldReturnServiceCodeWhenCodePresent() {
        assertThat(Utility.validateServiceCode(Optional.of("22")))
            .isEqualTo("22");
    }

    @Test
    void shouldMissingPathVariableExceptionWhenCodeIsEmpty() {
        assertThatThrownBy(() -> Utility.validateServiceCode(Optional.empty()))
            .isInstanceOf(MissingPathVariableException.class)
            .hasMessage("hmctsServiceCode is mandatory");
    }

}
