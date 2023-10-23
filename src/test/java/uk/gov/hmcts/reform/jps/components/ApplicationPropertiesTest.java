package uk.gov.hmcts.reform.jps.components;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class ApplicationPropertiesTest {

    @ParameterizedTest
    @CsvSource(quoteCharacter = '"', textBlock = """
      # JohRoleId,   MedicalMember
        100,        true,
        20,         true
        30,         false
        """)
    void shouldConfirmJohRoleIdConfigure(String johRoleId,
                                         boolean medicalMember) {
        String message = medicalMember
            ? "is a medical member" : "is not a medical member";
        ApplicationProperties properties = new ApplicationProperties(
            40,
            List.of("20-Higher Fee", "100-Standard Fee")
        );

        assertThat(properties.isMedicalMember(johRoleId))
            .as(message)
            .isEqualTo(medicalMember);
    }
}
