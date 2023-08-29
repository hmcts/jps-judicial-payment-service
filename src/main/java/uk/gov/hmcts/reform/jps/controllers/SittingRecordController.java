package uk.gov.hmcts.reform.jps.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.controllers.util.Utility;
import uk.gov.hmcts.reform.jps.model.RecordingUser;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.model.out.SittingRecordSearchResponse;
import uk.gov.hmcts.reform.jps.services.SittingRecordService;
import uk.gov.hmcts.reform.jps.services.StatusHistoryService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;
import uk.gov.hmcts.reform.jps.services.refdata.LocationService;

import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import static java.util.Collections.emptyList;
import static java.util.List.of;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.jps.model.StatusId.PUBLISHED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;
import static uk.gov.hmcts.reform.jps.model.StatusId.SUBMITTED;


@RestController
@Validated
@RequestMapping(
    path = "/sitting-records",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SittingRecordController {
    public static final List<StatusId> VALID_STATUS_IDS = of(RECORDED, PUBLISHED, SUBMITTED);
    private final SittingRecordService sittingRecordService;
    private final StatusHistoryService statusHistoryService;
    private final LocationService regionService;
    private final JudicialUserDetailsService judicialUserDetailsService;

    @Operation(description = "Root not to be displayed", hidden = true)
    @PostMapping(
        path = {"/searchSittingRecords"}
    )
    public ResponseEntity<String> searchSittingRecords() {
        return ResponseEntity.badRequest()
            .body(Utility.validateServiceCode(Optional.empty()));
    }

    @PostMapping(
        path = {"/searchSittingRecords/{hmctsServiceCode}"}
    )
    public ResponseEntity<SittingRecordSearchResponse> searchSittingRecords(
        @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
        @Valid @RequestBody SittingRecordSearchRequest sittingRecordSearchRequest) {

        String hmctsServiceCode = Utility.validateServiceCode(requestHmctsServiceCode);

        final long totalRecordCount = sittingRecordService.getTotalRecordCount(
            sittingRecordSearchRequest,
            hmctsServiceCode
        );

        List<SittingRecord> sittingRecords = emptyList();
        List<RecordingUser> recordingUsers = emptyList();

        if (totalRecordCount > 0) {
            sittingRecords = sittingRecordService.getSittingRecords(
                sittingRecordSearchRequest,
                hmctsServiceCode
            );

            if (!sittingRecords.isEmpty()) {
                regionService.setRegionName(hmctsServiceCode, sittingRecords);
                judicialUserDetailsService.setJudicialUserDetails(sittingRecords);

                recordingUsers =
                    statusHistoryService.findRecordingUsers(
                        hmctsServiceCode,
                        sittingRecordSearchRequest.getRegionId(),
                        VALID_STATUS_IDS,
                        sittingRecordSearchRequest.getDateRangeFrom(),
                        sittingRecordSearchRequest.getDateRangeTo()
                    );
            }
        }

        return ok(SittingRecordSearchResponse.builder()
                      .recordCount(totalRecordCount)
                      .recordingUsers(recordingUsers)
                      .sittingRecords(sittingRecords)
                      .build());
    }

}
