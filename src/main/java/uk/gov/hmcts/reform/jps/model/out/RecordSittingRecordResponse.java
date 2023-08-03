package uk.gov.hmcts.reform.jps.model.out;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Data
@Builder
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@JsonInclude(NON_NULL)
@SuppressWarnings("serial")
public class RecordSittingRecordResponse implements Serializable {
    private final String message;
    private final List<SittingRecordResponse> errorRecords;
}
