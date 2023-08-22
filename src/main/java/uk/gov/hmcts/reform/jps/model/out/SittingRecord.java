package uk.gov.hmcts.reform.jps.model.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SittingRecord {
    private Long sittingRecordId;
    private LocalDate sittingDate;
    private String statusId;
    private String regionId;
    private String regionName;
    private String epimmsId;
    private String venueName;
    private String hmctsServiceId;
    private String personalCode;
    private String personalName;
    private Long contractTypeId;
    private String contractTypeName;
    private String judgeRoleTypeId;
    private String judgeRoleTypeName;
    @Builder.Default
    private Boolean crownServantFlag = false;
    @Builder.Default
    private Boolean londonFlag = false;
    private String payrollId;
    private String accountCode;
    private Long fee;
    private Boolean am;
    private Boolean pm;
    private LocalDateTime createdDateTime;
    private String createdByUserId;
    private String createdByUserName;
    private LocalDateTime changedDateTime;
    private String changedByUserId;
    private String changedByUserName;

    @JsonIgnore
    @ToString.Exclude
    private List<StatusHistory> statusHistories;

    public String getCreatedByUserId() {
        StatusHistory statusHistory = getFirstStatusHistory();
        return null != statusHistory ? statusHistory.getChangedByUserId() : null;
    }

    @JsonIgnore
    public StatusHistory getFirstStatusHistory() {
        return statusHistories.stream().min(Comparator.comparingLong(StatusHistory::getId)).orElse(null);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(
            17,
            37
        ).append(sittingRecordId).append(sittingDate).append(statusId).append(regionId).append(regionName).append(
            epimmsId).append(hmctsServiceId).append(personalCode).append(personalName).append(contractTypeId).append(
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

        return (sittingRecord.getSittingRecordId().equals(this.getSittingRecordId())

            && ((null == sittingRecord.getAm() && null == this.getAm())
            || (null != sittingRecord.getAm() && sittingRecord.getAm().equals(this.getAm())))

            && ((null == sittingRecord.getContractTypeId() && null == this.getContractTypeId())
            || (null != sittingRecord.getContractTypeId() && null != this.getContractTypeId()
            && sittingRecord.getContractTypeId().equals(this.getContractTypeId())))

            && ((null == sittingRecord.getEpimmsId() && null == this.getEpimmsId())
            || (null != sittingRecord.getEpimmsId() && null != this.getEpimmsId()
            && sittingRecord.getEpimmsId().equals(this.getEpimmsId())))

            && ((null == sittingRecord.getPersonalCode() && null == this.getPersonalCode())
            || (null != sittingRecord.getPersonalCode() && null != this.getPersonalCode()
            && sittingRecord.getPersonalCode().equals(this.getPersonalCode())))

            && ((null == sittingRecord.getPersonalName() && null == this.getPersonalName())
            || (null != sittingRecord.getPersonalName() && null != this.getPersonalName()
            && sittingRecord.getPersonalName().equals(this.getPersonalName())))

            && ((null == sittingRecord.getPm() && null == this.getPm())
            || null != sittingRecord.getPm() && sittingRecord.getPm().equals(this.getPm()))

            && ((null == sittingRecord.getHmctsServiceId() && null == this.getHmctsServiceId())
            || (null != sittingRecord.getHmctsServiceId() && null != this.getHmctsServiceId()
            && sittingRecord.getHmctsServiceId().equals(this.getHmctsServiceId())))

            && ((null == sittingRecord.getJudgeRoleTypeId() && null == this.getJudgeRoleTypeId())
            || (null != sittingRecord.getJudgeRoleTypeId() && null != this.getJudgeRoleTypeId()
            && sittingRecord.getJudgeRoleTypeId().equals(this.getJudgeRoleTypeId())))

            && ((null == sittingRecord.getRegionName() && null == this.getRegionName())
            || (null != sittingRecord.getRegionName() && null != this.getRegionName()
            && sittingRecord.getRegionName().equals(this.getRegionName())))

            && ((null == sittingRecord.getRegionId() && null == this.getRegionId())
            || (null != sittingRecord.getRegionId() && null != this.getRegionId()
            && sittingRecord.getRegionId().equals(this.getRegionId())))

            && ((null == sittingRecord.getStatusId() && null == this.getStatusId())
            || (null != sittingRecord.getStatusId() && null != this.getStatusId()
            && sittingRecord.getStatusId().equals(this.getStatusId()))));
    }

    public boolean equalsDomainObject(Object object) {
        if (object == null) {
            return false;
        }

        uk.gov.hmcts.reform.jps.domain.SittingRecord sittingRecord
            = (uk.gov.hmcts.reform.jps.domain.SittingRecord) object;

        return (sittingRecord.getId().equals(this.getSittingRecordId())
            && (Objects.nonNull(this.getAm()) && this.getAm().equals(sittingRecord.isAm()))
            && sittingRecord.getContractTypeId().equals(this.getContractTypeId())
            && sittingRecord.getEpimmsId().equals(this.getEpimmsId())
            && sittingRecord.getPersonalCode().equals(this.getPersonalCode())
            && (Objects.nonNull(this.getPm()) && this.getPm().equals(sittingRecord.isPm()))
            && sittingRecord.getHmctsServiceId().equals(this.getHmctsServiceId())
            && sittingRecord.getJudgeRoleTypeId().equals(this.getJudgeRoleTypeId())
            && sittingRecord.getRegionId().equals(this.getRegionId())
            && (Objects.isNull(sittingRecord.getStatusHistories())
                && Objects.isNull(this.getStatusHistories())
                || (Objects.nonNull(sittingRecord.getStatusHistories())
                && Objects.nonNull(this.getStatusHistories())
                && sittingRecord.getStatusHistories().size() == this.getStatusHistories().size()
                && this.getStatusHistories().containsAll(sittingRecord.getStatusHistories()))));
    }

}
