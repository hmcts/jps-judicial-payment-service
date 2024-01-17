package uk.gov.hmcts.reform.jps.domain;

import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;

public class SittingRecordPublishProjection {
    public interface SittingRecordPublishFields {
        Long getId();

        String getPersonalCode();

        Long getContractTypeId();

        String getJudgeRoleTypeId();

        String getEpimmsId();

        LocalDate getSittingDate();

        StatusId getStatusId();
    }
}
