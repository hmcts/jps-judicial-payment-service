package uk.gov.hmcts.reform.jps.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FileInfo {
    private String fileId;
    private final LocalDate fileCreationDate = LocalDate.now();
    private String fileCreatedById;
    private String fileCreatedByName;
    private String fileName;
    private int recordCount;
}
