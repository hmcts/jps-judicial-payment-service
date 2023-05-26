package uk.gov.hmcts.reform.jps.config;

public enum Endpoints {

    Test("/test"),
    SittingRecord("/sittingRecord/{sittingRecordId}");

    public String endpoint;

    Endpoints(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getEndpoint(){
        return endpoint;
    }
}
