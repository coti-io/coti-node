package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;

public interface IValidationService {

    boolean validateSource(Hash hash);

    boolean validateAddress(Hash address);

    boolean validateTransactionDataIntegrity(TransactionData transactionData);

    boolean validatePropagatedTransactionDataIntegrity(TransactionData transactionData);

    boolean validateTransactionNodeSignature(TransactionData transactionData);

    boolean validateTransactionTrustScore(TransactionData transactionData);

    boolean validateBaseTransactionAmounts(TransactionData transactionData);

    boolean validateBalancesAndAddToPreBalance(TransactionData transactionData);

    boolean partialValidation(TransactionData transactionData);

    boolean fullValidation(TransactionData transactionData);

    boolean validatePot(TransactionData transactionData);
}