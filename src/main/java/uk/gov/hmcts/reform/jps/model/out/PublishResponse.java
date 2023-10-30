package uk.gov.hmcts.reform.jps.model.out;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.jps.model.FileInfos;
import uk.gov.hmcts.reform.jps.model.PublishErrors;

@Data
@Builder
public class PublishResponse {
    private String publishedByIdamId;
    private String publishedByName;
    private FileInfos fileInfos;
    private PublishErrors errors;
}
