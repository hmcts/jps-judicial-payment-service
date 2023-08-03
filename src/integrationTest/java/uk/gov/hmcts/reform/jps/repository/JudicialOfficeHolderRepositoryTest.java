package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.domain.SittingRecord;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.StatusId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
class JudicialOfficeHolderRepositoryTest {

    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;
    @Autowired
    private SittingRecordRepository recordRepository;
    private static final String PERSONAL_CODE = "001";

    private static final Logger LOGGER = LoggerFactory.getLogger(JudicialOfficeHolderRepositoryTest.class);

    @BeforeEach
    public void setUp() {

        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode(PERSONAL_CODE)
            .build();
        LOGGER.debug("judicialOfficeHolder:{}", judicialOfficeHolder);
        judicialOfficeHolderRepository.save(judicialOfficeHolder);

        SittingRecord sittingRecord = SittingRecord.builder()
            .am(true)
            .contractTypeId(2L)
            .epimsId("123")
            .hmctsServiceId("ssc_id")
            .judgeRoleTypeId("HighCourt")
            .personalCode(judicialOfficeHolder.getPersonalCode())
            .regionId("1")
            .sittingDate(LocalDate.now().minusDays(2))
            .build();

        StatusHistory statusHistory = StatusHistory.builder()
            .changeByName("John Doe")
            .changeByUserId("jp-recorder")
            .changeDateTime(LocalDateTime.now())
            .statusId(StatusId.RECORDED.name())
            .build();
        sittingRecord.addStatusHistory(statusHistory);

        SittingRecord persistedSittingRecord = recordRepository.save(sittingRecord);
        LOGGER.info("persistedSittingRecord:{}", persistedSittingRecord);

        List<JudicialOfficeHolder> list = judicialOfficeHolderRepository.findAll();
        LOGGER.info("list.size:{}", list);
        assertFalse(list.isEmpty());
    }

    @Test
    void shouldSaveJudicialOfficeHolder() {
        List<JudicialOfficeHolder> list = judicialOfficeHolderRepository.findAll();

        assertNotNull(list);
        assertFalse(list.isEmpty());
        JudicialOfficeHolder judicialOfficeHolder = list.get(0);
        assertEquals(judicialOfficeHolder.getId(), 1L);
        assertEquals(judicialOfficeHolder.getPersonalCode(), PERSONAL_CODE);
    }

}

