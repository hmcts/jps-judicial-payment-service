package uk.gov.hmcts.reform.jps.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudicialOfficeHolder {
    private String personalCode;
    private List<JohPayroll> johPayrolls;
    private Set<JohAttributes> johAttributes;
}
