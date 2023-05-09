package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;

import java.util.List;

public interface SittingRecordRepositorySearch {
    List<SittingRecord> find(SittingRecordSearchRequest recordSearchRequest,
                             String hmctsServiceCode);

    int totalRecords(SittingRecordSearchRequest recordSearchRequest,
                          String hmctsServiceCode);
}


