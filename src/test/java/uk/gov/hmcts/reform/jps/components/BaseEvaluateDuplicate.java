package uk.gov.hmcts.reform.jps.components;

import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.util.Random;

public class BaseEvaluateDuplicate {

    public SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields getDbRecord(LocalDate sittingDate,
                                                                                           String epimmsId,
                                                                                           String personalCode,
                                                                                           Boolean am,
                                                                                           Boolean pm,
                                                                                           String judgeRoleTypeId,
                                                                                           final StatusId statusId) {
        return new SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields() {

            @Override
            public Long getId() {
                return new Random().nextLong();
            }

            @Override
            public LocalDate getSittingDate() {
                return sittingDate;
            }

            @Override
            public String getEpimmsId() {
                return epimmsId;
            }

            @Override
            public String getPersonalCode() {
                return personalCode;
            }

            @Override
            public Boolean getAm() {
                return am;
            }

            @Override
            public Boolean getPm() {
                return pm;
            }

            @Override
            public StatusId getStatusId() {
                return statusId;
            }

            @Override
            public String getJudgeRoleTypeId() {
                return judgeRoleTypeId;
            }
        };
    }
}
