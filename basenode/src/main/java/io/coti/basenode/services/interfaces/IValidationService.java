package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.http.GetHistoryAddressesResponse;

import java.math.BigDecimal;

public interface IValidationService {

    boolean validateSource(Hash hash);

    boolean validateAddress(Hash address);

    boolean validateTransactionDataIntegrity(TransactionData transactionData);

    boolean validatePropagatedTransactionIntegrityPhase1(TransactionData transactionData);

    boolean validatePropagatedTransactionIntegrityPhase2(TransactionData transactionData);

    boolean validateTransactionNodeSignature(TransactionData transactionData);

    boolean validateTransactionSenderSignature(TransactionData transactionData);

    boolean validateTransactionTrustScore(TransactionData transactionData);

    boolean validateBaseTransactionAmounts(TransactionData transactionData);

    boolean validateBalancesAndAddToPreBalance(TransactionData transactionData);

    <T extends BaseTransactionData & ITrustScoreNodeValidatable> boolean validateBaseTransactionTrustScoreNodeResult(T baseTransactionData);

    boolean validatePot(TransactionData transactionData);

    boolean validateTransactionTimeFields(TransactionData transactionData);

    boolean validateAmountField(BigDecimal amount);

    boolean validateGetAddressesResponse(GetHistoryAddressesResponse getHistoryAddressesResponse);
}