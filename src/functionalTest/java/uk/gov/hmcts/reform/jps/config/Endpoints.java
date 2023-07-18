package uk.gov.hmcts.reform.jps.config;

public enum Endpoints {

    Test("/test"),
    RecordSittingRecords("/recordSittingRecords/{hmctsServiceCode}"),
    SearchSittingRecords("/sitting-records/searchSittingRecords/{hmctsServiceCode}"),
    SubmitSittingRecords("/submitSittingRecords/{hmctsServiceCode}");
    public String resource;

    Endpoints(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
