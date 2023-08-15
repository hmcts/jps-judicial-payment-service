package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiRequest;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JudicialUserDetailsService {
    private final JudicialUserDetailsServiceClient judicialUserServiceClient;

    public void setJudicialUserDetails(List<SittingRecord> sittingRecords) {

        JudicialUserDetailsApiRequest judicialUsersApiRequest = sittingRecords.stream()
            .map(SittingRecord::getPersonalCode)
            .collect(collectingAndThen(
                toList(),
                personalCodes -> JudicialUserDetailsApiRequest.builder()
                    .personalCode(personalCodes)
                    .build()
            ));

        List<JudicialUserDetailsApiResponse> judicialUserDetails = judicialUserServiceClient.getJudicialUserDetails(
            judicialUsersApiRequest);


        sittingRecords.forEach(sittingRecord -> {
            String personalName = getJudgeName(
                sittingRecord.getPersonalCode(),
                judicialUserDetails
            );

            sittingRecord.setPersonalName(personalName);
        });
    }

    public void setJudicialUserName(List<SittingRecordWrapper> recordsToBeUpdatedByJudgeName) {
        JudicialUserDetailsApiRequest judicialUsersApiRequest = recordsToBeUpdatedByJudgeName.stream()
                .map(SittingRecordWrapper::getSittingRecordRequest)
                .map(SittingRecordRequest::getPersonalCode)
                .collect(collectingAndThen(
                        toList(),
                        personalCodes -> JudicialUserDetailsApiRequest.builder()
                                .personalCode(personalCodes)
                                .build()
                ));

        List<JudicialUserDetailsApiResponse> judicialUserDetails = judicialUserServiceClient.getJudicialUserDetails(
                judicialUsersApiRequest);

        recordsToBeUpdatedByJudgeName.forEach(sittingRecordWrapper -> {
            String personalName = getJudgeName(
                    sittingRecordWrapper.getSittingRecordRequest().getPersonalCode(),
                    judicialUserDetails
            );

            sittingRecordWrapper.setJudgeRoleTypeName(personalName);
        });
    }

    private String getJudgeName(String personalCode,
                                    List<JudicialUserDetailsApiResponse> judicialUserDetails) {
        return getJudicialUserResponse(personalCode, judicialUserDetails)
            .map(JudicialUserDetailsApiResponse::getFullName)
            .orElse("N/A");
    }

    private Optional<JudicialUserDetailsApiResponse> getJudicialUserResponse(
        String personalCode,
        List<JudicialUserDetailsApiResponse> judicialUserDetails) {

        return judicialUserDetails.stream()
            .filter(judicialUsersApiResponse -> judicialUsersApiResponse.getPersonalCode().equals(personalCode))
            .findAny();
    }


}
