package uk.gov.hmcts.reform.jps.controllers;

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
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.in.RecordSittingRecordResponse;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordResponse;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.status;
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

    @PostMapping(
        path = {"", "/{hmctsServiceCode}"}
    )
    @PreAuthorize("hasAnyAuthority('jps-recorder', 'jps-submitter')")
    public ResponseEntity<RecordSittingRecordResponse> recordSittingRecords(
        @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
        @Valid @RequestBody RecordSittingRecordRequest recordSittingRecordRequest) {

        String hmctsServiceCode = Utility.validateServiceCode(requestHmctsServiceCode);

        regionService.setRegionId(hmctsServiceCode,
                                  recordSittingRecordRequest.getRecordedSittingRecords());

        sittingRecordService.saveSittingRecords(hmctsServiceCode,
                                                recordSittingRecordRequest
                                                );

        return status(HttpStatus.CREATED)
            .body(RecordSittingRecordResponse.builder()
                    .errorRecords(generateRecordSittingRecordResponse(recordSittingRecordRequest))
                    .build()
            );
    }

    private List<SittingRecordResponse> generateRecordSittingRecordResponse(
        RecordSittingRecordRequest recordSittingRecordRequest) {
        return recordSittingRecordRequest.getRecordedSittingRecords().stream()
            .map(request ->
                     SittingRecordResponse.builder()
                         .postedRecord(request)
                         .errorCode(VALID)
                         .createdByName(recordSittingRecordRequest.getRecordedByName())
                         .createdDateTime(request.getCreatedDateTime())
                         .statusId(RECORDED)
                         .build()
            ).toList();
    }
}
