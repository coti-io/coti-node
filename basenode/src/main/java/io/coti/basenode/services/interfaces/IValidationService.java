package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

public interface IValidationService {

    boolean validateBaseTransaction(BaseTransactionData baseTransactionData, Hash transactionHash);

    boolean validateSource(Hash hash);

    boolean validateAddress(Hash address);

    boolean partialValidation(TransactionData transactionData);

    boolean fullValidation(TransactionData transactionData);

    boolean validatePot(TransactionData transactionData);
}