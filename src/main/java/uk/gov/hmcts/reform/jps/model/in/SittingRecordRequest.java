package uk.gov.hmcts.reform.jps.model.in;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.model.DurationBoolean;
import uk.gov.hmcts.reform.jps.validator.ValidDuration;

import java.io.Serializable;
import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder(toBuilder = true)
@JsonInclude(NON_NULL)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@SuppressWarnings("serial")
public class SittingRecordRequest implements Serializable {

    private final Long sittingRecordId;

    @NotNull(message = "Sitting date is mandatory")
    private final LocalDate sittingDate;

    @NotBlank(message = "Epimms Id is mandatory")
    private final String epimmsId;

    @NotBlank(message = "Personal code is mandatory")
    private final String personalCode;

    @NotNull(message = "Contract Type Id  is mandatory")
    private final Long contractTypeId;

    @NotBlank(message = "Judge Role Type Id is mandatory")
    private final String judgeRoleTypeId;

    @JsonUnwrapped
    @ValidDuration(message = "AM/PM/Full Day is mandatory")
    private final DurationBoolean durationBoolean;

    private final Boolean replaceDuplicate;
}
