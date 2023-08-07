package uk.gov.hmcts.reform.jps.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import static org.springframework.http.ResponseEntity.ok;


@RestController
@Validated
@RequestMapping(
    path = "/sitting-records",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class SittingRecordController {
    private final SittingRecordService sittingRecordService;
    private final StatusHistoryService statusHistoryService;
    private final LocationService regionService;
    private final JudicialUserDetailsService judicialUserDetailsService;


    @PostMapping(
        path = {"/searchSittingRecords/{hmctsServiceCode}"}
    )
    public ResponseEntity<SittingRecordSearchResponse> searchSittingRecords(
        @PathVariable("hmctsServiceCode") Optional<String> requestHmctsServiceCode,
        @Valid @RequestBody SittingRecordSearchRequest sittingRecordSearchRequest) {

        String hmctsServiceCode = Utility.validateServiceCode(requestHmctsServiceCode);

        final int totalRecordCount = sittingRecordService.getTotalRecordCount(
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
                        List.of(StatusId.RECORDED, StatusId.PUBLISHED, StatusId.SUBMITTED),
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
