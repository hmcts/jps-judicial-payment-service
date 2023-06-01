package uk.gov.hmcts.reform.jps.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorisedException extends RuntimeException  {

    public UnauthorisedException(final String message) {
        super(message);
    }
}
