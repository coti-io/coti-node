package io.coti.basenode.exceptions;

public class RuleConditionValidationException extends CotiRunTimeException {

    public RuleConditionValidationException(String message) {
        super(message);
    }

    public RuleConditionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
