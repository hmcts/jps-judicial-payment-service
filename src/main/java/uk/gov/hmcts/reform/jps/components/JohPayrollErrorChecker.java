package uk.gov.hmcts.reform.jps.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
import uk.gov.hmcts.reform.jps.model.JohPayrollInError;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.refdata.judicial.model.JudicialUserDetailsApiResponse;
import uk.gov.hmcts.reform.jps.services.JudicialOfficeHolderService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JohPayrollErrorChecker extends ErrorChecker {
    private final JudicialOfficeHolderService judicialOfficeHolderService;
    private final JudicialUserDetailsService judicialUserDetailsService;

    @Override
    public void evaluate(PublishErrors publishErrors,
                         String hmctsServiceCode,
                         SittingRecordPublishFields sittingRecord) {
        evaluate(publishErrors, hmctsServiceCode, sittingRecord,
                 () ->
                     judicialOfficeHolderService.getJudicialOfficeHolderWithJohPayroll(
                         sittingRecord.getPersonalCode(),
                         sittingRecord.getSittingDate()
                     )
        );
    }

    @Override
    protected void addErrorInfo(PublishErrors publishErrors,
                                String hmctsServiceCode,
                                SittingRecordPublishFields sittingRecord) {
        JudicialUserDetailsApiResponse judicialUserDetailsApiResponse =
            judicialUserDetailsService.getJudicialUserDetails(
                    sittingRecord.getPersonalCode())
                .orElseThrow(() -> new IllegalArgumentException("JudicialUserDetails not found for personal code: "
                                                                    + sittingRecord.getPersonalCode()));

        publishErrors.addJohPayrollErrors(JohPayrollInError.builder()
                                              .personalCode(sittingRecord.getPersonalCode())
                                              .judgeRoleTypeId(sittingRecord.getJudgeRoleTypeId())
                                              .judgeRoleTypeName(judicialUserDetailsApiResponse.getFullName())
                                              .sittingDate(sittingRecord.getSittingDate())
                                              .email(judicialUserDetailsApiResponse.getEmailId())
                                              .postNominals(judicialUserDetailsApiResponse.getPostNominals())
                                              .fullName(judicialUserDetailsApiResponse.getFullName())
                                              .build());

    }
}
