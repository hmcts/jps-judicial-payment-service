package uk.gov.hmcts.reform.jps;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
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
import java.util.Optional;

import static java.util.List.of;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.ResponseEntity.badRequest;

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

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException exception,
        HttpHeaders headers,
        HttpStatus status,
        WebRequest request
    ) {
        ModelValidationError error = new ModelValidationError(
            of(new FieldError("RequestNotReadable",
                  Optional.ofNullable(exception.getRootCause())
                      .map(Throwable::getMessage)
                      .orElse(exception.getMessage())
                )
            )
        );
        return badRequest().body(error);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    protected ResponseEntity<Object> handleMissingPathVariableException(MissingPathVariableException exception) {

        ModelValidationError error = new ModelValidationError(
            of(new FieldError("PathVariable", exception.getMessage()))
        );
        return badRequest().body(error);
    }
}
