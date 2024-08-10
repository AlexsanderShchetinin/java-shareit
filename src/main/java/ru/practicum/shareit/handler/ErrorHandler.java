package ru.practicum.shareit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.exception.*;

import java.io.PrintWriter;
import java.io.StringWriter;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorResponse notFound(MyNotFoundException e) {
        log.debug("{}[33mNot found: {}", (char) 27, e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler
    public ErrorResponse notFound(RepositoryReceiveException e) {
        log.debug("{}[33mRepositoryReceiveException: {}", (char) 27, e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler
    public ErrorResponse duplicated(DuplicatedException e) {
        log.debug("{}[33mDuplicated: {}", (char) 27, e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler
    public ErrorResponse interrupted(InterruptionRuleException e) {
        log.debug("{}[33mInterruption Rule exception: {}", (char) 27, e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse wrongRequested(final BadRequestException e) {
        log.debug("{}[33mBad request: {}", (char) 27, e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalid(final MethodArgumentNotValidException e) {
        log.debug("{}[33mInvalid data: {}", (char) 27, e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalid(final MissingRequestHeaderException e) {
        log.debug("{}[33mMissing Request Header: {}", (char) 27, e.getMessage());
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(final Exception e) {
        log.warn("{}[31mError", (char) 27, e);
        ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        errorResponse.setStackTrace(sStackTrace);
        return errorResponse;
    }

}
