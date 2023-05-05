package uk.gov.hmcts.reform.jps.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.expection.MissingPathVariableException;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecordSearchResponse;

import java.util.Optional;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;


@RestController
@Validated
@RequestMapping(
    path = "/sitting-records",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Slf4j
public class SittingRecordController {

    @GetMapping("/")
    public ResponseEntity<String> welcome() {

        return ok("Welcome to jps-judicial-payment-service");
    }

    @PostMapping(
        path = {"/searchSittingRecords","/searchSittingRecords/{hmctsServiceCode}"}
    )
    public ResponseEntity<SittingRecordSearchResponse> searchSittingRecords(
        @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
        @Valid @RequestBody SittingRecordSearchRequest sittingRecordSearchRequest) {

        String hmctsServiceCode = requestHmctsServiceCode
            .orElseThrow(() -> new MissingPathVariableException("hmctsServiceCode is mandatory"));

        log.info("Value passed {}", sittingRecordSearchRequest);

        //TODO: Need to set these lookup fields
        /*regionName;
        personalName;
        createdByUserName;
        changeByUserName;*/
        return ok(SittingRecordSearchResponse.builder()
                  .build());
    }
}
