package uk.gov.hmcts.reform.jps;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
import uk.gov.hmcts.reform.jps.exceptions.ApiError;
import uk.gov.hmcts.reform.jps.exceptions.ConflictException;
import uk.gov.hmcts.reform.jps.exceptions.ForbiddenException;
import uk.gov.hmcts.reform.jps.exceptions.MissingPathVariableException;
import uk.gov.hmcts.reform.jps.exceptions.ResourceNotFoundException;
import uk.gov.hmcts.reform.jps.exceptions.ServiceException;
import uk.gov.hmcts.reform.jps.exceptions.UnauthorisedException;
import uk.gov.hmcts.reform.jps.exceptions.UnknownValueException;
import uk.gov.hmcts.reform.jps.model.out.errors.FieldError;
import uk.gov.hmcts.reform.jps.model.out.errors.ModelValidationError;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static java.util.List.of;
import static org.springframework.http.HttpStatus.FORBIDDEN;
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
                .toList();

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


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.debug("Resource could not be found: {}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ServiceException.class)
    protected ResponseEntity<Object> handleServiceException(ServiceException ex) {
        log.debug("BadRequestException:{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(UnauthorisedException.class)
    protected ResponseEntity<Object> handleUnauthorisedException(UnauthorisedException ex) {
        log.debug("BadRequestException:{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    protected ResponseEntity<Object> handleConflictException(ConflictException ex) {
        log.debug("BadRequestException:{}", ex.getLocalizedMessage());
        return toResponseEntity(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignStatusException(FeignException ex) {
        String errorMessage = ex.responseBody()
            .map(res -> new String(res.array(), StandardCharsets.UTF_8))
            .orElse(ex.getMessage());
        log.error("Downstream service errors: {}", errorMessage, ex);
        return toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }

    private ResponseEntity<Object> toResponseEntity(HttpStatus status, String... errors) {
        var apiError = new ApiError(status, errors == null ? null : List.of(errors));
        return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
    }

    @ExceptionHandler(UnknownValueException.class)
    protected ResponseEntity<Object> handleUnknowValueException(UnknownValueException exception) {
        ModelValidationError error = new ModelValidationError(
            of(new FieldError(exception.field, exception.getMessage()))
        );
        return badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolationException() {
        ModelValidationError error = new ModelValidationError(
            of(new FieldError("Bad request", "008 could not insert"))
        );
        return badRequest().body(error);
    }

    @ExceptionHandler(ForbiddenException.class)
    protected ResponseEntity<Object> handleForbiddenException(ForbiddenException exception) {
        return ResponseEntity.status(FORBIDDEN).body(exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }
}
