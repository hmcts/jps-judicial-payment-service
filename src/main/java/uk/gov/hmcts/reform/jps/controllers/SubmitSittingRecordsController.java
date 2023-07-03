package uk.gov.hmcts.reform.jps.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.controllers.util.Utility;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SubmitSittingRecordResponse;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;

import java.util.Optional;
import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Validated
@RequestMapping(
    path = "/submitSittingRecords",
    produces = APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SubmitSittingRecordsController {
    private final SittingRecordService sittingRecordService;

    @PostMapping(
        path = {"/", "/{hmctsServiceCode}"}
    )
    @PreAuthorize("hasAuthority('jps-submitter')")
    public ResponseEntity<SubmitSittingRecordResponse> submitSittingRecords(
        @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
        @Valid @RequestBody SubmitSittingRecordRequest submitSittingRecordRequest) {

        String hmctsServiceCode = Utility.validateServiceCode(requestHmctsServiceCode);
        int recordsSubmitted = sittingRecordService.submitSittingRecords(submitSittingRecordRequest,
                                                                         hmctsServiceCode);
        return ResponseEntity.ok(SubmitSittingRecordResponse.builder()
                                     .recordsSubmitted(recordsSubmitted)
                                     .build());
    }
}
