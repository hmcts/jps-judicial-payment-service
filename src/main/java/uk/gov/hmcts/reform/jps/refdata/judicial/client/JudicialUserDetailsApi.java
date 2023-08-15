package uk.gov.hmcts.reform.jps.refdata.judicial.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.jps.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiRequest;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "judicial-ref-data-api",
    url = "${judicialUsers.api.url}",
    configuration = FeignClientConfiguration.class)
public interface JudicialUserDetailsApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @PostMapping(
        value = "/refdata/judicial/users",
        headers = {"Content-Type=application/json"}
    )
    List<JudicialUserDetailsApiResponse> getJudicialUserDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody JudicialUserDetailsApiRequest judicialUsersApiRequest);
}
