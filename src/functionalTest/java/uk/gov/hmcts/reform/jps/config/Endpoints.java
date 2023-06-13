package uk.gov.hmcts.reform.jps.config;

public enum Endpoints {

    Test("/test"),
    SearchSittingRecordsEndpoint("/sitting-records/searchSittingRecords/{hmctsServiceCode}");
    public String resource;

    Endpoints(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
