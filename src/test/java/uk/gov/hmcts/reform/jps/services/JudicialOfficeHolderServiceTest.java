package uk.gov.hmcts.reform.jps.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderRequest;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class JudicialOfficeHolderServiceTest {

    @Mock
    private JudicialOfficeHolderRepository judicialOfficeHolderRepository;
    @InjectMocks
    private JudicialOfficeHolderService judicialOfficeHolderService;

    /**
     * Method under test: {@link JudicialOfficeHolderService#findById(Long)}.
     */
    @Test
    void testFindJudicialOfficeHolderById() {
        final String Personal_Code = "PersonalCode345";
        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder(1L, Personal_Code);
        JohPayroll johPayroll = createJohPayroll(1L, LocalDate.now(), "jr1111", "pr11222");
        judicialOfficeHolder.addJohPayroll(johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        Optional<JudicialOfficeHolder> ofResult = Optional.of(judicialOfficeHolder);
        when(judicialOfficeHolderRepository.findById(1L)).thenReturn(ofResult);

        Optional<JudicialOfficeHolder> savedJohHolder = judicialOfficeHolderService.findById(1L);
        assertThat(savedJohHolder)
            .map(JudicialOfficeHolder::getId)
            .hasValue(1L);
        assertThat(savedJohHolder)
            .map(JudicialOfficeHolder::getPersonalCode)
            .hasValue(Personal_Code);
        verify(judicialOfficeHolderRepository).findById(anyLong());
    }

    /**
     * Method under test: {@link JudicialOfficeHolderService#findByPersonalCode(String)}.
     */
    @Test
    void testFindJudicialOfficeHolderByPersonalCode() {
        final String Personal_Code = "PersonalCode777";
        JudicialOfficeHolder judicialOfficeHolder = createJudicialOfficeHolder(1L, Personal_Code);
        JohPayroll johPayroll = createJohPayroll(1L, LocalDate.now(), "jr1111", "pr11222");
        judicialOfficeHolder.addJohPayroll(johPayroll);
        johPayroll.setJudicialOfficeHolder(judicialOfficeHolder);
        when(judicialOfficeHolderRepository.findByPersonalCode(Personal_Code))
            .thenReturn(Optional.of(judicialOfficeHolder));


        Optional<JudicialOfficeHolder> savedJohHolder = judicialOfficeHolderService.findByPersonalCode(Personal_Code);

        assertThat(savedJohHolder)
            .map(JudicialOfficeHolder::getId)
            .hasValue(1L);
        assertThat(savedJohHolder)
            .map(JudicialOfficeHolder::getPersonalCode)
            .hasValue(Personal_Code);
        verify(judicialOfficeHolderRepository).findByPersonalCode(Personal_Code);
    }

    @Test
    void shouldReturnJohIdsWhenJohSaved() {
        JudicialOfficeHolderRequest judicialOfficeHolderRequest = new JudicialOfficeHolderRequest();

        uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder firstJohHolder =
            createDomianJudicialOfficeHolder("personalCode1");
        uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder secondJohHolder =
            createDomianJudicialOfficeHolder("personalCode2");
        judicialOfficeHolderRequest.setJudicialOfficeHolders(
            List.of(
                firstJohHolder,
                secondJohHolder
            )
        );
        setJohAttributes(firstJohHolder);
        setJohAttributes(secondJohHolder);
        setJohPayrolls(firstJohHolder);
        setJohPayrolls(secondJohHolder);

        when(judicialOfficeHolderRepository.saveAll(anyList()))
            .thenReturn(List.of(
                createJudicialOfficeHolder(1L, "personalCode1"),
                createJudicialOfficeHolder(2L, "personalCode1")
            ));

        List<Long> ids = judicialOfficeHolderService.save(judicialOfficeHolderRequest);

        assertThat(ids)
            .hasSize(2)
            .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldReturnJohIdsWhenJohPresent() {
        JudicialOfficeHolderRequest judicialOfficeHolderRequest = new JudicialOfficeHolderRequest();

        uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder firstJohHolder =
            createDomianJudicialOfficeHolder("personalCode1");
        uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder secondJohHolder =
            createDomianJudicialOfficeHolder("personalCode2");
        judicialOfficeHolderRequest.setJudicialOfficeHolders(
            List.of(
                firstJohHolder,
                secondJohHolder
            )
        );

        when(judicialOfficeHolderRepository.saveAll(anyList()))
            .thenReturn(List.of(
                createJudicialOfficeHolder(1L, "personalCode1"),
                createJudicialOfficeHolder(2L, "personalCode1")
            ));

        List<Long> ids = judicialOfficeHolderService.save(judicialOfficeHolderRequest);

        assertThat(ids)
            .hasSize(2)
            .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenJohHolderMissing() {
        JudicialOfficeHolderRequest judicialOfficeHolderRequest = new JudicialOfficeHolderRequest();
        assertThatThrownBy(() -> judicialOfficeHolderService.save(judicialOfficeHolderRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Joh records missing");
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenJohIdsMissing() {
        JudicialOfficeHolderDeleteRequest judicialOfficeHolderDeleteRequest = new JudicialOfficeHolderDeleteRequest();
        assertThatThrownBy(() -> judicialOfficeHolderService.delete(judicialOfficeHolderDeleteRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Joh ids missing");
    }

    @Test
    void shouldDeleteWhenRequestContainsIds() {
        JudicialOfficeHolderDeleteRequest judicialOfficeHolderDeleteRequest = new JudicialOfficeHolderDeleteRequest();
        judicialOfficeHolderDeleteRequest.setJudicialOfficeHolderIds(
            List.of(
                1L, 2L
            )
        );
        judicialOfficeHolderService.delete(judicialOfficeHolderDeleteRequest);
        verify(judicialOfficeHolderRepository).deleteAllById(anyList());
    }

    private void setJohAttributes(uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder judicialOfficeHolder) {
        judicialOfficeHolder.setJohAttributes(
            Set.of(
                createDomainJohAttributes(LocalDate.now().minusDays(2), false, true),
                createDomainJohAttributes(LocalDate.now().minusDays(10), true, false)
            )
        );
    }

    private void setJohPayrolls(uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder judicialOfficeHolder) {
        judicialOfficeHolder.setJohPayrolls(
            List.of(
                createDomainJohPayroll(LocalDate.now(), "jr1111", "pr11222"),
                createDomainJohPayroll(LocalDate.now(), "jr2222", "pr33333")
                )
        );
    }

    private uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder createDomianJudicialOfficeHolder(
        String personalCode
    ) {
        return  uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder.builder()
            .personalCode(personalCode)
            .build();
    }

    private JudicialOfficeHolder createJudicialOfficeHolder(Long id, String personalCode) {
        return JudicialOfficeHolder.builder()
            .id(id)
            .personalCode(personalCode)
            .build();
    }

    private JohPayroll createJohPayroll(Long id, LocalDate effectiveDate, String judgeRoleTypeId, String payrollId) {
        return JohPayroll.builder()
            .id(id)
            .effectiveStartDate(effectiveDate)
            .judgeRoleTypeId(judgeRoleTypeId)
            .payrollId(payrollId)
            .build();
    }

    private uk.gov.hmcts.reform.jps.model.JohPayroll createDomainJohPayroll(
        LocalDate effectiveDate,
        String judgeRoleTypeId,
        String payrollId
    ) {
        return uk.gov.hmcts.reform.jps.model.JohPayroll.builder()
            .effectiveStartDate(effectiveDate)
            .judgeRoleTypeId(judgeRoleTypeId)
            .payrollId(payrollId)
            .build();
    }

    private uk.gov.hmcts.reform.jps.model.JohAttributes createDomainJohAttributes(
        LocalDate effectiveDate,
        boolean crownServantFlag,
        boolean londonFlag) {
        return uk.gov.hmcts.reform.jps.model.JohAttributes.builder()
            .effectiveStartDate(effectiveDate)
            .crownServantFlag(crownServantFlag)
            .londonFlag(londonFlag)
            .build();
    }

}

