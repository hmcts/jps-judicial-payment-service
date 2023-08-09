package uk.gov.hmcts.reform.jps.exceptions;

public class UnknownValueException extends RuntimeException {
    public final String field;

    public UnknownValueException(String field, String message) {
        super(message);
        this.field = field;
    }
}
