package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class JohAttributes {

    private LocalDate effectiveStartDate;

    private boolean crownServantFlag;

    private boolean londonFlag;
}
