package uk.gov.hmcts.reform.jps.model.in;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("serial")
public class RecordSittingRecordRequest implements Serializable {
    private static final String INSUFFICIENT_SITTING_RECORDS = "006 Insufficient sitting Records";

    @NotBlank(message = "Recorded By Idam Id is mandatory")
    private final String recordedByIdamId;

    @NotBlank(message = "Recorded By Name is mandatory")
    private final String recordedByName;

    @NotNull(message = INSUFFICIENT_SITTING_RECORDS)
    @Size(min = 1, message = INSUFFICIENT_SITTING_RECORDS)
    @Valid
    private final List<SittingRecordRequest> recordedSittingRecords;
}
