package uk.gov.hmcts.reform.jps.model.in;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.model.ErrorCode;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("serial")
public class SittingRecordResponse implements Serializable {
    private final SittingRecordRequest postedRecord;
    private final ErrorCode errorCode;
    private final String createdByName;
    private final LocalDateTime createdDateTime;
    private final StatusId statusId;
}
