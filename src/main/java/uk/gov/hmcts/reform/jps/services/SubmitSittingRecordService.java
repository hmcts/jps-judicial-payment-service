package uk.gov.hmcts.reform.jps.services;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.components.ApplicationProperties;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

@Service
public class SubmitSittingRecordService extends PublishSittingRecordService {

    public SubmitSittingRecordService(SittingRecordRepository sittingRecordRepository, SittingDaysService sittingDaysService, FeeService feeService, JudicialOfficeHolderService judicialOfficeHolderService, ApplicationProperties properties) {
        super(sittingRecordRepository, sittingDaysService, feeService, judicialOfficeHolderService, properties);
    }

}
