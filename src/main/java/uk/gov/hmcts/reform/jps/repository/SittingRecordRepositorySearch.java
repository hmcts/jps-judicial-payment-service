package uk.gov.hmcts.reform.jps.repository;

import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.model.RecordSubmitFields;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.in.SubmitSittingRecordRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public interface SittingRecordRepositorySearch {
    Stream<SittingRecord> find(SittingRecordSearchRequest recordSearchRequest, String hmctsServiceCode,
                               LocalDate serviceOnboardedDate, List<String> medicalJohIds);

    long totalRecords(SittingRecordSearchRequest recordSearchRequest, String hmctsServiceCode,
                      LocalDate serviceOnboardedDate, List<String> medicalJohIds);

    List<RecordSubmitFields> findRecordsToSubmit(SubmitSittingRecordRequest recordSearchRequest,
                                                 String hmctsServiceCode);
}


