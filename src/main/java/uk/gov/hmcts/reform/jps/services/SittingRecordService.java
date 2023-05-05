package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.model.in.SittingRecordSearchRequest;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.jps.model.Duration.AM;
import static uk.gov.hmcts.reform.jps.model.Duration.PM;


@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class SittingRecordService {
    private final SittingRecordRepository sittingRecordRepository;

    public List<SittingRecord> getSittingRecords(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        String userId) {

        int recordCountByUser = sittingRecordRepository.recordCountByUser(
            recordSearchRequest,
            hmctsServiceCode,
            userId
        );

        List<SittingRecord> sittingRecordsByUser = emptyList();
        if (recordCountByUser > 0) {
            sittingRecordsByUser = getSittingRecordsByUser(
                recordSearchRequest,
                hmctsServiceCode,
                userId
            );
        }

        if (recordCountByUser == 0) {
            return getSittingRecords(
                    recordSearchRequest,
                    hmctsServiceCode
                );
        } else if (sittingRecordsByUser.size() < recordSearchRequest.getPageSize()) {

            Map<Integer, SittingRecord> container = new HashMap<>();
            List<SittingRecord> sittingRecords = getSittingRecordsIgnoringUser(recordSearchRequest,
                                                                               hmctsServiceCode,
                                                                               userId);
            for (SittingRecord sittingRecord: sittingRecords) {
                container.put(++recordCountByUser, sittingRecord);
            }

            int startIndex = recordSearchRequest.getOffset() + sittingRecordsByUser.size();
            List<SittingRecord> nonUserRecords = new ArrayList<>();

            for (int i = startIndex + 1, counter = 0;
                 counter < recordSearchRequest.getPageSize() - sittingRecordsByUser.size();
                 i++, counter++) {
                if (Objects.nonNull(container.get(i))) {
                    nonUserRecords.add(container.get(i));
                } else {
                    break;
                }
            }

            return Stream.of(sittingRecordsByUser, nonUserRecords)
                .flatMap(List::stream)
                .collect(toList());
        } else {
            return sittingRecordsByUser;
        }
    }

    private List<SittingRecord> getSittingRecords(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {

        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords = sittingRecordRepository.find(
            recordSearchRequest,
            hmctsServiceCode
        );

        return getSittingRecords(dbSittingRecords);
    }

    @NotNull
    private List<SittingRecord> getSittingRecords(
        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecords) {

        return sittingRecords.stream()
            .map(record ->
                     SittingRecord.builder()
                         .sittingRecordId(record.getId())
                         .sittingDate(record.getSittingDate())
                         .statusId(record.getStatusId())
                         .regionId(record.getRegionId())
                         .epimsId(record.getEpimsId())
                         .hmctsServiceId(record.getHmctsServiceId())
                         .personalCode(record.getPersonalCode())
                         .contractTypeId(record.getContractTypeId())
                         .judgeRoleTypeId(record.getJudgeRoleTypeId())
                         .am(record.isAm() ? AM.name() : null)
                         .pm(record.isPm() ? PM.name() : null)
                         .createdDateTime(record.getCreatedDateTime())
                         .createdByUserId(record.getCreatedByUserId())
                         .changeDateTime(record.getChangeDateTime())
                         .changeByUserId(record.getChangeByUserId())
                         .build())
            .collect(toList());
    }


    @NotNull
    private List<SittingRecord> getSittingRecordsIgnoringUser(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        String userId) {

        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> dbSittingRecords =
            sittingRecordRepository.findByIgnoreUserId(
                recordSearchRequest,
                hmctsServiceCode,
                userId
            );

        return getSittingRecords(dbSittingRecords);
    }

    private  List<SittingRecord> getSittingRecordsByUser(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode,
        String userId) {

        List<uk.gov.hmcts.reform.jps.domain.SittingRecord> sittingRecords =
            sittingRecordRepository.findByUser(
                recordSearchRequest,
                hmctsServiceCode,
                userId
            );

        return getSittingRecords(sittingRecords);
    }

    public int getTotalRecordCount(
        SittingRecordSearchRequest recordSearchRequest,
        String hmctsServiceCode) {

        return sittingRecordRepository.totalRecords(recordSearchRequest,
                                                    hmctsServiceCode);
    }
}
