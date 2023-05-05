package uk.gov.hmcts.reform.jps.model.in;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.model.DateOrder;
import uk.gov.hmcts.reform.jps.model.Duration;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.validator.EnumNamePattern;

import java.io.Serializable;
import java.time.LocalDate;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("serial")
public class SittingRecordSearchRequest implements Serializable {
    @Positive(message = "Page size can't be less than 1")
    private final int pageSize;

    @Min(value = 0, message = "Offset can't be less than 0")
    private final int offset;

    @NotBlank(message = "Region Id is mandatory")
    private final String regionId;

    @NotBlank(message = "Epims Id is mandatory")
    private final String epimsId;

    @EnumNamePattern(regexp = "ASCENDING|DESCENDING", message = "Expected value ascending|descending")
    @NotNull(message = "Date order is mandatory")
    private final DateOrder dateOrder;

    @NotNull(message = "Date range from is mandatory")
    private final LocalDate dateRangeFrom;

    @NotNull(message = "Date range to is mandatory")
    private final LocalDate dateRangeTo;

    //Optional parameters
    private final String personalCode;
    private final String judgeRoleTypeId;

    @EnumNamePattern(regexp = "AM|PM|FULL_DAY", message = "Expected value AM|PM|Full day")
    private final Duration duration;

    @EnumNamePattern(regexp = "RECORDED|SUBMITTED|PUBLISHED", message = "Expected value recorded|submitted|published")
    private final StatusId statusId;
    private final String createdByUserId;
}
