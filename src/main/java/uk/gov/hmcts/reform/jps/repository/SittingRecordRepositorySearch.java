package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;

import java.util.stream.Stream;

public interface SittingRecordRepositorySearch {
    Stream<SittingRecord> find(SittingRecordSearchRequest recordSearchRequest,
                               String hmctsServiceCode);

    long totalRecords(SittingRecordSearchRequest recordSearchRequest,
                          String hmctsServiceCode);
}


