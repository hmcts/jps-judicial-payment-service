package uk.gov.hmcts.reform.jps.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

import java.time.LocalDateTime;

import static uk.gov.hmcts.reform.jps.model.ErrorCode.VALID;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SittingRecordWrapper {
    private final SittingRecordRequest sittingRecordRequest;
    @Builder.Default
    private ErrorCode errorCode = VALID;
    private Long sittingRecordId;
    private LocalDateTime createdDateTime;
    private String createdByName;
    private StatusId statusId;
    private boolean delete;
    private String regionId;
}
