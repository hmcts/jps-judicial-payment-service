package uk.gov.hmcts.reform.jps.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_400;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_401;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_403;


@RestController
@Validated
@RequestMapping(
    path = "/submitSittingRecords",
    produces = APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SubmitSittingRecordsController {
    private final SittingRecordService sittingRecordService;

    @Operation(description = "Submit sitting records")
    @ApiResponse(responseCode = "200",
        content = @Content(schema = @Schema(implementation = SubmitSittingRecordResponse.class)),
        description = "Successfully submitted sitting records")
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(
        path = {"", "/{hmctsServiceCode}"}
    )
    @PreAuthorize("hasAuthority('jps-submitter')")
    public ResponseEntity<SubmitSittingRecordResponse> submitSittingRecords(
        @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
        @Valid @RequestBody SubmitSittingRecordRequest submitSittingRecordRequest) {

        String hmctsServiceCode = Utility.validateServiceCode(requestHmctsServiceCode);
        SubmitSittingRecordResponse submitSittingRecordResponse = sittingRecordService.submitSittingRecords(
            submitSittingRecordRequest,
            hmctsServiceCode
        );
        return ResponseEntity.ok(submitSittingRecordResponse);
    }
}
