package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.domain.StatusHistory;
import uk.gov.hmcts.reform.jps.model.JpsRole;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.caseworker.model.CaseWorkerApiResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
@Slf4j
public class CaseWorkerService {
    private final CaseWorkerClient caseWorkerClient;

    @NotNull
    private static String getName(CaseWorkerApiResponse caseWorkerApiResponse) {
        return String.join(
            " ",
            caseWorkerApiResponse.getFirstName(),
            caseWorkerApiResponse.getLastName()
        );
    }

    public void setCaseWorkerDetails(List<SittingRecord> sittingRecords) {
        setCaseWorkerDetails(
            sittingRecords,
            caseWorkerClient::getCaseWorkerDetails,
            SittingRecord::getChangeByUserId,
            (caseWorkerApiResponse, sittingRecord) -> {
                StatusHistory statusHistory = StatusHistory.builder()
                    .statusId(sittingRecord.getStatusId())
                    .changeDateTime(LocalDateTime.now())
                    .changeByUserId(JpsRole.ROLE_RECORDER.getValue())
                    .changeByName(getName(caseWorkerApiResponse))
                    .build();
                sittingRecord.setStatusHistories(List.of(statusHistory));
            }
        );
    }

    private void setCaseWorkerDetails(List<SittingRecord> sittingRecords,
                                      Function<String, CaseWorkerApiResponse> caseWorkerResponse,
                                      Function<SittingRecord, String> caseWorkerId,
                                      BiConsumer<CaseWorkerApiResponse, SittingRecord> caseWorkerUpdate) {
        sittingRecords.forEach(sittingRecord -> {
            try {
                if (Objects.nonNull(caseWorkerId.apply(sittingRecord))) {
                    CaseWorkerApiResponse caseWorkerApiResponse = caseWorkerResponse.apply(
                        caseWorkerId.apply(sittingRecord)
                    );
                    caseWorkerUpdate.accept(caseWorkerApiResponse, sittingRecord);
                }
            } catch (RuntimeException e) {
                log.error("Caseworker {} lookup error {}",
                        caseWorkerId.apply(sittingRecord),
                        e.getMessage());
            }
        });
    }
}
