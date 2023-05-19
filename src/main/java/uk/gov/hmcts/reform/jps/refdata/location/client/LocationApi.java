package uk.gov.hmcts.reform.jps.refdata.location.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.jps.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "location-api",
    url = "${location.api.url}",
    configuration = FeignClientConfiguration.class)
public interface LocationApi {
    String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @GetMapping(
        value = "/refdata/location/court-venues/services",
        headers = {"Content-Type=application/json"}
    )
    LocationApiResponse getCourtDetailsByServiceCode(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestParam("service_code") String serviceCode);
}
