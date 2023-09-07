package uk.gov.hmcts.reform.jps.model.out;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Data
@Builder
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class SubmitSittingRecordResponse {
    private final int recordsSubmitted;
    private final int recordsClosed;
}
