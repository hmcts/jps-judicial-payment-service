package uk.gov.hmcts.reform.jps.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.controllers.util.Utility;
import uk.gov.hmcts.reform.jps.model.ErrorCode;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordResponse;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordResponse;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import javax.validation.Valid;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_200;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_400;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_401;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_403;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@RestController
@Validated
@RequestMapping(
    path = "/recordSittingRecords",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class RecordSittingRecordsController {
    private final SittingRecordService sittingRecordService;
    private final LocationService regionService;


    @Operation(description = "Create a new sitting record")
    @ApiResponse(responseCode = "201",
        content = @Content(schema = @Schema(implementation = RecordSittingRecordResponse.class)),
        description = "Successfully created sitting record")
    @ApiResponse(responseCode = "200", description = RESPONSE_200)
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(
        path = {"", "/{hmctsServiceCode}"}
    )
    @PreAuthorize("hasAnyAuthority('jps-recorder', 'jps-submitter')")
    public ResponseEntity<RecordSittingRecordResponse> recordSittingRecords(
        @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
        @Valid @RequestBody RecordSittingRecordRequest recordSittingRecordRequest) {

        String hmctsServiceCode = Utility.validateServiceCode(requestHmctsServiceCode);

        List<SittingRecordWrapper> sittingRecordWrappers =
            recordSittingRecordRequest.getRecordedSittingRecords().stream()
                .map(SittingRecordWrapper::new)
                .toList();

        regionService.setRegionId(hmctsServiceCode,
                                  sittingRecordWrappers);

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);

        Optional<ErrorCode> errorCodeCheck = sittingRecordWrappers.stream()
            .map(SittingRecordWrapper::getErrorCode)
            .filter(errorCode -> errorCode != VALID)
            .findAny();

        if (errorCodeCheck.isPresent()) {
            return status(HttpStatus.BAD_REQUEST)
                .body(RecordSittingRecordResponse.builder()
                          .message("008 could not insert")
                          .errorRecords(generateResponse(sittingRecordWrappers,
                                                         errorCode -> errorCode,
                                                         SittingRecordWrapper::getCreatedByName,
                                                         statusId -> statusId
                          ))
                          .build()
                );
        } else {
            sittingRecordService.saveSittingRecords(hmctsServiceCode,
                                                    sittingRecordWrappers,
                                                    recordSittingRecordRequest.getRecordedByName(),
                                                    recordSittingRecordRequest.getRecordedByIdamId()
            );
            HttpStatus httpStatus = sittingRecordWrappers.stream()
                .filter(wrapper ->
                            Boolean.TRUE
                                .equals(wrapper.getSittingRecordRequest().getReplaceDuplicate()))
                .findAny()
                .map(sittingRecordWrapper -> OK)
                .orElse(CREATED);

            return status(httpStatus)
                .body(RecordSittingRecordResponse.builder()
                          .message("success")
                          .errorRecords(generateResponse(sittingRecordWrappers,
                                                         errorCode -> VALID,
                                                         sittingRecordWrapper ->
                                                             recordSittingRecordRequest.getRecordedByName(),
                                                         statusId -> RECORDED
                          ))
                          .build()
                );
        }
    }

    private List<SittingRecordResponse> generateResponse(
        List<SittingRecordWrapper> sittingRecordWrappers,
        UnaryOperator<ErrorCode> errorCodeOperator,
        Function<SittingRecordWrapper, String> getName,
        UnaryOperator<StatusId> statusIdOperation
    ) {
        return sittingRecordWrappers.stream()
            .map(sittingRecordWrapper ->
                     SittingRecordResponse.builder()
                         .postedRecord(sittingRecordWrapper.getSittingRecordRequest())
                         .errorCode(errorCodeOperator.apply(sittingRecordWrapper.getErrorCode()))
                         .createdByName(getName.apply(sittingRecordWrapper))
                         .createdDateTime(sittingRecordWrapper.getCreatedDateTime())
                         .statusId(statusIdOperation.apply(sittingRecordWrapper.getStatusId()))
                         .build()
            ).toList();
    }
}
