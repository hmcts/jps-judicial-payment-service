package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import uk.gov.hmcts.reform.jps.BaseTest;
import uk.gov.hmcts.reform.jps.domain.SittingRecordDuplicateProjection;
import uk.gov.hmcts.reform.jps.model.SittingRecordWrapper;
import uk.gov.hmcts.reform.jps.repository.SittingRecordRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static java.time.Month.JUNE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.jps.model.StatusId.CLOSED;
import static uk.gov.hmcts.reform.jps.model.StatusId.DELETED;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

public class StatusHistoryServiceITest extends BaseTest {
    @Autowired
    private SittingRecordRepository sittingRecordRepository;

    @Autowired
    private StatusHistoryService statusHistoryService;


    @Test
    @Sql(scripts = {RESET_DATABASE, ADD_SITTING_RECORD_STATUS_HISTORY})
    void shouldUpdateWithStatusHistoryWhenDbRecordPresent() {
        List<SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields> dbRecord
            = sittingRecordRepository.findBySittingDateAndEpimmsIdAndPersonalCodeAndStatusIdNotIn(
            LocalDate.of(2023, Month.MAY, 11),
            "852649",
            "4918178",
            List.of(DELETED, CLOSED)
        ).stream().toList();


        SittingRecordWrapper wrapper = SittingRecordWrapper.builder().build();

        SittingRecordDuplicateProjection.SittingRecordDuplicateCheckFields
            sittingRecordDuplicateCheckFields = dbRecord.get(0);


        statusHistoryService.updateFromStatusHistory(wrapper, sittingRecordDuplicateCheckFields);
        assertThat(wrapper.getCreatedByName()).isEqualTo("Recorder");
        assertThat(wrapper.getCreatedDateTime().toLocalDate())
            .isEqualTo(LocalDateTime.of(2023, JUNE, 27, 11, 40, 30, 490419)
                           .toLocalDate());
        assertThat(wrapper.getStatusId()).isEqualTo(RECORDED);
    }
}
