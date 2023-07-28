package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.RecordSubmitFields;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;

import java.util.List;

public interface SittingRecordRepositorySearch {
    List<SittingRecord> find(SittingRecordSearchRequest recordSearchRequest,
                             String hmctsServiceCode);

    int totalRecords(SittingRecordSearchRequest recordSearchRequest,
                          String hmctsServiceCode);

    List<RecordSubmitFields> findRecordsToSubmit(SubmitSittingRecordRequest recordSearchRequest,
                                                 String hmctsServiceCode);
}


