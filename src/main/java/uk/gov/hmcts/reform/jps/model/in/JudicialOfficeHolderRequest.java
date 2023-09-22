package uk.gov.hmcts.reform.jps.model.in;

import lombok.Data;
import uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder;

import java.util.List;

@Data
public class JudicialOfficeHolderRequest {
    private List<JudicialOfficeHolder> judicialOfficeHolders;
}
