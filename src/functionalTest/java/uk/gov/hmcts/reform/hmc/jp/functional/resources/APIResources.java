package uk.gov.hmcts.reform.hmc.jp.functional.resources;

public enum APIResources {

    FeeEndpoint("/fee/{hmctsServiceCode}");
    public String resource;

    APIResources(String resource) {
        this.resource = resource;
    }

    public String getResource(){
        return resource;
    }
}
