package uk.gov.hmcts.reform.jps.exceptions;

public class UnknowValueException extends RuntimeException {
    public final String field;

    public UnknowValueException(String field, String message) {
        super(message);
        this.field = field;
    }
}
