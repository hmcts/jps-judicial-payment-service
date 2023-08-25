package uk.gov.hmcts.reform.jps.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.controllers.util.Utility;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;

import java.util.Optional;

import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_ADMIN;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_SUBMITTER;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_400;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_401;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_403;


@RestController
@Validated
@RequestMapping(
    path = "/sittingRecord",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SittingRecordDeleteController {
    private final SittingRecordService sittingRecordService;

    @Operation(description = "Root not to be displayed", hidden = true)
    @DeleteMapping(
        path = {""}
    )
    @PreAuthorize("hasAnyAuthority('" + JPS_RECORDER + "','" + JPS_SUBMITTER + "','" + JPS_ADMIN + "')")
    public ResponseEntity<Long> deleteSittingRecord() {
        return ResponseEntity.badRequest()
            .body(Utility.validateSittingRecordId(Optional.empty()));
    }

    @Operation(description = "Delete sitting record")
    @ApiResponse(responseCode = "200",
        content = @Content(schema = @Schema(implementation = String.class)),
        description = "Successfully deleted sitting record")
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @DeleteMapping(
        path = {"/{sittingRecordId}"}
    )
    @PreAuthorize("hasAnyAuthority('jps-recorder', 'jps-submitter', 'jps-admin')")
    public ResponseEntity<String> deleteSittingRecord(
        @PathVariable("sittingRecordId") Optional<Long> requestSittingRecordId) {
        Long sittingRecordId = Utility.validateSittingRecordId(requestSittingRecordId);
        sittingRecordService.deleteSittingRecord(sittingRecordId);
        return ResponseEntity.ok().build();
    }
}
