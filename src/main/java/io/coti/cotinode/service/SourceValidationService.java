package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.ISourceValidationService;
import org.springframework.stereotype.Component;

@Component
public class SourceValidationService implements ISourceValidationService {

    @Override
    public boolean validateSources(TransactionData transactionData) {
        return validateSource(transactionData.getLeftParent()) && validateSource(transactionData.getRightParent());
    }

    private boolean validateSource(Hash hash) {
        return true;
    }
}
