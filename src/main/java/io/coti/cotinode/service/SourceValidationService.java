package io.coti.cotinode.service;

import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.ISourceValidationService;

public class SourceValidationService implements ISourceValidationService {

    @Override
    public boolean validateSources(TransactionData transactionData) {
        return validateSource(transactionData.getLeftParent()) && validateSource(transactionData.getRightParent());
    }

    private boolean validateSource(TransactionData transactionData) {
        return true;
    }
}
