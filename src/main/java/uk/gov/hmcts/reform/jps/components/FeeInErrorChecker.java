package uk.gov.hmcts.reform.jps.components;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.jps.domain.SittingRecordPublishProjection.SittingRecordPublishFields;
import uk.gov.hmcts.reform.jps.model.FeeInError;
import uk.gov.hmcts.reform.jps.model.PublishErrors;
import uk.gov.hmcts.reform.jps.services.FeeService;
import uk.gov.hmcts.reform.jps.services.refdata.JudicialUserDetailsService;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class FeeInErrorChecker extends ErrorChecker {
    private final FeeService feeService;
    private final JudicialUserDetailsService judicialUserDetailsService;

    @Override
    public void evaluate(PublishErrors publishErrors,
                         String hmctsServiceCode,
                         SittingRecordPublishFields sittingRecord) {
        evaluate(publishErrors, hmctsServiceCode, sittingRecord,
                 () ->
                     feeService.findByHmctsServiceIdAndJudgeRoleTypeIdAndSittingDate(
                         hmctsServiceCode,
                         sittingRecord.getJudgeRoleTypeId(),
                         sittingRecord.getSittingDate()
                     )
        );
    }

    @Override
    protected void addErrorInfo(PublishErrors publishErrors,
                                String hmctsServiceCode,
                                SittingRecordPublishFields sittingRecord) {
        String judicialUserName = judicialUserDetailsService.getJudicialUserName(sittingRecord.getPersonalCode());
        publishErrors.addFeeError(FeeInError.builder()
                                      .hmctsServiceId(hmctsServiceCode)
                                      .judgeRoleTypeId(sittingRecord.getJudgeRoleTypeId())
                                      .judgeRoleTypeName(judicialUserName)
                                      .sittingDate(sittingRecord.getSittingDate())
                                      .build());
    }
}
