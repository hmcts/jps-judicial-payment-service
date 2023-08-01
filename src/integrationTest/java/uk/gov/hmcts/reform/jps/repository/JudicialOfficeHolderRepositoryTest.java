package uk.gov.hmcts.reform.jps.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.jps.model.StatusId.RECORDED;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@ActiveProfiles("itest")
@Disabled
class JudicialOfficeHolderRepositoryTest {

    @Autowired
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;
    @Autowired
    private SittingRecordRepository recordRepository;
    private SittingRecord persistedSittingRecord;
    private static final String PERSONAL_CODE = "001";


    private static final Logger LOGGER = LoggerFactory.getLogger(JudicialOfficeHolderRepositoryTest.class);

    @BeforeEach
    public void setUp() {
        SittingRecord sittingRecord = SittingRecord.builder()
            .sittingDate(LocalDate.now().minusDays(2))
            .statusId(RECORDED)
            .regionId("1")
            .epimmsId("123")
            .hmctsServiceId("ssc_id")
            .contractTypeId(2L)
            .am(true)
            .judgeRoleTypeId("HighCourt")
            .build();

        StatusHistory statusHistory = StatusHistory.builder()
            .statusId(RECORDED)
            .changeDateTime(LocalDateTime.now())
            .changeByUserId("jp-recorder")
            .changeByName("John Doe")
            .build();
        sittingRecord.addStatusHistory(statusHistory);

        JudicialOfficeHolder judicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode(PERSONAL_CODE)
            .build();
        //sittingRecord.setJudicialOfficeHolder(judicialOfficeHolder);
        judicialOfficeHolder.addSittingRecord(sittingRecord);
        LOGGER.info("judicialOfficeHolder:{}", judicialOfficeHolder);
        LOGGER.info("sittingRecord:{}", sittingRecord);

        persistedSittingRecord = recordRepository.save(sittingRecord);
        judicialOfficeHolderRepository.save(judicialOfficeHolder);
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

        //assertEquals(persistedSittingRecord.getJudicialOfficeHolder(), judicialOfficeHolder);
    }

}

