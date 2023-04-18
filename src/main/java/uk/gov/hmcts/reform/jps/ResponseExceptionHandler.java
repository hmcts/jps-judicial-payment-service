package uk.gov.hmcts.reform.jps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import uk.gov.hmcts.reform.jps.expection.MissingPathVariableException;
import uk.gov.hmcts.reform.jps.model.out.errors.FieldError;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.http.ResponseEntity.status;

@ControllerAdvice
@Slf4j
public class ResponseExceptionHandler extends ResponseEntityExceptionHandler {

    @InitBinder
    protected void activateDirectFieldAccess(DataBinder dataBinder) {
        dataBinder.initDirectFieldAccess();
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request
    ) {
        List<FieldError> fieldErrors =
            exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new FieldError(err.getField(), err.getDefaultMessage()))
                .collect(toList());

        ModelValidationError error = new ModelValidationError(fieldErrors);

        log.info("Bad request: {}", error);

        return badRequest().body(error);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    protected ResponseEntity<String> handleMissingPathVariableException(MissingPathVariableException exception) {
        return status(BAD_REQUEST).body(exception.getMessage());
    }
}
