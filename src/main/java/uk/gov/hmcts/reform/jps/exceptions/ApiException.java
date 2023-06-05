package uk.gov.hmcts.reform.jps.exceptions;

public class ApiException extends RuntimeException {

    public ApiException(final String message, final Throwable e) {
        super(message, e);
    }
}