package uk.gov.hmcts.reform.jps.refdata.judicial.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class JudicialUserDetailsApiResponse {
    @JsonProperty("email_id")
    private String emailId;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("known_as")
    private String knownAs;

    @JsonProperty("personal_code")
    private String personalCode;

    @JsonProperty("post_nominals")
    private String postNominals;

    @JsonProperty("sidam_id")
    private String sidamId;

    @JsonProperty("surname")
    private String surname;
}
