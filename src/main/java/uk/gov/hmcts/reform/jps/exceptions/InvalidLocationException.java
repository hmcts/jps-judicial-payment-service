package uk.gov.hmcts.reform.jps.exceptions;

public class InvalidLocationException extends RuntimeException {
    public InvalidLocationException() {
        super("invalid location");
    }
}
