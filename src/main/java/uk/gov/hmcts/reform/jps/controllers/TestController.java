package uk.gov.hmcts.reform.jps.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.data.SecurityUtils;
import uk.gov.hmcts.reform.jps.exceptions.UnauthorisedException;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Validated
public class TestController {

    private final SecurityUtils securityUtils;

    public TestController(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    @GetMapping(path = "/test", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity getUserRoles() {
        List<String> allowedRoles = Arrays.asList("jps-recorder", "jps-submitter", "jps-publisher", "jps-admin");
        securityUtils.getUserInfo().getRoles().stream()
            .filter(role -> allowedRoles.contains(role)).findFirst()
            .orElseThrow(() -> new UnauthorisedException("you do not have the correct roles to access this endpoint"));
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
