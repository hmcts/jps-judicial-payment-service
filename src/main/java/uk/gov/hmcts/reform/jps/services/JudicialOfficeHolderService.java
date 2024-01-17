package uk.gov.hmcts.reform.jps.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.jps.domain.JohAttributes;
import uk.gov.hmcts.reform.jps.domain.JohPayroll;
import uk.gov.hmcts.reform.jps.domain.JudicialOfficeHolder;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderDeleteRequest;
import uk.gov.hmcts.reform.jps.model.in.JudicialOfficeHolderRequest;
import uk.gov.hmcts.reform.jps.repository.JudicialOfficeHolderRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class JudicialOfficeHolderService {

    private final JudicialOfficeHolderRepository judicialOfficeHolderRepository;

    public Optional<JudicialOfficeHolder> findById(Long johId) {
        return judicialOfficeHolderRepository.findById(johId);
    }

    public Optional<JudicialOfficeHolder> findByPersonalCode(String personalCode) {
        return judicialOfficeHolderRepository.findByPersonalCode(personalCode);
    }

    public JudicialOfficeHolder save(JudicialOfficeHolder judicialOfficeHolder) {
        return judicialOfficeHolderRepository.save(judicialOfficeHolder);
    }

    @Transactional
    public List<Long> save(JudicialOfficeHolderRequest judicialOfficeHoldersRequest) {

        return Optional.ofNullable(judicialOfficeHoldersRequest.getJudicialOfficeHolders())
            .orElseThrow(() -> new IllegalArgumentException("Joh records missing"))
            .stream()
            .map(this::getDomainJudicialOfficeHolder)
            .collect(collectingAndThen(
                toList(),
                judicialOfficeHolders -> judicialOfficeHolderRepository.saveAll(judicialOfficeHolders).stream()
                    .map(JudicialOfficeHolder::getId)
                    .toList()
            ));
    }


    public Optional<Boolean> getCrownServiceFlag(String personalCode, LocalDate sittingDate) {
        return getFlagValue(personalCode, sittingDate, JohAttributes::isCrownServantFlag);
    }

    public Optional<Boolean> getLondonFlag(String personalCode, LocalDate sittingDate) {
        return getFlagValue(personalCode, sittingDate, JohAttributes::isLondonFlag);
    }

    @NotNull
    private Optional<Boolean> getFlagValue(String personalCode,
                                           LocalDate sittingDate,
                                           Function<JohAttributes, Boolean> flag) {
        Optional<JudicialOfficeHolder> judicialOfficeHolder =
            getJudicialOfficeHolderWithJohAttributes(personalCode, sittingDate);

        return judicialOfficeHolder.stream()
            .map(JudicialOfficeHolder::getJohAttributes)
            .flatMap(Collection::stream)
            .max(comparing(JohAttributes::getEffectiveStartDate))
            .map(flag);
    }

    public Optional<JudicialOfficeHolder> getJudicialOfficeHolderWithJohAttributes(String personalCode,
                                                                                   LocalDate sittingDate) {
        return judicialOfficeHolderRepository.findJudicialOfficeHolderWithJohAttributesFilteredByEffectiveStartDate(
                personalCode,
                sittingDate
            );
    }

    public Optional<JudicialOfficeHolder> getJudicialOfficeHolderWithJohPayroll(String personalCode,
                                                                                LocalDate sittingDate) {
        return judicialOfficeHolderRepository.findJudicialOfficeHolderWithJohPayrollFilteredByEffectiveStartDate(
                personalCode,
                sittingDate
            );
    }

    private JudicialOfficeHolder getDomainJudicialOfficeHolder(
        uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder judicialOfficeHolder) {
        JudicialOfficeHolder domainJudicialOfficeHolder = JudicialOfficeHolder.builder()
            .personalCode(judicialOfficeHolder.getPersonalCode())
            .build();

        addJohAttributes(judicialOfficeHolder, domainJudicialOfficeHolder);

        addJohPayroll(judicialOfficeHolder, domainJudicialOfficeHolder);

        return domainJudicialOfficeHolder;
    }

    private void addJohPayroll(
        uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder judicialOfficeHolder,
        JudicialOfficeHolder domainJudicialOfficeHolder) {

        Optional.ofNullable(judicialOfficeHolder.getJohPayrolls()).stream()
            .flatMap(Collection::stream)
            .map(johPayroll -> JohPayroll.builder()
                .payrollId(johPayroll.getPayrollId())
                .effectiveStartDate(johPayroll.getEffectiveStartDate())
                .judgeRoleTypeId(johPayroll.getJudgeRoleTypeId())
                .build())
            .forEach(domainJudicialOfficeHolder::addJohPayroll);
    }

    private void addJohAttributes(uk.gov.hmcts.reform.jps.model.JudicialOfficeHolder judicialOfficeHolder,
                                  JudicialOfficeHolder domainJudicialOfficeHolder) {
        Optional.ofNullable(judicialOfficeHolder.getJohAttributes()).stream()
            .flatMap(Collection::stream)
            .map(johAttributes -> JohAttributes.builder()
                .londonFlag(johAttributes.isLondonFlag())
                .crownServantFlag(johAttributes.isCrownServantFlag())
                .effectiveStartDate(johAttributes.getEffectiveStartDate())
                .build()
            ).forEach(domainJudicialOfficeHolder::addJohAttributes);
    }

    @Transactional
    public void delete(JudicialOfficeHolderDeleteRequest judicialOfficeHolderDeleteRequest) {
        List<Long> ids = Optional.ofNullable(judicialOfficeHolderDeleteRequest.getJudicialOfficeHolderIds())
            .orElseThrow(() -> new IllegalArgumentException("Joh ids missing"));
        judicialOfficeHolderRepository
            .deleteAllById(ids);
    }
}

