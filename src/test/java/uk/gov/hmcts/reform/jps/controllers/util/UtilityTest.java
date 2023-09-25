package uk.gov.hmcts.reform.jps.controllers.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.exceptions.MissingPathVariableException;
import uk.gov.hmcts.reform.jps.exceptions.UnknownValueException;
import uk.gov.hmcts.reform.jps.services.ServiceService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UtilityTest {
    @Mock
    private ServiceService serviceService;

    @Test
    void shouldReturnServiceCodeWhenCodePresent() {
        assertThat(Utility.validateServiceCode(Optional.of("22")))
            .isEqualTo("22");
    }

    @Test
    void shouldMissingPathVariableExceptionWhenCodeIsEmpty() {
        Optional<String> empty = Optional.empty();
        assertThatThrownBy(() -> Utility.validateServiceCode(empty))
            .isInstanceOf(MissingPathVariableException.class)
            .hasMessage("hmctsServiceCode is mandatory");
    }
}
