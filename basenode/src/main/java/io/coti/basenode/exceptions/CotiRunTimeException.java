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
        Arrays.asList(getMessage().split("\n")).forEach(error -> log.error(error));
        if (getCause() != null) {
            log.error("Cause: {}", getCause().toString());
        }
    }

    public String getMessageAndCause() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getMessage());
        if (getCause() != null) {
            stringBuilder.append("\nCause: ").append(getCause().toString());
        }
        return stringBuilder.toString();
    }
}
