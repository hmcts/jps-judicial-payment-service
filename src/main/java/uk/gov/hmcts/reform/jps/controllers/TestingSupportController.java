package uk.gov.hmcts.reform.jps.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.CourtVenueRequest;
import uk.gov.hmcts.reform.jps.model.in.FeeDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.FeeRequest;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderRequest;
import uk.gov.hmcts.reform.jps.model.in.ServiceDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.ServiceRequest;
import uk.gov.hmcts.reform.jps.model.out.CourtVenueResponse;
import uk.gov.hmcts.reform.jps.model.out.FeeResponse;
import uk.gov.hmcts.reform.jps.model.out.JudicialOfficeHolderResponse;
import uk.gov.hmcts.reform.jps.model.out.ServiceResponse;
import uk.gov.hmcts.reform.jps.services.CourtVenueService;
import uk.gov.hmcts.reform.jps.services.FeeService;
import uk.gov.hmcts.reform.jps.services.JudicialOfficeHolderService;
import uk.gov.hmcts.reform.jps.services.ServiceService;

import java.util.List;

import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_400;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_401;
import static uk.gov.hmcts.reform.jps.controllers.ControllerResponseMessage.RESPONSE_403;

@RestController
@RequestMapping(
    path = "/testing-support",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(prefix = "testing", value = "support.enabled", havingValue = "true")
public class TestingSupportController {
    private final JudicialOfficeHolderService judicialOfficeHolderService;
    private final ServiceService serviceService;
    private final CourtVenueService courtVenueService;
    private final FeeService feeService;

    @Operation(description = "Creates Judicial Office Holder records")
    @ApiResponse(responseCode = "201",
        description = "Successfully created Judicial Office Holder records",
        content = @Content(schema = @Schema(implementation = CourtVenueResponse.class)))
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "save-judicial-office-holders")
    public ResponseEntity<JudicialOfficeHolderResponse> saveJudicialOfficeHolders(
        @RequestBody JudicialOfficeHolderRequest judicialOfficeHoldersRequest
    ) {
        List<Long> johIds = judicialOfficeHolderService.save(judicialOfficeHoldersRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(
            JudicialOfficeHolderResponse.builder()
                .judicialOfficeHolderIds(johIds)
                .build()
        );
    }

    @Operation(description = "Deletes Judicial Office Holder records")
    @ApiResponse(responseCode = "200",
        description = "Successfully deleted Judicial Office Holder records")
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "delete-judicial-office-holders")
    public ResponseEntity<Object> deleteJudicialOfficeHolders(
        @RequestBody JudicialOfficeHolderDeleteRequest judicialOfficeHolderDeleteRequest
    ) {
        judicialOfficeHolderService.delete(judicialOfficeHolderDeleteRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(description = "Creates service records")
    @ApiResponse(responseCode = "201",
        description = "Successfully created service records",
        content = @Content(schema = @Schema(implementation = ServiceResponse.class)))
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "save-service")
    public ResponseEntity<ServiceResponse> saveServices(@RequestBody ServiceRequest serviceRequests) {
        List<Long> serviceIds = serviceService.save(serviceRequests);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ServiceResponse.builder()
                      .serviceIds(serviceIds)
                      .build());
    }

    @Operation(description = "Deletes service records")
    @ApiResponse(responseCode = "200",
        description = "Successfully deleted service records")
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "delete-service")
    public ResponseEntity<Object> deleteServices(@RequestBody ServiceDeleteRequest serviceRequests) {
        serviceService.delete(serviceRequests);
        return ResponseEntity.ok().build();
    }

    @Operation(description = "Creates court venue records")
    @ApiResponse(responseCode = "201",
        description = "Successfully created court venue records",
        content = @Content(schema = @Schema(implementation = CourtVenueResponse.class)))
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "save-court-venue")
    public ResponseEntity<CourtVenueResponse> saveCourtVenues(@RequestBody CourtVenueRequest courtVenueRequest) {
        List<Long> courtIds = courtVenueService.save(courtVenueRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CourtVenueResponse.builder()
                      .courtVenueIds(courtIds)
                      .build());
    }

    @Operation(description = "Deletes court venue records")
    @ApiResponse(responseCode = "200",
        description = "Successfully deleted court venue records")
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "delete-court-venue")
    public ResponseEntity<CourtVenueResponse> deleteCourtVenues(
        @RequestBody CourtVenueDeleteRequest courtVenueRequest
    ) {
        courtVenueService.delete(courtVenueRequest);

        return ResponseEntity.ok().build();
    }

    @Operation(description = "Creates fee records")
    @ApiResponse(responseCode = "201",
        description = "Successfully created fee records",
        content = @Content(schema = @Schema(implementation = FeeResponse.class)))
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "save-fee")
    public ResponseEntity<FeeResponse> saveFee(@RequestBody FeeRequest feeRequest) {
        List<Long> feeIds = feeService.save(feeRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(FeeResponse.builder()
                      .fees(feeIds)
                      .build());
    }

    @Operation(description = "Deletes fee records")
    @ApiResponse(responseCode = "200",
        description = "Successfully deleted fee records")
    @ApiResponse(responseCode = "400", description = RESPONSE_400, content = @Content)
    @ApiResponse(responseCode = "401", description = RESPONSE_401, content = @Content)
    @ApiResponse(responseCode = "403", description = RESPONSE_403, content = @Content)

    @PostMapping(path = "delete-fee")
    public ResponseEntity<FeeResponse> deleteFee(@RequestBody FeeDeleteRequest feeDeleteRequest) {
        feeService.delete(feeDeleteRequest);

        return ResponseEntity.ok().build();
    }
}
