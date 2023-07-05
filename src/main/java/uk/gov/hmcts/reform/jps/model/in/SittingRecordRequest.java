package uk.gov.hmcts.reform.jps.model.in;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.validator.ValidDuration;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("serial")
public class SittingRecordRequest implements Serializable {

    @NotNull(message = "Sitting date is mandatory")
    private final LocalDate sittingDate;

    @NotBlank(message = "Epims Id is mandatory")
    private final String epimsId;

    @NotBlank(message = "Personal code is mandatory")
    private final String personalCode;

    @NotNull(message = "Contract Type Id  is mandatory")
    private final Long contractTypeId;

    @NotBlank(message = "Judge Role Type Id is mandatory")
    private final String judgeRoleTypeId;

    @JsonUnwrapped
    @ValidDuration(message = "AM/PM/Full Day is mandatory")
    private final DurationBoolean durationBoolean;

    private final String replaceDuplicate;

    @JsonIgnore
    private String regionId;

    @JsonIgnore
    private LocalDateTime createdDateTime;
}
