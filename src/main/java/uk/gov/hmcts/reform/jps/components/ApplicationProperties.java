package uk.gov.hmcts.reform.jps.components;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Getter
@Setter
@Component
@Configuration
@Validated
public class ApplicationProperties {

    private final int medicalThreshold;
    private final int maximumNumberOfRecordsPerFile;
    private final List<String> medicalJohRoleIds;

    public ApplicationProperties(
        @Value("${joh.medicalThreshold}") int medicalThreshold,
        @Value("${joh.maximumNumberOfRecordsPerFile}") int maximumNumberOfRecordsPerFile,
        @Value("${joh.medicalJohRoles}") List<String> medicalJohRoles) {
        this.medicalThreshold = medicalThreshold;
        this.maximumNumberOfRecordsPerFile = maximumNumberOfRecordsPerFile;
        this.medicalJohRoleIds = extractJohRolesIds(medicalJohRoles);
    }

    private List<String> extractJohRolesIds(List<String> medicalJohRoles) {
        return medicalJohRoles.stream()
            .map(roles -> roles.substring(0, roles.indexOf("-")))
            .toList();
    }

    public boolean isMedicalMember(String judgeRoleTypeId) {
        return medicalJohRoleIds.contains(judgeRoleTypeId);
    }
}
