package uk.gov.hmcts.reform.jps.model.in;

import lombok.Data;

import java.util.List;

@Data
public class JudicialOfficeHolderDeleteRequest {
    private List<Long> judicialOfficeHolderIds;
}
