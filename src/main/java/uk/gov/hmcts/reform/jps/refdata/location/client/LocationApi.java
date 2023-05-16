package uk.gov.hmcts.reform.jps.refdata.location.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.jps.config.feign.FeignClientConfiguration;
import uk.gov.hmcts.reform.jps.refdata.location.model.LocationApiResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "location-api",
    url = "${location.api.url}",
    configuration = FeignClientConfiguration.class)
public interface LocationApi {
    @RequestMapping(
        method = RequestMethod.GET,
        value = "/refdata/location/court-venues/services",
        headers = {"Content-Type=application/json"}
    )
    LocationApiResponse getCourtDetailsByServiceCode(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("service_code") String serviceCode);
}
