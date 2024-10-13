package com.findhomes.findhomesbe.exception.advice;

import com.findhomes.findhomesbe.DTO.ErrorReponse;
import com.findhomes.findhomesbe.DTO.HouseDetailResponse;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.exception.exception.DataNotFoundException;
import com.findhomes.findhomesbe.exception.exception.IllegalGptOutputException;
import com.findhomes.findhomesbe.exception.exception.PreconditionRequiredException;
import com.findhomes.findhomesbe.exception.exception.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(PreconditionRequiredException.class)
    public ResponseEntity<ErrorReponse> handleOptionalNotExistException(PreconditionRequiredException e) {
        log.error("[PreconditionRequiredException]", e);
        return new ResponseEntity<>(new ErrorReponse(
                false, 428, e.getMessage()
        ), HttpStatus.PRECONDITION_REQUIRED);
    }

    @ExceptionHandler(IllegalGptOutputException.class)
    public ResponseEntity<ErrorReponse> handleIllegalGptOutputException(IllegalGptOutputException e) {
        return new ResponseEntity<>(new ErrorReponse(
                false, 500, e.getMessage()
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorReponse> handleUnauthorizedException(UnauthorizedException e) {
        return new ResponseEntity<>(new ErrorReponse(
                false, 401, e.getMessage()
        ), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorReponse> handleDataNotFoundException(DataNotFoundException e) {
        return new ResponseEntity<>(new ErrorReponse(
                false, 404, e.getMessage()
        ), HttpStatus.NOT_FOUND);
    }

}
