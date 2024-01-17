package uk.gov.hmcts.reform.jps.model.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.jps.model.RecordingUser;

import java.util.List;

@Data
@Builder
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class SittingRecordSearchResponse {
    private long recordCount;
    private List<RecordingUser> recordingUsers;
    private List<String> johRoles;
    private List<SittingRecord> sittingRecords;
}
