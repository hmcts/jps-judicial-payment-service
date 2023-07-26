package uk.gov.hmcts.reform.jps.controllers;

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

    @DeleteMapping(
        path = {"/{sittingRecordId}"}
    )
    @PreAuthorize("hasAnyAuthority('" + JPS_RECORDER + "','" + JPS_SUBMITTER + "','" + JPS_ADMIN + "')")
    public ResponseEntity<String> deleteSittingRecord(
        @PathVariable("sittingRecordId") Optional<Long> requestSittingRecordId) {
        Long sittingRecordId = Utility.validateSittingRecordId(requestSittingRecordId);
        sittingRecordService.deleteSittingRecord(sittingRecordId);
        return ResponseEntity.ok().build();
    }
}
