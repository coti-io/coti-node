package io.coti.common.services.interfaces;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;

public interface IValidationService {

    boolean validateBaseTransaction(BaseTransactionData baseTransactionData, Hash transactionHash);

    boolean validateSource(Hash hash);

    boolean validateAddress(Hash address);

    boolean partialValidation(TransactionData transactionData);

    boolean fullValidation(TransactionData transactionData);

    boolean validatePow(TransactionData transactionData);
}