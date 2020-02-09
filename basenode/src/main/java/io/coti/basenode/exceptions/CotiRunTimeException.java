package io.coti.basenode.exceptions;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class CotiRunTimeException extends RuntimeException {

    public CotiRunTimeException(String message) {
        super(message, null);
    }

    public CotiRunTimeException(String message, Throwable e) {
        super(message);
        if (e != null) {
            Throwable cause = e.getClass().equals(this.getClass()) ? e.getCause() : e;
            this.initCause(cause);
        }
    }

    public void logMessage() {
        log.error("{} :", getClass().getName());
        Arrays.asList(getMessage().split("\n")).forEach(log::error);
        if (getCause() != null) {
            log.error("Cause: {}", getCause().toString());
        }
    }
}
