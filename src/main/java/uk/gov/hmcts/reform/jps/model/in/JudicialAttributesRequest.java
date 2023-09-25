package uk.gov.hmcts.reform.jps.model.in;

import lombok.Data;
import uk.gov.hmcts.reform.jps.model.JohAttributes;

import java.util.List;

@Data
public class JudicialAttributesRequest {
    private Long judicialOfficeHolderId;
    private List<JohAttributes> johAttributes;
}
