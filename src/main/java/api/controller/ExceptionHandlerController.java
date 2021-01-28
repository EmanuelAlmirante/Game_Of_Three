package api.controller;

import api.error.RestError;
import api.exception.RestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerController {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestError> handleExceptions(RestException restException) {
        RestError restError = new RestError(restException.getMessage());

        return new ResponseEntity<>(restError, restException.getHttpStatus());
    }
}
