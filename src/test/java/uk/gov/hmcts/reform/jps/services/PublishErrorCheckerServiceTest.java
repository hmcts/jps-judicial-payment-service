package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.jps.components.CourtVenueErrorChecker;
import uk.gov.hmcts.reform.jps.components.FeeInErrorChecker;
import uk.gov.hmcts.reform.jps.components.JohAttributesErrorChecker;
import uk.gov.hmcts.reform.jps.components.JohPayrollErrorChecker;
import uk.gov.hmcts.reform.jps.components.ServiceErrorChecker;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ContextConfiguration(classes = {PublishErrorCheckerService.class})
@ActiveProfiles({"test"})
@ExtendWith(MockitoExtension.class)
class PublishErrorCheckerServiceTest {

    @Mock
    CourtVenueService courtVenueService;
    @Mock
    JohPayrollService johPayrollService;
    @Mock
    private FeeService feeService;
    @Mock
    FeeInErrorChecker feeInErrorChecker;
    @Mock
    ServiceService serviceService;
    @Mock
    JudicialOfficeHolderService judicialOfficeHolderService;
    @Mock
    JudicialUserDetailsService judicialUserDetailsService;

    @InjectMocks
    CourtVenueErrorChecker courtVenueErrorChecker;
    @InjectMocks
    JohPayrollErrorChecker johPayrollErrorChecker;
    @InjectMocks
    ServiceErrorChecker serviceErrorChecker;

    JohAttributesErrorChecker johAttributesErrorChecker;

    PublishErrorCheckerService publishErrorCheckerService;

    @BeforeEach
    void setUp() {
        johAttributesErrorChecker = new JohAttributesErrorChecker(judicialOfficeHolderService,
                                                                  judicialUserDetailsService);

        publishErrorCheckerService = new PublishErrorCheckerService(courtVenueErrorChecker,
                                                                    johAttributesErrorChecker,
                                                                    johPayrollErrorChecker,
                                                                    feeInErrorChecker,
                                                                    serviceErrorChecker);
    }

    @Test
    void testAddJohAttributesErrorInfo() {

        PublishErrors publishErrors = PublishErrors.builder().build();
        String personalCode = "Personal Code";
        LocalDate sittingDate = LocalDate.of(1970, 1, 1);

        publishErrorCheckerService.addJohAttributesErrorInfo(publishErrors, personalCode, sittingDate);

        assertEquals(1, publishErrors.getErrorCount());
        assertEquals(personalCode, publishErrors.getJohAttributesInErrors().get(0).getPersonalCode());
        assertEquals(sittingDate, publishErrors.getJohAttributesInErrors().get(0).getSittingDate());
    }
}
