package uk.gov.hmcts.reform.jps.model.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SittingRecord {
    private long sittingRecordId;
    private LocalDate sittingDate;
    private String statusId;
    private String regionId;
    private String regionName;
    private String epimsId;
    private String hmctsServiceId;
    private String personalCode;
    private String personalName;
    private Long contractTypeId;
    private String judgeRoleTypeId;
    private String am;
    private String pm;
    private LocalDateTime createdDateTime;
    private String createdByUserId;
    private String createdByUserName;
    private LocalDateTime changeDateTime;
    private String changeByUserId;
    private String changeByUserName;

    @ToString.Exclude
    private List<StatusHistory> statusHistories;


    @Override
    public int hashCode() {
        return new HashCodeBuilder(
            17,
            37
        ).append(sittingRecordId).append(sittingDate).append(statusId).append(regionId).append(regionName).append(
            epimsId).append(hmctsServiceId).append(personalCode).append(personalName).append(contractTypeId).append(
            judgeRoleTypeId).append(am).append(pm).append(statusHistories).toHashCode();
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (this.getClass() != object.getClass()) {
            return false;
        }

        SittingRecord sittingRecord
            = (SittingRecord) object;


        if (null != sittingRecord.getStatusHistories() && null != this.getStatusHistories()
            && sittingRecord.getStatusHistories().size() == this.getStatusHistories().size()) {
            return true;
        }

        return (sittingRecord.getSittingRecordId() == this.getSittingRecordId()
            && (null == sittingRecord.getAm() && null == this.getAm()
            || null != sittingRecord.getAm() && sittingRecord.getAm().equals(this.getAm()))
            && sittingRecord.getContractTypeId().equals(this.getContractTypeId())
            && sittingRecord.getEpimsId().equals(this.getEpimsId())
            && sittingRecord.getPersonalCode().equals(this.getPersonalCode())
            && (null != sittingRecord.getPersonalName()
            && sittingRecord.getPersonalName().equals(this.getPersonalName())
            || null == sittingRecord.getPersonalName() && null == this.getPersonalName())
            && (null == sittingRecord.getPm() && null == this.getPm()
            || null != sittingRecord.getPm() && sittingRecord.getPm().equals(this.getPm()))
            && sittingRecord.getHmctsServiceId().equals(this.getHmctsServiceId())
            && sittingRecord.getJudgeRoleTypeId().equals(this.getJudgeRoleTypeId())
            && (null != sittingRecord.getRegionName() && sittingRecord.getRegionName().equals(this.getRegionName())
            || null == sittingRecord.getRegionName() && null == this.getRegionName())
            && sittingRecord.getRegionId().equals(this.getRegionId())
            && sittingRecord.getStatusId().equals(this.getStatusId())
            && (null != sittingRecord.getStatusHistories() && null != this.getStatusHistories()
            && sittingRecord.getStatusHistories().size() == this.getStatusHistories().size()
            || null == sittingRecord.getStatusHistories() && null == this.getStatusHistories()
            || null == sittingRecord.getStatusHistories() && null != this.getStatusHistories()
            && this.getStatusHistories().size() == 0
            || null == this.getStatusHistories() && null != sittingRecord.getStatusHistories()
            && sittingRecord.getStatusHistories().size() == 0));
    }

    public boolean equalsDomainObject(Object object) {
        if (object == null) {
            return false;
        }

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
            = (uk.gov.hmcts.reform.jps.domain.SittingRecord) object;

        return (sittingRecord.getId() == this.getSittingRecordId()
            && (null == this.getAm() && !sittingRecord.isAm()
            || null != this.getAm() && this.getAm().equals(AM.name()) && sittingRecord.isAm())
            && sittingRecord.getContractTypeId().equals(this.getContractTypeId())
            && sittingRecord.getEpimsId().equals(this.getEpimsId())
            && sittingRecord.getPersonalCode().equals(this.getPersonalCode())
            && (null == this.getPm() && !sittingRecord.isPm()
            || null != this.getPm() && this.getPm().equals(PM.name()) && sittingRecord.isPm())
            && sittingRecord.getHmctsServiceId().equals(this.getHmctsServiceId())
            && sittingRecord.getJudgeRoleTypeId().equals(this.getJudgeRoleTypeId())
            && sittingRecord.getRegionId().equals(this.getRegionId())
            && sittingRecord.getStatusId().equals(this.getStatusId())
            && (null == sittingRecord.getStatusHistories() && null == this.getStatusHistories()
            || null != sittingRecord.getStatusHistories() && null != this.getStatusHistories()
            && sittingRecord.getStatusHistories().size() == this.getStatusHistories().size()));
    }

}
