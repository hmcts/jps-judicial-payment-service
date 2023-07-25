package uk.gov.hmcts.reform.jps.model.in;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("serial")
public class RecordSittingRecordResponse implements Serializable {
    private final List<SittingRecordResponse> errorRecords;
}
