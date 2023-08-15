package uk.gov.hmcts.reform.jps.exceptions;

@SuppressWarnings("serial")
public class MissingPathVariableException extends RuntimeException {

    public MissingPathVariableException(String message) {
        super(message);
    }
}
