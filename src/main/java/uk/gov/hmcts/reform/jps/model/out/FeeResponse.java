package uk.gov.hmcts.reform.jps.model.out;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FeeResponse {
    private List<Long> fees;
}
