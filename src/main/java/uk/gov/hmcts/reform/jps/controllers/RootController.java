package uk.gov.hmcts.reform.jps.controllers;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import static org.springdoc.core.Constants.SWAGGER_UI_URL;
import static org.springframework.http.ResponseEntity.ok;

/**
 * Default endpoints per application.
 */
@RestController
@Hidden
public class RootController {

    @GetMapping(value = "/swagger")
    public RedirectView swaggerRedirect() {
        return new RedirectView(SWAGGER_UI_URL, true, false);
    }

    /**
     * Root GET endpoint.
     *
     * <p>Azure application service has a hidden feature of making requests to root endpoint when
     * "Always On" is turned on.
     * This is the endpoint to deal with that and therefore silence the unnecessary 404s as a response code.
     *
     * @return Welcome message from the service.
     */
    @GetMapping("/")
    public ResponseEntity<String> welcome() {

        return ok("Welcome to jps-judicial-payment-service");
    }
}
