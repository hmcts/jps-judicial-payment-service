package uk.gov.hmcts.reform.jps.services.refdata;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiRequest;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JudicialUserDetailsServiceTest {

    @Mock
    private JudicialUserDetailsServiceClient judicialUserServiceClient;

    @InjectMocks
    private JudicialUserDetailsService judicialUserDetailsService;

    @Test
    void setJudicialFullNameWhenJudicialDetailsFound() {
        List<SittingRecord> sittingRecords = List.of(
            SittingRecord.builder()
                .personalCode(judicialOfficeHolder1.getPersonalCode())
                .build(),
            SittingRecord.builder()
                .personalCode(judicialOfficeHolder2.getPersonalCode())
                .build(),
            SittingRecord.builder()
                .personalCode(judicialOfficeHolder3.getPersonalCode())
                .build()
        );
        List<String> personalCodes = List.of(
            "1","2","3"
        );
        List<JudicialUserDetailsApiResponse> response = List.of(
            JudicialUserDetailsApiResponse.builder()
                .personalCode("1")
                .fullName("First Judge")
                .build(),
            JudicialUserDetailsApiResponse.builder()
                .personalCode("3")
                .fullName("Third Judge")
                .build()
        );
        when(judicialUserServiceClient.getJudicialUserDetails(JudicialUserDetailsApiRequest.builder()
                                                                  .personalCode(personalCodes)
                                                                  .build()))
            .thenReturn(response);

        judicialUserDetailsService.setJudicialUserDetails(sittingRecords);

        assertThat(sittingRecords)
            .extracting("personalCode", "personalName")
            .contains(
                tuple("2", "N/A"),
                tuple("1", "First Judge"),
                tuple("3", "Third Judge")
            );

    }

}
