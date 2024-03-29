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
import uk.gov.hmcts.reform.jps.model.out.RecordSittingRecordResponse;
import uk.gov.hmcts.reform.jps.model.out.SittingRecordResponse;
import uk.gov.hmcts.reform.jps.services.ServiceService;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import javax.validation.Valid;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_RECORDER;
import static uk.gov.hmcts.reform.jps.constant.JpsRoles.JPS_SUBMITTER;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_200;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_400;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_401;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_403;
import static uk.gov.hmcts.reform.jps.model.ErrorCode.POTENTIAL_DUPLICATE_RECORD;
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
    private final JudicialUserDetailsService judicialUserDetailsService;
    private final ServiceService serviceService;

    @Operation(description = "Root not to be displayed", hidden = true)
    @PostMapping(
            path = {""}
    )
    @PreAuthorize("hasAnyAuthority('" + JPS_RECORDER + "','" + JPS_SUBMITTER + "')")
    public ResponseEntity<String> recordSittingRecords() {
        return ResponseEntity.badRequest()
                .body(Utility.validateServiceCode(Optional.empty()));
    }

    @Operation(description = "Create a new sitting record")
    @ApiResponse(responseCode = "201",
            content = @Content(schema = @Schema(implementation = RecordSittingRecordResponse.class)),
            description = "Successfully created sitting record")
    @ApiResponse(responseCode = "200", description = RESPONSE_200)
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(
            path = {"/{hmctsServiceCode}"}
    )
    @PreAuthorize("hasAnyAuthority('" + JPS_RECORDER + "','" + JPS_SUBMITTER + "')")
    public ResponseEntity<RecordSittingRecordResponse> recordSittingRecords(
            @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
            @Valid @RequestBody RecordSittingRecordRequest recordSittingRecordRequest) {

        String hmctsServiceCode = Utility.validateServiceCode(requestHmctsServiceCode, serviceService);

        List<SittingRecordWrapper> sittingRecordWrappers =
                recordSittingRecordRequest.getRecordedSittingRecords().stream()
                        .map(SittingRecordWrapper::new)
                        .toList();

        regionService.setRegionId(hmctsServiceCode,
                sittingRecordWrappers);

        sittingRecordService.checkDuplicateRecords(sittingRecordWrappers);
        Optional<ErrorCode> errorCodeCheck = checkForErrors(
                sittingRecordWrappers,
                not(sittingRecordWrapper ->
                        POTENTIAL_DUPLICATE_RECORD == sittingRecordWrapper.getErrorCode()
                                && TRUE.equals(sittingRecordWrapper.getSittingRecordRequest().getReplaceDuplicate())
                )
        );

        if (errorCodeCheck.isPresent()) {
            judicialUserDetailsService.setJudicialUserName(
                    sittingRecordWrappers.stream()
                            .filter(sittingRecordWrapper -> nonNull(sittingRecordWrapper.getJudgeRoleTypeId()))
                            .toList()
            );

            return status(HttpStatus.BAD_REQUEST)
                    .body(RecordSittingRecordResponse.builder()
                            .message("008 could not insert")
                            .errorRecords(generateResponse(hmctsServiceCode,
                                                            sittingRecordWrappers,
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
            return status(CREATED)
                    .body(RecordSittingRecordResponse.builder()
                            .errorRecords(generateResponse(
                                hmctsServiceCode,
                                sittingRecordWrappers,
                                    errorCode -> VALID,
                                    sittingRecordWrapper ->
                                            recordSittingRecordRequest.getRecordedByName(),
                                    statusId -> RECORDED
                            ))
                            .build()
                    );
        }
    }


    private Optional<ErrorCode> checkForErrors(List<SittingRecordWrapper> sittingRecordWrappers,
                                               Predicate<SittingRecordWrapper> predicate) {
        return sittingRecordWrappers.stream()
                .filter(predicate)
                .map(SittingRecordWrapper::getErrorCode)
                .filter(errorCode -> errorCode != VALID)
                .findAny();
    }


    private List<SittingRecordResponse> generateResponse(
            String hmctsServiceCode,
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
                                .am(sittingRecordWrapper.getAm())
                                .pm(sittingRecordWrapper.getPm())
                                .judgeRoleTypeId(sittingRecordWrapper.getJudgeRoleTypeId())
                                .judgeRoleTypeName(sittingRecordWrapper.getJudgeRoleTypeName())
                                .venue(getVenueName(hmctsServiceCode,
                                    sittingRecordWrapper.getEpimmsId()))
                                .build()
                ).toList();
    }

    private String getVenueName(String hmctsServiceCode, String epimmsId) {
        return regionService.getCourtName(hmctsServiceCode, epimmsId);
    }

}
