package uk.gov.hmcts.reform.jps.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException  {

    public ConflictException(final String message) {
        super(message);
    }
}
