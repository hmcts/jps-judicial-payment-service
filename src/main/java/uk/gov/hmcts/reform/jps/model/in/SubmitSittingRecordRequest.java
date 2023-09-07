package uk.gov.hmcts.reform.jps.model.in;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.validator.EnumNamePattern;

import java.time.LocalDate;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SubmitSittingRecordRequest {
    @NotBlank(message = "Submitted by Idam Id is mandatory")
    private final String submittedByIdamId;

    @NotBlank(message = "Submitted by name is mandatory")
    private final String submittedByName;

    @Size(min = 1, max = 2, message = "Region Id length required between 1 and 2")
    @NotBlank(message = "Region Id is mandatory")
    private final String regionId;

    @NotNull(message = "Date range from is mandatory")
    private final LocalDate dateRangeFrom;

    @NotNull(message = "Date range to is mandatory")
    private final LocalDate dateRangeTo;

    private final String epimmsId;
    private final String createdByUserId;
    private final String personalCode;
    private final String judgeRoleTypeId;

    @EnumNamePattern(regexp = "AM|PM|FULL_DAY", message = "Expected value AM|PM|Full day")
    private final Duration duration;
}
