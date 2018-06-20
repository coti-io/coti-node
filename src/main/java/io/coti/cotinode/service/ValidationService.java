package io.coti.cotinode.service;

import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.IValidationService;
import org.springframework.stereotype.Component;

@Component
public class ValidationService implements IValidationService {

    @Override
    public boolean validateUserHash(TransactionData transactionData) {
        return false;
    }

    @Override
    public boolean validateSources(TransactionData transactionData) {
        return validateSource(transactionData.getLeftParent()) && validateSource(transactionData.getRightParent());
    }

    private boolean validateSource(TransactionData transactionData) {
        return true;
    }
}
