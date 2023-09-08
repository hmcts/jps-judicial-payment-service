package uk.gov.hmcts.reform.jps.domain;

import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;

public class SittingRecordDuplicateProjection {
    public interface SittingRecordDuplicateCheckFields {
        Long getId();

        LocalDate getSittingDate();

        String getEpimmsId();

        String getPersonalCode();

        Boolean getAm();

        Boolean getPm();

        StatusId getStatusId();

        String getJudgeRoleTypeId();
    }
}
