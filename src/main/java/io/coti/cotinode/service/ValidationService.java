package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.IValidationService;
import org.springframework.stereotype.Component;

@Component
public class ValidationService implements IValidationService {

    @Override
    public boolean validateUserHash(Hash hash) {
        return true;
    }

    @Override
    public boolean validateSource(TransactionData transactionData) {
        return true;
    }
}
