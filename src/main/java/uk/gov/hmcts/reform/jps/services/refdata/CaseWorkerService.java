package uk.gov.hmcts.reform.jps.services.refdata;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.jps.model.out.SittingRecord;
import uk.gov.hmcts.reform.jps.refdata.caseworker.model.CaseWorkerApiResponse;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
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
            SittingRecord::getCreatedByUserId,
            (caseWorkerApiResponse, sittingRecord) -> sittingRecord.setCreatedByUserName(
                getName(caseWorkerApiResponse))
        );
        setCaseWorkerDetails(
            sittingRecords,
            caseWorkerClient::getCaseWorkerDetails,
            SittingRecord::getChangeByUserId,
            (caseWorkerApiResponse, sittingRecord) -> sittingRecord.setChangeByUserName(
                getName(caseWorkerApiResponse))
        );
    }

    private void setCaseWorkerDetails(List<SittingRecord> sittingRecords,
                                      Function<String, CaseWorkerApiResponse> caseWorkerResponse,
                                      Function<SittingRecord, String> caseWorkerId,
                                      BiConsumer<CaseWorkerApiResponse, SittingRecord> caseWorkerUpdate) {
        sittingRecords.forEach(sittingRecord -> {
                    if (Objects.nonNull(caseWorkerId.apply(sittingRecord))) {
                        CaseWorkerApiResponse caseWorkerApiResponse = caseWorkerResponse.apply(
                             caseWorkerId.apply(sittingRecord)
                         );
                        caseWorkerUpdate.accept(caseWorkerApiResponse, sittingRecord);
                    }
            }
            );
    }

}
