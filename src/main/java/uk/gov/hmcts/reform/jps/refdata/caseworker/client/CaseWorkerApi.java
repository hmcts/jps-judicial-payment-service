package uk.gov.hmcts.reform.jps.refdata.caseworker.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.jps.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.jps.refdata.caseworker.model.CaseWorkerApiResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "caseworker-ref-api",
    url = "${caseworker.api.url}",
    configuration = FeignClientConfiguration.class)
public interface CaseWorkerApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @GetMapping(
        value = "/refdata/case-worker/profile/search-by-id",
        headers = {"Content-Type=application/json"}
    )
    CaseWorkerApiResponse getCaseWorkerDetails(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam String id);
}
