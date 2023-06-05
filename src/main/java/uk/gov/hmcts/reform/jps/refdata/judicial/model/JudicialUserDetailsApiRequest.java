package uk.gov.hmcts.reform.jps.refdata.judicial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudicialUserDetailsApiRequest {
    private String ccdServiceName;

    @JsonProperty("personal_code")
    private List<String> personalCode;
}

