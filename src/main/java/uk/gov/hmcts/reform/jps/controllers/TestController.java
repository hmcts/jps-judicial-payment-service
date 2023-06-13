package uk.gov.hmcts.reform.jps.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Validated
public class TestController {

    private final SecurityUtils securityUtils;

    public TestController(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @GetMapping(path = "/test", produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('jps-recorder', 'jps-submitter', 'jps-publisher', 'jps-admin')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity getUserRoles() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
