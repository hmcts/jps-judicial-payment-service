package uk.gov.hmcts.reform.jps.model.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.jps.model.ErrorCode;
import uk.gov.hmcts.reform.jps.model.StatusId;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordRequest;

import java.io.Serializable;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JsonInclude(NON_NULL)
@SuppressWarnings("serial")
public class SittingRecordResponse implements Serializable {
    private final SittingRecordRequest postedRecord;
    private final ErrorCode errorCode;
    private final String createdByName;
    private final LocalDateTime createdDateTime;
    private final StatusId statusId;
    private final Boolean am;
    private final Boolean pm;
    private final String judgeRoleTypeId;
    private final String judgeRoleTypeName;
    private final String venue;
}
