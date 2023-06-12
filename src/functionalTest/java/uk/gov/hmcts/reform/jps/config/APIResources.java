package uk.gov.hmcts.reform.jps.config;

public enum APIResources {

    Test("/test"),
    FeeEndpoint("/fee/{hmctsServiceCode}"),
    SearchSittingRecordsEndpoint("/sitting-records/searchSittingRecords/{hmctsServiceCode}");
    public String resource;

    APIResources(String resource) {
        this.resource = resource;
    }

    public String getResource() {
        return resource;
    }
}
