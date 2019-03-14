package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;

public interface IValidationService {

    boolean validateSource(Hash hash);

    boolean validateAddress(Hash address);

    boolean validateTransactionDataIntegrity(TransactionData transactionData);

    boolean validatePropagatedTransactionDataIntegrity(TransactionData transactionData);

    boolean validateTransactionNodeSignature(TransactionData transactionData);

    boolean validateTransactionTrustScore(TransactionData transactionData);

    boolean validateBaseTransactionAmounts(TransactionData transactionData);

    boolean validateBalancesAndAddToPreBalance(TransactionData transactionData);

    <T extends BaseTransactionData & ITrustScoreNodeValidatable> boolean validateBaseTransactionTrustScoreNodeResult(T baseTransactionData);

    boolean partialValidation(TransactionData transactionData);

    boolean fullValidation(TransactionData transactionData);

    boolean validatePot(TransactionData transactionData);

    boolean validateRequestAndOffState(ClusterStampStateData clusterStampPreparationData, ClusterStampState currentClusterStampState);

    boolean validateRequestAndPreparingState(ClusterStampStateData nodeReadyForClusterStampData, ClusterStampState currentClusterStampState);

    boolean validateRequestAndReadyState(ClusterStampStateData clusterStampStateData, ClusterStampState currentClusterStampState);

}