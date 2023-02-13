package io.coti.basenode.utilities;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

public class CotiFailureAnalyzer
        extends AbstractFailureAnalyzer<Exception> {

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure,
                                      Exception cause) {
        return new FailureAnalysis(getDescription(cause), getAction(cause), cause);
    }

    private String getDescription(Exception ex) {
        return String.format("The bean %s could not be injected as %s "
                        + "because it is of type %s",
                ex.getMessage(),
                ex.getCause(),
                ex.getStackTrace());
    }

    private String getAction(Exception ex) {
        return String.format("Consider create a handler");
    }
}
