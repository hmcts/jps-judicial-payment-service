package uk.gov.hmcts.reform.jps.model.in;

import lombok.Data;
import uk.gov.hmcts.reform.jps.model.Fee;

import java.util.List;

@Data
public class FeeRequest {
    private List<Fee> fees;
}
